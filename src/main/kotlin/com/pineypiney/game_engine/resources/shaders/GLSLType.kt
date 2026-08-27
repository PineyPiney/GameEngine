package com.pineypiney.game_engine.resources.shaders

enum class GLSLType(val bytes: Int, val symbol: String) {
	BOOL(4, "b"),
	INT(4, "i"),
	UINT(4, "u"),
	INT64_T(8, "i64"),
	UINT64_T(8, "u64"),
	FLOAT(4, ""),
	DOUBLE(8, "d");

	fun varName() = name.lowercase()
}