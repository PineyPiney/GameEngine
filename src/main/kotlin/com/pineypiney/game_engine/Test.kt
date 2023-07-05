package com.pineypiney.game_engine

import com.pineypiney.game_engine.example.ExampleWindow
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.directory
import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.util.input.Inputs
import org.lwjgl.glfw.GLFW

fun main(){
    LibrarySetUp.initLibraries()
    val w = object : Window("window", 1000, 500, false, true){
        override val input: Inputs = DefaultInput(this)
    }
    val r = object : FileResourcesLoader("src/main/resources"){}
    r.loadResources()

    val smallTexture = TextureLoader[ResourceKey("cursor")]
    val largeTexture = TextureLoader[ResourceKey("fonts/Large Font")]

    val x = 10000000
    val st = ResourcesLoader.timeActionM {
        for(i in 1..x){
            smallTexture.bind()
        }
    }
    val lt = ResourcesLoader.timeActionM {
        for(i in 1..x){
            largeTexture.bind()
        }
    }
    println("Time to bind small texture: $st\nTime to bind large texture: $lt")
}

fun timeResourceLoading(){
    val window = ExampleWindow()
    val resources = FileResourcesLoader("$directory/src/main/resources")
    val times = DoubleArray(25){ ResourcesLoader.timeActionM { resources.loadTextures(resources.getStreams()) }}
    println("Average time is ${times.average()}ms")
    window.shouldClose = true
    GLFW.glfwTerminate()
}