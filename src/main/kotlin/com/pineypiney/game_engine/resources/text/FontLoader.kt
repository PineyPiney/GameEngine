package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.resources.AbstractResourceLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.length
import glm_.*
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i
import kool.*
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL12C
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import java.awt.Font as JavaFont

class FontLoader private constructor(): AbstractResourceLoader<Font>() {

    override val missing: Font = BitMapFont(Texture.broke, null, mapOf(), null)
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
    fun loadFontFromTexture(fontName: String, resourcesLoader: ResourcesLoader, letterWidth: Int, letterHeight: Int, charSpacing: Float, loadBold: Boolean = true, firstLetter: Int = 32, lineSpacing: Float = 1.2f, shader: Shader = Font.fontShader){

        val stream: InputStream = resourcesLoader.getStream("${resourcesLoader.fontLocation}$fontName") ?: return

        if(stream.available() < 1){
            GameEngineI.warn("Font $fontName does not exist")
            return
        }

        val image = ImageIO.read(stream)
        if(image == null){
            GameEngineI.warn("$fontName Font File is not an image type")
            return
        }

        val texWidth = image.width
        val texHeight = image.height

        val rows = texWidth/letterWidth
        val columns = texHeight/letterHeight

        val a = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
        val charMap = getCharDimensions(ByteArray(a.size){ (a[it] and 255).b }, rows, columns, letterWidth, letterHeight, firstLetter)


        val boldTexture: Texture? = if(loadBold) loadBoldFromTexture(fontName, image, columns, rows, 3, 5) else null
        val boldMap: Map<Char, Vec4i>? = if(loadBold) charMap.mapValues { it.value + Vec4i(-4, -4, 4, 4) } else null

        val key = ResourceKey(fontName.substringBefore('.'))
        val texture = TextureLoader[ResourceKey("fonts/${key.key}")]
        map[key] = BitMapFont(texture, boldTexture, charMap, boldMap, letterWidth, letterHeight, charSpacing, lineSpacing, firstLetter, shader)
    }

    fun getCharDimensions(image: ByteArray, rows: Int, columns: Int, letterWidth: Int, letterHeight: Int, firstLetter: Int): Map<Char, Vec4i>{

        var index = firstLetter
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
                        val pixel = image[i + (j * columns * letterWidth)] and 0x7f
                        if(pixel > 0){
                            charDim.x = min(charDim.x, i - textureX)
                            charDim.y = min(charDim.y, j - textureY)
                            charDim.z = max(charDim.z, i - textureX)
                            charDim.w = max(charDim.w, j - textureY)
                        }
                    }
                }

                // If no pixels were detected in this character, set the width to 1/8th of the total space
                if(charDim == Vec4i(letterWidth, letterHeight, 0, 0))
                    charDim = Vec4i(0, letterHeight / 2, letterWidth / 8, letterHeight / 2)
                charMap[index.c] = charDim// + offset.run { Vec4i(x, y, x, y) }
                index++
            }
        }
        return charMap.toMap()
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
            Texture("", TextureLoader.createTexture(array, size.x, size.y, GL12C.GL_RGBA, wrapping = GL12C.GL_CLAMP_TO_EDGE))
        }

        this.map[ResourceKey(fontName.substringBefore('.'))] = TrueTypeFont(font, map, ctx, shader)
    }

    fun loadBoldFromTexture(name: String, image: BufferedImage, columns: Int, rows: Int, boldWidth: Int, outerBoldWidth: Int): Texture {
        val t = Timer.getCurrentTime()
        val d = 2 * outerBoldWidth + 1
        val fadeDist = outerBoldWidth - boldWidth
        val vecs =
            List(d * d) { Vec2i(it % d - outerBoldWidth, (it / d) - outerBoldWidth) }.associateWith { it.length() }
        val strengths: Map<Vec2i, Int> = vecs.filter { it.value < outerBoldWidth }.mapValues { if(it.value <= boldWidth) 255 else ((outerBoldWidth - it.value) * 255 / fadeDist).i }
        val letterWidth = image.width / columns
        val letterHeight = image.height / rows

        val imageArray = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)

        val boldTextureWidth = image.width + (2 * columns * outerBoldWidth)
        val boldTextureHeight = image.height + (2 * rows * outerBoldWidth)
        val boldBuffer = IntBuffer(boldTextureWidth * boldTextureHeight)
        for(x in 0 until image.width){
            for(y in 0 until image.height){
                val lx = x % letterWidth
                val ly = y % letterHeight
                val c = imageArray[x + y * image.width] and 0xffffff
                if(c == 0) continue
                for((vec, s) in strengths){
                    // The position of the new bold pixel in the original texture
                    val bx = x + vec.x
                    val by = y + vec.y
                    // The position of the new bold pixel in the bold texture
                    val nx = bx + outerBoldWidth + (((x * columns) / image.width) * outerBoldWidth * 2)
                    val ny = by + outerBoldWidth + (((y * rows) / image.height) * outerBoldWidth * 2)
                    // The position of the new pixel in the BoldBuffer 8657573, 8655367
                    val i = nx + boldBuffer.cap - (ny + 1) * boldTextureWidth
                    val shouldSet =
                        if((lx + vec.x) in 0 until letterWidth && (ly + vec.y) in 0 until letterHeight) imageArray[bx + by * image.width] and 0xffffff == 0
                        else true

                    if(shouldSet && s > boldBuffer[i]){
                        boldBuffer[i] = s
                    }
                }
            }
        }
        val texture = Texture("$name bold", TextureLoader.createTexture(boldBuffer.toIntArray().toByteBuffer(), boldTextureWidth, boldTextureHeight, GL11C.GL_RED))

        boldBuffer.clear()
        TextureLoader.loadIndividualSettings(texture.texturePointer)

//        let{
//            val orderedBytes = boldBuffer.reversed().chunked(boldTextureWidth){ it.reversed() }.flatten().map { it.i * 0x010101 }
//            val im = BufferedImage(boldTextureWidth, boldTextureHeight, 10)
//            im.setRGB(0, 0, boldTextureWidth, boldTextureHeight, orderedBytes.toIntArray(), 0, boldTextureWidth)
//            ImageIO.write(im, "png", File("LargeBoldImage.png"))
//        }

        GameEngineI.debug("Time to generate bold image: ${Timer.getCurrentTime() - t} seconds")
        return texture
    }

    companion object{
        val INSTANCE = FontLoader()

        operator fun get(key: ResourceKey) = INSTANCE[key]
    }
}