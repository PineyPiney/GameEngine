package com.pineypiney.game_engine

import com.pineypiney.game_engine.example.ExampleWindow
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.directory
import org.lwjgl.glfw.GLFW

fun main(){

}

fun timeResourceLoading(){
    val window = ExampleWindow()
    val resources = FileResourcesLoader("$directory/src/main/resources")
    val times = DoubleArray(25){ ResourcesLoader.timeActionM { resources.loadTextures(resources.getStreams()) }}
    println("Average time is ${times.average()}ms")
    window.shouldClose = true
    GLFW.glfwTerminate()
}