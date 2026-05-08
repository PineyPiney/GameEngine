package com.pineypiney.game_engine.util.extension_functions

import glm_.*
import java.io.InputStream
import java.io.OutputStream


fun InputStream.bool() = read() != 0

fun InputStream.string(length: Int) = readNBytes(length).toString(Charsets.ISO_8859_1)

fun OutputStream.bool(value: Boolean) = write(if (value) 1 else 0)

fun OutputStream.short(value: Short, bigEndian: Boolean = true) {
	val array = ByteArray(2)
	array.putShort(0, value, bigEndian)
	write(array)
}

fun OutputStream.int(value: Int, bigEndian: Boolean = true) {
	val array = ByteArray(4)
	array.putInt(0, value, bigEndian)
	write(array)
}

fun OutputStream.long(value: Long, bigEndian: Boolean = true) {
	val array = ByteArray(8)
	array.putLong(0, value, bigEndian)
	write(array)
}

fun OutputStream.float(value: Float, bigEndian: Boolean = true) {
	val array = ByteArray(4)
	array.putFloat(0, value, bigEndian)
	write(array)
}

fun OutputStream.double(value: Double, bigEndian: Boolean = true) {
	val array = ByteArray(8)
	array.putDouble(0, value, bigEndian)
	write(array)
}

fun OutputStream.string(value: String) {
	write(value.toByteArray(Charsets.ISO_8859_1))
}