package com.pineypiney.game_engine_test.testVR

import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.extension_functions.rotationComponent
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.vr.VRGameEngine
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

class TestVRGame(override val gameEngine: TestVREngine) : GameLogic() {

	override val renderer = TestVRRenderer(gameEngine.rtWidth, gameEngine.rtHeight, gameEngine.hmd)

	val inputVR get() = gameEngine.inputVR

	override fun addObjects() {
		for(i in 0..999){
			add(GameObject.simpleRenderedGameObject(ShaderLoader[ResourceKey("vertex/3D"), ResourceKey("fragment/plain")], positions[i], Vec3(0.3f), Mesh.centerCubeShape){

			})
		}
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	fun handleVRInput(){
		val g = inputVR.getDigitalActionState(inputVR.leftGrip)
		val t = inputVR.getAnalogActionState(inputVR.leftTrigger)
		val j = inputVR.getAnalogActionState(inputVR.leftJoystick)
		VRGameEngine.logger.info("Grip is $g")
		VRGameEngine.logger.info("Trigger is $t")

		val xB = inputVR.getDigitalActionState(inputVR.leftX)
		if(xB) {
			gameEngine.hmd.shouldRun = false
		}

		if(j != Vec3(0f)){
			val jMat = I.translate(j.run { Vec3(x, 0f, y) })
			val hmdr = hmdRotation()
			val hmdMat = hmdr * jMat
			val vec = hmdMat.getTranslation().run { Vec3(x, y, -z) }
			renderer.viewOffset += vec * Timer.frameDelta
		}

	}

	fun hmdRotation(): Mat4 = gameEngine.hmd.hmdPose.rotationComponent()

	companion object{
		val positions = Array(1000){ i ->
			Vec3((i % 10) - 4.5f, ((i / 10) % 10) - 4.5f, ((i / 100) % 10) - 4.5f)
		}
	}
}