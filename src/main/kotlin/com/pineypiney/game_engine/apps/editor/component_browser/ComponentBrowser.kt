package com.pineypiney.game_engine.apps.editor.component_browser

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.FieldEditor
import com.pineypiney.game_engine.apps.editor.createEditor
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenu
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenuEntry
import com.pineypiney.game_engine.apps.editor.util.edits.ComponentFieldEdit
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.rendering.ChildContainingRenderer
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.extension_functions.sumOf
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.random.Random

class ComponentBrowser(parent: GameObject, val screen: EditorScreen): DefaultInteractorComponent(parent), UpdatingAspectRatioComponent {

	val componentContainer = MenuItem("Component Container")
	var adderPos = Vec3(.6f, .5f, 0f)

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
			val text = ActionTextField<TextFieldComponent>("Object Name Field", Vec2(.05f, .965f), Vec2(.9f, .03f), obj.name){ f, _, _ ->
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

	fun addComponentFields(component: ComponentI){
		val name = component.id
		val compCont = MenuItem("$name Container")

		val compText = Text.makeMenuText(name, alignment = Text.ALIGN_BOTTOM_LEFT)
		compText.position = Vec3(.01f, 0f, 0f)
		compText.scale = Vec3(1f, .025f, 1f)
		compCont.addChild(compText)

		// Add all component fields
		for(f in component.getAllFieldsExt()){
			val fieldID = "${component.id}.${f.id}"
			val editor = createEditor(
				MenuItem("Field Editor $fieldID"),
				f,
				Vec2(.01f, 0f),
				Vec2(.98f, .02f)
			) { _, ov, v ->
				f.set(v)
				screen.repositionTransformer()
				screen.editManager.addEdit(ComponentFieldEdit(component.parent, screen, fieldID, ov, v))
			}?.applied()?.parent ?: continue
			compCont.addChild(editor)
		}

		val totalHeight = (compCont.children.sumOf { it.scale.y } * 1.1f) + .01f
		compCont.scale = Vec3(1f, totalHeight, 1f)
		compCont.components.add(Container(compCont, this, component, totalHeight))

		var y = 1f - (.005f / totalHeight)
		for(c in compCont.children){
			c.scale = Vec3(c.scale.x, c.scale.y / totalHeight, c.scale.z)
			y -= c.scale.y * 1.1f

			//if(c.name == "$name Text Object") continue
			c.position = Vec3(c.position.x, y, 0f)
		}
		//compText.position = Vec3(.05f, 1f - (compText.scale.y * .5f), .01f)

		val r = Random(name.hashCode())
		val col = Vec3(r.nextFloat(), r.nextFloat(), r.nextFloat())
		compCont.components.add(ColourRendererComponent(compCont, col, ColourRendererComponent.menuShader, Mesh.cornerSquareShape))

		componentContainer.addChild(compCont)
		compCont.init()
	}

	fun positionComponents(yScale: Float = 840f / (screen.window.size.y - screen.settings.fileBrowserHeight)){
		val initialY = 1f + scroll
		var y = initialY

		for(c in componentContainer.children){

			val cont = c.getComponent<Container>()
			if(cont != null) {
				c.scale = Vec3(c.scale.x, cont.size * yScale, c.scale.z)
			}
			else c.scale = Vec3(c.scale.x, yScale * .03f, 1f)

			y -= (c.scale.y + (yScale * .01f))
			c.position = Vec3(c.position.x, y, 0f)
		}

		height = initialY - y
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		if(action == 1){
			screen.setContextMenu(ComponentBrowserContext(this), componentBrowserContextMenu, cursorPos)
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

	override fun updateAspectRatio(renderer: RendererI) {
		val trans = parent.getComponent<PixelTransformComponent>()
		val newScale = Vec2i(screen.settings.componentBrowserWidth, renderer.viewportSize.y - screen.settings.fileBrowserHeight)
		trans?.pixelScale = newScale

		val yScale = 840f / newScale.y
		height = componentContainer.children.sumOf { (it.getComponent<Container>()?.size ?: .03f) + .01f } * 840f / newScale.y
		scroll = if(height <= 1) 0f
		else scroll.coerceIn(0f, height - 1f)
		positionComponents(yScale)
	}

	class Container(parent: GameObject, val browser: ComponentBrowser, val component: ComponentI, val size: Float): DefaultInteractorComponent(parent){
		override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
			if(action == 1){
				browser.screen.setContextMenu(ComponentContext(browser, this), componentContextMenu, cursorPos)
				return INTERRUPT
			}
			return super.onSecondary(window, action, mods, cursorPos)
		}
	}

	companion object {

		data class ComponentBrowserContext(val browser: ComponentBrowser)
		data class ComponentContext(val browser: ComponentBrowser, val container: Container)

		val componentBrowserContextMenu = ContextMenu<ComponentBrowserContext>(
			arrayOf(
				ContextMenuEntry("New Component"){
					if(browser.screen.gameObjects.findTop("Component Adder", 1) == null) {
						val componentAdder = ComponentAdder(MenuItem("Component Adder"), browser).applied().parent
						componentAdder.components.add(ChildContainingRenderer(componentAdder, Mesh.cornerSquareShape, Vec4.fromHex(0xCFA2B0, 1f)))
						componentAdder.position = browser.adderPos
						componentAdder.scale = Vec3(.4f, .5f, 1f)
						componentAdder.init()
						browser.screen.add(componentAdder)
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