package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.util.ByteData
import java.io.InputStream

fun InputStream.readInt(bigEndian: Boolean = true) = ByteData.bytes2Int(readNBytes(4), 0, 4, bigEndian)
fun InputStream.readUInt(bigEndian: Boolean = true) = ByteData.bytes2UInt(readNBytes(4), 0, 4, bigEndian)
fun InputStream.readShort(bigEndian: Boolean = true) = ByteData.bytes2Short(readNBytes(2), 0, 2, bigEndian)
fun InputStream.readUShort(bigEndian: Boolean = true) = ByteData.bytes2UShort(readNBytes(2), 0, 2, bigEndian)

fun InputStream.readString(length: Int) = readNBytes(length).toString(Charsets.ISO_8859_1)
