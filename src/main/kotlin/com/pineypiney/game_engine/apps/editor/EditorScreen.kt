package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.util.Cursor
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
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11C
import java.io.File

class EditorScreen(
	override val gameEngine: WindowedGameEngineI<EditorScreen>
) : WindowGameLogic() {

	override val renderer = object : BufferedGameRenderer<EditorScreen>() {

		override val view = I
		override val projection = I
		override val guiProjection = I

		override val window: WindowI = this@EditorScreen.window
		override val camera: CameraI = OrthographicCamera(window)

		override fun init() {
			super.init()

			GLFunc.blend = true
			GLFunc.blendFunc = Vec2i(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA)
			GLFunc.clearColour = Vec4(1f)
		}

		override fun render(game: EditorScreen, tickDelta: Double) {
			camera.getView(view)
			camera.getProjection(projection)

			clearFrameBuffer()
			GLFunc.viewportO = Vec2i(buffer.width, buffer.height)

			for (o in game.gameObjects.map.flatMap { l -> l.value.flatMap { it.catchRenderingComponents() } }) {
				val renderedComponents = o.components.filterIsInstance<RenderedComponentI>().filter { it.visible }
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

	private val fileBrowser = FileBrowser(MenuItem("File Browser"), this).applied()
	private val objectBrowser = ObjectBrowser(MenuItem("Object Browser"), this).applied()
	private val componentBrowser = ComponentBrowser(MenuItem("Component Browser"), this).applied()

	var editingObject: GameObject? = null
		set(value) {
			field = value
			componentBrowser.setEditing(field)
		}

	// What the mouse is dragging, can be anything from any of the browsers e.g. Texture, GameObject
	var dragging: DraggedElement? = null

	private val saveButton = TextButton("Save", Vec2(0.45f, -0.9f), Vec2(0.2f, 0.2f)) { _, _ ->
		save()
	}

	private val properties = mutableMapOf<String, String>()

	override fun addObjects() {
		add(fileBrowser.parent, objectBrowser.parent, componentBrowser.parent)
		add(editingObject)
	}

	override fun init() {
		super.init()

		window.setCursor(Cursor(gameEngine, "textures/cursor.png", Vec2i(39, 1)))
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	override fun onInput(state: InputState, action: Int): Int {
		if (action == 1){
			when(state.c) {
				'P' -> {
					val file = File((editingObject?.name ?: "snake") + ".pfb")
					if(file.exists()) {
						val o = GameObjectSerializer.parse(file.inputStream())
						o.name
					}
				}
				'S' -> {
					if(state.mods.toInt() == GLFW.GLFW_MOD_CONTROL){
						save()
					}
				}
			}
		}
		return super.onInput(state, action)
	}

	fun setDragging(element: Any, addRender: (GameObject) -> Unit): GameObject{
		val o = MenuItem("Dragged Element")
		o.position = Vec3(input.mouse.lastPos, 1f)
		val comp = DraggedElement(o, element).applied()
		addRender(o)
		o.init()
		dragging = comp
		add(o)
		return o
	}

	fun setEditingName(newName: String){
		val text = objectBrowser.selected?.parent?.children?.firstNotNullOfOrNull { it.getComponent<TextRendererComponent>() }
		text?.text?.text = newName
	}

	fun clearDragging(){
		dragging?.parent?.delete()
		dragging = null
	}

	fun save(){
		editingObject?.let { i ->
			val n = i.name
			val s = GameObjectSerializer.serialise(i)
			val f = File("$n.pfb")
			f.createNewFile()
			val bytes = s.toByteArray(Charsets.ISO_8859_1)
			f.writeBytes(bytes)
		}
	}

	fun setAnimating(o: GameObject) {
		remove(this.editingObject)
		this.editingObject = o
		add(o)
		o.init()

		properties.clear()
	}
}