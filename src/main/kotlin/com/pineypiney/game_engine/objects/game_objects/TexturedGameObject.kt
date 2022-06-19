package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture

abstract class TexturedGameObject(shader: Shader): RenderedGameObject(shader) {

    abstract val texture: Texture

}