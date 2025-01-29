package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.apps.editor.component_browser.ComponentBrowser
import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser
import com.pineypiney.game_engine.apps.editor.object_browser.ObjectBrowser
import com.pineypiney.game_engine.apps.editor.util.DraggedElement
import com.pineypiney.game_engine.apps.editor.util.EditorSettings
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenu
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenuComponent
import com.pineypiney.game_engine.apps.editor.util.edits.ComponentFieldEdit
import com.pineypiney.game_engine.apps.editor.util.edits.EditManager
import com.pineypiney.game_engine.apps.editor.util.transformers.Transformer
import com.pineypiney.game_engine.apps.editor.util.transformers.TransformerSelector
import com.pineypiney.game_engine.apps.editor.util.transformers.Transformers
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
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import java.io.File

class EditorScreen(override val gameEngine: WindowedGameEngineI<EditorScreen>, val format: SceneFormat) : WindowGameLogic() {

	override val renderer = EditorRenderer(window)

	val settings = EditorSettings()

	private val fileBrowser = FileBrowser(MenuItem("File Browser"), this).applied()
	val objectBrowser = ObjectBrowser(MenuItem("Object Browser"), this).applied()
	val componentBrowser = ComponentBrowser(MenuItem("Component Browser"), this).applied()

	var openFile = File("src/main/resources/MainScene.${format.extension}")
	val sceneObjects = ObjectCollection()
	val editManager = EditManager()

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

	private val fpsText = FPSCounter.createCounterWithText(MenuItem("FPS Text").apply { pixel(Vec2i(288, 0), Vec2i(64, 20), Vec2(-1f, 1f)) }, 5.0, "FPS: $",
		Text.Params().withAlignment(Text.ALIGN_TOP_LEFT))

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
		val editor = componentBrowser.parent.getChild("Component Container")?.children?.firstNotNullOfOrNull{ cont -> cont.children.firstNotNullOfOrNull({ it.getComponent<FieldEditor<*, *>>() }){ it.hover } }
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
				'Z' -> {
					if(state.control){
						if(state.shift) editManager.redo()
						else editManager.undo()

						repositionTransformer()
					}
				}
			}
		}
		return super.onInput(state, action)
	}

	// DRAGGING ELEMENT

	fun setDragging(element: Any, addRender: (GameObject) -> Unit, position: (GameObject, Vec2) -> Unit): GameObject{
		val o = MenuItem("Dragged Element")
		o.position = Vec3(input.mouse.lastPos, 1f)
		val comp = DraggedElement(o, element, position).applied()
		addRender(o)
		o.init()
		dragging = comp
		add(o)
		return o
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

	// TRANSFORMER

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

	fun repositionTransformer(){
		editingObject?.let{ transformer?.getComponent<Transformer>()?.startAt(it, this) }
	}

	fun setEditingName(newName: String){
		val text = objectBrowser.selected?.parent?.children?.firstNotNullOfOrNull { it.getComponent<TextRendererComponent>() }
		text?.text?.text = newName
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

	fun setEditingWorldPos(newPos: Vec3){
		editingObject?.let {
			val oldPos = it.position
			it.transformComponent.worldPosition = newPos
			editManager.addEdit(ComponentFieldEdit.moveEdit(it, this, it.position, oldPos))
		}
	}

	fun setEditingWorldRot(newRot: Quat){
		editingObject?.let {
			val oldRot = it.rotation
			it.transformComponent.worldRotation = newRot
			editManager.addEdit(ComponentFieldEdit.rotateEdit(it, this, it.rotation, oldRot))
		}
	}

	fun setEditingScale(newScale: Vec3){
		editingObject?.let {
			editManager.addEdit(ComponentFieldEdit.scaleEdit(it, this, newScale, it.transformComponent.scale))
			it.transformComponent.scale = newScale
		}
	}

	fun setEditingWorldScale(newScale: Vec3){
		editingObject?.let {
			val oldScale = it.scale
			it.transformComponent.worldScale = newScale
			editManager.addEdit(ComponentFieldEdit.scaleEdit(it, this, it.scale, oldScale))
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