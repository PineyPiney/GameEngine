package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.Deletable
import java.nio.ByteBuffer

// https://wikis.khronos.org/opengl/Shader_Storage_Buffer_Object
interface ShaderStorageBuffer : Deletable {

	val size: Int

	fun getData(offset: Long = 0L, size: Int = this.size): ByteBuffer

	fun setData(data: ByteBuffer)

	fun setSubData(data: ByteBuffer, offset: Long = 0L)

	fun resize(size: Int)
}