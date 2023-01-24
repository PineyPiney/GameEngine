package com.pineypiney.game_engine

import com.pineypiney.game_engine.example.ExampleWindow
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.directory
import org.lwjgl.glfw.GLFW
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

fun main(){
    val copy = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor)

    timeResourceLoading()
}

fun timeResourceLoading(){
    val window = ExampleWindow("")
    val resources = FileResourcesLoader("$directory/src/main/resources")
    val times = DoubleArray(25){ ResourcesLoader.timeActionM { TextureLoader.INSTANCE.loadTextures(resources, resources.streamList.filter { it.startsWith(resources.textureLocation) }.map { it.removePrefix(resources.textureLocation) }) }}
    println("Average time is ${times.average()}ms")
    window.shouldClose = true
    GLFW.glfwTerminate()
}