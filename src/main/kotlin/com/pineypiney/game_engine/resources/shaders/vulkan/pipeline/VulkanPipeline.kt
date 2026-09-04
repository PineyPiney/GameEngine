package com.pineypiney.game_engine.resources.shaders.vulkan.pipeline

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.vulkan.VulkanMesh
import com.pineypiney.game_engine.resources.shaders.DataType
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.resources.shaders.vulkan.*
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage
import com.pineypiney.game_engine.util.extension_functions.getOrPut
import com.pineypiney.game_engine.util.extension_functions.put
import com.pineypiney.game_engine.util.vulkanMask
import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import com.pineypiney.game_engine.vulkan.VulkanDevice
import glm_.ToBuffer
import glm_.bool
import glm_.mat2x2.Mat2
import glm_.mat2x2.Mat2d
import glm_.mat2x3.Mat2x3
import glm_.mat2x3.Mat2x3d
import glm_.mat2x4.Mat2x4
import glm_.mat2x4.Mat2x4d
import glm_.mat3x2.Mat3x2
import glm_.mat3x2.Mat3x2d
import glm_.mat3x3.Mat3
import glm_.mat3x3.Mat3d
import glm_.mat3x4.Mat3x4
import glm_.mat3x4.Mat3x4d
import glm_.mat4x2.Mat4x2
import glm_.mat4x2.Mat4x2d
import glm_.mat4x3.Mat4x3
import glm_.mat4x3.Mat4x3d
import glm_.mat4x4.Mat4
import glm_.mat4x4.Mat4d
import glm_.vec2.*
import glm_.vec3.*
import glm_.vec4.*
import org.lwjgl.vulkan.VK10
import java.nio.*

abstract class VulkanPipeline(val pipeline: Long, val layout: VulkanPipelineLayout) : Shader {

	val device get() = layout.device
	val descriptorLayouts get() = layout.descriptorLayouts

	override fun use(api: RenderingApi) {
		api.bindPipeline(this)
	}

	override fun endUniforms(api: RenderingApi) {
		api.updateUniforms(this)
	}

	abstract fun getBindPoint(): Int

	fun setImage(name: String, image: VulkanImage, imageLayout: Int = image.layout, sampler: Long = image.getSampler()) {
		for (descriptorLayout in descriptorLayouts) {
			for (binding in descriptorLayout.bindings) {
				if (binding.contains(name) && binding is VulkanDescriptorBinding.Image) {
					binding.setImage(image, imageLayout, sampler)
					return
				}
			}
		}
	}

	fun setImage(binding: Int, image: VulkanImage, imageLayout: Int = image.layout, sampler: Long = image.getSampler()) {
		for (descriptorLayout in descriptorLayouts) {
			for (descBinding in descriptorLayout.bindings) {
				if (descBinding.binding == binding && descBinding is VulkanDescriptorBinding.Image) {
					descBinding.setImage(image, imageLayout, sampler)
					return
				}
			}
		}
	}

	fun setBuffer(name: String, buffer: ByteBuffer) {
		layout.pushConstants.getBufferSlice(name)?.put(buffer)

		for (layout in descriptorLayouts) {
			for (binding in layout.bindings) {
				if (binding.contains(name) && binding is VulkanDescriptorBinding.Buffer) {
					binding.set(name, buffer)
					return
				}
			}
		}
	}

	fun getBuffer(name: String): ByteBuffer? {
		layout.pushConstants.getBufferSlice(name)?.let { return it }

		for (layout in descriptorLayouts) {
			for (binding in layout.bindings) {
				if (binding is VulkanDescriptorBinding.Buffer) {
					binding.get(name)?.let { return it }
				}
			}
		}
		return null
	}

	fun updateDescriptors(commands: PoolAndBuffer, descriptorAllocator: VulkanDescriptorAllocator) {

		val sets = descriptorLayouts.map(descriptorAllocator::allocateDescriptorSet)

		val writer = VulkanDescriptorWriter()
		for (imageSet in sets) {
			for (binding in imageSet.layout.bindings) {
				binding.bind(writer)
			}
			writer.updateSet(device, imageSet).clear()
		}

		commands.bindDescriptorSets(this, sets)
	}

	fun updatePushConstants(commands: PoolAndBuffer) {
		layout.pushConstants.push(commands, this)
	}

	override fun setTexture(name: String, texture: Texture) {
		setImage(name, texture as VulkanImage)
	}

	override fun setTexture(binding: Int, texture: Texture) {
		setImage(binding, texture as VulkanImage)
	}

	override fun setMesh(name: String, mesh: Mesh) {
		setLong(name, (mesh as VulkanMesh).vertexBufferAddress)
	}

	override fun setBool(name: String, value: Boolean) {
		getBuffer(name)?.putInt(if (value) 1 else 0)
	}

	override fun setBools(name: String, values: BooleanArray) {
		val buffer = getBuffer(name) ?: return
		for (bool in values) buffer.putInt(if (bool) 1 else 0)
	}

	override fun setInt(name: String, value: Int) = set1(name, value, ByteBuffer::putInt)
	override fun setInts(name: String, values: IntBuffer) {
		getBuffer(name)?.asIntBuffer()?.put(values)
	}

	override fun setUInt(name: String, value: UInt) = set1(name, value.toInt(), ByteBuffer::putInt)
	override fun setUInts(name: String, values: IntBuffer) {
		getBuffer(name)?.asIntBuffer()?.put(values)
	}

	override fun setLong(name: String, value: Long) = set1(name, value, ByteBuffer::putLong)
	override fun setLongs(name: String, values: LongBuffer) {
		getBuffer(name)?.asLongBuffer()?.put(values)
	}

	override fun setULong(name: String, value: ULong) = set1(name, value.toLong(), ByteBuffer::putLong)
	override fun setULongs(name: String, values: LongBuffer) {
		getBuffer(name)?.asLongBuffer()?.put(values)
	}

	override fun setFloat(name: String, value: Float) = set1(name, value, ByteBuffer::putFloat)
	override fun setFloats(name: String, values: FloatBuffer) {
		getBuffer(name)?.asFloatBuffer()?.put(values)
	}

	override fun setDouble(name: String, value: Double) = set1(name, value, ByteBuffer::putDouble)
	override fun setDoubles(name: String, values: DoubleBuffer) {
		getBuffer(name)?.asDoubleBuffer()?.put(values)
	}

	override fun setVec2i(name: String, v: Vec2t<*>) = set(name, Vec2i(v))
	override fun setVec2i(name: String, x: Number, y: Number) = set2(name, x.toInt(), y.toInt(), ByteBuffer::putInt)
	override fun setVec2ui(name: String, v: Vec2t<*>) = set(name, Vec2ui(v))
	override fun setVec2ui(name: String, x: Number, y: Number) = set2(name, x.toInt(), y.toInt(), ByteBuffer::putInt)

	override fun setVec2(name: String, v: Vec2t<*>) = set(name, Vec2(v))
	override fun setVec2(name: String, x: Number, y: Number) = set2(name, x.toFloat(), y.toFloat(), ByteBuffer::putFloat)
	override fun setVec2d(name: String, v: Vec2t<*>) = set(name, Vec2d(v))
	override fun setVec2d(name: String, x: Number, y: Number) = set2(name, x.toDouble(), y.toDouble(), ByteBuffer::putDouble)

	override fun setVec3i(name: String, v: Vec3t<*>) = set(name, Vec3i(v))
	override fun setVec3i(name: String, x: Number, y: Number, z: Number) = set3(name, x.toInt(), y.toInt(), z.toInt(), ByteBuffer::putInt)
	override fun setVec3ui(name: String, v: Vec3t<*>) = set(name, Vec3ui(v))
	override fun setVec3ui(name: String, x: Number, y: Number, z: Number) = set3(name, x.toInt(), y.toInt(), z.toInt(), ByteBuffer::putInt)

	override fun setVec3(name: String, v: Vec3t<*>) = set(name, Vec3(v))
	override fun setVec3(name: String, x: Number, y: Number, z: Number) = set3(name, x.toFloat(), y.toFloat(), z.toFloat(), ByteBuffer::putFloat)
	override fun setVec3d(name: String, v: Vec3t<*>) = set(name, Vec3d(v))
	override fun setVec3d(name: String, x: Number, y: Number, z: Number) = set3(name, x.toDouble(), y.toDouble(), z.toDouble(), ByteBuffer::putDouble)

	override fun setVec4i(name: String, v: Vec4t<*>) = set(name, Vec4i(v))
	override fun setVec4i(name: String, x: Number, y: Number, z: Number, w: Number) = set4(name, x.toInt(), y.toInt(), z.toInt(), w.toInt(), ByteBuffer::putInt)
	override fun setVec4ui(name: String, v: Vec4t<*>) = set(name, Vec4ui(v))
	override fun setVec4ui(name: String, x: Number, y: Number, z: Number, w: Number) = set4(name, x.toInt(), y.toInt(), z.toInt(), w.toInt(), ByteBuffer::putInt)

	override fun setVec4(name: String, v: Vec4t<*>) = set(name, Vec4(v))
	override fun setVec4(name: String, x: Number, y: Number, z: Number, w: Number) = set4(name, x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat(), ByteBuffer::putFloat)
	override fun setVec4d(name: String, v: Vec4t<*>) = set(name, Vec4d(v))
	override fun setVec4d(name: String, x: Number, y: Number, z: Number, w: Number) = set4(name, x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble(), ByteBuffer::putDouble)

	override fun setVec2is(name: String, values: Array<Vec2t<*>>) = setArray(name, values)
	override fun setVec2uis(name: String, values: Array<Vec2t<*>>) = setArray(name, values)
	override fun setVec2s(name: String, values: Array<Vec2t<*>>) = setArray(name, values)
	override fun setVec2ds(name: String, values: Array<Vec2t<*>>) = setArray(name, values)

	override fun setVec2is(name: String, values: List<Vec2t<*>>) = setList(name, values)
	override fun setVec2uis(name: String, values: List<Vec2t<*>>) = setList(name, values)
	override fun setVec2s(name: String, values: List<Vec2t<*>>) = setList(name, values)
	override fun setVec2ds(name: String, values: List<Vec2t<*>>) = setList(name, values)

	override fun setVec3is(name: String, values: Array<Vec3t<*>>) = setArray(name, values)
	override fun setVec3uis(name: String, values: Array<Vec3t<*>>) = setArray(name, values)
	override fun setVec3s(name: String, values: Array<Vec3t<*>>) = setArray(name, values)
	override fun setVec3ds(name: String, values: Array<Vec3t<*>>) = setArray(name, values)

	override fun setVec3is(name: String, values: List<Vec3t<*>>) = setList(name, values)
	override fun setVec3uis(name: String, values: List<Vec3t<*>>) = setList(name, values)
	override fun setVec3s(name: String, values: List<Vec3t<*>>) = setList(name, values)
	override fun setVec3ds(name: String, values: List<Vec3t<*>>) = setList(name, values)

	override fun setVec4is(name: String, values: Array<Vec4t<*>>) = setArray(name, values)
	override fun setVec4uis(name: String, values: Array<Vec4t<*>>) = setArray(name, values)
	override fun setVec4s(name: String, values: Array<Vec4t<*>>) = setArray(name, values)
	override fun setVec4ds(name: String, values: Array<Vec4t<*>>) = setArray(name, values)

	override fun setVec4is(name: String, values: List<Vec4t<*>>) = setList(name, values)
	override fun setVec4uis(name: String, values: List<Vec4t<*>>) = setList(name, values)
	override fun setVec4s(name: String, values: List<Vec4t<*>>) = setList(name, values)
	override fun setVec4ds(name: String, values: List<Vec4t<*>>) = setList(name, values)


	override fun setMat2(name: String, value: Mat2) = set(name, value)
	override fun setMat2d(name: String, value: Mat2d) = set(name, value)
	override fun setMat2s(name: String, value: Array<Mat2>) = setArray(name, value)
	override fun setMat2ds(name: String, value: Array<Mat2d>) = setArray(name, value)

	override fun setMat2x3(name: String, value: Mat2x3) = set(name, value)
	override fun setMat2x3d(name: String, value: Mat2x3d) = set(name, value)
	override fun setMat2x3s(name: String, value: Array<Mat2x3>) = setArray(name, value)
	override fun setMat2x3ds(name: String, value: Array<Mat2x3d>) = setArray(name, value)

	override fun setMat2x4(name: String, value: Mat2x4) = set(name, value)
	override fun setMat2x4d(name: String, value: Mat2x4d) = set(name, value)
	override fun setMat2x4s(name: String, value: Array<Mat2x4>) = setArray(name, value)
	override fun setMat2x4ds(name: String, value: Array<Mat2x4d>) = setArray(name, value)

	override fun setMat3x2(name: String, value: Mat3x2) = set(name, value)
	override fun setMat3x2d(name: String, value: Mat3x2d) = set(name, value)
	override fun setMat3x2s(name: String, value: Array<Mat3x2>) = setArray(name, value)
	override fun setMat3x2ds(name: String, value: Array<Mat3x2d>) = setArray(name, value)

	override fun setMat3(name: String, value: Mat3) = set(name, value)
	override fun setMat3d(name: String, value: Mat3d) = set(name, value)
	override fun setMat3s(name: String, value: Array<Mat3>) = setArray(name, value)
	override fun setMat3ds(name: String, value: Array<Mat3d>) = setArray(name, value)

	override fun setMat3x4(name: String, value: Mat3x4) = set(name, value)
	override fun setMat3x4d(name: String, value: Mat3x4d) = set(name, value)
	override fun setMat3x4s(name: String, value: Array<Mat3x4>) = setArray(name, value)
	override fun setMat3x4ds(name: String, value: Array<Mat3x4d>) = setArray(name, value)

	override fun setMat4x2(name: String, value: Mat4x2) = set(name, value)
	override fun setMat4x2d(name: String, value: Mat4x2d) = set(name, value)
	override fun setMat4x2s(name: String, value: Array<Mat4x2>) = setArray(name, value)
	override fun setMat4x2ds(name: String, value: Array<Mat4x2d>) = setArray(name, value)

	override fun setMat4x3(name: String, value: Mat4x3) = set(name, value)
	override fun setMat4x3d(name: String, value: Mat4x3d) = set(name, value)
	override fun setMat4x3s(name: String, value: Array<Mat4x3>) = setArray(name, value)
	override fun setMat4x3ds(name: String, value: Array<Mat4x3d>) = setArray(name, value)

	override fun setMat4(name: String, value: Mat4) = set(name, value)
	override fun setMat4d(name: String, value: Mat4d) = set(name, value)
	override fun setMat4s(name: String, value: Array<Mat4>) = setArray(name, value)
	override fun setMat4ds(name: String, value: Array<Mat4d>) = setArray(name, value)

	fun <E> set1(name: String, value: E, func: (ByteBuffer, E) -> Unit) {
		func(getBuffer(name) ?: return, value)
	}

	fun <E> set2(name: String, x: E, y: E, func: ByteBuffer.(E) -> ByteBuffer) {
		val buffer = getBuffer(name) ?: return
		buffer.func(x).func(y)
	}

	fun <E> set3(name: String, x: E, y: E, z: E, func: ByteBuffer.(E) -> ByteBuffer) {
		val buffer = getBuffer(name) ?: return
		buffer.func(x).func(y).func(z)
	}

	fun <E> set4(name: String, x: E, y: E, z: E, w: E, func: ByteBuffer.(E) -> ByteBuffer) {
		val buffer = getBuffer(name) ?: return
		buffer.func(x).func(y).func(z).func(w)
	}

	fun set(name: String, element: ToBuffer) {
		getBuffer(name)?.put(element)
	}

	fun setArray(name: String, array: Array<out ToBuffer>) {
		val buffer = getBuffer(name) ?: return
		for (entry in array) buffer.put(entry)
	}

	fun setList(name: String, array: List<ToBuffer>) {
		val buffer = getBuffer(name) ?: return
		for (entry in array) buffer.put(entry)
	}

	override fun getBool(name: String): Boolean {
		return getBuffer(name)?.getInt()?.bool ?: false
	}

	override fun getInt(name: String): Int = get(name, ByteBuffer::getInt, 0)
	override fun getInts(name: String, dst: IntBuffer) {
		dst.put(getBuffer(name)?.asIntBuffer())
	}

	override fun getUInt(name: String): UInt = get(name, ByteBuffer::getInt, 0).toUInt()
	override fun getUInts(name: String, dst: IntBuffer) {
		dst.put(getBuffer(name)?.asIntBuffer())
	}

	override fun getLong(name: String): Long = get(name, ByteBuffer::getLong, 0L)
	override fun getLongs(name: String, dst: LongBuffer) {
		dst.put(getBuffer(name)?.asLongBuffer())
	}

	override fun getFloat(name: String): Float = get(name, ByteBuffer::getFloat, 0f)
	override fun getFloats(name: String, dst: FloatBuffer) {
		dst.put(getBuffer(name)?.asFloatBuffer())
	}

	override fun getDouble(name: String): Double = get(name, ByteBuffer::getDouble, 0.0)
	override fun getDoubles(name: String, dst: DoubleBuffer) {
		dst.put(getBuffer(name)?.asDoubleBuffer())
	}

	override fun getVec2i(name: String): Vec2i = get(name, ::Vec2i, ::Vec2i)
	override fun getVec2ui(name: String): Vec2ui = get(name, ::Vec2ui, ::Vec2ui)
	override fun getVec2(name: String): Vec2 = get(name, ::Vec2, ::Vec2)
	override fun getVec2d(name: String): Vec2d = get(name, ::Vec2d, ::Vec2d)
	override fun getVec2is(name: String, size: Int): Array<Vec2i> = get(name, size, 8, ::Vec2i, ::Vec2i)
	override fun getVec2uis(name: String, size: Int): Array<Vec2ui> = get(name, size, 8, ::Vec2ui, ::Vec2ui)
	override fun getVec2s(name: String, size: Int): Array<Vec2> = get(name, size, 8, ::Vec2, ::Vec2)
	override fun getVec2ds(name: String, size: Int): Array<Vec2d> = get(name, size, 8, ::Vec2d, ::Vec2d)

	override fun getVec3i(name: String): Vec3i = get(name, ::Vec3i, ::Vec3i)
	override fun getVec3ui(name: String): Vec3ui = get(name, ::Vec3ui, ::Vec3ui)
	override fun getVec3(name: String): Vec3 = get(name, ::Vec3, ::Vec3)
	override fun getVec3d(name: String): Vec3d = get(name, ::Vec3d, ::Vec3d)
	override fun getVec3is(name: String, size: Int): Array<Vec3i> = get(name, size, 12, ::Vec3i, ::Vec3i)
	override fun getVec3uis(name: String, size: Int): Array<Vec3ui> = get(name, size, 12, ::Vec3ui, ::Vec3ui)
	override fun getVec3s(name: String, size: Int): Array<Vec3> = get(name, size, 12, ::Vec3, ::Vec3)
	override fun getVec3ds(name: String, size: Int): Array<Vec3d> = get(name, size, 12, ::Vec3d, ::Vec3d)

	override fun getVec4i(name: String): Vec4i = get(name, ::Vec4i, ::Vec4i)
	override fun getVec4ui(name: String): Vec4ui = get(name, ::Vec4ui, ::Vec4ui)
	override fun getVec4(name: String): Vec4 = get(name, ::Vec4, ::Vec4)
	override fun getVec4d(name: String): Vec4d = get(name, ::Vec4d, ::Vec4d)
	override fun getVec4is(name: String, size: Int): Array<Vec4i> = get(name, size, 16, ::Vec4i, ::Vec4i)
	override fun getVec4uis(name: String, size: Int): Array<Vec4ui> = get(name, size, 16, ::Vec4ui, ::Vec4ui)
	override fun getVec4s(name: String, size: Int): Array<Vec4> = get(name, size, 16, ::Vec4, ::Vec4)
	override fun getVec4ds(name: String, size: Int): Array<Vec4d> = get(name, size, 16, ::Vec4d, ::Vec4d)

	override fun getMat2(name: String): Mat2 = getMatrix(name, 4, ::toFloats, ::Mat2, ::Mat2)
	override fun getMat2d(name: String): Mat2d = getMatrix(name, 4, ::toDoubles, ::Mat2d, ::Mat2d)
	override fun getMat2s(name: String, size: Int): Array<Mat2> = getMatrices(name, size, 4, ::toFloats, ::Mat2, ::Mat2)
	override fun getMat2ds(name: String, size: Int): Array<Mat2d> = getMatrices(name, size, 4, ::toDoubles, ::Mat2d, ::Mat2d)
	override fun getMat2x3(name: String): Mat2x3 = getMatrix(name, 6, ::toFloats, ::Mat2x3, ::Mat2x3)
	override fun getMat2x3d(name: String): Mat2x3d = getMatrix(name, 6, ::toDoubles, ::Mat2x3d, ::Mat2x3d)
	override fun getMat2x3s(name: String, size: Int): Array<Mat2x3> = getMatrices(name, size, 6, ::toFloats, ::Mat2x3, ::Mat2x3)
	override fun getMat2x3ds(name: String, size: Int): Array<Mat2x3d> = getMatrices(name, size, 6, ::toDoubles, ::Mat2x3d, ::Mat2x3d)
	override fun getMat2x4(name: String): Mat2x4 = getMatrix(name, 8, ::toFloats, ::Mat2x4, ::Mat2x4)
	override fun getMat2x4d(name: String): Mat2x4d = getMatrix(name, 8, ::toDoubles, ::Mat2x4d, ::Mat2x4d)
	override fun getMat2x4s(name: String, size: Int): Array<Mat2x4> = getMatrices(name, size, 8, ::toFloats, ::Mat2x4, ::Mat2x4)
	override fun getMat2x4ds(name: String, size: Int): Array<Mat2x4d> = getMatrices(name, size, 8, ::toDoubles, ::Mat2x4d, ::Mat2x4d)

	override fun getMat3x2(name: String): Mat3x2 = getMatrix(name, 6, ::toFloats, ::Mat3x2, ::Mat3x2)
	override fun getMat3x2d(name: String): Mat3x2d = getMatrix(name, 6, ::toDoubles, ::Mat3x2d, ::Mat3x2d)
	override fun getMat3x2s(name: String, size: Int): Array<Mat3x2> = getMatrices(name, size, 6, ::toFloats, ::Mat3x2, ::Mat3x2)
	override fun getMat3x2ds(name: String, size: Int): Array<Mat3x2d> = getMatrices(name, size, 6, ::toDoubles, ::Mat3x2d, ::Mat3x2d)
	override fun getMat3(name: String): Mat3 = getMatrix(name, 9, ::toFloats, ::Mat3, ::Mat3)
	override fun getMat3d(name: String): Mat3d = getMatrix(name, 9, ::toDoubles, ::Mat3d, ::Mat3d)
	override fun getMat3s(name: String, size: Int): Array<Mat3> = getMatrices(name, size, 9, ::toFloats, ::Mat3, ::Mat3)
	override fun getMat3ds(name: String, size: Int): Array<Mat3d> = getMatrices(name, size, 9, ::toDoubles, ::Mat3d, ::Mat3d)
	override fun getMat3x4(name: String): Mat3x4 = getMatrix(name, 12, ::toFloats, ::Mat3x4, ::Mat3x4)
	override fun getMat3x4d(name: String): Mat3x4d = getMatrix(name, 12, ::toDoubles, ::Mat3x4d, ::Mat3x4d)
	override fun getMat3x4s(name: String, size: Int): Array<Mat3x4> = getMatrices(name, size, 12, ::toFloats, ::Mat3x4, ::Mat3x4)
	override fun getMat3x4ds(name: String, size: Int): Array<Mat3x4d> = getMatrices(name, size, 12, ::toDoubles, ::Mat3x4d, ::Mat3x4d)

	override fun getMat4x2(name: String): Mat4x2 = getMatrix(name, 8, ::toFloats, ::Mat4x2, ::Mat4x2)
	override fun getMat4x2d(name: String): Mat4x2d = getMatrix(name, 8, ::toDoubles, ::Mat4x2d, ::Mat4x2d)
	override fun getMat4x2s(name: String, size: Int): Array<Mat4x2> = getMatrices(name, size, 8, ::toFloats, ::Mat4x2, ::Mat4x2)
	override fun getMat4x2ds(name: String, size: Int): Array<Mat4x2d> = getMatrices(name, size, 8, ::toDoubles, ::Mat4x2d, ::Mat4x2d)
	override fun getMat4x3(name: String): Mat4x3 = getMatrix(name, 12, ::toFloats, ::Mat4x3, ::Mat4x3)
	override fun getMat4x3d(name: String): Mat4x3d = getMatrix(name, 12, ::toDoubles, ::Mat4x3d, ::Mat4x3d)
	override fun getMat4x3s(name: String, size: Int): Array<Mat4x3> = getMatrices(name, size, 12, ::toFloats, ::Mat4x3, ::Mat4x3)
	override fun getMat4x3ds(name: String, size: Int): Array<Mat4x3d> = getMatrices(name, size, 12, ::toDoubles, ::Mat4x3d, ::Mat4x3d)
	override fun getMat4(name: String): Mat4 = getMatrix(name, 16, ::toFloats, ::Mat4, ::Mat4)
	override fun getMat4d(name: String): Mat4d = getMatrix(name, 16, ::toDoubles, ::Mat4d, ::Mat4d)
	override fun getMat4s(name: String, size: Int): Array<Mat4> = getMatrices(name, size, 16, ::toFloats, ::Mat4, ::Mat4)
	override fun getMat4ds(name: String, size: Int): Array<Mat4d> = getMatrices(name, size, 16, ::toDoubles, ::Mat4d, ::Mat4d)


	fun <E> get(name: String, func: ByteBuffer.() -> E, default: E): E {
		return getBuffer(name)?.func() ?: default
	}

	fun <E> get(name: String, fromBuffer: (ByteBuffer) -> E, default: () -> E): E {
		return fromBuffer(getBuffer(name) ?: return default())
	}

	inline fun <reified E> get(name: String, size: Int, elementBytes: Int, fromBuffer: (ByteBuffer, Int) -> E, default: () -> E): Array<E> {
		val buffer = getBuffer(name)
		return if (buffer == null) Array(size) { default() }
		else Array(size) { fromBuffer(buffer, elementBytes) }
	}

	fun <E, B> getMatrix(name: String, elementSize: Int, convertBuffer: (ByteBuffer, Int, Int) -> B, fromBuffer: (B) -> E, default: (Iterable<*>) -> E): E {
		return fromBuffer(convertBuffer(getBuffer(name) ?: return default(List(elementSize) { 0f }), elementSize, 0))
	}

	inline fun <reified E, B> getMatrices(name: String, size: Int, elementSize: Int, convertBuffer: (ByteBuffer, Int, Int) -> B, fromBuffer: (B) -> E, default: (Iterable<*>) -> E): Array<E> {
		val buffer = getBuffer(name)
		return if (buffer == null) {
			val list = List(elementSize) { 0f }
			Array(size) { default(list) }
		} else {
			Array(size) { fromBuffer(convertBuffer(buffer, elementSize, it)) }
		}
	}

	abstract fun getAllModules(): Iterable<VulkanShaderModule>

	override fun compileUniforms(): Uniforms {
		val map = mutableMapOf<String, String>()
		for (module in getAllModules()) {
			val moduleMap = module.data.getUniformMap()
			for ((name, type) in moduleMap) {
				val currentType = map[name]
				if (currentType == null) map[name] = type
				else if (type != currentType) {
					GameEngineI.logger.warn("Shader $this contains 2 uniforms called $name, one of type $type and the other of type $currentType")
				}
			}
		}
		val uniforms = map.mapNotNull { (name, type) -> Uniform.parse(name, type) }
		return Uniforms(uniforms.toTypedArray())
	}

	override fun delete() {
		VK10.vkDestroyPipeline(device.device, pipeline, null)
		layout.delete()
	}

	companion object {
		fun compileDescriptorLayouts(device: VulkanDevice, data: Map<ShaderStage, VulkanShaderData>): List<VulkanDescriptorLayout> {
			val builders = mutableListOf<VulkanDescriptorLayout.Builder>()
			for ((stage, stageData) in data) {
				for (uniformBuffer in stageData.uniforms) {
					val builder = builders.getOrPut(uniformBuffer.set, VulkanDescriptorLayout.Builder::set, VulkanDescriptorLayout::Builder)
					builder.addStage(stage)
					when (val data = uniformBuffer.data) {
						is DataType.Sampler -> builder.addCombinedImage(uniformBuffer.binding, uniformBuffer.name)
						is DataType.Image -> builder.addStorageImage(uniformBuffer.binding, uniformBuffer.name)
						is DataType.Struct -> builder.addStorageBuffer(device, uniformBuffer.binding, uniformBuffer.name, data)
					}
				}
			}
			return builders.map { it.build(device) }
		}

		fun compilePushConstants(modules: List<VulkanShaderModule>): VulkanPushConstantManager {
			val ranges = mutableMapOf<ShaderStage, Pair<Int, Int>>()
			val pushConstantVariables = mutableListOf<Triple<String, DataType, Int>>()
			for ((_, module) in modules.withIndex()) {
				val (_, pushConstants) = module.data.pushConstants ?: continue
				ranges[module.getStage()] = pushConstants.min to pushConstants.max
				for ((vn, v) in pushConstants.variables) pushConstantVariables.add(Triple(vn, v.first, v.second))
			}
			if (ranges.isEmpty()) return VulkanPushConstantManager(emptyMap(), emptyMap())

			val sections = mutableMapOf<Int, Pair<Int, Int>>()
			var start = 0
			var end = 0
			var stageMask = 0

			val indexOrdered = pushConstantVariables.sortedBy { it.third }
			for ((_, type, i) in indexOrdered) {
				val stages = ranges.filter { (_, v) -> i >= v.first && i < v.second }.keys
				val mask = stages.vulkanMask()
				if (stageMask == 0) stageMask = mask
				else if (mask and stageMask == 0) {
					sections[stageMask] = start to end - start
					stageMask = mask
					start = i
				} else {
					stageMask = mask or stageMask
				}
				end = i + type.size
			}
			sections[stageMask] = start to end - start

			pushConstantVariables.clear()
			val constants = indexOrdered.associate { it.first to (it.second to it.third) }
			return VulkanPushConstantManager(constants, sections)
		}

		fun toFloats(buffer: ByteBuffer, size: Int, i: Int): FloatArray {
			val array = FloatArray(size)
			buffer.asFloatBuffer().get(size * i, array)
			return array
		}

		fun toDoubles(buffer: ByteBuffer, size: Int, i: Int): DoubleArray {
			val array = DoubleArray(size)
			buffer.asDoubleBuffer().get(size * i, array)
			return array
		}
	}

	abstract class Builder<P : VulkanPipeline> : Deletable {

		var layout: VulkanPipelineLayout? = null

		abstract fun build(device: VulkanDevice): P
	}
}