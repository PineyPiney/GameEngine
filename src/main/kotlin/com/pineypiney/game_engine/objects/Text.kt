package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.text.Font
import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import glm_.f
import glm_.glm
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import java.awt.Point
import kotlin.math.max
import kotlin.math.min

open class Text(val text: String, var colour: Vec4 = Vec4(1, 1, 1, 1), val textMaxWidth: Float = 2f, val textMaxHeight: Float = 2f,
                val font: Font = Font.defaultFont,
                val shader: Shader? = font.shader, val window: Window = Window.INSTANCE
): Drawable, Deleteable {

    // In this constructor bounds is used to set the max height and width,
    // useful if a class already has a Vec2 that fits this format
    constructor(text: String, bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                font: Font = Font.defaultFont,
                shader: Shader = Font.fontShader, window: Window = Window.INSTANCE):
            this(text, colour, bounds.x, bounds.y, font, shader, window)


    override var visible: Boolean = true

    override var origin: Vec2 = Vec2()
    // dimensions is Vec4(0, min y, pixel width, max y). min y and max y are used so that all letters are lined up vertically
    val dimensions = getPixelSize()
    final override val size: Vec2 = Vec2(dimensions.z, dimensions.w - dimensions.y)

    val pixelLength = size.x
    val pixelHeight = size.y
    private var defaultCharHeight = 0.0f
    private var defaultCharWidth = 0.0f

    private val letterIndices: List<Int> = List(text.length){ text[it].i - 32 }
    private val letterPoints: List<Point> = List(text.length){
        val index = letterIndices[it]
        Point((index%font.columns) * font.letterWidth, font.texture.height - ((index/font.columns) * font.letterHeight))
    }
    private val letterSize: List<Vec2> = List(text.length){
        val char = text[it]
        Vec2(getCharWidth(char) / font.texture.width, -pixelHeight/ font.texture.height)
    }

    private val quads: Array<TextQuad> = Array(text.length) { i ->
        val texturePos = Vec2(letterPoints[i].x.f / font.texture.width, (letterPoints[i].y - dimensions.y) / font.texture.height)
        TextQuad(texturePos, texturePos + (letterSize[i]))
    }

    init{
        defaultCharHeight = textMaxHeight

        updateAspectRatio(Window.INSTANCE)
    }

    fun setDefaults(height: Float){
        defaultCharHeight = height
        defaultCharWidth = height * (font.letterWidth / pixelHeight) / window.aspectRatio
    }

    fun getCharWidth(char: Char): Float = (font.getDimensions(char)?.z?.f ?: 0f)
    fun getCharHeight(char: Char): Vec2i = Vec2i(font.getDimensions(char)?.y ?: 31, font.getDimensions(char)?.w ?: 31)

    /**
     * Add the widths of all letters together in terms of pixels,
     * leaving space in between each letter according to the font
     *
     * @param text The text to find the width of
     * @return The width in pixels
     */
    fun getPixelWidth(text: String): Float{
        // Starting at 2 accounts for the margin at the beginning of the text
        var l = 2f
        text.indices.forEach { l += (getCharWidth(text[it]) + font.characterSpacing) }
        return l
    }

    fun getPixelSize(): Vec4 {
        val w = getPixelWidth(text)
        var min = font.letterHeight
        var max = 0
        text.forEach  ret@{
            val v = font.getDimensions(it)?: return@ret
            min = min(min, v.y)
            max = max(max, v.w)
        }

        return Vec4(0, min - 5, w, max + 5)
    }

    fun getScreenSize(): Vec2{
        return Vec2(defaultCharWidth * (getPixelWidth(text)/ font.letterWidth), defaultCharHeight)
    }

    final override fun updateAspectRatio(window: Window) {

        // First scale the text so it touches the vertical bounds
        setDefaults(textMaxHeight)

        // Then, if it extends beyond the max width, scale it back down
        val widthRatio = getScreenSize().x/textMaxWidth
        if(widthRatio > 1){
            setDefaults(textMaxHeight/widthRatio)
        }
    }

    fun getQuad(i: Int): TextQuad?{
        return try{
            quads[i]
        }
        catch (e: IndexOutOfBoundsException){
            e.printStackTrace()
            null
        }
    }

    open fun setUniversalUniforms(shader: Shader){
        shader.setVec4("colour", colour)
    }

    open fun setIndividualUniforms(shader: Shader, index: Int){}

    override fun draw() {

        val shader = shader ?: font.shader ?: Font.fontShader
        shader.use()
        setUniversalUniforms(shader)
        font.texture.bind()

        // Add a bit of space at the beginning
        var xOffset = font.characterSpacing.f

        for(i in text.indices){

            setIndividualUniforms(shader, i)

            val charWidth = getCharWidth(text[i])
            quads[i].bind()

            var model = glm.translate(I, Vec3(origin.x + (xOffset * defaultCharWidth/ font.letterWidth), origin.y, 0))
            model = model.scale(Vec3(defaultCharWidth * (charWidth/ font.letterWidth), defaultCharHeight, 1))
            shader.setMat4("model", model)

            quads[i].draw()
            xOffset += (charWidth + font.characterSpacing)
        }
    }

    override fun drawCentered(p: Vec2){
        val size = getScreenSize()
        origin = p - (size/2)
        draw()
    }

    override fun drawCenteredLeft(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(0f, size.y * 0.5f)
        draw()
    }

    override fun drawCenteredTop(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(size.x * 0.5f, size.y)
        draw()
    }

    override fun drawCenteredRight(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(size.x, size.y * 0.5f)
        draw()
    }

    override fun drawCenteredBottom(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(size.x * 0.5f, 0f)
        draw()
    }

    override fun drawTopLeft(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(0, size.y)
        draw()
    }

    override fun drawTopRight(p: Vec2) {
        val size = getScreenSize()
        origin = p - size
        draw()
    }

    override fun drawBottomLeft(p: Vec2) {
        origin = p
        draw()
    }

    override fun drawBottomRight(p: Vec2) {
        val size = getScreenSize()
        origin = p - Vec2(size.x, 0)
        draw()
    }

    override fun delete(){
        quads.asList().delete()
    }

    override fun toString(): String {
        return "Text[\"$text\"]"
    }
}