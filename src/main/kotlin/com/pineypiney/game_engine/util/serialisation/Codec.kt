package com.pineypiney.game_engine.util.serialisation

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.fields.GameObjectField
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.*
import glm_.*
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

interface Codec<A> {
	fun <E> encode(ops: SerialOps<E>, value: A): E
	fun <E> decode(ops: SerialOps<E>, value: E): A
	fun encode(stream: OutputStream, value: A)
	fun decode(stream: InputStream): A

	fun optional(default: A) = OptionalCodec(this, default)
	fun nullable() = NullableCodec(this)
	fun opnull(default: A? = null) = this.nullable().optional(default)

	fun <P> field(getter: (P) -> A, name: String) = Field(this, getter, name)
	fun <P> field(name: String, getter: (P) -> A) = Field(this, getter, name)

	fun <B> map(
		g1: (B) -> A,
		factory: (A) -> B
	): Codec<B> = object : Codec<B> {
		override fun <E> encode(ops: SerialOps<E>, value: B): E {
			return encode(ops, g1(value))
		}

		override fun <E> decode(ops: SerialOps<E>, value: E): B {
			val p1 = this@Codec.decode(ops, value)
			return factory(p1)
		}

		override fun encode(stream: OutputStream, value: B) {
			encode(stream, g1(value))
		}

		override fun decode(stream: InputStream): B {
			val p1 = this@Codec.decode(stream)
			return factory(p1)
		}
	}

	fun <B> map(
		name: String,
		getter: (B) -> A,
		factory: (A) -> B
	): Codec<B> = object : Codec<B> {
		override fun <E> encode(ops: SerialOps<E>, value: B): E {
			return ops.createMap(name, encode(ops, getter(value)))
		}

		override fun <E> decode(ops: SerialOps<E>, value: E): B {
			val p1 = this@Codec.decode(ops, ops.getChild(value, name))
			return factory(p1)
		}

		override fun encode(stream: OutputStream, value: B) {
			encode(stream, getter(value))
		}

		override fun decode(stream: InputStream): B {
			val p1 = this@Codec.decode(stream)
			return factory(p1)
		}
	}

	fun list(): Codec<List<A>> {
		return object : Codec<List<A>> {
			override fun <E> encode(ops: SerialOps<E>, value: List<A>): E {
				val array = ops.createArray()
				for (entry in value) ops.appendArray(array, encode(ops, entry))
				return array
			}

			override fun <E> decode(ops: SerialOps<E>, value: E): List<A> {
				val list = mutableListOf<A>()
				ops.forEach(value) { list.add(this@Codec.decode(ops, it)) }
				return list
			}

			override fun encode(stream: OutputStream, value: List<A>) {
				stream.int(value.size)
				for (entry in value) encode(stream, entry)
			}

			override fun decode(stream: InputStream): List<A> {
				return List(stream.int()) { this@Codec.decode(stream) }
			}
		}
	}

	fun <E> encodeUnsafe(ops: SerialOps<E>, value: Any): E {
		@Suppress("UNCHECKED_CAST")
		return encode(ops, value as A)
	}

	fun encodeUnsafe(stream: OutputStream, value: Any) {
		@Suppress("UNCHECKED_CAST")
		encode(stream, value as A)
	}

	fun encodeBytes(value: A): ByteArray {
		return ByteArrayOutputStream().use { stream ->
			encode(stream, value)
			stream.toByteArray()
		}
	}

	fun decodeBytes(bytes: ByteArray): A {
		return ByteArrayInputStream(bytes).use { stream ->
			decode(stream)
		}
	}

	companion object {

		fun <A, C1, C2> map(
			f1: Field<A, C1>,
			f2: Field<A, C2>,
			factory: (C1, C2) -> A
		): Codec<A> = object : Codec<A> {

			override fun <E> encode(ops: SerialOps<E>, value: A): E {
				val map = ops.createMap(f1.key, f1.encode(ops, value))
				ops.appendMap(map, f2.key, f2.encode(ops, value))
				return map
			}

			override fun <E> decode(ops: SerialOps<E>, value: E): A {
				if (value == ops.nul()) throw CodecException()
				val p1 = f1.decode(ops, value)
				val p2 = f2.decode(ops, value)
				return factory(p1, p2)
			}

			override fun encode(stream: OutputStream, value: A) {
				f1.encode(stream, value)
				f2.encode(stream, value)
			}

			override fun decode(stream: InputStream): A {
				val p1 = f1.decode(stream)
				val p2 = f2.decode(stream)
				return factory(p1, p2)
			}
		}

		fun <A, C1, C2, C3> map(
			f1: Field<A, C1>,
			f2: Field<A, C2>,
			f3: Field<A, C3>,
			factory: (C1, C2, C3) -> A
		): Codec<A> = object : Codec<A> {
			override fun <E> encode(ops: SerialOps<E>, value: A): E {
				val map = ops.createMap(f1.key, f1.encode(ops, value))
				ops.appendMap(map, f2.key, f2.encode(ops, value))
				ops.appendMap(map, f3.key, f3.encode(ops, value))
				return map
			}

			override fun <E> decode(ops: SerialOps<E>, value: E): A {
				if (value == null) throw CodecException()
				val p1 = f1.decode(ops, value)
				val p2 = f2.decode(ops, value)
				val p3 = f3.decode(ops, value)
				return factory(p1, p2, p3)
			}

			override fun encode(stream: OutputStream, value: A) {
				f1.encode(stream, value)
				f2.encode(stream, value)
				f3.encode(stream, value)
			}

			override fun decode(stream: InputStream): A {
				val p1 = f1.decode(stream)
				val p2 = f2.decode(stream)
				val p3 = f3.decode(stream)
				return factory(p1, p2, p3)
			}
		}

		fun <A, C1, C2, C3, C4> map(
			f1: Field<A, C1>,
			f2: Field<A, C2>,
			f3: Field<A, C3>,
			f4: Field<A, C4>,
			factory: (C1, C2, C3, C4) -> A
		): Codec<A> = object : Codec<A> {
			override fun <E> encode(ops: SerialOps<E>, value: A): E {
				val map = ops.createMap(f1.key, f1.encode(ops, value))
				ops.appendMap(map, f2.key, f2.encode(ops, value))
				ops.appendMap(map, f3.key, f3.encode(ops, value))
				ops.appendMap(map, f4.key, f4.encode(ops, value))
				return map
			}

			override fun <E> decode(ops: SerialOps<E>, value: E): A {
				if (value == null) throw CodecException()
				val p1 = f1.decode(ops, value)
				val p2 = f2.decode(ops, value)
				val p3 = f3.decode(ops, value)
				val p4 = f4.decode(ops, value)
				return factory(p1, p2, p3, p4)
			}

			override fun encode(stream: OutputStream, value: A) {
				f1.encode(stream, value)
				f2.encode(stream, value)
				f3.encode(stream, value)
				f4.encode(stream, value)
			}

			override fun decode(stream: InputStream): A {
				val p1 = f1.decode(stream)
				val p2 = f2.decode(stream)
				val p3 = f3.decode(stream)
				val p4 = f4.decode(stream)
				return factory(p1, p2, p3, p4)
			}
		}

		fun <A, C1, C2, C3, C4, C5> map(
			f1: Field<A, C1>,
			f2: Field<A, C2>,
			f3: Field<A, C3>,
			f4: Field<A, C4>,
			f5: Field<A, C5>,
			factory: (C1, C2, C3, C4, C5) -> A
		): Codec<A> = object : Codec<A> {
			override fun <E> encode(ops: SerialOps<E>, value: A): E {
				val map = ops.createMap(f1.key, f1.encode(ops, value))
				ops.appendMap(map, f2.key, f2.encode(ops, value))
				ops.appendMap(map, f3.key, f3.encode(ops, value))
				ops.appendMap(map, f4.key, f4.encode(ops, value))
				ops.appendMap(map, f5.key, f5.encode(ops, value))
				return map
			}

			override fun <E> decode(ops: SerialOps<E>, value: E): A {
				if (value == null) throw CodecException()
				val p1 = f1.decode(ops, value)
				val p2 = f2.decode(ops, value)
				val p3 = f3.decode(ops, value)
				val p4 = f4.decode(ops, value)
				val p5 = f5.decode(ops, value)
				return factory(p1, p2, p3, p4, p5)
			}

			override fun encode(stream: OutputStream, value: A) {
				f1.encode(stream, value)
				f2.encode(stream, value)
				f3.encode(stream, value)
				f4.encode(stream, value)
				f5.encode(stream, value)
			}

			override fun decode(stream: InputStream): A {
				val p1 = f1.decode(stream)
				val p2 = f2.decode(stream)
				val p3 = f3.decode(stream)
				val p4 = f4.decode(stream)
				val p5 = f5.decode(stream)
				return factory(p1, p2, p3, p4, p5)
			}
		}

		val BOOL = object : Codec<Boolean> {
			override fun <E> encode(ops: SerialOps<E>, value: Boolean): E = ops.writeBool(value)
			override fun <E> decode(ops: SerialOps<E>, value: E): Boolean = ops.readBool(value)
			override fun encode(stream: OutputStream, value: Boolean) = stream.write(value.i)
			override fun decode(stream: InputStream): Boolean = stream.read() > 0
		}
		val BYTE = object : Codec<Byte> {
			override fun <E> encode(ops: SerialOps<E>, value: Byte): E = ops.writeByte(value)
			override fun <E> decode(ops: SerialOps<E>, value: E): Byte = ops.readByte(value)
			override fun encode(stream: OutputStream, value: Byte) = stream.write(value.toInt())
			override fun decode(stream: InputStream): Byte = stream.read().toByte()
		}
		val SHORT = object : Codec<Short> {
			override fun <E> encode(ops: SerialOps<E>, value: Short): E = ops.writeShort(value)
			override fun <E> decode(ops: SerialOps<E>, value: E): Short = ops.readShort(value)
			override fun encode(stream: OutputStream, value: Short) = stream.short(value)
			override fun decode(stream: InputStream) = stream.short().toShort()
		}
		val INT = object : Codec<Int> {
			override fun <E> encode(ops: SerialOps<E>, value: Int): E = ops.writeInt(value)
			override fun <E> decode(ops: SerialOps<E>, value: E): Int = ops.readInt(value)
			override fun encode(stream: OutputStream, value: Int) = stream.int(value)
			override fun decode(stream: InputStream) = stream.int()
		}
		val UINT = object : Codec<UInt> {
			override fun <E> encode(ops: SerialOps<E>, value: UInt): E = ops.writeInt(value.toInt())
			override fun <E> decode(ops: SerialOps<E>, value: E): UInt = ops.readInt(value).toUInt()
			override fun encode(stream: OutputStream, value: UInt) = stream.int(value.toInt())
			override fun decode(stream: InputStream) = stream.int().toUInt()
		}
		val LONG = object : Codec<Long> {
			override fun <E> encode(ops: SerialOps<E>, value: Long): E = ops.writeLong(value)
			override fun <E> decode(ops: SerialOps<E>, value: E): Long = ops.readLong(value)
			override fun encode(stream: OutputStream, value: Long) = stream.long(value)
			override fun decode(stream: InputStream) = stream.long()
		}
		val FLOAT = object : Codec<Float> {
			override fun <E> encode(ops: SerialOps<E>, value: Float): E = ops.writeFloat(value)
			override fun <E> decode(ops: SerialOps<E>, value: E): Float = ops.readFloat(value)
			override fun encode(stream: OutputStream, value: Float) = stream.float(value)
			override fun decode(stream: InputStream) = stream.float()
		}
		val DOUBLE = object : Codec<Double> {
			override fun <E> encode(ops: SerialOps<E>, value: Double): E = ops.writeDouble(value)
			override fun <E> decode(ops: SerialOps<E>, value: E): Double = ops.readDouble(value)
			override fun encode(stream: OutputStream, value: Double) = stream.double(value)
			override fun decode(stream: InputStream) = stream.double()
		}
		val STRING = object : Codec<String> {
			override fun <E> encode(ops: SerialOps<E>, value: String): E = ops.writeString(value)
			override fun <E> decode(ops: SerialOps<E>, value: E): String = ops.readString(value)
			override fun encode(stream: OutputStream, value: String) {
				stream.int(value.length)
				stream.string(value)
			}

			override fun decode(stream: InputStream): String {
				val len = stream.int()
				return stream.string(len)
			}
		}

		fun <T> serial(baseOps: SerialOps<T>) = object : Codec<T> {
			override fun <E> encode(ops: SerialOps<E>, value: T): E {
				@Suppress("UNCHECKED_CAST")
				return if (baseOps == ops) value as E
				else ops.writeString(baseOps.stringify(value))
			}

			override fun <E> decode(ops: SerialOps<E>, value: E): T {
				@Suppress("UNCHECKED_CAST")
				return if (baseOps == ops) value as T
				else baseOps.writeString(ops.stringify(value))
			}

			override fun encode(stream: OutputStream, value: T) {
				STRING.encode(stream, baseOps.stringify(value))
			}

			override fun decode(stream: InputStream): T {
				val string = STRING.decode(stream)
				return baseOps.parse(string.reader())
			}
		}


		val BYTES = object : Codec<ByteArray> {
			override fun <E> encode(ops: SerialOps<E>, value: ByteArray): E = ops.writeString(value.toString(Charsets.ISO_8859_1))
			override fun <E> decode(ops: SerialOps<E>, value: E): ByteArray = ops.readString(value).toByteArray(Charsets.ISO_8859_1)
			override fun encode(stream: OutputStream, value: ByteArray) {
				stream.int(value.size)
				stream.write(value)
			}

			override fun decode(stream: InputStream): ByteArray {
				return stream.readNBytes(stream.int())
			}
		}

		val INTS = object : Codec<Iterable<Int>> {
			override fun <E> encode(ops: SerialOps<E>, value: Iterable<Int>): E = ops.writeInts(value)
			override fun <E> decode(ops: SerialOps<E>, value: E): Iterable<Int> = ops.readInts(value)
			override fun encode(stream: OutputStream, value: Iterable<Int>) {
				stream.int(value.count())
				for (entry in value) stream.int(entry)
			}

			override fun decode(stream: InputStream): Iterable<Int> {
				return List(stream.int()) { stream.int() }
			}
		}
		val FLOATS = object : Codec<Iterable<Float>> {
			override fun <E> encode(ops: SerialOps<E>, value: Iterable<Float>): E = ops.writeFloats(value)
			override fun <E> decode(ops: SerialOps<E>, value: E): Iterable<Float> = ops.readFloats(value)
			override fun encode(stream: OutputStream, value: Iterable<Float>) {
				stream.int(value.count())
				for (entry in value) stream.float(entry)
			}

			override fun decode(stream: InputStream): Iterable<Float> {
				return List(stream.int()) { stream.float() }
			}
		}

		val KEY = STRING.map(ResourceKey::key, ::ResourceKey)

		val VEC2 = FLOATS.map({ it.toFloatArray().asIterable() }, ::Vec2)
		val VEC2I = INTS.map({ it.toIntArray().asIterable() }, ::Vec2i)
		val VEC3 = FLOATS.map({ it.toFloatArray().asIterable() }, ::Vec3)
		val VEC3I = INTS.map({ it.toIntArray().asIterable() }, ::Vec3i)
		val VEC4 = FLOATS.map({ it.toFloatArray().asIterable() }, ::Vec4)
		val VEC4I = INTS.map({ it.toIntArray().asIterable() }, ::Vec4i)
		val QUAT = FLOATS.map({ List(4) { i -> it[i] } }) {
			val iter = it.iterator()
			val x = iter.next()
			val y = iter.next()
			val z = iter.next()
			Quat(iter.next(), x, y, z)
		}

		val SHADER = map(
			KEY.field("v") { it: RenderShader -> ResourceKey(it.vertex.id) },
			KEY.field("f") { it: RenderShader -> ResourceKey(it.fragment.id) },
			KEY.opnull().field("tc") { it: RenderShader -> it.getSubShader(ShaderStage.TESS_CTRL)?.let { ResourceKey(it.id) } },
			KEY.opnull().field("te") { it: RenderShader -> it.getSubShader(ShaderStage.TESS_EVAL)?.let { ResourceKey(it.id) } },
			KEY.opnull().field("g") { it: RenderShader -> it.getSubShader(ShaderStage.GEOMETRY)?.let { ResourceKey(it.id) } },
		) { v, f, tc, te, g -> ShaderLoader[v, f, tc, te, g] }

		val TEXTURE = STRING.map(Texture2D::id) { TextureLoader[ResourceKey(it)] }
		val MODEL = STRING.map(Model::name) { ModelLoader[ResourceKey(it)] }

		fun relativeGameObject(container: Any): Codec<GameObject?> {
			return STRING.map({ GameObjectField.serialise(it, container) }) { GameObjectField.parse(it, container) }
		}
	}

	class Field<P, A>(val codec: Codec<A>, val getter: (P) -> A, val key: String) {

		val aliases = mutableSetOf<String>()

		fun alias(alias: String): Field<P, A> {
			aliases.add(alias)
			return this
		}

		fun <E> encode(ops: SerialOps<E>, value: P) = codec.encode(ops, getter(value))

		fun <E> decode(ops: SerialOps<E>, map: E): A {
			if (ops.hasChild(map, key)) return codec.decode(ops, ops.getChild(map, key))
			for (alias in aliases) if (ops.hasChild(map, alias)) return codec.decode(ops, ops.getChild(map, alias))
			return codec.decode(ops, ops.missing())
		}

		fun encode(stream: OutputStream, value: P) = codec.encode(stream, getter(value))

		fun decode(stream: InputStream) = codec.decode(stream)
	}
}