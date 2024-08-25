package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.apps.ComponentEditor
import com.pineypiney.game_engine.apps.ComponentSelector
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponent
import com.pineypiney.game_engine.objects.components.slider.OutlinedSliderRendererComponent
import com.pineypiney.game_engine.objects.menu_items.CheckBox
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C
import java.io.File

class AnimatorLogic(
	override val gameEngine: WindowedGameEngineI<AnimatorLogic>,
	var o: GameObject?,
	var creator: () -> GameObject
) : WindowGameLogic() {

	override val renderer = object : BufferedGameRenderer<AnimatorLogic>() {

		override val view = I
		override val projection = I
		override val guiProjection = I

		override val window: WindowI = this@AnimatorLogic.window
		override val camera: CameraI = OrthographicCamera(window)

		override fun init() {
			super.init()

			GLFunc.blend = true
			GLFunc.blendFunc = Vec2i(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA)
			GLFunc.clearColour = Vec4(1f)
		}

		override fun render(game: AnimatorLogic, tickDelta: Double) {
			camera.getView(view)
			camera.getProjection(projection)

			clearFrameBuffer()
			GLFunc.viewportO = Vec2i(buffer.width, buffer.height)

			for (o in game.gameObjects.map.flatMap { it.value.flatMap { it.allActiveDescendants() } }) {
				val renderedComponents = o.components.filterIsInstance<RenderedComponent>().filter { it.visible }
				if (renderedComponents.isNotEmpty()) {
					for (c in o.components.filterIsInstance<PreRenderComponent>()) c.preRender(tickDelta)
					for (c in renderedComponents) c.render(this, tickDelta)
				}
			}

			// This draws the buffer onto the screen
			FrameBuffer.unbind()
			GLFunc.viewportO = window.framebufferSize
			clear()
			screenShader.setUp(screenUniforms, this)
			buffer.draw()
			GL11C.glClear(GL11C.GL_DEPTH_BUFFER_BIT)
		}

		override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
			super.updateAspectRatio(window, objects)
			val w = window.aspectRatio
			glm.ortho(-w, w, -1f, 1f, guiProjection)
		}
	}

	val animationSelector: AnimationSelector.AnimationSelectorComponent = AnimationSelector(
		null,
		Vec2(-0.6f, 0.8f),
		Vec2(0.5f, 0.15f),
		this::setAnimation
	).getComponent<AnimationSelector.AnimationSelectorComponent>()!!
	private val componentSelector: ComponentSelector.ComponentSelectorComponent = ComponentSelector(
		null,
		Vec2(0.1f, 0.8f),
		Vec2(0.5f, 0.15f),
		this::setComponent
	).getComponent<ComponentSelector.ComponentSelectorComponent>()!!
	private val animationTimeLine = object : MenuItem("Animation Timeline") {

		init {
			position = Vec3(-0.4f, -0.9f, 0f)
			scale = Vec3(0.8f, 0.2f, 1f)
		}

		override fun addComponents() {
			super.addComponents()
			components.add(AnimationTimeLine(this, this@AnimatorLogic))
			components.add(OutlinedSliderRendererComponent(this))
		}
	}

	private val playButton = object : CheckBox("Play Button") {

		init {
			position = Vec3(-0.65f, -0.9f, 0f)
			scale = Vec3(0.2f, 0.2f, 1f)
		}

		override fun init() {
			super.init()
			getComponent<CheckBoxComponent>()?.ticked = true
		}

		override val action: (Boolean) -> Unit = {
			animationSelector.item?.playing = it
		}
	}
	private val saveButton = TextButton("Save", Vec2(0.45f, -0.9f), Vec2(0.2f, 0.2f)) { _, _ ->
		animationSelector.item?.animation?.save()
		animationSelector.item?.parent?.let { i ->
			val n = i.name
			val s = GameObjectSerializer.serialise(i)
			val f = File("$n.pfb")
			f.createNewFile()
			val bytes = s.toByteArray(Charsets.ISO_8859_1)
			f.writeBytes(bytes)
		}
	}

	private var componentEditor: ComponentEditor? = null

	private val properties = mutableMapOf<String, String>()

	override fun addObjects() {
		add(o)
		add(animationSelector.parent)
		add(componentSelector.parent)
		add(animationTimeLine)
		add(componentEditor)
		add(playButton)
		add(saveButton)
	}

	override fun init() {
		super.init()

		setAnimating(creator())
		updateGUI()
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
		(o?.getComponent<AnimatedComponent>())?.let {
			animationTimeLine.getComponent<AnimationTimeLine>()!!.value = it.animationTime
		}

		componentEditor?.let { ce ->
			var parentPath = ""
			var parent = ce.editingComponent.parent
			while (parent != o) {
				parentPath = "${parent.name}.$parentPath"
				parent = parent.parent ?: break
			}
			val fullId = parentPath + ce.editingComponent.id

			val newProperties = o?.getComponent<AnimatedComponent>()?.animation?.getFrameProperties() ?: return
			for ((k, v) in newProperties.filterKeys { it.startsWith(fullId) }) {
				if (properties[k] != v) {
					ce.updateField(k.substringAfterLast('.'))
					properties[k] = v
				}
			}
		}
	}

	override fun onInput(state: InputState, action: Int): Int {
		if (action == 1 && state.c == 'P') {
			val o = GameObjectSerializer.parse(
				File(
					(animationSelector.item?.parent?.name ?: "snake") + ".pfb"
				).inputStream()
			)
			o.name
		}
		return super.onInput(state, action)
	}

	private fun setAnimation() {
		animationTimeLine.getComponent<AnimationTimeLine>()!!
			.setAnimationLength(animationSelector.item?.animation?.length ?: 1f)
	}

	private fun setComponent(component: ComponentI) {
		componentEditor?.editingComponent = component
	}

	private fun updateGUI() {
		val item = o
		animationSelector.item = o?.getComponent()
		componentSelector.item = o
		setAnimation()

		if (item != null) {
			val component = componentSelector.item?.components?.firstOrNull()
			if (component != null) {
				if (componentEditor == null) {
					componentEditor = ComponentEditor(
						item,
						component,
						Vec2(0.5f, -0.4f),
						Vec2(0.5f, 0.8f),
						this::updateKeyFrameField
					).apply { init() }
					add(componentEditor)
				} else componentEditor?.editingComponent = component
				return
			}
		}

		componentEditor?.delete()
		componentEditor = null
	}

	fun setAnimating(o: GameObject) {
		remove(this.o)
		this.o = o
		add(o)
		o.init()
		updateGUI()

		properties.clear()
		o.getComponent<AnimatedComponent>()?.let { a -> properties.putAll(a.getProperties()) }
	}

	private fun updateKeyFrameField(key: String, value: String) {
		val animation = animationSelector.item?.animation ?: return
		animation.frames[animation.lastFrame]?.properties?.set(key, value)
	}
}