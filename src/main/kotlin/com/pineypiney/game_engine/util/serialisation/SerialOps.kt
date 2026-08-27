package com.pineypiney.game_engine.util.serialisation

import java.io.Reader

interface SerialOps<E> {

	fun nul(): E
	fun missing(): E
	fun parse(reader: Reader): E

	fun readBool(input: E): Boolean
	fun readByte(input: E): Byte
	fun readShort(input: E): Short
	fun readInt(input: E): Int
	fun readLong(input: E): Long
	fun readFloat(input: E): Float
	fun readDouble(input: E): Double
	fun readString(input: E): String

	fun writeBool(input: Boolean): E
	fun writeByte(input: Byte): E
	fun writeShort(input: Short): E
	fun writeInt(input: Int): E
	fun writeLong(input: Long): E
	fun writeFloat(input: Float): E
	fun writeDouble(input: Double): E
	fun writeNumber(input: Number): E
	fun writeString(input: String): E

	fun readInts(input: E): Iterable<Int>
	fun readFloats(input: E): Iterable<Float>

	fun writeInts(input: Iterable<Int>): E
	fun writeFloats(input: Iterable<Float>): E

	fun createArray(): E
	fun createArray(first: E): E
	fun createMap(): E
	fun createMap(key: String, first: E): E

	fun appendArray(array: E, value: E)
	fun appendMap(map: E, key: String, value: E)

	fun hasChild(parent: E, name: String): Boolean
	fun getChild(parent: E, name: String): E

	fun iterator(parent: E): Iterator<E>
	fun mapIterator(parent: E): Iterator<Map.Entry<String, E>>
	fun forEach(parent: E, action: (E) -> Unit)
	fun forEachEntry(parent: E, action: (Map.Entry<String, E>) -> Unit)

	fun put(map: E, key: String, value: Boolean) = appendMap(map, key, writeBool(value))
	fun put(map: E, key: String, value: Number) = appendMap(map, key, writeNumber(value))
	fun put(map: E, key: String, value: String) = appendMap(map, key, writeString(value))

	fun getBool(map: E, key: String) = readBool(getChild(map, key))
	fun getInt(map: E, key: String) = readInt(getChild(map, key))
	fun getFloat(map: E, key: String) = readFloat(getChild(map, key))
	fun getString(map: E, key: String) = readString(getChild(map, key))

	fun stringify(value: E): String
}