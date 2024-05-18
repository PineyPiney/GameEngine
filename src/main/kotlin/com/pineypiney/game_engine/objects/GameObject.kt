package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.transforms.Transform3D
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.lighting.Light
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.addToCollectionOr
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class GameObject(open var name: String = "GameObject"): Initialisable {


    var parent: GameObject? = null
    var active = true

    // Every GameObject has a list of all object collections it is stored in.
    // This makes it easier to delete objects and make sure they are not being stored in random places
    var objects: ObjectCollection? = null
    val components: MutableSet<Component> = mutableSetOf()
    val children: MutableSet<GameObject> = mutableSetOf()

    val transformComponent by lazy {
        if(!hasComponent<TransformComponent>()) components.add(TransformComponent(this))
        getComponent<TransformComponent>()!!
    }
    val transform: Transform3D get() = transformComponent.transform

    var velocity: Vec3
        get() = transformComponent.velocity
        set(value) { transformComponent.velocity = value }

    var position: Vec3
        get() = transform.position
        set(value){
            transform.position = value
        }
    var scale: Vec3
        get() = transform.scale
        set(value){
            transform.scale = value
        }
    var rotation: Quat
        get() = transform.rotation
        set(value){
            transform.rotation = value
        }

    var layer: Int = 0
        set(value) {
            objects?.let {
                it[field].remove(this)
                it.map.addToCollectionOr(value, this){ mutableSetOf() }
            }
            field = value
        }
    val relativeModel: Mat4 get() = transform.fetchModel()
    val worldModel: Mat4 get() = parent?.let { it.worldModel * relativeModel } ?: relativeModel

    val renderer get() = getComponent<RenderedComponentI>()
    val parentRenderSize get() = parent?.renderer?.renderSize ?: Vec2(1f, 1f)

    open fun addComponents(){
        components.add(transformComponent)
    }
    open fun addChildren(){

    }

    inline fun <reified E: Component> hasComponent() = components.any { it is E }

    override fun init() {
        addComponents()
        for(c in components) c.init()
        addChildren()
        for(c in children) c.init()
        for(c in components.filterIsInstance<PostChildrenInit>()) c.postChildrenInit()
    }

    fun setProperty(key: String, value: String){
        val keys = key.split('.')
        if(keys.size > 2) {
            val childName = keys[0]
            val child = objects?.getAllObjects()?.firstOrNull { it.name == childName }
            child?.setProperty(key.substringAfter('.'), value)
        }
        else if(keys.size == 2){
            components.firstOrNull { it.id == keys[0] }?.setValue(keys[1], value)
        }
    }

    fun translate(vec: Vec3)=transform.translate(vec)
    fun scale(vec: Vec3)=transform.scale(vec)
    fun rotate(euler: Vec3)=transform.rotate(euler)
    fun rotate(quat: Quat)=transform.rotate(quat)

    // These function define where in an object collection an object is stored
    open fun addTo(objects: ObjectCollection){
        objects.map.addToCollectionOr(layer, this) { mutableSetOf() }
    }

    open fun removeFrom(objects: ObjectCollection){
        objects.map[layer]?.remove(this)
    }

    fun addChild(vararg children: GameObject){
        this.children.addAll(children.toSet())
        for(c in children) c.parent = this
    }
    fun addAndInitChild(vararg children: GameObject){
        this.children.addAll(children.toSet())
        for(c in children) {
            c.parent = this
            c.init()
        }
    }
    fun addChildren(children: Iterable<GameObject>){
        this.children.addAll(children.toSet())
        for(c in children) c.parent = this
    }
    fun removeChild(vararg children: GameObject?){
        val childList = children.filterNotNull().toSet()
        this.children.removeAll(childList)
        for(c in childList) c.parent = null
    }
    fun removeAndDeleteChild(vararg children: GameObject?){
        val childList = children.filterNotNull().toSet()
        this.children.removeAll(childList)
        for(c in childList) {
            c.parent = null
            c.delete()
        }
    }
    fun removeChildren(children: Iterable<GameObject>){
        this.children.removeAll(children.toSet())
        for(c in children) c.parent = null
    }


    fun getChild(name: String) = children.firstOrNull { it.name == name }

    fun getComponent(id: String): Component?{
        val parts = id.split('.')
        if(parts.size == 1) return components.firstOrNull { it.id == parts[0] }
        return children.firstOrNull { it.name == parts[0] }?.getComponent(id.substring(parts[0].length + 1))
    }

    inline fun <reified T: ComponentI> getComponent(): T?{
        return components.firstOrNull { it is T } as? T
    }

    fun getShape(): Shape{
        
        getComponent<Collider2DComponent>()?.let { return it.transformedBox }
        getComponent<Collider3DComponent>()?.let { return it.transformedBox }

        val renderer = this.renderer
        if(renderer != null){
            return renderer.shape transformedBy worldModel.scale(Vec3(renderer.renderSize, 1f))
        }

        return Rect2D(Vec2(), Vec2(1f))
    }

    fun getLineage(): Set<GameObject>{
        var o = this.parent ?: return emptySet()
        val set = mutableSetOf(o)
        while (true){
            o = o.parent ?: return set
            set.add(o)
        }
    }

    fun allDescendants(set: MutableSet<GameObject> = mutableSetOf()): Set<GameObject>{
        set.add(this)
        for (c in children) c.allDescendants(set)
        return set
    }

    fun allActiveDescendants(set: MutableSet<GameObject> = mutableSetOf()): Set<GameObject>{
        if(active) {
            set.add(this)
            for (c in children) c.allActiveDescendants(set)
        }
        return set
    }

    fun copy(): GameObject{
        val o = GameObject(name)

        o.parent = parent
        o.active = active
        o.layer = layer

        o.components.addAll(components.map { it.copy(o) })
        o.children.addAll(children.map { it.copy() })

        return o
    }

    override fun toString(): String {
        return "GameObject[$name]"
    }

    override fun delete() {
        objects?.map?.get(layer)?.remove(this)
        objects = null
    }

    companion object{

        fun simpleRenderedGameObject(shader: Shader, position: Vec3 = Vec3(), scale: Vec3 = Vec3(1f), shape: VertexShape = VertexShape.centerSquareShape, setUniformsFunc: RenderedComponentI.() -> Unit): GameObject{
            return object : GameObject(){

                override fun addComponents() {
                    super.addComponents()
                    val x: GameObject = this // Literally no clue, intellij explain yourself
                    components.add(object : RenderedComponent(x, shader){

                        override val shape: Shape = shape.shape
                        override val renderSize: Vec2 = Vec2(1f)

                        override fun setUniforms() {
                            super.setUniforms()
                            setUniformsFunc()
                        }

                        override fun render(renderer: RendererI<*>, tickDelta: Double) {
                            shader.setUp(uniforms, renderer)
                            shape.bindAndDraw()
                        }
                    })
                }

                override fun init() {
                    super.init()
                    this.position = position
                    this.scale = scale
                }
            }
        }

        fun simpleTextureGameObject(texture: Texture, shape: VertexShape = VertexShape.centerSquareShape, shader: Shader = MeshedTextureComponent.default2DShader): GameObject {
            val o = object : GameObject(){
                override fun addComponents() {
                    super.addComponents()
                    components.add(MeshedTextureComponent(this, texture, shader, shape))
                }
            }

            return o
        }

        fun simpleModelledGameObject(model: Model, shader: Shader = ModelRendererComponent.defaultShader, debug: Int = 0): GameObject {
            val o = object : GameObject(){
                override fun addComponents() {
                    super.addComponents()
                    components.add(ModelRendererComponent(this, model, shader).apply { this.debug = debug })
                    components.add(model.box.getComponent(this))
                }
            }

            return o
        }

        fun simpleLightObject(light: Light, render: Boolean = true): GameObject{
            val o = object : GameObject(){
                override fun addComponents() {
                    super.addComponents()
                    components.add(LightComponent(this, light))
                    if(render) components.add(ColourRendererComponent(this, Vec4(1f), ColourRendererComponent.shader3D, VertexShape.centerCubeShape))
                }
            }
            return o
        }
    }
}