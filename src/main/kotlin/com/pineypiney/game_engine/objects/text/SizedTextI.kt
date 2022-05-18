package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.util.extension_functions.replaceWhiteSpaces

interface SizedTextI: TextI {

    val fontSize: Int
    val separation: Float

    val lines: Array<String>
    val lengths: FloatArray

    fun generateLines(window: Window): Array<String>{
        val lines = mutableListOf<String>()
        var currentText = ""
        var i = 0
        var lastBreak = -1
        while(i < text.length){
            while(i < text.length - 1 && !" \n".contains(text[i])){
                i++
            }

            val word = text.slice(lastBreak+1  ..  i)
            val lineWidth = getPixelWidth(currentText + word.removeSuffix(" "))

            val screenWidth = pixelToRelative(lineWidth)
            if(screenWidth > maxWidth) {
                lines.add(currentText)
                currentText = ""
            }

            if(text[i] == '\n'){
                lines.add(currentText + word.substring(0, word.length - 1))
                currentText = ""
            }
            else{
                currentText += word
            }

            lastBreak = i
            i++
        }
        lines.add(currentText)
        lines.removeAll { it.replaceWhiteSpaces() == "" }
        return lines.toTypedArray()
    }
}