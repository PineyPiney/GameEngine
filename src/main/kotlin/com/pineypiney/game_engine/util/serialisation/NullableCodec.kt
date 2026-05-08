package com.pineypiney.game_engine.util.serialisation

import com.pineypiney.game_engine.util.extension_functions.bool
import java.io.InputStream
import java.io.OutputStream

class NullableCodec<A>(val parent: Codec<A>) : Codec<A?> {

	override fun <E> encode(ops: SerialOps<E>, value: A?): E {
		return if (value == null) ops.nul()
		else parent.encode(ops, value)
	}

	override fun <E> decode(ops: SerialOps<E>, value: E): A? {
		return if (value == ops.nul()) null
		else parent.decode(ops, value)
	}

	override fun encode(stream: OutputStream, value: A?) {
		if (value == null) stream.write(0)
		else {
			stream.write(1)
			parent.encode(stream, value)
		}
	}

	override fun decode(stream: InputStream): A? {
		val b = stream.bool()
		return if (b) parent.decode(stream)
		else null
	}
}