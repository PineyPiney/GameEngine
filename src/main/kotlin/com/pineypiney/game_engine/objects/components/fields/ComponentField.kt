package com.pineypiney.game_engine.objects.components.fields

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.toString
import com.pineypiney.game_engine.util.maths.shapes.Parallelogram
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import glm_.asHexString
import glm_.intValue
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i

open class ComponentField<T>(
	val id: String,
	val getter: () -> T,
	val setter: (T) -> Unit,
	val serialise: (ComponentI, T) -> String,
	val parse: (ComponentI, String) -> T?,
	val copy: (T) -> T = { it }
) {

	constructor(id: String, getter: () -> T, setter: (T) -> Unit, serialise: (T) -> String, parse: (String) -> T?, copy: (T) -> T = { it }):
			this(id, getter, setter, { _, s -> serialise(s)}, { _, s -> parse(s) }, copy)

	fun set(value: String, component: ComponentI) {
		try { setter(parse(component, value) ?: return) }
		catch (_: Exception){
			GameEngineI.logger.warn("Could not set $this in $component value to $value")
		}
	}

	fun copyTo(other: ComponentField<T>) {
		other.setter(copy(getter()))
	}

	open fun isLateParse() = false

	override fun toString(): String {
		return "ComponentField[$id]"
	}

	fun serialiseValue(component: ComponentI) = serialise(component, getter())

	fun serialise(head: StringBuilder, data: StringBuilder, component: ComponentI) {
		val s = serialiseValue(component)
		head.append(ByteData.int2String(id.length, 1) + id + ByteData.int2String(s.length))
		data.append(s)
	}
}

class BoolField(id: String, getter: () -> Boolean, setter: (Boolean) -> Unit) : ComponentField<Boolean>(id,
	getter, setter, { b -> if(b) Char(1).toString() else Char(0).toString() }, { s -> s[0].code > 0 })

class IntField(id: String, getter: () -> Int, setter: (Int) -> Unit) : ComponentField<Int>(id,
	getter, setter, { i -> i.asHexString }, { s -> s.intValue(16) })

class IntRangeField(id: String, val range: IntRange, getter: () -> Int, setter: (Int) -> Unit) : ComponentField<Int>(id,
	getter, setter, { i -> i.asHexString }, { s -> s.intValue(16) })

class UIntField(id: String, getter: () -> UInt, setter: (UInt) -> Unit) : ComponentField<UInt>(id,
	getter, setter, { i -> i.toInt().asHexString }, { s -> s.intValue(16).toUInt() })

class FloatField(id: String, getter: () -> Float, setter: (Float) -> Unit) : ComponentField<Float>(id,
	getter, setter,
	ByteData::float2String, { s -> ByteData.string2Float(s) })

class DoubleField(id: String, getter: () -> Double, setter: (Double) -> Unit) : ComponentField<Double>(id,
	getter, setter,
	ByteData::double2String, { s -> ByteData.string2Double(s) })

open class VecTField<T, V>(id: String, getter: () -> V, setter: (V) -> Unit, t2string: T.() -> String, serialise: V.(String, T.() -> String) -> String, parse: (String) -> V, default: () -> V, copy: (V) -> V):
	ComponentField<V>(id, getter, setter, { v -> v.serialise("", t2string) }, { s ->
		try {
			parse(s)
		}
		catch (_: NumberFormatException){
			default()
		}}, copy)

open class VeciField<V>(id: String, getter: () -> V, setter: (V) -> Unit, serialise: V.(String, Int.() -> String) -> String, parse: (String) -> V, default: () -> V, copy: (V) -> V) : VecTField<Int, V>(
	id, getter, setter, ByteData::int2String, serialise, parse, default, copy)

class Vec2iField(id: String, getter: () -> Vec2i, setter: (Vec2i) -> Unit) :
	VeciField<Vec2i>(id, getter, setter, Vec2i::toString, ByteData::string2Vec2i, ::Vec2i, ::Vec2i)

class Vec3iField(id: String, getter: () -> Vec3i, setter: (Vec3i) -> Unit) :
	VeciField<Vec3i>(id, getter, setter, Vec3i::toString, ByteData::string2Vec3i, ::Vec3i, ::Vec3i)

class Vec4iField(id: String, getter: () -> Vec4i, setter: (Vec4i) -> Unit) :
	VeciField<Vec4i>(id, getter, setter, Vec4i::toString, ByteData::string2Vec4i, ::Vec4i, ::Vec4i)

open class VecField<V>(id: String, getter: () -> V, setter: (V) -> Unit, serialise: V.(String, Float.() -> String) -> String, parse: (String) -> V, default: () -> V, copy: (V) -> V) : VecTField<Float, V>(
	id, getter, setter, ByteData::float2String, serialise, parse, default, copy)

class Vec2Field(id: String, getter: () -> Vec2, setter: (Vec2) -> Unit) : 
	VecField<Vec2>(id, getter, setter, Vec2::toString, ByteData::string2Vec2, ::Vec2, ::Vec2)

class Vec3Field(id: String, getter: () -> Vec3, setter: (Vec3) -> Unit) :
	VecField<Vec3>(id, getter, setter, Vec3::toString, ByteData::string2Vec3, ::Vec3, ::Vec3)

class Vec4Field(id: String, getter: () -> Vec4, setter: (Vec4) -> Unit) :
	VecField<Vec4>(id, getter, setter, Vec4::toString, ByteData::string2Vec4, ::Vec4, ::Vec4)

class QuatField(id: String, getter: () -> Quat, setter: (Quat) -> Unit) : ComponentField<Quat>(id,
	getter, setter, { q -> q.toString("", ByteData::float2String) }, ByteData::string2Quat, { Quat(it.w, it.x, it.y, it.z) })

class ShaderField(id: String, getter: () -> RenderShader, setter: (RenderShader) -> Unit): ComponentField<RenderShader>(id,
	getter, setter, ::serialise, ::parse){

	companion object {
		fun serialise(shader: RenderShader): String {
			val sb = StringBuilder()
			val g = shader.gName != null
			sb.append(if (g) '3' else '2')
			sb.append(shader.vName.length.toChar() + shader.vName)
			sb.append(shader.fName.length.toChar() + shader.fName)
			if (g) sb.append(shader.gName.length.toChar() + shader.gName)
			return sb.toString()
		}

		fun parse(s: String): RenderShader{
			val hasG = s[0] == '3'
			val vl = s[1].code
			val v = s.substring(2, 2+vl)
			val fl = s[2+vl].code
			val f = s.substring(3+vl, 3+vl+fl)
			if(hasG){
				val gl = s[3+vl+fl].code
				val g = s.substring(4+vl+fl, 4+vl+fl+gl)
				return ShaderLoader[ResourceKey(v), ResourceKey(f), ResourceKey(g)]
			}
			else return ShaderLoader[ResourceKey(v), ResourceKey(f)]
		}
	}
}

class TextureField(id: String, getter: () -> Texture, setter: (Texture) -> Unit): ComponentField<Texture>(id,
	getter, setter, { it.id },
	{ s -> TextureLoader[ResourceKey(s)] })

class ModelField(id: String, getter: () -> Model, setter: (Model) -> Unit): ComponentField<Model>(id,
	getter, setter, { it.name.substringBefore('.') },
	{ s -> ModelLoader[ResourceKey(s)] })

class Shape2DField(id: String, getter: () -> Shape2D, setter: (Shape2D) -> Unit): ComponentField<Shape2D>(id,
	getter, setter, ::serialise, ::parse){

	companion object {
		fun serialise(shape: Shape2D): String {
			return when(shape){
				is Rect2D -> {
					"RCT2" +
							shape.origin.toString("", ByteData::float2String) +
							ByteData.float2String(shape.length1) +
							ByteData.float2String(shape.length2) +
							ByteData.float2String(shape.angle)
				}
				is Parallelogram -> {
					"PARA" +
							shape.origin.toString("", ByteData::float2String) +
							shape.side1.toString("", ByteData::float2String) +
							shape.side2.toString("", ByteData::float2String)
				}
				else -> "DFLT"
			}
		}

		fun parse(s: String): Shape2D{
			val id = s.take(4)
			return when(id){
				"RCT2" -> {
					Rect2D(ByteData.string2Vec2(s.substring(4, 12)), ByteData.string2Float(s.substring(12, 16)), ByteData.string2Float(s.substring(16, 20)), ByteData.string2Float(s.substring(20, 24)))
				}
				"PARA" -> {
					Parallelogram(ByteData.string2Vec2(s.substring(4, 12)), ByteData.string2Vec2(s.substring(12, 20)), ByteData.string2Vec2(s.substring(20, 28)))
				}
				else -> Rect2D(Vec2(0f), 1f, 1f)
			}
		}
	}
}

class GameObjectField(id: String, getter: () -> GameObject?, setter: (GameObject?) -> Unit) : ComponentField<GameObject?>(
	id, getter, setter, Companion::serialise, Companion::parse) {

	override fun isLateParse(): Boolean = true

	companion object {
		fun <T : GameObject?> serialise(component: ComponentI, obj: T): String {
			// If field value is null then just return a null string
			if(obj == null) return "null"

			val parent = component.parent
			if(obj == parent) return ""

			val thisAncestry = parent.getAncestry()
			val objAncestry = obj.getAncestry()

			// obj is a top level object with no parent
			if(objAncestry.isEmpty()){
				// obj is the highest ancestor of field's component's object (FCO), return the appropriate number of parent characters
				return if(obj == thisAncestry.lastOrNull()){
					"/\\".repeat(thisAncestry.size)
				}
				// Otherwise they are not related, look for top level object with obj's name
				else {
					"/;${obj.name.replace("/", "//")}"
				}
			}
			// If FCO is a top level object, or it is not related to obj,
			// then serialise the full ancestry of obj,
			// including FCO only if necessary
			else if(thisAncestry.isEmpty() || thisAncestry.last() != objAncestry.last()){
				val sb = StringBuilder()
				if(parent != objAncestry.last()){
					sb.append("/;${parent.name.replace("/", "//")}/~")
				}
				for(i in (0..objAncestry.size - 2).reversed()){
					sb.append(objAncestry[i].name.replace("/", "//") + "/~")
				}
				return sb.append(obj.name.replace("/", "//")).toString()
			}
			// FCO and obj are non-directly related to each other
			var i = 1
			while(++i < thisAncestry.size){
				if(thisAncestry[thisAncestry.size - i] != objAncestry[objAncestry.size - i]) break
			}
			val shared = i - 1
			val sb = StringBuilder("/\\".repeat(thisAncestry.size + 1 - shared)).append("/~")
			if(shared < objAncestry.size) {
				for (index in (0..objAncestry.size - i).reversed()) {
					sb.append(objAncestry[index].name.replace("/", "//") + "/~")
				}
			}
			return sb.append(obj.name.replace("/", "//")).toString()
		}

		@Suppress("UNCHECKED_CAST")
		fun parse(component: ComponentI, s: String): GameObject? {
			if(s.isEmpty()) return component.parent
			else if(s == "null") return null

			val parts = mutableListOf<String>()
			var start = 1
			var i = 2
			while(i < s.length){
				if(s[i++] == '/'){
					if(s[i++] != '/'){
						parts.add(s.substring(start, i - 2))
						start = i - 1
					}
				}
			}
			parts.add(s.substring(start))

			var obj = component.parent
			i = 0
			if(parts.first().startsWith(";")){
				obj = component.parent.getObjectCollection()?.findTop(parts.first().substring(1)) ?: return null
				i++
			}
			while(i < parts.size){
				val part = parts[i++]
				when(part[0]){
					'\\' -> obj = obj.parent ?: return null
					'~' -> obj = obj.getChild(part.substring(1)) ?: return null
				}
			}

			return obj
		}
	}
}

class CollectionField<T, C : Collection<T>>(
	id: String,
	getter: () -> C,
	setter: (C) -> Unit,
	val separator: String,
	serialise: (T) -> String,
	parse: (String) -> T?,
	collectionConverter: (List<T>) -> C
) : ComponentField<C>(
	id, getter, setter, { it.joinToString(separator, transform = serialise) },
	{ s ->
		collectionConverter(s.split(separator).mapNotNull { parse(it) })
	})
