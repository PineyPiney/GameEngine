package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.rendering.ModelRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.CheckBox
import com.pineypiney.game_engine.objects.menu_items.slider.BasicActionSlider
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.rendering.DefaultWindowRenderer
import com.pineypiney.game_engine.rendering.cameras.PerspectiveCamera
import com.pineypiney.game_engine.rendering.lighting.DirectionalLight
import com.pineypiney.game_engine.rendering.lighting.PointLight
import com.pineypiney.game_engine.rendering.lighting.SpotLight
import com.pineypiney.game_engine.resources.models.*
import com.pineypiney.game_engine.resources.models.materials.PBRMaterial
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.Debug
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.PIF
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.extension_functions.angle
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.maths.shapes.AxisAlignedCuboid
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.mat2x2.Mat2
import glm_.pow
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xz
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW.*
import java.io.File
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LightingTest(override val gameEngine: WindowedGameEngineI<*>): WindowGameLogic() {

	override val renderer = DefaultWindowRenderer<LightingTest, PerspectiveCamera>(window, PerspectiveCamera(window))
	private val camera get() = renderer.camera
	val vel = Vec3(0f)

	private val pressedKeys = mutableSetOf<Short>()
	private var moveMouse = false

	val zArrow = GameObject.simpleModelledGameObject(ModelLoader[ResourceKey("gltf/arrow")], ModelRendererComponent.pbrShader).apply{ position = Vec3(0f, -4f, 0f)}

	//val model = ModelLoader[ResourceKey("SavedModel")]
	val model get() = Model("icosahedron",  arrayOf(createSphereMeshWithTangents(1f, 3)))
	val firstModel = Model("model", arrayOf(createInvertedCuboid(AxisAlignedCuboid(Vec3(0f), Vec3(1f)))))
	val modelShader = ShaderLoader[ResourceKey("vertex/tangent_model"), ResourceKey("fragment/pbr_lit_tangent_model")]
	private val litModel = GameObject.simpleModelledGameObject(firstModel, modelShader)

	val debugShader = ShaderLoader[ResourceKey("vertex/tangent_model"), ResourceKey("fragment/colour_in"), ResourceKey("geometry/visualise_tangents")]
	val debugModel = GameObject.simpleModelledGameObject(firstModel, debugShader)

	var rotatePoint = true

	val sun = GameObject.simpleLightObject(DirectionalLight(Vec3(.1f, -.9f, .1f)))
	val light = GameObject.simpleLightObject(PointLight())
	val torch = GameObject.simpleLightObject(SpotLight(camera.cameraFront), false)

	val fpsText = Text.makeMenuText("FPS: 0.0", fontSize = .1f, alignment = Text.ALIGN_TOP_RIGHT)

	val material get() = (litModel.getComponent<ModelRendererComponent>()?.model?.meshes[0]?.material as? PBRMaterial)

	val roughnessSlider = BasicActionSlider("Roughness Slider", 0f, 1f, .5f){ material?.roughness = it.value }.apply { relative(Vec2(-.9f, .85f), Vec2(.5f, .13f)) }
	val metallicSlider = BasicActionSlider("Metallic Slider", 0f, 1f, 0f){ material?.metallicness = it.value }.apply { relative(Vec2(-.9f, .7f), Vec2(.5f, .13f)) }

	val specularSlider = BasicActionSlider("Specular Slider", 0f, 1f, .5f){ material?.specular = it.value }.apply { relative(Vec2(-.9f, .55f), Vec2(.5f, .13f)) }
	val specTintSlider = BasicActionSlider("Specular Tint Slider", 0f, 1f, 0f){ material?.specTint = it.value }.apply { relative(Vec2(-.9f, .4f), Vec2(.5f, .13f)) }

	val sheenSlider = BasicActionSlider("Sheen Slider", 0f, 1f, 0f){ material?.sheen = it.value }.apply { relative(Vec2(.4f, .85f), Vec2(.5f, .13f)) }
	val sheenTintSlider = BasicActionSlider("SheenTint Slider", 0f, 1f, .5f){ material?.sheenTint = it.value }.apply { relative(Vec2(.4f, .7f), Vec2(.5f, .13f)) }
	val anisotropicSlider = BasicActionSlider("Anisotropic Slider", 0f, 1f, 0f){ material?.anisotropic = it.value }.apply { relative(Vec2(.4f, .55f), Vec2(.5f, .13f)) }

	val vecIdSlider = BasicActionSlider("Vec Id Slider", 0, 2, 0){ debugModel.getComponent<ModelRendererComponent>()?.uniforms?.setIntUniform("vecID", it::value)}.apply { relative(Vec2(.4f, -.98f), Vec2(.5f, .13f)) }

	val fresnelBox = CheckBox("Fresnel", true).apply { os(Vec2(-1.6f, -.98f), Vec2(.13f)) }

	override fun init() {
		super.init()
		glfwSetInputMode(window.windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
		GLFunc.multiSample = true
		//GLFunc.cullface = true
		//GLFunc.cullFaceMode = GL11C.GL_FRONT

		litModel.getComponent<ShaderRenderedComponent>()?.uniforms?.setBoolUniform("doFresnel"){ fresnelBox.getComponent<CheckBoxComponent>()?.ticked == true }
	}

	override fun addObjects() {
		add(zArrow, litModel, debugModel)
		add(light, torch, sun.apply { position = Vec3(0f, 900f, 0f); scale = Vec3(50f) })
		add(fpsText.apply {
			components.add(FPSCounter(this, 2.0){ getComponent<TextRendererComponent>()?.text?.text = it.toString()})
			components.add(RelativeTransformComponent(this, Vec2(1f), Vec2(1f), Vec2(0f), window.aspectRatio))
		})
		add(roughnessSlider, metallicSlider, specularSlider, specTintSlider, sheenSlider, sheenTintSlider, anisotropicSlider, fresnelBox)
		add(vecIdSlider)
	}

	override fun update(interval: Float, input: Inputs) {
		super.update(interval, input)

		val dirLight = sun.getComponent<LightComponent>()!!.light as DirectionalLight
		dirLight.direction = Vec3(.8 * cos(Timer.time), -.6, .8 * sin(Timer.time))

		if(rotatePoint) {
			val angle = light.position.xz.angle() + interval
			light.position = Vec3(10 * sin(angle), 2f, 10 * cos(angle))
		}
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)

		val speed = 10 * Timer.frameDelta
		val travel = Vec3()

		val forward = camera.cameraUp cross camera.cameraRight
		if(pressedKeys.contains('W'.s)) travel += forward
		if(pressedKeys.contains('S'.s)) travel -= forward
		if(pressedKeys.contains('A'.s)) travel -= camera.cameraRight
		if(pressedKeys.contains('D'.s)) travel += camera.cameraRight
		if(pressedKeys.contains(' '.s)) travel += camera.cameraUp
		if(pressedKeys.contains(GLFW_KEY_LEFT_CONTROL.s)) travel -= camera.cameraUp

		vel += (travel - vel) * Timer.frameDelta * if(travel.length2() < vel.length2()) 4f else 2f

		if(vel != Vec3(0)){
			camera.translate(vel * speed)
			torch.position = camera.cameraPos
		}
	}

	override fun onInput(state: InputState, action: Int): Int {
		if(super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT

		if(action == 1){
			if(state.i == GLFW_KEY_ESCAPE){
				window.shouldClose = true
			}
			else if(state.triggers(InputState('S', 2))){
				GLTFModelSaver(model).saveGLB(File("src/main/resources/models/SavedModel.glb"))
			}
			else when(state.c){
				'P' -> rotatePoint = !rotatePoint
				'R' -> litModel.getComponent<ModelRendererComponent>()?.model = model
				'F' -> toggleFullscreen()
				'M' -> toggleMouse()
				'T' -> torch.getComponent<LightComponent>()?.toggle()
				'L' -> {
					camera.setPos(Vec3(0f, 0f, -5f))
					camera.cameraYaw = -90.0
					camera.cameraPitch = 0.0
					camera.updateCameraVectors()
					torch.position = camera.cameraPos
					(torch.getComponent<LightComponent>()?.light as? SpotLight)?.direction = camera.cameraFront
				}
			}
		}


		if(action == 0) pressedKeys.remove(state.key)
		else pressedKeys.add(state.key)
		return action
	}

	override fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2) {
		super.onCursorMove(cursorPos, cursorDelta)

		if(!moveMouse){
			input.mouse.setCursorAt(Vec2(0))
			camera.cameraYaw += cursorDelta.x * 20
			camera.cameraPitch = (camera.cameraPitch + cursorDelta.y * 20).coerceIn(-89.99, 89.99)
			camera.updateCameraVectors()
			(torch.getComponent<LightComponent>()?.light as? SpotLight)?.direction = camera.cameraFront
		}
	}

	override fun updateAspectRatio(window: WindowI) {
		super.updateAspectRatio(window)
		GLFunc.viewportO = Vec2i(window.width, window.height)
	}

	private fun toggleMouse(){
		moveMouse = !moveMouse
		glfwSetInputMode(window.windowHandle, GLFW_CURSOR, if(moveMouse) GLFW_CURSOR_NORMAL else GLFW_CURSOR_DISABLED)
	}

	fun createSphereMesh(radius: Float, subdivisions: Int): ModelMesh{

		val d = Debug().start()

		val gr = 1.6180339887f
		val m = 1f / sqrt(gr * gr + 1f)
		val ngr = gr * m

		val texDivs = Vec2(1f / 11f, 1f / 3f)
		val pixSize = Vec2(1f/2048f, 1f/1024f)

		class PointData(val normal: Vec3, val tex: Vec2){

			fun calculateTangent(p2: PointData, p3: PointData): Vec3{
				val uv1 = p2.tex - tex
				val uv2 = p3.tex - tex

				val e1 = p2.normal - normal
				val e2 = p3.normal - normal

				val uvm = Mat2(uv1, uv2).inverse()
				val tangent = Vec3(
					uvm[0][0] * e1.x + uvm[1][0] * e2.x,
					uvm[0][0] * e1.y + uvm[1][0] * e2.y,
					uvm[0][0] * e1.z + uvm[1][0] * e2.z)
				return tangent
			}
		}


		val icoPoints = arrayOf<PointData>(
			// Bottom Point
			PointData(Vec3(0f, -ngr, -m), Vec2(379f, 7f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(749f, 10f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(1120f, 13f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(1490f, 16f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(1860f, 19f) * pixSize),

			// Lower Ring
			PointData(Vec3(0f, -ngr, m), Vec2(191f, 344f) * pixSize),
			PointData(Vec3(ngr, -m, 0f), Vec2(texDivs.x * 3f, texDivs.y + .01f)),
			PointData(Vec3(m, 0f, -ngr), Vec2(texDivs.x * 5f, texDivs.y + .01f)),
			PointData(Vec3(-m, 0f, -ngr), Vec2(texDivs.x * 7f, texDivs.y + .01f)),
			PointData(Vec3(-ngr, -m, 0f), Vec2(texDivs.x * 9f, texDivs.y + .01f)),
			PointData(Vec3(0f, -ngr, m), Vec2(2041f, 354f) * pixSize),

			// Upper Ring
			PointData(Vec3(-m, 0f, ngr), Vec2(6f, 677f) * pixSize),
			PointData(Vec3(m, 0f, ngr), Vec2(texDivs.x * 2f, texDivs.y * 2f - .01f)),
			PointData(Vec3(ngr, m, 0f), Vec2(texDivs.x * 4f, texDivs.y * 2f - .01f)),
			PointData(Vec3(0f, ngr, -m), Vec2(texDivs.x * 6f, texDivs.y * 2f - .01f)),
			PointData(Vec3(-ngr, m, 0f), Vec2(texDivs.x * 8f, texDivs.y * 2f - .01f)),
			PointData(Vec3(-m, 0f, ngr), Vec2(1857f, 687f) * pixSize),

			// Top Point
			PointData(Vec3(0f, ngr, m), Vec2(187f, 1013f) * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(557f, 1016f) * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(927f, 1019f) * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(1297f, 2022f) * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(1668f, 1024f) * pixSize),
		)

		val icoIndicies = intArrayOf(
			0, 5, 6,
			1, 6, 7,
			2, 7, 8,
			3, 8, 9,
			4, 9, 10,

			5, 12, 6,
			6, 13, 7,
			7, 14, 8,
			8, 15, 9,
			9, 16, 10,

			5, 11, 12,
			6, 12, 13,
			7, 13, 14,
			8, 14, 15,
			9, 15, 16,

			11, 17, 12,
			12, 18, 13,
			13, 19, 14,
			14, 20, 15,
			15, 21, 16
		)

		val points = mutableListOf<PointData>()
		val indices = mutableListOf<Int>()
		points.addAll(icoPoints)
		for(i in 0..19){
			val p0 = icoPoints[icoIndicies[i * 3]]
			val p1 = icoPoints[icoIndicies[i * 3 + 1]]
			val p2 = icoPoints[icoIndicies[i * 3 + 2]]

			val divisionsPoints = mutableListOf<PointData>()

			val layers = 2.pow(subdivisions)
			for(i in 0..layers){
				val rows = layers - i
				val s0 = (i.toFloat() / layers)
				val s12 = 1f - s0
				for(j in 0..rows){
					val s1 = if(rows == 0) 0f else s12 * j / rows
					val s2 = 1f - (s0 + s1)
					divisionsPoints.add(PointData(
						(p0.normal * s0 + p1.normal * s1 + p2.normal * s2).normalize(),
						p0.tex * s0 + p1.tex * s1 + p2.tex * s2
					))
				}
			}
			//println(divisionsPoints.joinToString("\n"){ "${it.normal.x}\t${it.normal.y}\t${it.normal.z}" })

			val divisionIndices = mutableListOf<Int>()
			for(point in divisionsPoints){
				val i = points.indexOfFirst { (it.normal - point.normal).length2() < .0001f }
				if(i == -1){
					divisionIndices.add(points.size)
					points.add(point)
				}
				else divisionIndices.add(i)
			}

			var layerOffset = 0
			for(i in 1..layers){
				val layerSize = layers + 2 - i
				for(j in 0..layerSize - 2){
					indices.add(divisionIndices[layerOffset + j])
					indices.add(divisionIndices[layerOffset + layerSize + j])
					indices.add(divisionIndices[layerOffset + j + 1])
				}
				for(j in 0..layerSize - 3){
					indices.add(divisionIndices[layerOffset + j + 1])
					indices.add(divisionIndices[layerOffset + layerSize + j])
					indices.add(divisionIndices[layerOffset + layerSize + j + 1])
				}
				layerOffset += layerSize
			}
		}

		d.add()
		d.printDiffs()

		return ModelMesh("icosahedron", points.map { ModelMesh.MeshVertex(it.normal * radius, it.tex, it.normal) }.toTypedArray(), indices.toIntArray(), material = PBRMaterial("icosphere", mapOf("baseColour" to TextureLoader[ResourceKey("ico_texture")])))
	}

	fun createSphereMeshWithTangents(radius: Float, subdivisions: Int): ModelMesh{

		val d = Debug().start()

		val icoIndicies = intArrayOf(
			0, 5, 6,
			1, 6, 7,
			2, 7, 8,
			3, 8, 9,
			4, 9, 10,

			5, 12, 6,
			6, 13, 7,
			7, 14, 8,
			8, 15, 9,
			9, 16, 10,

			5, 11, 12,
			6, 12, 13,
			7, 13, 14,
			8, 14, 15,
			9, 15, 16,

			11, 17, 12,
			12, 18, 13,
			13, 19, 14,
			14, 20, 15,
			15, 21, 16
		)

		val chosenPoints = orientatedIcoPoints

		val points = mutableListOf<Pair<PointData, MutableList<Vec3>>>()
		val indices = mutableListOf<Int>()
		points.addAll(chosenPoints.map { it to mutableListOf() })
		for(i in 0..19){
			val p0 = chosenPoints[icoIndicies[i * 3]]
			val p1 = chosenPoints[icoIndicies[i * 3 + 1]]
			val p2 = chosenPoints[icoIndicies[i * 3 + 2]]

			val divisionsPoints = mutableListOf<PointData>()

			val layers = 2.pow(subdivisions)
			for(i in 0..layers){
				val rows = layers - i
				val s0 = (i.toFloat() / layers)
				val s12 = 1f - s0
				for(j in 0..rows){
					val s1 = if(rows == 0) 0f else s12 * j / rows
					val s2 = 1f - (s0 + s1)
					divisionsPoints.add(PointData(
						(p0.normal * s0 + p1.normal * s1 + p2.normal * s2).normalize(),
						p0.tex * s0 + p1.tex * s1 + p2.tex * s2
					))
				}
			}
			//println(divisionsPoints.joinToString("\n"){ "${it.normal.x}\t${it.normal.y}\t${it.normal.z}" })

			val divisionIndices = mutableListOf<Int>()
			for(point in divisionsPoints){
				val i = points.indexOfFirst { (it.first.normal - point.normal).length2() < 1e-6f && (it.first.tex - point.tex).length2() < .01f }
				if(i == -1){
					divisionIndices.add(points.size)
					points.add(point to mutableListOf())
				}
				else divisionIndices.add(i)
			}

			var layerOffset = 0
			for(i in 1..layers){
				val layerSize = layers + 2 - i
				for(j in 0..layerSize - 2){
					val i0 = divisionIndices[layerOffset + j]
					val i1 = divisionIndices[layerOffset + layerSize + j]
					val i2 = divisionIndices[layerOffset + j + 1]
					indices.addAll(i0, i1, i2)

					val tangent = points[i0].first.calculateTangent(points[i1].first, points[i2].first)
					points[i0].second.add(tangent)
					points[i1].second.add(tangent)
					points[i2].second.add(tangent)
				}
				for(j in 0..layerSize - 3){
					val i0 = divisionIndices[layerOffset + j + 1]
					val i1 = divisionIndices[layerOffset + layerSize + j]
					val i2 = divisionIndices[layerOffset + layerSize + j + 1]
					indices.addAll(i0, i1, i2)

					val tangent = points[i0].first.calculateTangent(points[i1].first, points[i2].first)
					points[i0].second.add(tangent)
					points[i1].second.add(tangent)
					points[i2].second.add(tangent)
				}
				layerOffset += layerSize
			}
		}

		d.add()
		d.printDiffs()

		val floats = points.map {
			ModelTangentMesh.TangentMeshVertex(it.first.normal * radius, it.first.tex, it.first.normal,
				//if(it.second.isEmpty()) Vec3(0f, 0f, 1f) else (it.second.fold(Vec3()) { v, t -> v + t } / it.second.size).normalize().cross(it.first.normal).cross(it.first.normal)
				if(abs(it.first.normal.y) == 1f) Vec3(1f, 0f, 0f) else (it.first.normal.cross(Vec3(0f, 1f, 0f))).normalize()
			)
		}.toTypedArray()
		return ModelTangentMesh("icosahedron", floats, indices.toIntArray(), material = PBRMaterial("icosphere", mapOf(
			//"baseColour" to TextureLoader[ResourceKey("ico_texture")],
			//"normals" to TextureLoader[ResourceKey("cratered_normals")]
		)))
	}

	fun createUVSphereMeshWithTangents(radius: Float, divisions: Int): ModelMesh {

		val numRings = divisions + 1
		val numSections = 2 * numRings
		val sectionDivisions = numSections + 1

		val ringDelta = 1f / numRings

		val bottom = ModelTangentMesh.TangentMeshVertex(Vec3(0f, -radius, 0f), Vec2(.5f, 0f), Vec3(0f, -1f, 0f), Vec3(1f, 0f, 0f))
		val top = ModelTangentMesh.TangentMeshVertex(Vec3(0f, radius, 0f), Vec2(.5f, 1f), Vec3(0f, 1f, 0f), Vec3(1f, 0f, 0f))
		val rings = Array(divisions){ r ->
			val tr = (r + 1) * ringDelta
			val y = -cos(tr * PIF)
			val xz = sqrt(1f - (y*y))
			Array(sectionDivisions){ s ->
				val ts = s * ringDelta
				val x = xz * cos(PIF * ts)
				val z = xz * sin(PIF * ts)
				val normal = Vec3(x, y, z)
				ModelTangentMesh.TangentMeshVertex(normal * radius, Vec2(ts * .5f, tr), normal, normal.cross(Vec3(0f, 1f, 0f)).normalize()) }
		}

		val indices = IntArray((divisions + 1) * numSections * 6)
		val lastIndexOffset = (numSections * 3) + (numSections * divisions * 6)
		val lastVertexOffset = ((divisions - 1) * sectionDivisions) + 1
		for(i in 0..<numSections){
			indices[3*i] = 0
			indices[3*i + 1] = i+1
			indices[3*i + 2] = i+2

			indices[lastIndexOffset + 3*i] = lastVertexOffset + i
			indices[lastIndexOffset + 3*i + 1] = (divisions * sectionDivisions) + 1
			indices[lastIndexOffset + 3*i + 2] = lastVertexOffset + i + 1
		}
		for(j in 0..divisions - 2){
			val ringIndexOffset = (j * numSections * 6) + (numSections * 3)
			val ringVertexOffset = (j * sectionDivisions) + 1
			for(i in 0..<numSections){
				val iOffset = ringIndexOffset + (i * 6)
				val vOffset = ringVertexOffset + i
				indices[iOffset] = vOffset
				indices[iOffset + 1] = vOffset + sectionDivisions
				indices[iOffset + 2] = vOffset + sectionDivisions + 1
				indices[iOffset + 3] = vOffset + sectionDivisions + 1
				indices[iOffset + 4] = vOffset + 1
				indices[iOffset + 5] = vOffset
			}
		}

		val vertices = Array((divisions * sectionDivisions) + 2) { bottom }
		for((i, a) in rings.withIndex()){
			a.copyInto(vertices, (i * sectionDivisions) + 1)
		}
		vertices[vertices.size - 1] = top
		return ModelTangentMesh("UV Sphere", vertices, indices, material = PBRMaterial("uv sphere", mapOf(
			//"baseColour" to TextureLoader[ResourceKey("wood")],
			//"normals" to TextureLoader[ResourceKey("planet_normals")]
		), Vec4(.93f, .66f, .6f, 1f)))
	}

	fun createInvertedCuboid(cuboid: AxisAlignedCuboid): ModelMesh {
		val indices = IntArray(72)

		val rt2 = sqrt(.5f)
		val topVerts = mutableListOf(
			ModelTangentMesh.TangentMeshVertex(Vec3(0f, 1f, 0f), Vec2(0f, 0f), Vec3(0f, rt2, rt2), Vec3(1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(0f, 1f, 0f), Vec2(0f, 0f), Vec3(rt2, rt2, 0f), Vec3(0f, 0f, -1f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(0f, 1f, 0f), Vec2(0f, 0f), Vec3(0f, rt2, -rt2), Vec3(-1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(0f, 1f, 0f), Vec2(0f, 0f), Vec3(-rt2, rt2, 0f), Vec3(0f, 0f, 1f)),

			ModelTangentMesh.TangentMeshVertex(Vec3(-.5f, .5f, .5f), Vec2(0f, 0f), Vec3(0f, rt2, rt2), Vec3(1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(.5f, .5f, .5f), Vec2(0f, 0f), Vec3(0f, rt2, rt2), Vec3(1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(.5f, .5f, .5f), Vec2(0f, 0f), Vec3(rt2, rt2, 0f), Vec3(0f, 0f, -1f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(.5f, .5f, -.5f), Vec2(0f, 0f), Vec3(rt2, rt2, 0f), Vec3(0f, 0f, -1f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(.5f, .5f, -.5f), Vec2(0f, 0f), Vec3(0f, rt2, -rt2), Vec3(-1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(-.5f, .5f, -.5f), Vec2(0f, 0f), Vec3(0f, rt2, -rt2), Vec3(-1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(-.5f, .5f, -.5f), Vec2(0f, 0f), Vec3(-rt2, rt2, 0f), Vec3(0f, 0f, 1f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(-.5f, .5f, .5f), Vec2(0f, 0f), Vec3(-rt2, rt2, 0f), Vec3(0f, 0f, 1f)),
		)
		fun addDirection(dir: Vec2, edge1: Vec2){
			val leftNormal = Vec3(-edge1.y * rt2, 0f, edge1.x * rt2).normalize()
			val rightNormal = Vec3(edge1.x * rt2, 0f, edge1.y * rt2).normalize()
			topVerts.addAll(
				ModelTangentMesh.TangentMeshVertex(Vec3(dir.x, 0f, dir.y), Vec2(0f, 0f), Vec3(dir.x * rt2, rt2, dir.y * rt2), Vec3(dir.y, 0f, -dir.x)),
				ModelTangentMesh.TangentMeshVertex(Vec3(dir.x, 0f, dir.y), Vec2(0f, 0f), rightNormal, -leftNormal),
				ModelTangentMesh.TangentMeshVertex(Vec3(dir.x, 0f, dir.y), Vec2(0f, 0f), Vec3(dir.x * rt2, -rt2, dir.y * rt2), Vec3(dir.y, 0f, -dir.x)),
				ModelTangentMesh.TangentMeshVertex(Vec3(dir.x, 0f, dir.y), Vec2(0f, 0f), leftNormal, rightNormal),

				ModelTangentMesh.TangentMeshVertex(Vec3(edge1.x, .5f, edge1.y), Vec2(0f, 0f), rightNormal, -leftNormal),
				ModelTangentMesh.TangentMeshVertex(Vec3(edge1.x, -.5f, edge1.y), Vec2(0f, 0f), rightNormal, -leftNormal),
			)
		}

		addDirection(Vec2(0, 1), Vec2(.5f, .5f))
		addDirection(Vec2(1, 0), Vec2(.5f, -.5f))
		addDirection(Vec2(0, -1), Vec2(-.5f, -.5f))
		addDirection(Vec2(-1, 0), Vec2(-.5f, .5f))

		topVerts.addAll(
			ModelTangentMesh.TangentMeshVertex(Vec3(-.5f, -.5f, .5f), Vec2(0f, 0f), Vec3(0f, -rt2, rt2), Vec3(1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(.5f, -.5f, .5f), Vec2(0f, 0f), Vec3(0f, -rt2, rt2), Vec3(1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(.5f, -.5f, .5f), Vec2(0f, 0f), Vec3(rt2, -rt2, 0f), Vec3(0f, 0f, -1f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(.5f, -.5f, -.5f), Vec2(0f, 0f), Vec3(rt2, -rt2, 0f), Vec3(0f, 0f, -1f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(.5f, -.5f, -.5f), Vec2(0f, 0f), Vec3(0f, -rt2, -rt2), Vec3(-1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(-.5f, -.5f, -.5f), Vec2(0f, 0f), Vec3(0f, -rt2, -rt2), Vec3(-1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(-.5f, -.5f, -.5f), Vec2(0f, 0f), Vec3(-rt2, -rt2, 0f), Vec3(0f, 0f, 1f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(-.5f, -.5f, .5f), Vec2(0f, 0f), Vec3(-rt2, -rt2, 0f), Vec3(0f, 0f, 1f)),

			ModelTangentMesh.TangentMeshVertex(Vec3(0f, -1f, 0f), Vec2(0f, 0f), Vec3(0f, -rt2, rt2), Vec3(1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(0f, -1f, 0f), Vec2(0f, 0f), Vec3(rt2, -rt2, 0f), Vec3(0f, 0f, -1f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(0f, -1f, 0f), Vec2(0f, 0f), Vec3(0f, -rt2, -rt2), Vec3(-1f, 0f, 0f)),
			ModelTangentMesh.TangentMeshVertex(Vec3(0f, -1f, 0f), Vec2(0f, 0f), Vec3(-rt2, -rt2, 0f), Vec3(0f, 0f, 1f)),
		)

		intArrayOf(0, 4, 5, 1, 6, 7, 2, 8, 9, 3, 10, 11).copyInto(indices)
		for(i in 0..3){
			val o = 12 + (i * 6)
			val ol = 12 + (((i + 3) % 4) * 6)
			val i2 = 2 * i
			val triangles = intArrayOf(
				o, i2 + 4, i2 + 5, // top triangle
				o + 1, o + 4, o + 5, // left triangle
				o + 2, i2 + 36, i2 + 37, // bottom triangle
				o + 3, ol + 4, ol + 5 // right triangle
			)
			triangles.copyInto(indices, 12 * (i + 1))
		}
		intArrayOf(36, 37, 44, 38, 39, 45, 40, 41, 46, 42, 43, 47).copyInto(indices, 60)

		return ModelTangentMesh("Inverted Cuboid", topVerts.toTypedArray(), indices, material = PBRMaterial("invCub Mat", mapOf()))
	}

	companion object {

		class PointData(val normal: Vec3, val tex: Vec2){

			fun calculateTangent(p2: PointData, p3: PointData): Vec3{
				val uv1 = p2.tex - tex
				val uv2 = p3.tex - tex

				val e1 = p2.normal - normal
				val e2 = p3.normal - normal

				val uvm = Mat2(uv1, uv2).inverse()
				val tangent = Vec3(
					uvm[0][0] * e1.x + uvm[1][0] * e2.x,
					uvm[0][0] * e1.y + uvm[1][0] * e2.y,
					uvm[0][0] * e1.z + uvm[1][0] * e2.z)
				return tangent
			}
		}

		val gr = 1.6180339887f
		val m = 1f / sqrt(gr * gr + 1f)
		val ngr = gr * m

		val texDivs = Vec2(1f / 11f, 1f / 3f)
		val pixSize = Vec2(1f/2048f, 1f/1024f)

		val icoPoints = arrayOf<PointData>(
			// Bottom Point
			PointData(Vec3(0f, -ngr, -m), Vec2(379f, 7f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(749f, 10f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(1120f, 13f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(1490f, 16f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(1860f, 19f) * pixSize),

			// Lower Ring
			PointData(Vec3(0f, -ngr, m), Vec2(191f, 344f) * pixSize),
			PointData(Vec3(ngr, -m, 0f), Vec2(texDivs.x * 3f, texDivs.y + .01f)),
			PointData(Vec3(m, 0f, -ngr), Vec2(texDivs.x * 5f, texDivs.y + .01f)),
			PointData(Vec3(-m, 0f, -ngr), Vec2(texDivs.x * 7f, texDivs.y + .01f)),
			PointData(Vec3(-ngr, -m, 0f), Vec2(texDivs.x * 9f, texDivs.y + .01f)),
			PointData(Vec3(0f, -ngr, m), Vec2(2041f, 354f) * pixSize),

			// Upper Ring
			PointData(Vec3(-m, 0f, ngr), Vec2(6f, 677f) * pixSize),
			PointData(Vec3(m, 0f, ngr), Vec2(texDivs.x * 2f, texDivs.y * 2f - .01f)),
			PointData(Vec3(ngr, m, 0f), Vec2(texDivs.x * 4f, texDivs.y * 2f - .01f)),
			PointData(Vec3(0f, ngr, -m), Vec2(texDivs.x * 6f, texDivs.y * 2f - .01f)),
			PointData(Vec3(-ngr, m, 0f), Vec2(texDivs.x * 8f, texDivs.y * 2f - .01f)),
			PointData(Vec3(-m, 0f, ngr), Vec2(1857f, 687f) * pixSize),

			// Top Point
			PointData(Vec3(0f, ngr, m), Vec2(187f, 1013f) * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(557f, 1016f) * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(927f, 1019f) * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(1297f, 1022f) * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(1668f, 1024f) * pixSize),
		)

		val p = (sqrt(5f)+1f)*.5f
		val s = 2f/(sqrt(p*p + 1f))
		val c0 = (2f - s*s)* .5f
		val d = sqrt(1 - c0*c0)

		val c72 = d * cos(.4f * PIF)
		val s72 = d * sin(.4f * PIF)
		val c144 = d * cos(.2 * PIF)
		val s144 = d * sin(.8f * PIF)

		val orientatedIcoPoints = arrayOf<PointData>(
			// Bottom Point
			PointData(Vec3(0f, -1f, 0f), Vec2(379f, 7f) * pixSize),
			PointData(Vec3(0f, -1f, 0f), Vec2(749f, 10f) * pixSize),
			PointData(Vec3(0f, -1f, 0f), Vec2(1120f, 13f) * pixSize),
			PointData(Vec3(0f, -1f, 0f), Vec2(1490f, 16f) * pixSize),
			PointData(Vec3(0f, -1f, 0f), Vec2(1860f, 19f) * pixSize),

			// Lower Ring
			PointData(Vec3(0f, -c0, -d), Vec2(191f, 344f) * pixSize),
			PointData(Vec3(-s72, -c0, -c72), Vec2(texDivs.x * 3f, texDivs.y + .01f)),
			PointData(Vec3(-s144, -c0, c144), Vec2(texDivs.x * 5f, texDivs.y + .01f)),
			PointData(Vec3(s144, -c0, c144), Vec2(texDivs.x * 7f, texDivs.y + .01f)),
			PointData(Vec3(s72, -c0, -c72), Vec2(texDivs.x * 9f, texDivs.y + .01f)),
			PointData(Vec3(0f, -c0, -d), Vec2(2041f, 354f) * pixSize),

			// Upper Ring
			PointData(Vec3(s144, c0, -c144), Vec2(6f, 677f) * pixSize),
			PointData(Vec3(-s144, c0, -c144), Vec2(texDivs.x * 2f, texDivs.y * 2f - .01f)),
			PointData(Vec3(-s72, c0, c72), Vec2(texDivs.x * 4f, texDivs.y * 2f - .01f)),
			PointData(Vec3(0f, c0, d), Vec2(texDivs.x * 6f, texDivs.y * 2f - .01f)),
			PointData(Vec3(s72, c0, c72), Vec2(texDivs.x * 8f, texDivs.y * 2f - .01f)),
			PointData(Vec3(s144, c0, -c144), Vec2(1857f, 687f) * pixSize),

			// Top Point
			PointData(Vec3(0f, 1f, 0f), Vec2(187f, 1013f) * pixSize),
			PointData(Vec3(0f, 1f, 0f), Vec2(557f, 1016f) * pixSize),
			PointData(Vec3(0f, 1f, 0f), Vec2(927f, 1019f) * pixSize),
			PointData(Vec3(0f, 1f, 0f), Vec2(1297f, 1022f) * pixSize),
			PointData(Vec3(0f, 1f, 0f), Vec2(1668f, 1024f) * pixSize),
		)
	}
}