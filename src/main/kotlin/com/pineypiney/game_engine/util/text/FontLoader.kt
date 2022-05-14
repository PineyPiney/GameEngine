package com.pineypiney.game_engine.util.text

import com.pineypiney.game_engine.resources.ResourceLoader
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.c
import glm_.vec4.Vec4i
import java.io.InputStream
import javax.imageio.ImageIO

class FontLoader private constructor() {

    private val fonts = mutableMapOf<ResourceKey, Font>()

    fun loadFontWithTexture(fontName: String, letterWidth: Int, letterHeight: Int, charSpacing: Int, shader: Shader? = Font.fontShader){

        val stream: InputStream = ResourceLoader.getStream("textures/fonts/$fontName")

        if(stream.available() < 1){
            println("Font $fontName does not exist")
            return
        }

        val image = ImageIO.read(stream)
        if(image == null){
            println("$fontName Font File is not an image type")
            return
        }

        val texWidth = image.width
        val texHeight = image.height

        val rows = texWidth/letterWidth
        val columns = texHeight/letterHeight

        var index = 32


        val key = ResourceKey("fonts\\${fontName.split(".")[0]}")
        fonts[key] = Font(key, letterWidth, letterHeight, charSpacing, rows, columns, shader)

        // Iterate through every row of letters
        for(y in 0 until rows){
            val textureY = y * letterHeight
            // Iterate through every column of that row
            for(x in 0 until columns){
                val textureX = x * letterWidth

                var charDim = Vec4i(letterWidth, letterHeight, 0, 0)
                for(i in textureX until textureX + letterWidth){
                    for(j in textureY until textureY + letterHeight){
                        val pixel = image.getRGB(i, j) and 0x00ffffff
                        if(pixel > 0){
                            if((i - textureX) < charDim.x)
                                charDim.x = (i - textureX)
                            if((j - textureY) < charDim.y)
                                charDim.y = (j - textureY)
                            if((i - textureX) > charDim.z)
                                charDim.z = (i - textureX)
                            if((j - textureY) > charDim.w)
                                charDim.w = (j - textureY)
                        }
                    }
                }

                if(charDim == Vec4i(letterWidth, letterHeight, 0, 0))
                    charDim = Vec4i(0, letterHeight / 2, 0, letterHeight / 2)
                fonts[key]?.setChar(index.c, charDim)
                index++
            }
        }
        println("Loaded Font $fontName")
    }

    fun getFont(key: ResourceKey): Font{
        val f = fonts[key]
        return f ?: Font.defaultFont
    }

    companion object{
        val INSTANCE = FontLoader()

        fun getFont(key: ResourceKey): Font = INSTANCE.getFont(key)
    }
}