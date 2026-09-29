package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.objects.Deletable

interface Input : Deletable {

	fun update()
}