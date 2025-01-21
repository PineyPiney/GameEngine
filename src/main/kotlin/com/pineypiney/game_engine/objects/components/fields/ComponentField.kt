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
import com.pineypiney.game_engine.util.extension_functions.fromString
import com.pineypiney.game_engine.util.extension_functions.toByteString
import com.pineypiney.game_engine.util.extension_functions.toString
import glm_.asHexString
import glm_.intValue
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

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

	fun serialise(head: StringBuilder, data: StringBuilder) {
		val s = serialise(getter())
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

class Vec2Field(id: String, getter: () -> Vec2, setter: (Vec2) -> Unit) : ComponentField<Vec2>(id,
	getter, setter, { v -> v.toString(",", ByteData::float2String) }, { s ->
		try {
			Vec2.fromString(s, false, ByteData::string2Float)
		} catch (_: NumberFormatException) {
			Vec2()
		}
	}, { Vec2(it.x, it.y) })

class Vec3Field(id: String, getter: () -> Vec3, setter: (Vec3) -> Unit) : ComponentField<Vec3>(id,
	getter, setter, { v -> v.toString(",", ByteData::float2String) }, { s ->
		Vec3.fromString(
			s, false,
			ByteData::string2Float
		)
	}, { Vec3(it.x, it.y, it.z) })

class Vec4Field(id: String, getter: () -> Vec4, setter: (Vec4) -> Unit) : ComponentField<Vec4>(id,
	getter, setter, { v -> v.toString(",", ByteData::float2String) }, { s ->
		Vec4.fromString(
			s, false,
			ByteData::string2Float
		)
	}, { Vec4(it.x, it.y, it.z, it.w) })

class QuatField(id: String, getter: () -> Quat, setter: (Quat) -> Unit) : ComponentField<Quat>(id,
	getter, setter, { q -> q.toString(",", ByteData::float2String) }, { s ->
		Quat.fromString(
			s, false, true,
			ByteData::string2Float
		)
	}, { Quat(it.w, it.x, it.y, it.z) })

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
