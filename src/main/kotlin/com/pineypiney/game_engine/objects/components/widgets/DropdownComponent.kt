package com.pineypiney.game_engine.objects.components.widgets

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.components.widgets.scrollList.SelectableScrollListComponent
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

open class DropdownComponent(parent: GameObject, var selectedOption: String) : Component(parent){

	open fun selectOption(option: String){
		setSelected(option)
	}

	open fun setSelected(option: String){
		selectedOption = option
	}

	companion object {
		fun createDropdownMenu(name: String, options: Array<String>, listHeight: Float, textParams: Text.Params, callback: (DropdownComponent, String) -> Unit): GameObject{
			val obj = GameObject(name, 1)

			val text = Text.makeMenuText(options.first(), textParams)
			text.position = Vec3(.05f, .2f, 0f)
			text.scale = Vec3(.8f, .8f, 1f)
			obj.addChild(text)

			val dropdown = object : DropdownComponent(obj, options.first()){

				override fun selectOption(option: String) {
					super.selectOption(option)
					callback(this, option)
				}

				override fun setSelected(option: String) {
					super.setSelected(option)
					text.getComponent<TextRendererComponent>()?.setTextContent(option)
				}
			}
			obj.components.add(dropdown)
			obj.components.add(ColourRendererComponent(obj, Vec3(.7f), ColourRendererComponent.menuShader, Mesh.cornerSquareShape))

			val list = SelectableScrollListComponent.createSelectableScrollList("$name List", options, 1f / listHeight, .05f){
				dropdown.selectOption(it)
			}
			list.active = false
			list.position = Vec3(0f, -listHeight - .3f, 0f)
			list.scale = Vec3(1f, listHeight, 1f)
			obj.addChild(list)

			val buttonIcon = TextureLoader[ResourceKey("menu_items/arrow")]
			val button = ButtonComponent.createSpriteButton("$name Button", Sprite(buttonIcon, buttonIcon.height.toFloat())
			) { b, _ ->
				list.active = !list.active
				b.parent.getComponent<SpriteComponent>()?.sprite?.flipY = list.active
			}
			button.pixel(Vec2i(-buttonIcon.width / 2, 0), Vec2i(buttonIcon.height), Vec2(1f, .5f))
			obj.addChild(button)

			return obj
		}
	}
}