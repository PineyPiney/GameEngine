package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class BasicTextButton(override val string: String, override var origin: Vec2, override val size: Vec2, override val action: (button: Button) -> Unit, override val textColour: Vec4 = Vec4(0, 0, 0, 1), override val shader: Shader = menuShader) : TextButton() {

}