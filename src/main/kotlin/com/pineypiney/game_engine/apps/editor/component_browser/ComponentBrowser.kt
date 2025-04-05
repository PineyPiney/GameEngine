package com.pineypiney.game_engine.apps.editor.component_browser

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.FieldEditor
import com.pineypiney.game_engine.apps.editor.createEditor
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenu
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenuEntry
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.rendering.ChildContainingRenderer
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.meshes.Mesh
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.window.Viewport
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.ceil
import kotlin.random.Random

class ComponentBrowser(parent: GameObject, val screen: EditorScreen): DefaultInteractorComponent(parent), UpdatingAspectRatioComponent {

	val componentContainer = MenuItem("Component Container")
	var adderPos = Vec2(.6f, .5f)

	var height = 1f
	var scroll: Float = 0f

	init {
		parent.components.add(PixelTransformComponent(parent, Vec2i(-screen.settings.componentBrowserWidth, screen.settings.fileBrowserHeight), Vec2i(screen.settings.componentBrowserWidth, 324), Vec2(1f, -1f)))
		parent.components.add(ChildContainingRenderer(parent, Mesh.cornerSquareShape, Vec3(.7f)))
	}

	override fun init() {
		super.init()
		parent.addChild(componentContainer)
	}

	fun addComponent(name: String){
		val obj = screen.editingObject ?: return
		val comp = Components.createComponent(name, obj) ?: return
		obj.components.add(comp)
		comp.init()

		addComponentFields(comp)
		positionComponents()
	}

	fun setEditing(obj: GameObject?){
		componentContainer.deleteAllChildren()

		if(obj != null) {
			val text = ActionTextField<TextFieldComponent>("Object Name Field", Vec2i(10, -30), Vec2i(screen.settings.componentBrowserWidth - 20, 20), Vec2(0f, 1f), obj.name, 16){ f, _, _ ->
				obj.name = f.text
				screen.setEditingName(f.text)
			}
			text.init()
			componentContainer.addChild(text)

			for(c in obj.components) addComponentFields(c)

			positionComponents()
		}
	}

	fun refreshField(fieldID: String){
		val container = componentContainer.getChild(fieldID.substringBefore('.') + " Container") ?: return
		val obj = container.getChild("Field Editor $fieldID") ?: return
		obj.getComponent<FieldEditor<*, *>>()?.update()
	}

	/**
	 *  Create a section in the component browser for [component]
	 */
	fun addComponentFields(component: ComponentI){
		val name = component.id
		val compCont = MenuItem("$name Container")
		val textHeight = screen.settings.textScale
		val spacing = ceil(textHeight * 1.1f).toInt()

		// Displays the name of the component
		val compText = Text.makeMenuText(name, Vec4(0f, 0f, 0f, 1f), textHeight, Text.ALIGN_CENTER_LEFT)
		compText.pixel(Vec2i(0, -textHeight * 2), Vec2i(screen.settings.componentBrowserWidth, textHeight * 2), Vec2(0f, 1f))
		compCont.addChild(compText)

		// Tallies the pixel height of the whole component section
		var pixelHeight = textHeight * 2

		// Add all component fields
		for(f in component.getAllFieldsExt()){
			val fieldID = "${component.id}.${f.id}"
			val editor = createEditor(
				MenuItem("Field Editor $fieldID"),
				f,
				Vec2i(0, -pixelHeight),
				Vec2i(screen.settings.componentBrowserWidth, spacing)
			) { _, ov, v ->
				screen.setFieldValue(fieldID, f, ov, v)
			}?.applied()?.parent ?: continue

			pixelHeight += editor.getComponent<PixelTransformComponent>()!!.pixelScale.y
			compCont.addChild(editor)
		}
		pixelHeight += textHeight / 2
		compCont.pixel(0, -pixelHeight, screen.settings.componentBrowserWidth, pixelHeight, 0f, 1f)
		compCont.components.add(Container(compCont, this, component))

		// Colour the background for each component based on it's name
		val r = Random(name.hashCode())
		val col = Vec3(r.nextFloat(), r.nextFloat(), r.nextFloat())
		compCont.components.add(ColourRendererComponent(compCont, col, ColourRendererComponent.menuShader, Mesh.cornerSquareShape))

		componentContainer.addChild(compCont)
		compCont.init()
	}

	fun positionComponents(){
		var y = 0
		for(c in componentContainer.children){
			val pixel = c.getComponent<PixelTransformComponent>() ?: continue

			y -= 10
			y -= pixel.pixelScale.y
			pixel.pixelPos = Vec2i(pixel.pixelPos.x, y)
		}

		height = -y.toFloat() / parent.getComponent<PixelTransformComponent>()!!.pixelScale.y
		scroll = if(height <= 1) 0f
		else scroll.coerceIn(0f, height - 1f)

		componentContainer.position = Vec3(0f, scroll, 0f)
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		if(action == 1){
			screen.setContextMenu(ComponentBrowserContext(this), componentBrowserContextMenu, cursorPos.position)
			return INTERRUPT
		}
		return super.onSecondary(window, action, mods, cursorPos)
	}

	override fun onScroll(window: WindowI, scrollDelta: Vec2): Int {
		super.onScroll(window, scrollDelta)

		scroll = if(height <= 1) 0f
		else (scroll - scrollDelta.y * .1f).coerceIn(0f, height - 1f)
		positionComponents()

		return INTERRUPT
	}

	override fun updateAspectRatio(view: Viewport) {
		val trans = parent.getComponent<PixelTransformComponent>()
		val newScale = Vec2i(screen.settings.componentBrowserWidth, view.size.y - screen.settings.fileBrowserHeight)
		trans?.pixelScale = newScale

		positionComponents()
	}

	class Container(parent: GameObject, val browser: ComponentBrowser, val component: ComponentI): DefaultInteractorComponent(parent){
		override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
			if(action == 1){
				browser.screen.setContextMenu(ComponentContext(browser, this), componentContextMenu, cursorPos.position)
				return INTERRUPT
			}
			return super.onSecondary(window, action, mods, cursorPos)
		}
	}

	companion object {

		data class ComponentBrowserContext(val browser: ComponentBrowser): ContextMenu.Context(browser.screen.settings, browser.screen.renderer.viewportSize)
		data class ComponentContext(val browser: ComponentBrowser, val container: Container): ContextMenu.Context(browser.screen.settings, browser.screen.renderer.viewportSize)

		val componentBrowserContextMenu = ContextMenu<ComponentBrowserContext>(
			arrayOf(
				ContextMenuEntry("New Component"){
					if(browser.screen.gameObjects.findTop("Component Adder", 1) == null) {
						val obj = MenuItem("Component Adder")
						obj.pixel(Vec2i(0, -240), Vec2i(192, 240), Vec2(browser.adderPos))
						obj.components.addAll(
							ComponentAdder(obj, browser),
							ChildContainingRenderer(obj, Mesh.cornerSquareShape, Vec4.fromHex(0xCFA2B0, 1f))
						)
						obj.init()
						browser.screen.add(obj)
					}
				}
			)
		)

		val componentContextMenu = ContextMenu<ComponentContext>(
			arrayOf(
				ContextMenuEntry("Delete") {
					container.delete()
					if(container.component.parent.components.remove(container.component)){
						browser.componentContainer.removeAndDeleteChild(container.parent)
						browser.positionComponents()
					}
				}
			))
	}
}