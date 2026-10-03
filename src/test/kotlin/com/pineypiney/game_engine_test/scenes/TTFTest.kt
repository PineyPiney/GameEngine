package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.GameRenderer
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.fonts.Glyph
import com.pineypiney.game_engine.resources.fonts.TrueTypeLoader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.ShaderStorageBuffer
import com.pineypiney.game_engine.resources.shaders.parameters.InputTopology
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.PIF
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.BufferUtils
import java.io.FileInputStream

@OptIn(ExperimentalUnsignedTypes::class)
@Suppress("UNUSED")
class TTFTest(gameEngine: WindowedGameEngineI<*>, renderer: GameRenderer<MultiTest>) : MultiTest(gameEngine, renderer) {

	val font = FileInputStream("src/main/resources/fonts/LightSlab.ttf").use { stream ->
		TrueTypeLoader().load(stream)
	}!!

	val outlineObjects = mutableListOf<GameObject>()
	val shader = ShaderLoader[ResourceKey("vertex/2D"), ResourceKey("fragment/colour"), RenderShaderParameters(InputTopology.LINE_STRIP)]

	val fillObjects = mutableListOf<GameObject>()
	val curveBuffers = mutableMapOf<Char, ShaderStorageBuffer>()
	val arrays = mutableMapOf<Char, Array<UByteArray>>()
	val bezierShader = ShaderLoader[ResourceKey("vertex/2D"), ResourceKey("fragment/bezier_font"), RenderShaderParameters()]

	init {
		val m = 4f
		var x = -12f
		var y = 4f
		for (char in CharRange('A', 'z')) {
			val glyph = font.getGlyph(char) as? Glyph.Simple ?: continue
//			val buffer = BufferUtils.createByteBuffer(glyph.contourPoints.sumOf { it.size * 8 })

			val contours = glyph.contourPoints
			for (contour in contours) {
				val vertices = FloatArray(contour.size * 40 + 4)

				fun put(p: Int, vec: Vec2) {
					vertices[p] = vec.x
					vertices[p + 1] = vec.y
					vertices[p + 2] = vec.x
					vertices[p + 3] = vec.y
				}
				repeat(contour.size shr 1) { curveIndex ->
					val start = Vec2(contour[curveIndex * 2]) * m
					val mid = Vec2(contour[curveIndex * 2 + 1]) * m
					val end = Vec2(contour[(curveIndex * 2 + 2) % contour.size]) * m
					repeat(16) { seg ->
						val d = seg * .0625f
						val r = 1f - d
						val point = start * r * r +
								mid * 2f * d * r +
								end * d * d
						put(curveIndex * 80 + seg * 4, point)
					}

					val dir = (end - mid).normalize() * m * .015f
					val pl = end + dir.rotate(PIF * .75f)
					val pr = end - dir.rotate(PIF * .25f)
					put(curveIndex * 80 + 64, end)
					put(curveIndex * 80 + 68, pl)
					put(curveIndex * 80 + 72, end)
					put(curveIndex * 80 + 76, pr)
				}
				put(contour.size * 40, contour[0] * m)

				val mesh = gameEngine.resourcesLoader.factory.createArrayMesh(
					"$char Mesh", vertices,
					Mesh.createAttributes(listOf(VertexAttribute.POSITION2D, VertexAttribute.TEX_COORD))
				)

				val obj = GameObject.simpleRenderedGameObject("$char Outline Object", shader, Vec3(x, y, 0f), mesh = mesh, setUniformsFunc = {
					uniforms.setVec4Uniform("colour") { Vec4(1f) }
				})
				outlineObjects.add(obj)
				deletionQueue.push(mesh)
			}

			val pixels = Array(200) { UByteArray(200) { 255u } }
			arrays[char] = pixels

			val ssbo = bezierShader.createSSBO("$char Curve SSBO", glyph.curves.size * 24)
			curveBuffers[char] = ssbo
			val buffer = BufferUtils.createByteBuffer(ssbo.size)
			for (curve in glyph.curves) {
				buffer.putFloat(curve.a.x * m)
				buffer.putFloat(curve.a.y * m)
				buffer.putFloat(curve.b.x * m)
				buffer.putFloat(curve.b.y * m)
				buffer.putFloat(curve.c.x * m)
				buffer.putFloat(curve.c.y * m)
			}
			ssbo.setData(buffer.flip())

			val fillMesh = Mesh.textureQuad(gameEngine.resourcesLoader.factory, "$char Quad Mesh", glyph.min * m, glyph.max * m, glyph.min * m, glyph.max * m)
			val fillObj = GameObject.simpleRenderedGameObject("$char Fill Object", bezierShader, Vec3(x + 12f, y, 0f), Vec3(1f), fillMesh) {
				shader.setSSBO("BezierCurves", ssbo)
			}
			fillObjects.add(fillObj)
			deletionQueue.pushAll(ssbo, fillMesh)

			x += (glyph.size.x + .05f) * m
			if (x > -1f) {
				x = -12f
				y -= m
			}
		}
	}

	val letterATest = object : Test(ObjectCollection(outlineObjects).apply { addObjects(fillObjects) }) {
		override fun onInput(state: InputState, action: Int) {

		}
	}


	override val tests: List<Test> = listOf(letterATest)
}