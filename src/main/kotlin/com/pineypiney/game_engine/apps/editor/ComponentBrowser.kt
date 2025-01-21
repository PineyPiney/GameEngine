package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
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
		positionComponents(obj)
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

			positionComponents(obj)
		}
	}

	fun refreshField(fieldID: String){
		val obj = componentContainer.getChild("Field Editor $fieldID") ?: return
		obj.getComponent<FieldEditor<*, *>>()?.update()
	}

	fun positionComponents(obj: GameObject, initialY: Float = .95f){
		var y = initialY
		for(c in obj.components){

			// Add component title
			y -= .05f
			val compText = Text.makeMenuText(c::class.simpleName ?: "")
			compText.position = Vec3(0f, y + .025f, .01f)
			compText.scale = Vec3(1f, .05f, 1f)
			componentContainer.addChild(compText)
			compText.init()

			// Add all component fields
			for(f in c.getAllNewFieldsExt()){
				val fieldID = "${c.id}.${f.id}"
				val editor = createEditor(MenuItem("Field Editor $fieldID"), f, Vec2(0f, y), Vec2(1f, .05f), { _, v -> f.set(v)})?.applied()?.parent ?: continue
				val dy = editor.scale.y
				editor.translate(Vec3(0f, -dy, 0f))
				y -= dy
				componentContainer.addChild(editor)
				editor.init()
			}
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

	override fun updateAspectRatio(renderer: RendererI) {}
}