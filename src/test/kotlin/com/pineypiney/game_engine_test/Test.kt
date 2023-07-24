package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.LibrarySetUp
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.directory
import com.pineypiney.game_engine_test.test2D.Game2D
import com.pineypiney.game_engine_test.test3D.Game3D
import org.junit.Test

class Test{

    @Test
    fun test2D() {
        run(::Game2D)
    }

    @Test
    fun test3D(){
        run(::Game3D)
    }

    fun <E: GameLogicI> run(screen: (GameEngineI<E>) -> E){

        LibrarySetUp.initLibraries()
        TestWindow.INSTANCE.init()

        val fileResources = FileResourcesLoader("$directory/src/main/resources")
        TextureLoader.setFlags("fonts/Large Font.png", 1)

        val engine = TestEngine(fileResources, screen)

        engine.run()
    }
}
