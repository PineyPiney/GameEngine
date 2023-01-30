package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.AbstractResourceLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.b
import glm_.c
import glm_.d
import glm_.i
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i
import org.lwjgl.opengl.GL12C
import java.awt.font.FontRenderContext
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.math.floor
import java.awt.Font as JavaFont

class FontLoader private constructor(): AbstractResourceLoader<Font>() {

    override val missing: Font = BitMapFont(Texture.broke, mapOf())
    /**
     * Load and save a BitMap font from an image file
     *
     * @param fontName The location of the font from the fontLocation defined in [resourcesLoader]
     * @param resourcesLoader The ResourcesLoader containing the InputStream for the font file
     * @param letterWidth The width reserved for each character in the texture, in pixels
     * @param letterHeight The height reserved for each character in the texture, in pixels
     * @param charSpacing The space to be left between each character,
     * where 1 is the maximum width of a character according to [letterWidth]
     * @param shader The default shader used to render the text,
     * this can be changed for individual uses and is just the default
     */
    fun loadFontWithTexture(fontName: String, resourcesLoader: ResourcesLoader, letterWidth: Int, letterHeight: Int, charSpacing: Float, shader: Shader = Font.fontShader){

        val stream: InputStream = resourcesLoader.getStream("${resourcesLoader.fontLocation}$fontName") ?: return

        if(stream.available() < 1){
            GameEngineI.logger.warn("Font $fontName does not exist")
            return
        }

        val image = ImageIO.read(stream)
        if(image == null){
            GameEngineI.logger.warn("$fontName Font File is not an image type")
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
        map[key] = BitMapFont(texture, charMap.toMap(), letterWidth, letterHeight, charSpacing, rows, columns, shader)
    }

    /**
     * Load and save a TrueType font from a .ttf file
     *
     * @param fontName The location of the font from the fontLocation defined in [resourcesLoader]
     * @param resourcesLoader The ResourcesLoader containing the InputStream for the font file
     * @param chars The chars to generate textures for
     * @param ctx The FontRenderContext for generating the character textures
     * @param res The resolution of the texture for each character, in pixels per unit in the ttf file
     * @param shader The default shader used to render the text,
     * this can be changed for individual uses and is just the default
     */
    fun loadFontFromTTF(fontName: String, resourcesLoader: ResourcesLoader, chars: CharArray = ('!'..127.c).distinct().toCharArray(), ctx: FontRenderContext = FontRenderContext(null, true, true), res: Int = 200, shader: Shader = Font.fontShader){
        val stream: InputStream = resourcesLoader.getStream("${resourcesLoader.fontLocation}$fontName") ?: return
        val font = JavaFont.createFont(JavaFont.TRUETYPE_FONT, stream)

        val map = chars.associateWith { char ->
            val glyph = font.createGlyphVector(ctx, char.toString())
            val shape = glyph.outline
            val offset = shape.bounds2D.let { Vec2d(it.x, it.y) }
            val size = shape.bounds2D.let { Vec2i(it.width * res, it.height * res) }

            val pixels = List(size.x * size.y){
                val pos = Vec2d((it % size.x).d / res, (size.y - floor(it.d / size.x)) / res) + offset
                val pixel = shape.contains(pos.x, pos.y)
                if(pixel){
                    val pd = 1.0 / res
                    val strength = shape.contains(pos.x + pd, pos.y).i +
                            shape.contains(pos.x - pd, pos.y).i +
                            shape.contains(pos.x, pos.y + pd).i +
                            shape.contains(pos.x, pos.y - pd).i
                    List(3){ (strength * 255).b } + (255).b
                }
                else listOf(0.b, 0.b, 0.b, 255.b)
            }
            val array = pixels.flatten().toByteArray()
            Texture("", TextureLoader.createTexture(array, size.x, size.y, GL12C.GL_RGBA, wrapping = GL12C.GL_CLAMP_TO_EDGE)).apply { setSamples(4) }
        }

        this.map[ResourceKey(fontName.substringBefore('.'))] = TrueTypeFont(font, map, ctx, shader)
    }

    companion object{
        val INSTANCE = FontLoader()

        operator fun get(key: ResourceKey) = INSTANCE[key]
    }
}