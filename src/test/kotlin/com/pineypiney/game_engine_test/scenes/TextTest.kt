package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.meshes.Mesh
import com.pineypiney.game_engine.rendering.DefaultWindowRenderer
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import kotlin.math.pow

class TextTest(override val gameEngine: WindowedGameEngineI<*>): WindowGameLogic() {

	override val renderer = DefaultWindowRenderer<TextTest, OrthographicCamera>(window, OrthographicCamera(window))

	val ttfTextObject = Text.makeMenuText("TTF Text\nTest", Vec4(0f, 0f, 0f, 1f), 40).apply {
		components.add(ColourRendererComponent(this, Vec4(.6f), ColourRendererComponent.menuShader, Mesh.cornerSquareShape))
	}
	val ttfText by lazy{ ttfTextObject.getComponent<TextRendererComponent>()!! }

	val bitmapTextObject = Text.makeMenuText("Bitmap Text\nTest", Vec4(0f, 0f, 0f, 1f), 40, font = FontLoader[ResourceKey("Large Font")]).apply {
		components.add(ColourRendererComponent(this, Vec4(.6f), ColourRendererComponent.menuShader, Mesh.cornerSquareShape))
	}
	val bitmapText by lazy{ bitmapTextObject.getComponent<TextRendererComponent>()!! }

	val alignmentPanel = MenuItem("Alignment Panel").apply { pixel(Vec2i(-220, -145), Vec2i(210, 135), Vec2(1f)) }

	override fun addObjects() {
		ttfTextObject.position = Vec3(-1.7f, -.5f, 0f)
		ttfTextObject.scale = Vec3(1.6f, 1f, 1f)
		bitmapTextObject.position = Vec3(.1f, -.5f, 0f)
		bitmapTextObject.scale = Vec3(1.6f, 1f, 1f)

		for(i in 0..2){
			val nameI = when(i){ 0 -> 'L';1 -> 'C';else -> 'R'}
			for(j in 0..2){
				val nameJ = when(j){ 0 -> 'B';1 -> 'C';else -> 'T'}
				val button = TextButton(charArrayOf(nameJ, nameI).concatToString(), Vec2(i * .333f, j * .333f), Vec2(.333f)){
					val newAlignment = 2.0.pow(i.toDouble()).toInt() + (2.0.pow(j.toDouble()).toInt() shl 4)
					ttfText.setAlignment(newAlignment)
					bitmapText.setAlignment(newAlignment)
				}
				alignmentPanel.addChild(button)
			}
		}
		add(ttfTextObject, bitmapTextObject, alignmentPanel)
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	override fun onInput(state: InputState, action: Int): Int {
		if(super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT

		if(action == 1){
			if(state.i == GLFW_KEY_ESCAPE){
				window.shouldClose = true
			}
			else when(state.c){
				'F' -> toggleFullscreen()
			}
		}
		return action
	}

	override fun updateAspectRatio() {
		super.updateAspectRatio()
		GLFunc.viewportO = Vec2i(window.width, window.height)
	}
}