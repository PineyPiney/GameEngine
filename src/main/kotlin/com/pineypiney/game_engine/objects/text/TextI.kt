package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.Shaded
import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.extension_functions.delete
import glm_.vec4.Vec4

interface TextI: Initialisable, Shaded {

    val text: String
    val colour: Vec4
    val maxWidth: Float
    val maxHeight: Float

    val font: Font
    var italic: Float
    var underlineThickness: Float
    var underlineOffset: Float
    var underlineAmount: Float

    var defaultCharHeight: Float
    val quads: Array<TextQuad>

    fun setDefaults(height: Float)
    fun getWidth(text: String): Float

    fun setIndividualUniforms(shader: Shader, quad: TextQuad){}

    override fun delete(){
        quads.toSet().delete()
    }
}