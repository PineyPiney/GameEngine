package com.pineypiney.game_engine.objects.components.fields

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.LateParse
import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.toString
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import com.pineypiney.game_engine.util.serialisation.Codec
import com.pineypiney.game_engine.util.serialisation.SerialOps
import glm_.asHexString
import glm_.int
import glm_.intValue
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream

abstract class ComponentField<T>(
	val id: String,
	val container: Any,
	val codec: Codec<T>,
	val getter: () -> T,
	val setter: (T) -> Unit,
	val serialise: (T, Any) -> String,
	val parse: (String, Any) -> T?,
	val copy: (T) -> T = { it }
) {

	constructor(
		id: String, container: Any, codec: Codec<T>,
		getter: () -> T, setter: (T) -> Unit,
		serialise: (T) -> String, parse: (String) -> T?, copy: (T) -> T = { it }
	) :
			this(
				id, container, codec, getter, setter,
				{ t, _ -> serialise(t) }, { s, _ -> parse(s) }, copy
			)


	fun <E> set(ops: SerialOps<E>, head: E) {
		try {
			setter(codec.decode(ops, head))
		}
		catch (_: Exception){
			GameEngineI.logger.warn("Could not set $this value to $head")
		}
	}

	fun set(stream: InputStream) {
		try {
			setter(codec.decode(stream))
		} catch (_: Exception) {
			GameEngineI.logger.warn("Could not set $this value to $stream")
		}
	}

	fun copyTo(other: ComponentField<T>) {
		other.setter(copy(getter()))
	}

	open fun isLateParse() = false

	override fun toString(): String {
		return "ComponentField[$id]"
	}

	fun serialise(stream: OutputStream) {
		codec.encode(stream, getter())
	}

	fun parse(stream: InputStream, container: Any, lateParse: LateParse<ByteArray>) {
		val fieldSize = stream.int()
		val value = stream.readNBytes(fieldSize)

		if (isLateParse()) lateParse.add(Triple(container, this, value))
		else set(ByteArrayInputStream(value))
	}

	open fun <E> encode(ops: SerialOps<E>): E {
		return codec.encode(ops, getter())
	}

	fun <E> decode(ops: SerialOps<E>, head: E, component: ComponentI, lateParse: LateParse<E>) {
		if (isLateParse()) lateParse.add(Triple(component, this, head))
		else setter(codec.decode(ops, head))
	}

	companion object {
		fun smallString(string: String) = string.length.toChar() + string
	}
}

class BoolField(id: String, container: Any, getter: () -> Boolean, setter: (Boolean) -> Unit) : ComponentField<Boolean>(
	id, container, Codec.BOOL,
	getter, setter, { b -> if(b) Char(1).toString() else Char(0).toString() }, { s -> s[0].code > 0 })

class IntField(id: String, container: Any, getter: () -> Int, setter: (Int) -> Unit) : ComponentField<Int>(
	id, container, Codec.INT,
	getter, setter, { i -> i.asHexString }, { s -> s.intValue(16) })

class IntRangeField(id: String, container: Any, val range: IntRange, getter: () -> Int, setter: (Int) -> Unit) : ComponentField<Int>(
	id, container, Codec.INT,
	getter, setter, { i -> i.asHexString }, { s -> s.intValue(16) })

class UIntField(id: String, container: Any, getter: () -> UInt, setter: (UInt) -> Unit) : ComponentField<UInt>(
	id, container, Codec.UINT,
	getter, setter, { i -> i.toInt().asHexString }, { s -> s.intValue(16).toUInt() })

class FloatField(id: String, container: Any, getter: () -> Float, setter: (Float) -> Unit) : ComponentField<Float>(
	id, container, Codec.FLOAT,
	getter, setter,
	ByteData::float2String, ByteData::string2Float
)

class DoubleField(id: String, container: Any, getter: () -> Double, setter: (Double) -> Unit) : ComponentField<Double>(
	id, container, Codec.DOUBLE,
	getter, setter,
	ByteData::double2String, ByteData::string2Double
)

open class VecTField<T, V>(
	id: String,
	container: Any,
	codec: Codec<V>,
	getter: () -> V,
	setter: (V) -> Unit,
	t2string: T.() -> String,
	serialise: V.(String, T.() -> String) -> String,
	parse: (String) -> V,
	default: () -> V,
	copy: (V) -> V
) :
	ComponentField<V>(id, container, codec, getter, setter, { v -> v.serialise("", t2string) }, { s ->
		try {
			parse(s)
		}
		catch (_: NumberFormatException){
			default()
		}}, copy)

open class VeciField<V>(
	id: String,
	container: Any,
	codec: Codec<V>,
	getter: () -> V,
	setter: (V) -> Unit,
	serialise: V.(String, Int.() -> String) -> String,
	parse: (String) -> V,
	default: () -> V,
	copy: (V) -> V
) : VecTField<Int, V>(
	id, container, codec, getter, setter, ByteData::int2String, serialise, parse, default, copy
)

class Vec2iField(id: String, container: Any, getter: () -> Vec2i, setter: (Vec2i) -> Unit) :
	VeciField<Vec2i>(id, container, Codec.VEC2I, getter, setter, Vec2i::toString, ByteData::string2Vec2i, ::Vec2i, ::Vec2i)

class Vec3iField(id: String, container: Any, getter: () -> Vec3i, setter: (Vec3i) -> Unit) :
	VeciField<Vec3i>(id, container, Codec.VEC3I, getter, setter, Vec3i::toString, ByteData::string2Vec3i, ::Vec3i, ::Vec3i)

class Vec4iField(id: String, container: Any, getter: () -> Vec4i, setter: (Vec4i) -> Unit) :
	VeciField<Vec4i>(id, container, Codec.VEC4I, getter, setter, Vec4i::toString, ByteData::string2Vec4i, ::Vec4i, ::Vec4i)

open class VecField<V>(
	id: String,
	container: Any,
	codec: Codec<V>,
	getter: () -> V,
	setter: (V) -> Unit,
	serialise: V.(String, Float.() -> String) -> String,
	parse: (String) -> V,
	default: () -> V,
	copy: (V) -> V
) : VecTField<Float, V>(
	id, container, codec, getter, setter, ByteData::float2String, serialise, parse, default, copy
)

class Vec2Field(id: String, container: Any, getter: () -> Vec2, setter: (Vec2) -> Unit) :
	VecField<Vec2>(id, container, Codec.VEC2, getter, setter, Vec2::toString, ByteData::string2Vec2, ::Vec2, ::Vec2)

class Vec3Field(id: String, container: Any, getter: () -> Vec3, setter: (Vec3) -> Unit) :
	VecField<Vec3>(id, container, Codec.VEC3, getter, setter, Vec3::toString, ByteData::string2Vec3, ::Vec3, ::Vec3)

class Vec4Field(id: String, container: Any, getter: () -> Vec4, setter: (Vec4) -> Unit) :
	VecField<Vec4>(id, container, Codec.VEC4, getter, setter, Vec4::toString, ByteData::string2Vec4, ::Vec4, ::Vec4)

class QuatField(id: String, container: Any, getter: () -> Quat, setter: (Quat) -> Unit) : ComponentField<Quat>(
	id, container, Codec.QUAT,
	getter, setter, { q -> q.toString("", ByteData::float2String) }, ByteData::string2Quat, { Quat(it.w, it.x, it.y, it.z) })

class ShaderField(id: String, container: Any, getter: () -> RenderShader, setter: (RenderShader) -> Unit) : ComponentField<RenderShader>(
	id, container, RenderShader.CODEC,
	getter, setter, RenderShader::toString, { RenderShader.missing })

class TextureField(id: String, container: Any, getter: () -> Texture2D, setter: (Texture2D) -> Unit) : ComponentField<Texture2D>(
	id, container,
	Codec.TEXTURE,
	getter, setter, { it.id },
	{ s -> TextureLoader[ResourceKey(s)] })

class ModelField(id: String, container: Any, getter: () -> Model, setter: (Model) -> Unit) : ComponentField<Model>(
	id, container, Codec.MODEL,
	getter, setter, { it.name.substringBefore('.') },
	{ s -> ModelLoader[ResourceKey(s)] })

class Shape2DField(id: String, container: Any, getter: () -> Shape2D, setter: (Shape2D) -> Unit) : ComponentField<Shape2D>(
	id,
	container, Shape2D.CODEC, getter, setter, ::serialise, ::parse
) {

	companion object {
		fun serialise(shape: Shape2D): String {
			return Shape2D.CODEC.encodeBytes(shape).toString(Charsets.ISO_8859_1)
		}

		fun parse(s: String): Shape2D{
			return Shape2D.CODEC.decodeBytes(s.toByteArray(Charsets.ISO_8859_1))
		}
	}
}

class GameObjectField(id: String, container: Any, getter: () -> GameObject?, setter: (GameObject?) -> Unit) : ComponentField<GameObject?>(
	id, container, Codec.relativeGameObject(container), getter, setter, ::serialise, ::parse
) {

	override fun isLateParse(): Boolean = true

	companion object {
		fun serialise(obj: GameObject?, ctx: Any): String {
			// If field value is null then just return a null string
			if(obj == null) return "null"
			val parent = if (ctx !is ComponentI) GameObject()
			else ctx.parent
			
			if(obj == parent) return ""

			val thisAncestry = parent.getAncestry()
			val objAncestry = obj.getAncestry()

			// obj is a top level object with no parent
			if(objAncestry.isEmpty()){
				// obj is the highest ancestor of field's component's object (FCO), return the appropriate number of parent characters
				return if(obj == thisAncestry.lastOrNull()){
					"/\\".repeat(thisAncestry.size).substring(1)
				}
				// Otherwise they are not related, look for top level object with obj's name
				else {
					";${obj.name.replace("/", "//")}"
				}
			}
			// If FCO is a top level object, or it is not related to obj,
			// then serialise the full ancestry of obj,
			// including FCO only if necessary
			else if(thisAncestry.isEmpty() || thisAncestry.last() != objAncestry.last()){
				val sb = StringBuilder()
				if(parent != objAncestry.last()){
					sb.append(";${parent.name.replace("/", "//")}/~")
				} else sb.append('~')
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

			val sb = StringBuilder("\\/".repeat(thisAncestry.size - shared))
			sb.append("~")

			if(shared < objAncestry.size) {
				for (index in (0..<objAncestry.size - i).reversed()) {
					sb.append(objAncestry[index].name.replace("/", "//") + "/~")
				}
			}
			return sb.append(obj.name.replace("/", "//")).toString()
		}

		@Suppress("UNCHECKED_CAST")
		fun parse(s: String, container: Any): GameObject? {
			if (container !is ComponentI) return null
			if (s.isEmpty()) return container.parent
			else if(s == "null") return null

			val parts = mutableListOf<String>()
			var start = 0
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

			var obj = container.parent
			i = 0
			if(parts.first().startsWith(";")){
				obj = container.parent.getObjectCollection()?.findTop(parts.first().substring(1)) ?: return null
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
