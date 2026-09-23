package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.rendering.ObjectRenderer
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.vulkan.*
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10

class VulkanObjectRenderer(override val viewPos: Vec3, val device: VulkanDevice, viewportSize: Vec2i = Vec2i(64), override val projection: Mat4 = I) : ObjectRenderer {

	override val viewportSize: Vec2i get() = image.size
	override val view: Mat4 = I.translate(viewPos)
	override val guiProjection: Mat4 = projection
	override val aspectRatio: Float = viewportSize.x.toFloat() / viewportSize.y

	val descriptorAllocator = GrowableVulkanDescriptorAllocator(device)
	val submitter = VulkanImmediateSubmitter(device)
	lateinit var image: VulkanImage2D
	lateinit var depthImage: VulkanImage2D
	val api = VulkanRendering.create(submitter.immediateCommands, descriptorAllocator, ::getViewport)
	private var clearColour = Vec4(0f)

	init {
		generateImages(viewportSize)
	}

	override fun init() {
		val ratios = mapOf(
			VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE to 3f,
			VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER to 3f,
			VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER to 3f,
			VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER to 4f,
		)
		descriptorAllocator.init(1000, ratios)
	}

	override fun setSize(size: Vec2i) {
		image.delete()
		depthImage.delete()
		generateImages(size)
	}

	fun generateImages(size: Vec2i, skipDepth: Boolean = false) {
		image = VkUtil.createImage(
			device,
			"Object Renderer Colour Image",
			TextureFormat.RGBA8,
			VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT or VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT or VK10.VK_IMAGE_USAGE_SAMPLED_BIT,
			VK10.VK_IMAGE_ASPECT_COLOR_BIT,
			size
		)
		if (skipDepth) return
		depthImage = VkUtil.createImage(
			device,
			"Object Renderer Depth Image",
			TextureFormat.DEPTH24_STENCIL8,
			VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
			VK10.VK_IMAGE_ASPECT_DEPTH_BIT or VK10.VK_IMAGE_ASPECT_STENCIL_BIT,
			size
		)
	}

	override fun render(obj: GameObject) {

		descriptorAllocator.clearPools()

		submitter.submitImmediate { cmd ->
			image.transition(cmd, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, false)
			depthImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, false)

			MemoryStack.stackPush().use { stack ->
				val colourAttachments = VkStructs.createColourAttachmentInfos(stack, image, clearColour)
				val depthAttachment = VkStructs.createDepthStencilAttachmentInfo(stack, depthImage, 0f, 0)
				val info = VkStructs.createRenderingInfo(stack, image.size, colourAttachments, depthAttachment)
				cmd.beginRendering(info)

				val viewport = getViewport()
				api.setViewport(viewport)
				api.setScissors(viewport)

				val des = obj.catchRenderingComponents().flatMap { obj -> obj.components.filterIsInstance<RenderedComponentI>().filter { it.visible } }
					.sortedBy { it.parent.transformComponent.worldPosition.z }
				for (i in des) {
					i.render(this, 0.0)
				}

				cmd.endRendering()
			}

			image.transition(cmd, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, false)
		}
	}

	override fun getTexture(id: String): Texture2D {
		val returnImage = image
		generateImages(viewportSize, true)
		return returnImage
	}

	override fun getRenderingApi(): RenderingApi = api

	override fun setClearColour(colour: Vec4) {
		clearColour = colour
	}

	override fun delete() {
		image.delete()
		depthImage.delete()
		descriptorAllocator.delete()
		submitter.delete()
	}
}