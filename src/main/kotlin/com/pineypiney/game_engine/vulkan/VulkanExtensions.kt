package com.pineypiney.game_engine.vulkan

import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import org.lwjgl.vulkan.VkExtent2D
import org.lwjgl.vulkan.VkExtent3D
import org.lwjgl.vulkan.VkOffset2D
import org.lwjgl.vulkan.VkOffset3D

fun VkExtent2D.set(size: Vec2i) = set(size.x, size.y)
fun VkOffset2D.set(size: Vec2i) = set(size.x, size.y)
fun VkExtent3D.set(size: Vec3i) = set(size.x, size.y, size.z)
fun VkOffset3D.set(size: Vec3i) = set(size.x, size.y, size.z)