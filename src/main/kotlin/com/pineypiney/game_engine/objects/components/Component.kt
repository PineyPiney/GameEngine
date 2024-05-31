package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.util.extension_functions.fromString
import com.pineypiney.game_engine.util.extension_functions.toString
import glm_.*
import glm_.quat.Quat
import glm_.vec1.Vec1Vars
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaType

abstract class Component(final override val parent: GameObject, override val id: String): ComponentI {

    val parentPath = parent

    override fun init(){

    }

    override fun setValue(key: String, value: String){
        val field = fields.firstOrNull { it.id == key } ?: return
        field.set(this, value)
    }

    override fun <F: Field<*>> getField(id: String): F?{
        val f = fields.firstOrNull { it.id == id } ?: return null
        return f as? F
    }

    @Throws(InstantiationError::class)
    override fun copy(newParent: GameObject): Component{
        val clazz = this::class
        val constructors = clazz.constructors

        val oClass = GameObject::class.java
        val smallConst = constructors.firstOrNull { it.parameters.size == 1 && it.parameters[0].type.javaType == oClass }

        val newComponent: Component =
            if(smallConst != null) smallConst.call(newParent)
            else{
                val constructor = clazz.primaryConstructor ?: constructors.first()
                val params = mutableMapOf<KParameter, Any?>()

                for(param in constructor.parameters){
                    if(param.type.javaType == oClass) params[param] = newParent
                    else {
                        val memberProperty = clazz.memberProperties.firstOrNull { it.name == param.name && it.returnType.isSupertypeOf(param.type) }
                        if(memberProperty != null) params[param] = memberProperty.call(this)
                        else {
                            throw InstantiationError("Could not copy Component Class $clazz, did not have a default constructor and could not find backing field for parameter ${param.name}")
                        }
                    }
                }

                constructor.callBy(params)
            }

        copyFieldsTo(newComponent)
        return newComponent
    }

    override fun copyFieldsTo(dst: ComponentI){
        for(f in fields){
            copyFieldTo(dst, f)
        }
    }

    override fun <T> copyFieldTo(dst: ComponentI, field: Field<T>){
        val dstField = getMatchingField(dst, field)
        if(dstField == null) {
            GameEngineI.warn("Copying component $this, $dst did not have field ${field.id}")
            return
        }
        field.copyTo(dstField)
    }

    override fun <T> getMatchingField(other: ComponentI, field: Field<T>): Field<T>?{
        return try{
            other.fields.firstOrNull{ it.id == field.id } as? Field<T>
        } catch (e: Exception){
            null
        }
    }

    override fun delete() {

    }

    override fun toString(): String {
        return "Component[$id]"
    }

    open class Field<T>(val id: String, val editor: FieldCreator<T>, val getter: () -> T, val setter: (T) -> Unit, val serialise: (T) -> String, val parse: (Component, String) -> T?, val copy: (T) -> T = { it }){
        fun set(component: Component, value: String){
            setter(parse(component, value) ?: return)
        }

        fun copyTo(other: Field<T>){
            other.setter(copy(getter()))
        }

        override fun toString(): String {
            return "ComponentField[$id]"
        }
    }

    class IntField(id: String, getter: () -> Int, setter: (Int) -> Unit): Field<Int>(id,
        ::DefaultFieldEditor, getter, setter, { i -> i.asHexString }, { _, s -> s.intValue(16) })
    class FloatField(id: String, getter: () -> Float, setter: (Float) -> Unit): Field<Float>(id,
        ::FloatFieldEditor, getter, setter,
        ::float2String, { _, s -> string2Float(s) }){

        companion object{
            fun float2String(f: Float): String{
                val n = f.asIntBits
                val a = CharArray(4){x -> ((n shr (24 - (x * 8))) and 255).c}
                return String(a)
            }
            fun string2Float(s: String): Float{
                var i = 0
                for(a in 0..3){
                    try {
                        i += s[a].i shl (24 - (a * 8))
                    }
                    catch (e: StringIndexOutOfBoundsException){
                        GameEngineI.logger.warn("Couldn't parse encoded float string $s length ${s.length}")
                    }
                }
                return Float.fromBits(i)
            }
        }
    }
    class DoubleField(id: String, getter: () -> Double, setter: (Double) -> Unit): Field<Double>(id,
        ::DoubleFieldEditor, getter, setter,
        ::double2String, { _, s -> string2Float(s) }){

        companion object{
            fun double2String(d: Double): String{
                val n = d.asLongBits
                val a = CharArray(8){x -> ((n shr (56 - (x * 8))) and 255).c}
                return String(a)
            }
            fun string2Float(s: String): Double{
                var i = 0L
                for(a in 0..7){
                    try {
                        i += s[a].i shl (56 - (a * 8))
                    }
                    catch (e: StringIndexOutOfBoundsException){
                        GameEngineI.logger.warn("Couldn't parse encoded double string $s length ${s.length}")
                    }
                }
                return Double.fromBits(i)
            }
        }
    }
    class BooleanField(id: String, getter: () -> Boolean, setter: (Boolean) -> Unit): Field<Boolean>(id,
        ::DefaultFieldEditor, getter, setter, { b -> b.i.toString() }, { _, s -> s.toBoolean() })
    class Vec2Field(id: String, getter: () -> Vec2, setter: (Vec2) -> Unit): Field<Vec2>(id,
        ::Vec2FieldEditor, getter, setter, { v -> v.toString(",", FloatField.Companion::float2String) }, { _, s ->
        try{
            Vec2.fromString(s, false, FloatField.Companion::string2Float)
        }
        catch (e: NumberFormatException){
            Vec2()
        }
    }, { Vec2(it.x, it.y) })
    class Vec3Field(id: String, getter: () -> Vec3, setter: (Vec3) -> Unit): Field<Vec3>(id,
        ::Vec3FieldEditor, getter, setter, { v -> v.toString(",", FloatField.Companion::float2String) }, { _, s -> Vec3.fromString(s, false,
            FloatField.Companion::string2Float
        ) }, { Vec3(it.x, it.y, it.z) })
    class Vec4Field(id: String, getter: () -> Vec4, setter: (Vec4) -> Unit): Field<Vec4>(id,
        ::Vec4FieldEditor, getter, setter, { v -> v.toString(",", FloatField.Companion::float2String) }, { _, s -> Vec4.fromString(s, false,
            FloatField.Companion::string2Float
        ) }, { Vec4(it.x, it.y, it.z, it.w)})
    class QuatField(id: String, getter: () -> Quat, setter: (Quat) -> Unit): Field<Quat>(id,
        ::QuatFieldEditor, getter, setter, { q -> q.toString(",", FloatField.Companion::float2String) }, { _, s -> Quat.fromString(s, false,
            FloatField.Companion::string2Float
        ) }, { Quat(it.w, it.x, it.y, it.z) })
    class GameObjectField<T: GameObject?>(id: String, getter: () -> T, setter: (T) -> Unit): Field<T>(id,
        ::DefaultFieldEditor, getter, setter,
        Companion::serialise,
        Companion::parse
    ){
        
        companion object{
            fun <T: GameObject?> serialise(o: T): String{
                return o?.name ?: "Storable"
            }
            fun <T: GameObject?> parse(c: Component, s: String): T?{
                return c.parent.objects?.getAllObjects()?.firstOrNull { it.name == s } as? T
            }
        }
    }
    class CollectionField<T, C: Collection<T>>(id: String, getter: () -> C, setter: (C) -> Unit, val separator: String, serialise: (T) -> String, parse: (Component, String) -> T?, collectionConverter: (List<T>) -> C, val subEditor: FieldCreator<T>): Field<C>(id, { c, i, o, s, cb -> CollectionFieldEditor(c, i, o, s, subEditor, cb) }, getter, setter, { it.joinToString(separator, transform = serialise)}, { c, s ->
        collectionConverter(s.split(separator).mapNotNull { parse(c, it) })
    })

    val components: Array<(GameObject) -> Component> = arrayOf(
        ::TransformComponent,
        ::SpriteComponent,
        ::ColouredSpriteComponent,
    )

    val components2D: Array<(GameObject) -> Component> = arrayOf(
        ::Collider2DComponent
    )

    abstract class FieldEditor<T, out F: Field<T>>(component: Component, val fullId: String, origin: Vec2, size: Vec2): MenuItem(){

        val id = fullId.substringAfterLast('.')
        val field: F = component.getField(removeIDCollectionNumber()) ?: throw Exception("Component $component does not contain field $id}")

        init {
            os(origin, size)
        }

        override fun init() {
            super.init()
            update()
        }

        abstract fun update()
        
        private fun removeIDCollectionNumber(): String{
            return id.substringBefore('#')
        }
    }
    open class DefaultFieldEditor<T, F: Field<T>>(component: Component, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): FieldEditor<T, F>(component, id, origin, size){

        val textField = ActionTextField<ActionTextFieldComponent<*>>(Vec2(0f, 0f), Vec2(1f, 1f)){ f, _, _ ->
            try{
                field.parse(component, f.text)?.let { field.setter(it) }
                callback(fullId, f.text)
            }
            catch (_: Exception){

            }
        }

        override fun addChildren() {
            addChild(textField)
        }

        override fun update() {
            textField.text = field.serialise(field.getter())
        }
    }

    open class FloatFieldEditor(component: Component, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): FieldEditor<Float, FloatField>(component, id, origin, size){

        val textField = ActionTextField<TextFieldComponent>(Vec2(0f, 0f), Vec2(1f, 1f)){ f, _, _ ->
            try{
                val value = java.lang.Float.parseFloat(f.text)
                field.setter(value)
                callback(fullId, field.serialise(value))
            }
            catch (_: NumberFormatException){

            }
        }

        override fun addChildren() {
            addChild(textField)
        }

        override fun update() {
            textField.text = field.getter().toString()
        }
    }

    open class DoubleFieldEditor(component: Component, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): FieldEditor<Double, DoubleField>(component, id, origin, size){

        val textField = ActionTextField<TextFieldComponent>(Vec2(0f, 0f), Vec2(1f, 1f)){ f, _, _ ->
            try{
                val value = java.lang.Double.parseDouble(f.text)
                field.setter(value)
                callback(fullId, field.serialise(value))
            }
            catch (_: NumberFormatException){

            }
        }

        override fun addChildren() {
            addChild(textField)
        }

        override fun update() {
            textField.text = field.getter().toString()
        }
    }

    open class VecFieldEditor<T: Vec1Vars<Float>, out F: Field<T>>(component: Component, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit, vecSize: Int, copy: (T) -> T, inSet: T.(Int, Float) -> Unit): FieldEditor<T, F>(component, id, origin, size){
        
        val fields = Array<ActionTextField<*>>(vecSize){
            ActionTextField<ActionTextFieldComponent<*>>(Vec2((size.x * it / vecSize), 0f), Vec2(size.x  / vecSize, 1f)){ f, _, _ ->
                try {
                    val newVal = copy(field.getter())
                    newVal.inSet(it, java.lang.Float.parseFloat(f.text))
                    field.setter(newVal)
                    callback(fullId, field.serialise(newVal))
                }
                catch (_: NumberFormatException){

                }
            }
        }

        override fun addChildren() {
            addChild(*fields)
        }

        override fun update() {
            val v = field.getter()
            fields.forEachIndexed { index, actionTextField -> 
                actionTextField.text = v[index].toString()
            }
        }
    }

    open class Vec2FieldEditor(component: Component, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): VecFieldEditor<Vec2, Vec2Field>(component, id, origin, size, callback, 2, ::Vec2, Vec2::set)
    open class Vec3FieldEditor(component: Component, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): VecFieldEditor<Vec3, Vec3Field>(component, id, origin, size, callback, 3, ::Vec3, Vec3::set)
    open class Vec4FieldEditor(component: Component, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): VecFieldEditor<Vec4, Vec4Field>(component, id, origin, size, callback, 4, ::Vec4, Vec4::set)


    open class QuatFieldEditor(component: Component, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): FieldEditor<Quat, QuatField>(component, id, origin, size){

        val xField = ActionTextField<ActionTextFieldComponent<*>>(Vec2(0f, 0f), Vec2(size.x * 0.25f, 1f)){ f, _, _ ->
            try{
                val v = field.getter()
                val newVal = Quat(v.w, java.lang.Float.parseFloat(f.text), v.y, v.z)
                field.setter(newVal)
                callback(fullId, field.serialise(newVal))
            }
            catch (_: NumberFormatException){

            }
        }

        val yField = ActionTextField<ActionTextFieldComponent<*>>(Vec2(size.x * 0.25f, 0f), Vec2(size.x * 0.25f, 1f)){ f, _, _ ->
            try{
                val v = field.getter()
                val newVal = Quat(v.w, v.x, java.lang.Float.parseFloat(f.text), v.z)
                field.setter(newVal)
                callback(fullId, field.serialise(newVal))
            }
            catch (_: NumberFormatException){

            }
        }

        val zField = ActionTextField<ActionTextFieldComponent<*>>(Vec2(size.x * 0.5f, 0f), Vec2(size.x * 0.25f, 1f)){ f, _, _ ->
            try{
                val v = field.getter()
                val newVal = Quat(v.w, v.x, v.y, java.lang.Float.parseFloat(f.text))
                field.setter(newVal)
                callback(fullId, field.serialise(newVal))
            }
            catch (_: NumberFormatException){

            }
        }

        val wField = ActionTextField<ActionTextFieldComponent<*>>(Vec2(size.x * 0.75f, 0f), Vec2(size.x * 0.25f, 1f)){ f, _, _ ->
            try{
                val v = field.getter()
                val newVal = Quat(java.lang.Float.parseFloat(f.text), v.x, v.y, v.z)
                field.setter(newVal)
                callback(fullId, field.serialise(newVal))
            }
            catch (_: NumberFormatException){

            }
        }

        val fields = arrayOf(xField, yField, zField, wField)

        override fun addChildren() {
            addChild(*fields)
        }

        override fun update() {
            val v = field.getter()
            xField.text = v.x.toString()
            yField.text = v.y.toString()
            zField.text = v.z.toString()
            wField.text = v.w.toString()
        }
    }

    class CollectionFieldEditor<T, C: Collection<T>>(component: Component, id: String, origin: Vec2, size: Vec2, val editor: FieldCreator<T>, callback: (String, String) -> Unit): FieldEditor<C, CollectionField<T, C>>(component, id, origin, size){

        val fields = field.getter().mapIndexed { i, _ ->
            editor(component, "$id#$i", origin, size, callback)
        }

        override fun addChildren() {
            addChildren(fields)
        }

        override fun update() {
            val v = field.getter()
            v.forEachIndexed { index, t ->
                fields[index].update()
            }
        }
    }
}

typealias FieldCreator<T> = (component: Component, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit) -> Component.FieldEditor<T, Component.Field<T>>

fun <C: Component> C.applied(): C{
    parent.components.add(this)
    return this
}