#version 430 core

layout (local_size_x = 32, local_size_y = 32, local_size_z = 1) in;

layout(rgba8ui, binding = 0) uniform uimage2D imgOutput;

layout (location = 0) uniform float time;
layout (location = 1) uniform float speed;

void main() {
	uvec4 value = uvec4(0, 0, 0, 255);
	ivec2 texelCoord = ivec2(gl_GlobalInvocationID.xy);

	// the width of the texture
	float width = 1024;

	value.x = abs(int(300.0 * (mod(float(texelCoord.x) + time * speed, width) / (gl_NumWorkGroups.x * gl_WorkGroupSize.x))) - 150) + 50;
	value.y = abs(int(300.0 * (mod(float(texelCoord.y) + .5 + time * speed, width) / (gl_NumWorkGroups.y * gl_WorkGroupSize.y))) - 150) + 50;
	imageStore(imgOutput, texelCoord, value);
}
