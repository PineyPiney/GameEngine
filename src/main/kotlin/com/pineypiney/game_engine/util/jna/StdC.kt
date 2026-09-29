package com.pineypiney.game_engine.util.jna

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.Pointer

interface StdC : Library {

	fun malloc(n: Long): Pointer
	fun calloc(n: Long, s: Long): Pointer
	fun free(p: Pointer)

	companion object {
		val INSTANCE: StdC = Native.load(if (Platform.isWindows()) "ucrtbase" else "libc", StdC::class.java)
	}
}