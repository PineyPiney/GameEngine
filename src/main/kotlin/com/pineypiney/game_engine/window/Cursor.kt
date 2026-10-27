package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.objects.Deletable

interface Cursor : Deletable {

	val handle: Long

}