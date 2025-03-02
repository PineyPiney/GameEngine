package com.pineypiney.game_engine.objects.util

import com.pineypiney.game_engine.util.extension_functions.associateIndexed
import glm_.f
import glm_.i
import java.io.File

class Animation(
	val name: String,
	val frames: MutableMap<Int, KeyFrame>,
	var fps: Float,
	var totalFrames: Int,
	val fileLocation: String? = null
) {

	val length = totalFrames / fps


	constructor(name: String, fps: Float, root: String, textures: List<String>, file: String? = null) : this(
		name,
		textures.associateIndexed { i, it -> i to KeyFrame(mutableMapOf("RND.txr" to "$root/$it")) }.toMutableMap(),
		fps,
		textures.size,
		file
	) {
		save()
	}

	var lastFrame = -1

	fun getCurrentFrame(time: Float): Int {
		val frame = time * fps
		return frames.keys.lastOrNull { it <= frame } ?: frames.minOf { it.key }
	}

	fun getProperties(time: Float): Map<String, String> {
		val frameTime = getCurrentFrame(time)
		if (frameTime == lastFrame) return emptyMap()

		lastFrame = frameTime
		return frames[frameTime]?.properties!!
	}

	fun getFrameProperties(): Map<String, String> {
		return frames[lastFrame]?.properties!!
	}

	override fun toString(): String {
		return "Animation[$name]"
	}

	fun save(): Boolean {
		val relativeLocation = fileLocation ?: return false
		val file = File(fullPath(relativeLocation))
		if (!file.exists()) {
			if (file.parentFile.mkdirs()) file.createNewFile()
			else return false
		}

		val binaryString = StringBuilder()
		var text = "fps=$fps\nframes=$totalFrames\ndefinedFrames=${frames.size}"
		for ((t, f) in frames) {
			text += f.properties.entries.joinToString("&", "\n$t:") { (k, v) ->
				val p = binaryString.length
				binaryString.append(v)
				"$k=$p-${p + v.length}"
			}
		}

		file.writeText(text + '\n' + binaryString)
		return true
	}

	companion object {

		fun fullPath(relative: String) = "src/main/resources/animations/$relative.anim"
		operator fun invoke(file: String): Animation {
			val lines = File(fullPath(file)).readLines()
			val name = file.substringAfterLast("/")
			var fps = 0f
			var numFrames = 0
			var definedFrames = 0
			val frames = mutableMapOf<Int, KeyFrame>()

			if (lines.size >= 5) {
				if (lines[0].startsWith("fps")) {
					fps = lines[0].substring(4, lines[0].length).f
				}
				if (lines[1].startsWith("frames")) {
					numFrames = lines[1].substring(7, lines[1].length).i
				}
				if (lines[2].startsWith("definedFrames")) {
					definedFrames = lines[2].substring(14, lines[2].length).i
				}

				val binaryString = lines.subList(3 + definedFrames, lines.size).joinToString("\n")
				for (i in 3..2 + definedFrames) {
					val line = lines[i]
					val s = line.indexOf(':')
					val t = line.substring(0, s).i
					val m = line.substring(s + 1).split("&").associate {
						val e = it.indexOf('=')
						val k = it.substring(0, e)
						val (start, end) = it.substring(e + 1).split('-')
						k to binaryString.substring(start.i, end.i)

					}.toMutableMap()
					frames[t] = KeyFrame(m)
				}
			}

			return Animation(name, frames, fps, numFrames, file)
		}
	}

	class KeyFrame(val properties: MutableMap<String, String>) {
		override fun toString(): String {
			return "KeyFrame[" +
					properties.map { (k, v) ->
						"{$k = $v}"
					}.joinToString() + "]"
		}
	}
}