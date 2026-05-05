package com.pineypiney.game_engine.resources.shaders

enum class GLSLType(val bytes: Int) {
	BOOL(4),
	INT(4),
	UINT(4),
	FLOAT(4),
	DOUBLE(8)
}