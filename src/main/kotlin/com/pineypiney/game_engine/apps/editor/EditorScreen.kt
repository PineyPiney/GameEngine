package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.apps.editor.component_browser.ComponentBrowser
import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser
import com.pineypiney.game_engine.apps.editor.file_browser.files.SavableFiles
import com.pineypiney.game_engine.apps.editor.object_browser.ObjectBrowser
import com.pineypiney.game_engine.apps.editor.util.Draggable
import com.pineypiney.game_engine.apps.editor.util.DraggedElement
import com.pineypiney.game_engine.apps.editor.util.EditorPositioningComponent
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
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.fields.ComponentField
import com.pineypiney.game_engine.objects.components.fields.Vec3Field
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.Colour
import com.pineypiney.game_engine.util.Cursor
import com.pineypiney.game_engine.util.extension_functions.firstNotNullOfOrNull
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.util.extension_functions.isBetween
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.Viewport
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.int
import glm_.pow
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class EditorScreen(override val gameEngine: WindowedGameEngineI<EditorScreen>) : WindowGameLogic() {

	val settings = EditorSettings()
	var sceneSize = window.size - Vec2i(settings.objectBrowserWidth + settings.componentBrowserWidth, settings.fileBrowserHeight)

	override val renderer = EditorRenderer(window, settings)

	private val fileBrowser = FileBrowser(MenuItem("File Browser"), this).applied()
	val objectBrowser = ObjectBrowser(MenuItem("Object Browser"), this).applied()
	val componentBrowser = ComponentBrowser(MenuItem("Component Browser"), this).applied()

	var openFile = File("src/main/resources/MainScene.scn")
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

	private val fpsText = FPSCounter.createCounterWithText(MenuItem("FPS Text").apply { pixel(Vec2i(293, -25), Vec2i(180, 20), Vec2(-1f, 1f)) }, 5.0, "FPS: $",
		Text.Params().withFontSize(16).withAlignment(Text.ALIGN_TOP_LEFT))

	private val properties = mutableMapOf<String, String>()

	override fun addObjects() {
		add(fileBrowser.parent, objectBrowser.parent, componentBrowser.parent, TransformerSelector(this))
		add(fpsText)
	}

	override fun init() {
		super.init()

		val cursor = Cursor.create(gameEngine, "textures/cursor.png", Vec2i(39, 1)) ?: return
		window.setCursor(cursor)
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	override fun onCursorMove(cursorPos: CursorPosition, cursorDelta: CursorPosition) {
		super.onCursorMove(cursorPos, cursorDelta)

		val scenePos = getSceneCursorPosition(cursorPos)
		val ray = renderer.camera.getRay(scenePos.screenSpace)

		// ScenePos.pixels is (-1, -1) if the cursor is outside the scene screen
		if(scenePos.pixels.x >= 0) {
			val sceneDelta = getSceneCursorDelta(cursorDelta)
			val transformerComponent = transformer?.getComponent<InteractorComponent>()
			if(transformerComponent != null) checkHovers(setOf(transformerComponent), ray, scenePos, sceneDelta)
		}

		dragging?.let { dragging ->

			dragging.element.position(dragging.parent, cursorPos.position, scenePos)

			if(dragging.hoveringScene){
				// Left scene, now hovering over menu
				if(scenePos.pixels.x == -1) {
					dragging.hoveringScene = false
					val sceneRenderer = dragging.parent.getChild("Scene Renderer")
					if(sceneRenderer != null) {
						sceneObjects.removeObject(dragging.parent)
						add(dragging.parent)
						sceneRenderer.active = false
						dragging.parent.getChild("Menu Renderer")?.active = true
					}
				}
			}
			// Started hovering over the scene
			else if(scenePos.pixels.x >= 0){
				dragging.hoveringScene = true
				val sceneRenderer = dragging.parent.getChild("Scene Renderer")
				if(sceneRenderer != null){
					sceneObjects.addObject(dragging.parent)
					remove(dragging.parent)
					dragging.parent.getChild("Menu Renderer")?.active = false
					sceneRenderer.active = true
				}
			}

			// Get the field editor currently being hovered over
			val editor = componentBrowser.parent.getChild("Component Container")?.children?.firstNotNullOfOrNull{ cont -> cont.children.firstNotNullOfOrNull({ it.getComponent<FieldEditor<*, *>>() }){ it.hover } }

			// If no field editor is being hovered over then make sure to update and old one
			if(editor == null){
				if(draggedField != null) {
					draggedField?.onHoverElement(-1, cursorPos.position)
					dragging.isDroppable = false
					draggedField = null
				}
			}
			// Else if a new field editor has been hovered over then update the previous and new ones
			else {
				dragging.isDroppable = editor.onHoverElement(dragging.element.getElement(), cursorPos.position)
				if(editor != draggedField){
					draggedField?.onHoverElement(-1, cursorPos.position)
					draggedField = editor
				}
			}
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
		if(super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT
		transformer?.getComponent<InteractorComponent>()?.let {
			val sceneCursor = getSceneCursorPosition(input.mouse.lastPos)
			if(componentInput(it, state, action, sceneCursor, false)) return InteractorComponent.INTERRUPT
		}
		return action
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte) {
		if(action != 1) return
		// Select one of the objects in the scene
		val allObjects = sceneObjects.map.flatMap { it.value.flatMap { it.catchRenderingComponents() } }
		var closest: GameObject? = null
		var minDist: Float = Float.MAX_VALUE
		val sceneCursor = getSceneCursorPosition(input.mouse.lastPos)
		if(sceneCursor.pixels.x == -1) return
		val ray = renderer.camera.getRay(sceneCursor.screenSpace)
		for(obj in allObjects){
			val renderer = obj.renderer ?: continue
			val intersections = (renderer.getMeshShape() transformedBy obj.worldModel).intersectedBy(ray)
			val dist = -(intersections.firstOrNull()?.z ?: continue)
			if(dist < minDist){
				minDist = dist
				closest = obj
			}
		}
		closest?.let {
			editingObject = it
			objectBrowser.setSelectedObject(it)
		}
	}

	override fun onScroll(scrollDelta: Vec2): Int {
		if(super.onScroll(scrollDelta) == InteractorComponent.INTERRUPT) return -1
		val mousePos = getSceneCursorPosition(input.mouse.lastPos).screenSpace
		val initialMousePos = Vec2(renderer.camera.screenToWorld(mousePos))
		renderer.camera.height *= .9f.pow(scrollDelta.y)
		val newMousePos = Vec2(renderer.camera.screenToWorld(mousePos))
		renderer.camera.translate(initialMousePos - newMousePos)
		repositionTransformer()
		return 1
	}

	// DRAGGING ELEMENT

	fun setDragging(element: Draggable, cursor: CursorPosition): GameObject{
		val o = MenuItem("Dragged Element")
		o.position = Vec3(input.mouse.lastPos.position, .5f)
		val comp = DraggedElement(o, element).applied()
		element.addRenderer(o, cursor)
		o.getChild("Scene Renderer")?.active = false
		o.init()
		add(o)

		dragging = comp
		return o
	}

	fun clearDragging(cursorPos: Vec2){
		val element = dragging?.element?.getElement() ?: return

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
		o.pixel(Vec2i(0, 0), Vec2i(64), Vec2(0f))
		transformer = o
		t.creator(o, this)
		o.getComponent<Transformer>()?.startAt(obj, this)
		o.init()
	}

	fun repositionTransformer(){
		editingObject?.let{ transformer?.getComponent<Transformer>()?.startAt(it, this) }
	}

	fun setEditingName(newName: String){
		val text = objectBrowser.selected?.parent?.children?.firstNotNullOfOrNull { it.getComponent<TextRendererComponent>() }
		text?.setTextContent(newName)
	}

	@Suppress("UNCHECKED_CAST")
	fun <T, F: ComponentField<T>> setFieldValue(component: ComponentI, fieldID: String, field: F, oldValue: T, value: T){
		var newValue = value
		when(fieldID){
			"TransformComponent.position" -> {
				editingObject?.getComponent<EditorPositioningComponent>()?.let{ pc ->
					(field as? Vec3Field)?.let {
						if(oldValue != null && value != null) {
							val p = pc.place(oldValue as Vec3, value as Vec3)
							field.setter(p)
							newValue = p as T
						}
					}
				}
			}
		}

		repositionTransformer()
		editManager.addEdit(ComponentFieldEdit(editingObject ?: return, this, fieldID, field.serialise(component, oldValue), field.serialise(component, newValue)))
	}

	fun <C: ContextMenu.Context> setContextMenu(context: C, menu: ContextMenu<C>, pos: Vec2){
		gameObjects.findTop("ContextMenu", 1)?.delete()
		val menu = ContextMenuComponent(MenuItem("ContextMenu"), context, menu).applied()
		add(menu.parent.apply { position = Vec3(pos, .1f); init() })
	}

	override fun updateAspectRatio() {
		super.updateAspectRatio()
		transformer?.forAllActiveDescendants {
			getComponent<UpdatingAspectRatioComponent>()?.updateAspectRatio(getSceneBox())
		}
		repositionTransformer()
	}

	fun save(file: File = openFile){
		file.mkdirs()
		file.createNewFile()

		if(file.exists()) {
			val fileType = SavableFiles.list.firstOrNull { it.ext == file.extension } ?: return
			fileType.save(file, this)
		}
	}

	fun loadedFile(file: File){
		sceneObjects.map.flatMap { it.value }.init()
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

	fun getSceneBox(): Viewport {
		return Viewport(settings.objectBrowserWidth, settings.fileBrowserHeight, window.width - settings.componentBrowserWidth, window.height)
	}

	fun getSceneCursorPosition(pos: CursorPosition): CursorPosition{
		val (bl, tr, sizeI) = getSceneBox()
		val size = Vec2(sizeI)
		if(!pos.pixels.isBetween(bl, tr)) return CursorPosition(Vec2(0f), Vec2(0f), Vec2i(-1))
		val pixels = pos.pixels - bl
		val aspect = size.x / size.y

		val sceneRelative = Vec2(pixels) / (size * .5f) - Vec2(1f)
		val scenePos = CursorPosition(Vec2(renderer.camera.height * .5f * sceneRelative.x * aspect, renderer.camera.height * .5f * sceneRelative.y), sceneRelative, pixels)
		return scenePos
	}

	fun getSceneCursorDelta(delta: CursorPosition): CursorPosition{
		val size = Vec2(getSceneBox().size)
		val ratio = size / window.size
		return CursorPosition(delta.position * ratio, delta.screenSpace * ratio, delta.pixels)
	}

	override fun cleanUp() {
		super.cleanUp()
		sceneObjects.delete()
		transformer?.delete()
	}

	companion object {
		init {
			SavableFiles.add("Prefab", "pfb", ByteArray(4), { file, screen ->
				screen.sceneObjects.map.flatMap { it.value }.firstOrNull()?.let{
					file.writeText(GameObjectSerializer.serialise(it), Charsets.ISO_8859_1)
				}
			}){ file, screen ->
				screen.renderer.backgroundColour = Colour(0xFF4f4f4fu)
				screen.sceneObjects.addObject(GameObjectSerializer.parse(file.inputStream()))
			}

			SavableFiles.add("Scene", "scn", ByteArray(4), { file, screen ->
				defaultSceneSave(file.outputStream(), screen)
			}){ file, screen ->
				screen.renderer.backgroundColour = Colour(0xFF4f4f4fu)
				defaultSceneParse(file.inputStream(), screen)
			}
		}

		fun defaultSceneSave(stream: OutputStream, scene: EditorScreen){
			val objects = scene.sceneObjects.map.flatMap { it.value }
			stream.write(ByteData.int2Bytes(objects.size))
			for(o in objects){
				val s = GameObjectSerializer.serialise(o)
				val f = ByteData.int2String(s.length) + s
				val a = f.toByteArray(Charsets.ISO_8859_1)
				stream.write(a)
			}
		}

		fun defaultSceneParse(stream: InputStream, scene: EditorScreen){
			val numObjects = stream.int()
			val list = mutableListOf<Triple<ComponentI, ComponentField<*>, String>>()
			repeat(numObjects) {
				try {
					val objSize = stream.int()
					val objData = stream.readNBytes(objSize)
					val o = GameObjectSerializer.parse(ByteArrayInputStream(objData), null, list, true)
					scene.sceneObjects.addObject(o)
				} catch (_: Exception) {
				}
			}
			for((comp, field, str) in list) field.set(str, comp)
		}

		fun getSceneBox(settings: EditorSettings, window: WindowI): Viewport {
			return Viewport(settings.objectBrowserWidth, settings.fileBrowserHeight, window.width - settings.componentBrowserWidth, window.height)
		}
	}
}