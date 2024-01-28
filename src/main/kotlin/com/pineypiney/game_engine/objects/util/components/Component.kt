package com.pineypiney.game_engine.objects.util.components

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Drawable
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.StaticInteractableMenuItem
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.fromString
import com.pineypiney.game_engine.util.extension_functions.toString
import com.pineypiney.game_engine.window.WindowI
import glm_.*
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class Component(val id: String, val parent: Storable) {

    abstract val fields: Array<Field<*>>
    val parentPath = parent

    fun setValue(key: String, value: String){
        val field = fields.firstOrNull { it.id == key } ?: return
        field.set(this, value)
    }

    fun <F: Field<*>> getField(id: String): F?{
        val f = fields.firstOrNull { it.id == id } ?: return null
        return f as? F
    }

    override fun toString(): String {
        return "Component[$id]"
    }

    open class Field<T>(val id: String, val editor: FieldCreator<T>, val getter: () -> T, val setter: (T) -> Unit, val serialise: (T) -> String, val parse: (Component, String) -> T?){
        fun set(component: Component, value: String){
            setter(parse(component, value) ?: return)
        }
    }

    class IntField(id: String, getter: () -> Int, setter: (Int) -> Unit): Field<Int>(id, ::DefaultFieldEditor, getter, setter, { i -> i.asHexString }, { _, s -> s.intValue(16) })
    class FloatField(id: String, getter: () -> Float, setter: (Float) -> Unit): Field<Float>(id, ::FloatFieldEditor, getter, setter, ::float2String, { _, s -> string2Float(s)}){

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
    class BooleanField(id: String, getter: () -> Boolean, setter: (Boolean) -> Unit): Field<Boolean>(id, ::DefaultFieldEditor, getter, setter, { b -> b.i.toString() }, { _, s -> s.toBoolean() })
    class Vec2Field(id: String, getter: () -> Vec2, setter: (Vec2) -> Unit): Field<Vec2>(id, ::Vec2FieldEditor, getter, setter, { v -> v.toString(",", FloatField::float2String) }, { _, s ->
        try{
            Vec2.fromString(s, FloatField::string2Float)
        }
        catch (e: NumberFormatException){
            Vec2()
        }
    })
    class Vec3Field(id: String, getter: () -> Vec3, setter: (Vec3) -> Unit): Field<Vec3>(id, ::Vec3FieldEditor, getter, setter, { v -> v.toString(",", FloatField::float2String) }, { _, s -> Vec3.fromString(s, FloatField::string2Float) })
    class StorableField<T: Storable?>(id: String, getter: () -> T, setter: (T) -> Unit): Field<T>(id, ::DefaultFieldEditor, getter, setter, ::serialise, ::parse){
        
        companion object{
            fun <T: Storable?> serialise(o: T): String{
                return o?.name ?: "Storable"
            }
            fun <T: Storable?> parse(c: Component, s: String): T?{
                return c.parent.objects?.getAllObjects()?.firstOrNull { it.name == s } as? T
            }
        }
    }
    class CollectionField<T, C: Collection<T>>(id: String, getter: () -> C, setter: (C) -> Unit, val separator: String, serialise: (T) -> String, parse: (Component, String) -> T?, collectionConverter: (List<T>) -> C, val subEditor: FieldCreator<T>): Field<C>(id, { c, i, w, o, s, cb -> CollectionFieldEditor(c, i, w, o, s, subEditor, cb)}, getter, setter, { it.joinToString(separator, transform = serialise)}, { c, s ->
        collectionConverter(s.split(separator).mapNotNull { parse(c, it) })
    })

    val components: Array<(Storable) -> Component> = arrayOf(
        ::Transform2DComponent,
        ::TextureComponent,
        ::ColouredTextureComponent,
    )

    val components2D: Array<(GameObject2D) -> Component> = arrayOf(
        ::ColliderComponent
    )

    abstract class FieldEditor<T, out F: Field<T>>(component: Component, val fullId: String, window: WindowI, final override val origin: Vec2, final override val size: Vec2): StaticInteractableMenuItem(){

        val id = fullId.substringAfterLast('.')
        val field: F = component.getField(removeIDCollectionNumber()) ?: throw Exception("Component $component does not contain field $id}")


        override fun init() {
            super.init()
            update()
        }

        abstract fun update()
        
        private fun removeIDCollectionNumber(): String{
            return id.substringBefore('#')
        }
    }
    open class DefaultFieldEditor<T, F: Field<T>>(component: Component, id: String, window: WindowI, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): FieldEditor<T, F>(component, id, window, origin, size){

        val textField = ActionTextField<ActionTextField<*>>(origin, size, window){ f, c, i ->
            try{
                field.parse(component, f.text)?.let { field.setter(it) }
                callback(fullId, f.text)
            }
            catch (_: Exception){

            }
        }

        override fun setChildren() {
            addChild(textField)
        }

        override fun update() {
            textField.text = field.serialise(field.getter())
        }

        override fun draw() {
            textField.draw()
        }

        override fun delete() {
            super.delete()
            textField.delete()
        }
    }

    open class FloatFieldEditor(component: Component, id: String, window: WindowI, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): FieldEditor<Float, FloatField>(component, id, window, origin, size){

        val textField = ActionTextField<ActionTextField<*>>(origin, size, window){ f, c, i ->
            try{
                val value = java.lang.Float.parseFloat(f.text)
                field.setter(value)
                callback(fullId, field.serialise(value))
            }
            catch (_: NumberFormatException){

            }
        }

        override fun setChildren() {
            addChild(textField)
        }

        override fun update() {
            textField.text = field.getter().toString()
        }

        override fun draw() {
            textField.draw()
        }

        override fun delete() {
            super.delete()
            textField.delete()
        }
    }

    open class Vec2FieldEditor(component: Component, id: String, window: WindowI, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): FieldEditor<Vec2, Vec2Field>(component, id, window, origin, size){

        val xField = ActionTextField<ActionTextField<*>>(origin, Vec2(size.x * 0.5f, size.y), window){ f, c, i ->
            try{
                val newVal = Vec2(java.lang.Float.parseFloat(f.text), field.getter().y)
                field.setter(newVal)
                callback(fullId, field.serialise(newVal))
            }
            catch (_: NumberFormatException){

            }
        }

        val yField = ActionTextField<ActionTextField<*>>(Vec2(origin.x + (size.x * 0.5f), origin.y), Vec2(size.x * 0.5f, size.y), window){ f, c, i ->
            try{
                val newVal = Vec2(field.getter().x, java.lang.Float.parseFloat(f.text))
                field.setter(newVal)
                callback(fullId, field.serialise(newVal))
            }
            catch (_: NumberFormatException){

            }
        }

        override fun setChildren() {
            addChild(xField)
            addChild(yField)
        }

        override fun update() {
            val v = field.getter()
            xField.text = v.x.toString()
            yField.text = v.y.toString()
        }

        override fun draw() {
            xField.draw()
            yField.draw()
        }

        override fun delete() {
            super.delete()
            xField.delete()
            yField.delete()
        }
    }

    open class Vec3FieldEditor(component: Component, id: String, window: WindowI, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): FieldEditor<Vec3, Vec3Field>(component, id, window, origin, size){

        val xField = ActionTextField<ActionTextField<*>>(origin, Vec2(size.x * 0.33f, size.y), window){ f, c, i ->
            try{
                val v = field.getter()
                val newVal = Vec3(java.lang.Float.parseFloat(f.text), v.y, v.z)
                field.setter(newVal)
                callback(fullId, field.serialise(newVal))
            }
            catch (_: NumberFormatException){

            }
        }

        val yField = ActionTextField<ActionTextField<*>>(Vec2(origin.x + (size.x * 0.33f), origin.y), Vec2(size.x * 0.34f, size.y), window){ f, c, i ->
            try{
                val v = field.getter()
                val newVal = Vec3(v.x, java.lang.Float.parseFloat(f.text), v.z)
                field.setter(newVal)
                callback(fullId, field.serialise(newVal))
            }
            catch (_: NumberFormatException){

            }
        }

        val zField = ActionTextField<ActionTextField<*>>(Vec2(origin.x + (size.x * 0.67f), origin.y), Vec2(size.x * 0.33f, size.y), window){ f, c, i ->
            try{
                val v = field.getter()
                val newVal = Vec3(v.x, v.y, java.lang.Float.parseFloat(f.text))
                field.setter(newVal)
                callback(fullId, field.serialise(newVal))
            }
            catch (_: NumberFormatException){

            }
        }

        override fun setChildren() {
            addChild(xField)
            addChild(yField)
            addChild(zField)
        }

        override fun update() {
            val v = field.getter()
            xField.text = v.x.toString()
            yField.text = v.y.toString()
            zField.text = v.z.toString()
        }

        override fun draw() {
            xField.draw()
            yField.draw()
            zField.draw()
        }

        override fun delete() {
            super.delete()
            xField.delete()
            yField.delete()
            zField.delete()
        }
    }

    class CollectionFieldEditor<T, C: Collection<T>>(component: Component, id: String, window: WindowI, origin: Vec2, size: Vec2, val editor: FieldCreator<T>, callback: (String, String) -> Unit): FieldEditor<C, CollectionField<T, C>>(component, id, window, origin, size){

        val fields = field.getter().mapIndexed { i, _ ->
            editor(component, "$id#$i", window, origin, size, callback)
        }

        override fun setChildren() {
            addChildren(fields)
        }

        override fun update() {
            val v = field.getter()
            v.forEachIndexed { index, t ->
                fields[index].update()
            }
        }

        override fun draw() {
            fields.forEach(Drawable::draw)
        }

        override fun delete() {
            super.delete()
            fields.delete()
        }
    }
}

typealias FieldCreator<T> = (component: Component, id: String, window: WindowI, origin: Vec2, size: Vec2, callback: (String, String) -> Unit) -> Component.FieldEditor<T, Component.Field<T>>