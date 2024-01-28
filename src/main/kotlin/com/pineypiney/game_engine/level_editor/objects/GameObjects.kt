package com.pineypiney.game_engine.level_editor.objects

import com.pineypiney.game_engine.objects.game_objects.GameObject
import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.objects.game_objects.objects_2D.RenderedGameObject2D
import com.pineypiney.game_engine.objects.game_objects.objects_2D.SimpleTexturedGameObject2D
import com.pineypiney.game_engine.objects.util.shapes.IndicesShape
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.filterValueIsInstance

class GameObjects {

    companion object{

        val footSquareVertices = VertexShape.floatArrayOf(
            -0.5, 0, 0, 0,
            -0.5, 1, 0, 1,
            0.5, 1, 1, 1,
            0.5, 0, 1, 0
        )

        val footSquare = IndicesShape(footSquareVertices, intArrayOf(2, 2), intArrayOf(0, 1, 2, 2, 3, 0))

        val bush1: () -> SimpleTexturedGameObject2D get() = { SimpleTexturedGameObject2D(ResourceKey("background/foliage/bushes/bush1"), footSquare) }
        val bush2: () -> SimpleTexturedGameObject2D get() = { SimpleTexturedGameObject2D(ResourceKey("background/foliage/bushes/bush2"), footSquare) }
        val tree1: () -> SimpleTexturedGameObject2D get() = { SimpleTexturedGameObject2D(ResourceKey("background/foliage/trees/tree1"), footSquare) }
        val tree2: () -> SimpleTexturedGameObject2D get() = { SimpleTexturedGameObject2D(ResourceKey("background/foliage/trees/tree2"), footSquare) }
        val cloud1: () -> SimpleTexturedGameObject2D get() = { SimpleTexturedGameObject2D(ResourceKey("background/clouds/cloud1"), footSquare) }
        val cloud2: () -> SimpleTexturedGameObject2D get() = { SimpleTexturedGameObject2D(ResourceKey("background/clouds/cloud2"), footSquare) }
        val cloud3: () -> SimpleTexturedGameObject2D get() = { SimpleTexturedGameObject2D(ResourceKey("background/clouds/cloud3"), footSquare) }
        val mountain1: () -> SimpleTexturedGameObject2D get() = { SimpleTexturedGameObject2D(ResourceKey("background/mountains/mountain1"), footSquare) }
        val mountain2: () -> SimpleTexturedGameObject2D get() = { SimpleTexturedGameObject2D(ResourceKey("background/mountains/mountain2"), footSquare) }
        val mountain3: () -> SimpleTexturedGameObject2D get() = { SimpleTexturedGameObject2D(ResourceKey("background/mountains/mountain3"), footSquare) }
        val volcano: () -> SimpleTexturedGameObject2D get() = { SimpleTexturedGameObject2D(ResourceKey("background/mountains/volcano"), footSquare) }

        fun getAllItems(): Map<String, () -> GameObject2D> = listOf(
            bush1,
            bush2,
            tree1,
            tree2,
            cloud1,
            cloud2,
            cloud3,
            mountain1,
            mountain2,
            mountain3,
            volcano,
        ).associateBy { it().name }

        fun getAllRenderedItems(): Map<String, () -> RenderedGameObject2D> = getAllItems().filterValueIsInstance<String, () -> RenderedGameObject2D>()

        fun getObject(name: String): GameObject? {
            val items = getAllItems()
            for(i in items){
                val item = i.value()
                if(item.name == name) return item
            }
            return null
        }
    }
}