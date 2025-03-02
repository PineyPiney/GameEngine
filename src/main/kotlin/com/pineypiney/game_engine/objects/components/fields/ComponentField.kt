package com.pineypiney.game_engine.objects.components.fields

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.toByteString
import com.pineypiney.game_engine.util.extension_functions.toString
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
	val serialise: (T) -> String,
	val parse: (String) -> T?,
	val copy: (T) -> T = { it }
) {
	fun set(value: String) {
		setter(parse(value) ?: return)
	}

	fun copyTo(other: ComponentField<T>) {
		other.setter(copy(getter()))
	}

	override fun toString(): String {
		return "ComponentField[$id]"
	}

	fun serialiseValue() = serialise(getter())

	fun serialise(head: StringBuilder, data: StringBuilder) {
		val s = serialiseValue()
		head.append(id.length.toByteString(1) + id + s.length.toByteString())
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

class ShaderField(id: String, getter: () -> Shader, setter: (Shader) -> Unit): ComponentField<Shader>(id,
	getter, setter, ::serialise, ::parse){

	companion object {
		fun serialise(shader: Shader): String {
			val sb = StringBuilder()
			val g = shader.gName != null
			sb.append(if (g) '3' else '2')
			sb.append(shader.vName.length.toChar() + shader.vName)
			sb.append(shader.fName.length.toChar() + shader.fName)
			if (g) sb.append(shader.gName.length.toChar() + shader.gName)
			return sb.toString()
		}

		fun parse(s: String): Shader{
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
	getter, setter, { it.fileLocation.substringBefore('.') },
	{ s -> TextureLoader[ResourceKey(s)] })

class ModelField(id: String, getter: () -> Model, setter: (Model) -> Unit): ComponentField<Model>(id,
	getter, setter, { it.name.substringBefore('.') },
	{ s -> ModelLoader[ResourceKey(s)] })

class GameObjectField<T : GameObject?>(id: String, getter: () -> T, setter: (T) -> Unit) : ComponentField<T>(
	id, getter, setter, Companion::serialise, Companion::parse) {

	companion object {
		fun <T : GameObject?> serialise(o: T): String {
			return o?.name ?: "Storable"
		}

		@Suppress("UNCHECKED_CAST")
		fun <T : GameObject?> parse(s: String): T? {
			// TODO
			//return c.parent.objects?.getAllObjects()?.firstOrNull { it.name == s } as? T
			return null
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
