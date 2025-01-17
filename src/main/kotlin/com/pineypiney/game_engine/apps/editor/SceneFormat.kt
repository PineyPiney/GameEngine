package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.objects.GameObjectSerializer
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.extension_functions.toByteString
import glm_.int
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream

class SceneFormat(val extension: String, val serialise: (OutputStream, EditorScreen) -> Unit, val parse: (InputStream, EditorScreen) -> Unit) {

	companion object {

		val nativeFormat = SceneFormat("scn", ::serialise, ::parse)

		fun serialise(stream: OutputStream, scene: EditorScreen){
			val objects = scene.sceneObjects.map.flatMap { it.value }
			stream.write(ByteData.int2Bytes(objects.size))
			for(o in objects){
				val s = GameObjectSerializer.serialise(o)
				val f = s.length.toByteString() + s
				stream.write(f.toByteArray(Charsets.ISO_8859_1))
			}
		}

		fun parse(stream: InputStream, scene: EditorScreen){
			val numObjects = stream.int()
			for(i in 1..numObjects){
				val objSize = stream.int()
				val objData = stream.readNBytes(objSize)
				val o = GameObjectSerializer.parse(ByteArrayInputStream(objData))
				scene.sceneObjects.addObject(o)
			}
		}
	}
}