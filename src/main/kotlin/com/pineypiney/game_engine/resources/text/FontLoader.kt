package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.c
import glm_.vec4.Vec4i
import java.io.InputStream
import javax.imageio.ImageIO
import java.awt.Font as JavaFont

class FontLoader private constructor() {

    private val fonts = mutableMapOf<ResourceKey, Font>()

    fun loadFontWithTexture(fontName: String, resourcesLoader: ResourcesLoader, letterWidth: Int, letterHeight: Int, charSpacing: Int, shader: Shader = BitMapFont.fontShader){

        val stream: InputStream = resourcesLoader.getStream("${resourcesLoader.fontLocation}$fontName") ?: return

        if(stream.available() < 1){
            GameEngine.logger.warn("Font $fontName does not exist")
            return
        }

        val image = ImageIO.read(stream)
        if(image == null){
            GameEngine.logger.warn("$fontName Font File is not an image type")
            return
        }

        val texWidth = image.width
        val texHeight = image.height

        val rows = texWidth/letterWidth
        val columns = texHeight/letterHeight

        var index = 32

        val charMap = mutableMapOf<Char, Vec4i>()
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

                // If no pixels were detected in this character, set the width to 1/8th of the total space
                if(charDim == Vec4i(letterWidth, letterHeight, 0, 0))
                    charDim = Vec4i(0, letterHeight / 2, letterWidth / 8, letterHeight / 2)
                charMap[index.c] = charDim
                index++
            }
        }

        val key = ResourceKey(fontName.substringBefore('.'))
        val texture = TextureLoader[ResourceKey("fonts/${key.key}")]
        fonts[key] = BitMapFont(texture, charMap.toMap(), letterWidth, letterHeight, charSpacing, rows, columns, shader)
    }

    fun loadFontFromTTF(fontName: String, resourcesLoader: ResourcesLoader){
        val stream: InputStream = resourcesLoader.getStream("${resourcesLoader.fontLocation}$fontName") ?: return

        val font = JavaFont.createFont(JavaFont.TRUETYPE_FONT, stream)
        fonts[ResourceKey(fontName.substringBefore('.'))] = TrueTypeFont(font)
    }

    fun getFont(key: ResourceKey): Font {
        val f = fonts[key]
        return f ?: brokeFont
    }

    companion object{
        val INSTANCE = FontLoader()
        val brokeFont = BitMapFont(Texture.broke, mapOf())

        fun getFont(key: ResourceKey): Font = INSTANCE.getFont(key)
        operator fun get(key: ResourceKey) = INSTANCE.getFont(key)
    }
}