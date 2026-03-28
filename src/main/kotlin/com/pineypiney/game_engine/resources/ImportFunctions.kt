package com.pineypiney.game_engine.resources

import java.io.InputStream

fun InputStream.readString(length: Int) = readNBytes(length).toString(Charsets.ISO_8859_1)
