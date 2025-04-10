package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider3DComponent
import com.pineypiney.game_engine.objects.components.rendering.*
import com.pineypiney.game_engine.objects.util.meshes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.lighting.Light
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.addToCollectionOr
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class GameObject(open var name: String = "GameObject") : Initialisable {


	var parent: GameObject? = null
	var active = true

	// Every GameObject has a list of all object collections it is stored in.
	// This makes it easier to delete objects and make sure they are not being stored in random places
	var objects: ObjectCollection? = null

	var transformComponent: TransformComponent = TransformComponent(this)
	val components: MutableSet<ComponentI> = mutableSetOf(transformComponent)
	val children: MutableSet<GameObject> = mutableSetOf()

	var position: Vec3
		get() = transformComponent.position
		set(value) {
			if(transformComponent.position != value) transformComponent.position = value
		}
	var scale: Vec3
		get() = transformComponent.scale
		set(value) {
			transformComponent.scale = value
		}
	var rotation: Quat
		get() = transformComponent.rotation
		set(value) {
			transformComponent.rotation = value
		}

	var layer: Int = 0
		set(value) {
			objects?.let {
				it[field].remove(this)
				it.map.addToCollectionOr(value, this) { ObjectCollectionLayer(value) }
			}
			field = value
		}
	val relativeModel: Mat4 get() = transformComponent.fetchModel()
	val worldModel: Mat4 get() = transformComponent.fetchWorldModel()

	val renderer get() = getComponent<RenderedComponentI>()

	var throwKidsAtRenderer = true

	open fun addComponents() {
		components.add(transformComponent)
	}

	open fun addChildren() {

	}

	override fun init() {
		addComponents()
		for (c in components) c.init()
		addChildren()
		for (c in children) c.init()
		for (c in components.filterIsInstance<PostChildrenInit>()) c.postChildrenInit()
	}

	fun setProperty(key: String, value: String) {
		val dolI = key.indexOf('$')
		if(dolI != -1) {
			val childName = key.substring(0, dolI)
			val child = objects?.getAllObjects()?.firstOrNull { it.name == childName }
			child?.setProperty(key.substring(dolI + 1), value)
		}
		else {
			val dotI = key.indexOf('.')
			components.firstOrNull { it.id == key.substring(0, dotI) }?.setValue(key.substring(dotI + 1), value)
		}
	}

	infix fun translate(vec: Vec3) = transformComponent translate vec
	infix fun resize(vec: Vec3) = transformComponent scale vec
	infix fun rotate(euler: Vec3) = transformComponent rotate euler
	infix fun rotate(quat: Quat) = transformComponent rotate quat

	fun addChild(vararg children: GameObject) {
		this.children.addAll(children.toSet())
		for (c in children) c.parent = this
	}

	fun addAndInitChild(vararg children: GameObject) {
		this.children.addAll(children.toSet())
		for (c in children) {
			c.parent = this
			c.init()
		}
	}

	fun addChildren(children: Iterable<GameObject>) {
		this.children.addAll(children.toSet())
		for (c in children) c.parent = this
	}

	fun addAndInitChildren(children: Iterable<GameObject>) {
		this.children.addAll(children.toSet())
		for (c in children) {
			c.parent = this
			c.init()
		}
	}

	fun removeChild(vararg children: GameObject?) {
		val childList = children.filterNotNull().toSet()
		this.children.removeAll(childList)
		for (c in childList) c.parent = null
	}

	fun removeAndDeleteChild(vararg children: GameObject?) {
		val childList = children.filterNotNull().toSet()
		this.children.removeAll(childList)
		for (c in childList) {
			c.parent = null
			c.delete()
		}
	}

	fun removeAndDeleteChild(name: String): Boolean {
		val child = getChild(name) ?: return false
		child.parent = null
		child.delete()
		return children.remove(child)
	}

	fun removeChildren(children: Iterable<GameObject>) {
		this.children.removeAll(children.toSet())
		for (c in children) c.parent = null
	}

	fun removeAndDeleteChildren(children: Iterable<GameObject>) {
		val childList = children.toSet()
		this.children.removeAll(childList)
		for (c in childList) {
			c.parent = null
			c.delete()
		}
	}

	fun deleteAllChildren() {
		for (c in children) {
			c.parent = null
			c.delete()
		}
		children.clear()
	}

	fun getChild(name: String) = children.firstOrNull { it.name == name }

	inline fun <reified E : Component> hasComponent() = components.any { it is E }

	fun getComponent(id: String): ComponentI? {
		val parts = id.split('.')
		if (parts.size == 1) return components.firstOrNull { it.id == parts[0] }
		return children.firstOrNull { it.name == parts[0] }?.getComponent(id.substring(parts[0].length + 1))
	}

	inline fun <reified T : ComponentI> getComponent(): T? {
		return components.firstOrNull { it is T } as? T
	}

	fun  getShape(): Shape<*> {

		getComponent<Collider2DComponent>()?.let { return it.transformedShape }
		getComponent<Collider3DComponent>()?.let { return it.transformedShape }

		val renderer = renderer
		if (renderer != null) {
			return renderer.getScreenShape() transformedBy worldModel
		}

		return Rect2D(Vec2(), Vec2(1f))
	}

	/**
	 * Returns the ancestor [generation] generations above this
	 *
	 * @param generation The number of generations above this to search, if <=0 or greater than the top level parent it will return the top level parent.
	 */
	fun getAncestor(generation: Int): GameObject{
		var i = 1
		var o = parent ?: return this
		while(i++ != generation){
			o = o.parent ?: return o
		}
		return o
	}

	/**
	 * Gets the ancestors of this GameObject going back [generations] generations, or all of them is generations is -1
	 *
	 * @param generations The number of ancestors to get, or -1 to get all of them
	 *
	 * @return A list of ancestors
	 */
	fun getAncestry(generations: Int = -1): List<GameObject>{
		val objects = mutableListOf<GameObject>(parent ?: return emptyList())

		while(generations == -1 || objects.size < generations){
			objects.add(objects.last().parent ?: break)
		}

		return objects
	}

	fun getLineage(): Set<GameObject> {
		var o = this.parent ?: return emptySet()
		val set = mutableSetOf(o)
		while (true) {
			o = o.parent ?: return set
			set.add(o)
		}
	}

	fun forAllDescendants(action: GameObject.() -> Unit){
		this.action()
		for(c in children) c.forAllDescendants(action)
	}

	fun forAllActiveDescendants(action: GameObject.() -> Unit){
		if(active) {
			this.action()
			for (c in children) c.forAllActiveDescendants(action)
		}
	}

	fun allDescendants(set: MutableSet<GameObject> = mutableSetOf()): Set<GameObject> {
		set.add(this)
		for (c in children) c.allDescendants(set)
		return set
	}

	fun allActiveDescendants(set: MutableSet<GameObject> = mutableSetOf()): Set<GameObject> {
		if (active) {
			set.add(this)
			for (c in children) c.allActiveDescendants(set)
		}
		return set
	}

	fun catchRenderingComponents(set: MutableSet<GameObject> = mutableSetOf()): Set<GameObject>{
		if(active){
			set.add(this)
			if(throwKidsAtRenderer) for(c in children) c.catchRenderingComponents(set)
		}
		return set
	}

	fun copy(): GameObject {
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
		components.delete()
		while(children.isNotEmpty()) children.firstOrNull()?.delete() ?: break
		parent?.removeChild(this)

		objects?.map?.get(layer)?.remove(this)
		objects = null
	}

	companion object {

		fun simpleRenderedGameObject(
			shader: Shader,
			position: Vec3 = Vec3(),
			scale: Vec3 = Vec3(1f),
			mesh: Mesh = Mesh.centerSquareShape,
			setUniformsFunc: ShaderRenderedComponent.() -> Unit
		): GameObject {
			return object : GameObject() {

				override fun addComponents() {
					super.addComponents()
					val x: GameObject = this // Literally no clue, intellij explain yourself
					components.add(object : ShaderRenderedComponent(x, shader) {

						override fun setUniforms() {
							super.setUniforms()
							setUniformsFunc()
						}

						override fun render(renderer: RendererI, tickDelta: Double) {
							shader.setUp(uniforms, renderer)
							mesh.bindAndDraw()
						}

						override fun getScreenShape(): Shape<*> {
							return mesh.shape
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

		fun simpleTextureGameObject(
			texture: Texture,
			shape: Mesh = Mesh.centerSquareShape,
			shader: Shader = MeshedTextureComponent.default2DShader
		): GameObject {
			val o = object : GameObject() {
				override fun addComponents() {
					super.addComponents()
					components.add(MeshedTextureComponent(this, texture, shader, shape))
				}
			}

			return o
		}

		fun simpleModelledGameObject(
			model: Model,
			shader: Shader = ModelRendererComponent.defaultShader,
			debug: Int = 0
		): GameObject {
			val o = object : GameObject() {
				override fun addComponents() {
					super.addComponents()
					components.add(ModelRendererComponent(this, model, shader).apply { this.debug = debug })
					components.add(model.box.getComponent(this))
				}
			}

			return o
		}

		fun simpleLightObject(light: Light, render: Boolean = true): GameObject {
			val o = object : GameObject(light.javaClass.name + " Object") {
				override fun addComponents() {
					super.addComponents()
					components.add(LightComponent(this, light))
					if (render) components.add(
						ColourRendererComponent(
							this,
							Vec4(1f),
							ColourRendererComponent.shader3D,
							Mesh.centerCubeShape
						)
					)
				}
			}
			return o
		}
	}
}