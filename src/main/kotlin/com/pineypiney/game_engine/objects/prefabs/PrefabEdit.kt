package com.pineypiney.game_engine.objects.prefabs

import com.google.gson.JsonElement
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.LateParse
import com.pineypiney.game_engine.util.extension_functions.string
import com.pineypiney.game_engine.util.serialisation.Codec
import com.pineypiney.game_engine.util.serialisation.SerialOps
import java.io.InputStream
import java.io.OutputStream

abstract class PrefabEdit {

	fun findDescendant(obj: GameObject, parentLoc: String): GameObject? {
		if(parentLoc.isEmpty()) return obj
		val parts = parentLoc.split('$')
		var child = obj
		for (part in parts) {
			child = child.getChild(part) ?: return null
		}
		return child
	}

	abstract fun execute(obj: GameObject, parentLoc: String, list: LateParse<JsonElement>)

	abstract fun getID(): String

	fun <E> serialise(ops: SerialOps<E>): E = CODEC.encode(ops, this)

	object CODEC : Codec<PrefabEdit?> {
		override fun <E> encode(ops: SerialOps<E>, value: PrefabEdit?): E {
			if (value == null) return ops.nul()
			val codec = codecs[value.getID()]!!
			val e = codec.encodeUnsafe(ops, value)
			ops.put(e, "type", value.getID())
			return e
		}

		override fun <E> decode(ops: SerialOps<E>, value: E): PrefabEdit? {
			val editType = ops.getString(value, "type")
			return codecs[editType]!!.decode(ops, value)
		}

		override fun encode(stream: OutputStream, value: PrefabEdit?) {
			if (value == null) return
			val codec = codecs[value.getID()]!!
			stream.string(value.getID())
			codec.encodeUnsafe(stream, value)
		}

		override fun decode(stream: InputStream): PrefabEdit? {
			val editType = stream.string(4)
			return codecs[editType]!!.decode(stream)
		}
	}

	companion object {
		val codecs: MutableMap<String, Codec<out PrefabEdit?>> = mutableMapOf(
			"chad" to PrefabChildAddEdit.CODEC,
			"chrm" to PrefabChildRemoveEdit.CODEC,
			"cpad" to PrefabComponentAddEdit.CODEC,
			"cprm" to PrefabComponentRemoveEdit.CODEC,
			"fled" to PrefabFieldEdit.CODEC
		)
	}
}