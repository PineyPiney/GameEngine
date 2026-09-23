package com.pineypiney.game_engine.resources.shaders.parameters

import com.pineypiney.game_engine.util.ApiEnum
import com.pineypiney.game_engine.util.serialisation.Codec
import com.pineypiney.game_engine.util.serialisation.SerialOps
import java.io.InputStream
import java.io.OutputStream

data class Blending(
	val src: ColourAndAlpha<BlendFactor> = Separate(BlendFactor.SRC_ALPHA, BlendFactor.ONE),
	val dst: ColourAndAlpha<BlendFactor> = Separate(BlendFactor.ONE_MINUS_SRC_ALPHA, BlendFactor.ZERO),
	val op: ColourAndAlpha<BlendOp> = Same(BlendOp.ADD),
) {

	companion object {

		val DEFAULT = Blending(
			Separate(BlendFactor.SRC_ALPHA, BlendFactor.SRC_ALPHA),
			Separate(BlendFactor.ONE_MINUS_SRC_ALPHA, BlendFactor.DST_ALPHA),
			Separate(BlendOp.ADD, BlendOp.MAX)
		)

		val FACTOR_CODEC = ColourAndAlpha.codec(Codec.enum(BlendFactor::valueOf))
		val OP_CODEC = ColourAndAlpha.codec(Codec.enum(BlendOp::valueOf))

		val CODEC = Codec.map(
			FACTOR_CODEC.field("src", Blending::src),
			FACTOR_CODEC.field("dst", Blending::dst),
			OP_CODEC.field("op", Blending::op),
			::Blending
		)

	}

	sealed class ColourAndAlpha<E : ApiEnum> {

		abstract fun colour(): E
		abstract fun alpha(): E

		companion object {
			fun <A : ApiEnum> codec(enumCodec: Codec<A>) = object : Codec<ColourAndAlpha<A>> {
				override fun <E> encode(ops: SerialOps<E>, value: ColourAndAlpha<A>): E {
					return when (value) {
						is Separate<A> -> {
							val map = ops.createMap("rgb", enumCodec.encode(ops, value.rgb))
							ops.appendMap(map, "a", enumCodec.encode(ops, value.a))
							map
						}

						is Same<A> -> {
							ops.createMap("value", enumCodec.encode(ops, value.value))
						}
					}
				}

				override fun <E> decode(ops: SerialOps<E>, value: E): ColourAndAlpha<A> {
					if (ops.hasChild(value, "rgb")) {
						val rgb = enumCodec.decode(ops, ops.getChild(value, "rgb"))
						val a = enumCodec.decode(ops, ops.getChild(value, "a"))
						return Separate(rgb, a)
					} else {
						val value = enumCodec.decode(ops, ops.getChild(value, "value"))
						return Same(value)
					}
				}

				override fun encode(stream: OutputStream, value: ColourAndAlpha<A>) {
					when (value) {
						is Separate<A> -> {
							Codec.BOOL.encode(stream, true)
							enumCodec.encode(stream, value.rgb)
							enumCodec.encode(stream, value.a)
						}

						is Same<A> -> {
							Codec.BOOL.encode(stream, false)
							enumCodec.encode(stream, value.value)
						}
					}
				}

				override fun decode(stream: InputStream): ColourAndAlpha<A> {
					val separate = Codec.BOOL.decode(stream)
					if (separate) {
						val rgb = enumCodec.decode(stream)
						val a = enumCodec.decode(stream)
						return Separate(rgb, a)
					} else {
						val value = enumCodec.decode(stream)
						return Same(value)
					}
				}

			}
		}
	}

	data class Separate<E : ApiEnum>(val rgb: E, val a: E) : ColourAndAlpha<E>() {

		override fun colour(): E = rgb
		override fun alpha(): E = a
	}

	data class Same<E : ApiEnum>(val value: E) : ColourAndAlpha<E>() {

		override fun colour(): E = value
		override fun alpha(): E = value
	}
}