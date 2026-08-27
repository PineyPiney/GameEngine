package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.rendering.ObjectRenderer
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.vulkan.GrowableVulkanDescriptorAllocator
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanDevice
import com.pineypiney.game_engine.vulkan.VulkanImmediateSubmitter
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.vulkan.VK10

class VulkanObjectRenderer(override val viewPos: Vec3, val device: VulkanDevice, viewportSize: Vec2i = Vec2i(64), override val projection: Mat4 = I) : ObjectRenderer {

	override val viewportSize: Vec2i get() = image.size
	override val view: Mat4 = I.translate(viewPos)
	override val guiProjection: Mat4 = projection
	override val aspectRatio: Float = viewportSize.x.toFloat() / viewportSize.y

	val descriptorAllocator = GrowableVulkanDescriptorAllocator(device)
	val submitter = VulkanImmediateSubmitter(device)
	var image = VkUtil.createImage(
		device,
		"Object Renderer Image",
		TextureFormat.RGBA8,
		VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT or VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
		VK10.VK_IMAGE_ASPECT_COLOR_BIT,
		viewportSize
	)
	var depthImage = VkUtil.createImage(
		device,
		"Object Renderer Image",
		TextureFormat.RGBA8,
		VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT or VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
		VK10.VK_IMAGE_ASPECT_COLOR_BIT,
		viewportSize
	)
	val api = VulkanRendering(submitter.immediateCommands, descriptorAllocator, ::image, ::depthImage)
	private var clearColour = Vec4(0f)

	override fun init() {

	}

	override fun setSize(size: Vec2i) {
		image.delete()
		depthImage.delete()
	}

	override fun render(obj: GameObject) {
		submitter.submitImmediate { cmd ->
			image.transition(cmd, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
			depthImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)

			clear()
			val des =
				obj.allActiveDescendants().flatMap { obj -> obj.components.filterIsInstance<RenderedComponentI>().filter { it.visible } }.sortedBy { it.parent.transformComponent.worldPosition.z }
			for (i in des) {
				i.render(this, 0.0)
			}
		}
	}

	override fun getTexture(id: String): Texture2D {
		return image
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