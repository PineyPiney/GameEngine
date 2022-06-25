package com.pineypiney.game_engine.objects.game_objects.objects_3D

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture

abstract class TexturedGameObject3D(shader: Shader): RenderedGameObject3D(shader) {

    abstract val texture: Texture

}