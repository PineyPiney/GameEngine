package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture

abstract class TexturedGameObject2D(shader: Shader): RenderedGameObject2D(shader) {

    abstract val texture: Texture

}