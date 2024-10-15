package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class ComponentBrowser(parent: GameObject, val screen: EditorScreen): DefaultInteractorComponent(parent, "CBS"), UpdatingAspectRatioComponent {

	val componentContainer = MenuItem("Component Container")
	var adderPos = Vec3(.6f, .5f, 0f)

	init {
		parent.components.add(RelativeTransformComponent(parent, Vec2(.6f, -.6f), Vec2(.4f, 1.6f), Vec2(0f)))
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

		componentContainer.deleteAllChildren()
		positionComponents()
	}

	fun setEditing(obj: GameObject?){
		componentContainer.deleteAllChildren()

		if(obj != null) {
			val text = ActionTextField<TextFieldComponent>("Object Name Field", Vec2(.05f, .95f), Vec2(.9f, .045f), obj.name){ f, _, _ ->
				obj.name = f.text
				screen.setEditingName(f.text)
			}
			text.init()
			componentContainer.addChild(text)

			positionComponents()
		}
	}

	fun positionComponents(initialY: Float = .95f){
		val comps = screen.editingObject?.components ?: return
		var y = initialY
		for(c in comps){

			// Add component title
			y -= .05f
			val compText = Text.makeMenuText(c::class.simpleName ?: "")
			compText.position = Vec3(0f, y + .025f, .01f)
			compText.scale = Vec3(1f, .05f, 1f)
			compText.init()
			componentContainer.addChild(compText)

			// Add all component fields
			for(f in c.fields){
				y -= .05f
				val fieldID = "${c.id}.${f.id}"
				val editor = f.editor(MenuItem("Field Editor $fieldID"), c, fieldID, Vec2(0f, y), Vec2(1f, .05f), c::setValue).applied().parent
				editor.init()
				componentContainer.addChild(editor)
			}
		}
	}

	override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)

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
		val invAsp = 1f / renderer.aspectRatio

	}
}