package com.pineypiney.game_engine.apps.editor.component_browser

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.FieldEditor
import com.pineypiney.game_engine.apps.editor.createEditor
import com.pineypiney.game_engine.apps.editor.util.edits.ComponentFieldEdit
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.util.extension_functions.sumOf
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import kotlin.random.Random

class ComponentBrowser(parent: GameObject, val screen: EditorScreen): DefaultInteractorComponent(parent), UpdatingAspectRatioComponent {

	val componentContainer = MenuItem("Component Container")
	var adderPos = Vec3(.6f, .5f, 0f)

	init {
		parent.components.add(PixelTransformComponent(parent, Vec2i(-screen.settings.componentBrowserWidth, screen.settings.fileBrowserHeight), Vec2i(screen.settings.componentBrowserWidth, 324), Vec2(1f, -1f)))
		parent.components.add(ColourRendererComponent(parent, Vec3(.7f), ColourRendererComponent.menuShader, Mesh.cornerSquareShape))
	}

	override fun init() {
		super.init()
		componentContainer.position = Vec3(0f, 0f, .01f)
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
		val name = component::class.simpleName ?: "Anon"
		val compCont = MenuItem("$name Container")

		val compText = Text.makeMenuText(name)
		compText.scale = Vec3(1f, .025f, 1f)
		compCont.addChild(compText)

		// Add all component fields
		for(f in component.getAllNewFieldsExt()){
			val fieldID = "${component.id}.${f.id}"
			val editor = createEditor(
				MenuItem("Field Editor $fieldID"),
				f,
				Vec2(.025f, 0f),
				Vec2(.95f, .02f)
			) { _, ov, v ->
				f.set(v)
				screen.repositionTransformer()
				screen.editManager.addEdit(ComponentFieldEdit(component.parent, screen, fieldID, ov, v))
			}?.applied()?.parent ?: continue
			compCont.addChild(editor)
		}

		val totalHeight = compCont.children.sumOf { it.scale.y } * 1.1f
		compCont.scale = Vec3(1f, totalHeight, 1f)
		compCont.components.add(Container(compCont, totalHeight))

		var y = 1f
		for(c in compCont.children){
			c.scale = Vec3(c.scale.x, c.scale.y / totalHeight, c.scale.z)
			y -= c.scale.y * 1.1f

			if(c.name == "$name Text Object") continue
			c.position = Vec3(c.position.x, y, c.position.z)
		}
		compText.position = Vec3(.05f, 1f - (compText.scale.y * .5f), .01f)

		val r = Random(name.hashCode())
		val col = Vec3(r.nextFloat(), r.nextFloat(), r.nextFloat())
		compCont.components.add(ColourRendererComponent(compCont, col, ColourRendererComponent.menuShader, Mesh.cornerSquareShape))

		componentContainer.addChild(compCont)
		compCont.init()
	}

	fun positionComponents(initialY: Float = 1f, yScale: Float = 840f / (screen.window.size.y - screen.settings.fileBrowserHeight)){
		var y = initialY

		for(c in componentContainer.children){

			val cont = c.getComponent<Container>()
			if(cont != null) {
				c.scale = Vec3(c.scale.x, cont.size * yScale, c.scale.z)
			}
			else c.scale = Vec3(c.scale.x, yScale * .03f, 1f)

			y -= (c.scale.y + (yScale * .01f))
			c.position = Vec3(c.position.x, y, .01f)
		}
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		if(action == 1 && screen.gameObjects.findTop("Component Adder", 1) == null) {
			val componentAdder = ComponentAdder(MenuItem("Component Adder"), this).applied().parent
			componentAdder.position = adderPos
			componentAdder.scale = Vec3(.4f, .5f, 1f)
			componentAdder.init()
			screen.add(componentAdder)
			return INTERRUPT
		}
		return super.onSecondary(window, action, mods, cursorPos)
	}

	override fun updateAspectRatio(renderer: RendererI) {
		val trans = parent.getComponent<PixelTransformComponent>()
		val newScale = Vec2i(screen.settings.componentBrowserWidth, renderer.viewportSize.y - screen.settings.fileBrowserHeight)
		trans?.pixelScale = newScale

		positionComponents(1f, 840f / newScale.y)
	}

	class Container(parent: GameObject, val size: Float): Component(parent)
}