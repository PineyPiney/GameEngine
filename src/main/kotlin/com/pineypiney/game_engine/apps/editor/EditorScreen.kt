package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.apps.editor.context_menus.ContextMenu
import com.pineypiney.game_engine.apps.editor.context_menus.ContextMenuComponent
import com.pineypiney.game_engine.apps.editor.transformers.Transformer
import com.pineypiney.game_engine.apps.editor.transformers.TransformerSelector
import com.pineypiney.game_engine.apps.editor.transformers.Transformers
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.FPSCounter
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.util.Cursor
import com.pineypiney.game_engine.util.extension_functions.firstNotNullOfOrNull
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import java.io.File

class EditorScreen(override val gameEngine: WindowedGameEngineI<EditorScreen>, val format: SceneFormat) : WindowGameLogic() {

	override val renderer = EditorRenderer(window)

	private val fileBrowser = FileBrowser(MenuItem("File Browser"), this).applied()
	private val objectBrowser = ObjectBrowser(MenuItem("Object Browser"), this).applied()
	val componentBrowser = ComponentBrowser(MenuItem("Component Browser"), this).applied()

	var openFile = File("src/main/resources/MainScene.${format.extension}")
	val sceneObjects = ObjectCollection()

	var editingObject: GameObject? = null
		set(value) {
			componentBrowser.setEditing(value)
			if(value == null){
				transformer?.delete()
				transformer = null
			}
			else if(value != field){
				setTransformer(Transformers.TRANSLATE2D, value)
			}
			field = value
		}
	var transformer: GameObject? = null

	// What the mouse is dragging, can be anything from any of the browsers e.g. Texture, GameObject
	var dragging: DraggedElement? = null
	var draggedField: FieldEditor<*, *>? = null

	private val saveButton = TextButton("Save", Vec2(0.45f, -0.9f), Vec2(0.2f, 0.2f)) { _, _ ->
		save()
	}

	private val fpsText = FPSCounter.createCounterWithText(MenuItem("FPS Text").apply { relative(Vec2(-.7f, 1f), Vec2(1f)) }, 5.0, "FPS: $",
		Text.Params().withAlignment(Text.ALIGN_TOP_LEFT).withFontSize(.05f))

	private val properties = mutableMapOf<String, String>()

	override fun addObjects() {
		add(fileBrowser.parent, objectBrowser.parent, componentBrowser.parent, TransformerSelector(this))
		add(fpsText)
	}

	override fun init() {
		super.init()

		window.setCursor(Cursor(gameEngine, "textures/cursor.png", Vec2i(39, 1)))
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	override fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2) {
		super.onCursorMove(cursorPos, cursorDelta)
		val element = dragging?.element ?: return
		val editor = componentBrowser.parent.getChild("Component Container")?.children?.firstNotNullOfOrNull({ it.getComponent<FieldEditor<*, *>>() }){ it.hover }
		if(editor == null){
			if(draggedField != null) {
				draggedField?.onHoverElement(-1, cursorPos)
				dragging?.isDroppable = false
				draggedField = null
			}
		}
		else if(editor != draggedField){
			draggedField?.onHoverElement(-1, cursorPos)
			dragging?.isDroppable = editor.onHoverElement(element, cursorPos)
			draggedField = editor
		}
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
					if(state.control){
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

	fun clearDragging(cursorPos: Vec2){
		val element = dragging?.element ?: return

		if(draggedField != null){
			if(dragging?.isDroppable == true) draggedField?.onDropElement(element, cursorPos, this)
			draggedField?.onHoverElement(-1, cursorPos)
			dragging?.isDroppable = false
			draggedField = null
		}

		dragging?.parent?.delete()
		dragging = null
	}

	fun setTransformer(t: Transformers, obj: GameObject? = editingObject){
		if(obj == null) return
		transformer?.delete()
		val o = MenuItem("Transforming Object")
		transformer = o
		t.creator(o, this)
		o.getComponent<Transformer>()?.startAt(obj, this)
		o.init()
		add(o)
	}

	fun <C> setContextMenu(context: C, menu: ContextMenu<C>, pos: Vec2){
		gameObjects.findTop("ContextMenu", 1)?.delete()
		val menu = ContextMenuComponent(MenuItem("ContextMenu"), context, menu).applied()
		add(menu.parent.apply { position = Vec3(pos, .1f); init() })
	}

	fun save(file: File = openFile){
		openFile.mkdirs()
		file.createNewFile()

		when(file.extension){
			format.extension -> format.serialise(file.outputStream(), this)
			"pfb" -> file.writeText(GameObjectSerializer.serialise(sceneObjects.map.flatMap { it.value }.firstOrNull() ?: return), Charsets.ISO_8859_1)
		}
	}

	fun loadScene(file: File){
		format.parse(file.inputStream(), this)
		sceneObjects.map.flatMap { it.value }.init()
		objectBrowser.reset()
		editingObject = null
		openFile = file
	}

	fun loadPrefab(file: File){
		val prefab = GameObjectSerializer.parse(file.inputStream())
		sceneObjects.addObject(prefab)
		prefab.init()
		objectBrowser.reset()
		editingObject = null
		openFile = file
	}

	fun setAnimating(o: GameObject) {
		remove(this.editingObject)
		this.editingObject = o
		add(o)
		o.init()

		properties.clear()
	}
}