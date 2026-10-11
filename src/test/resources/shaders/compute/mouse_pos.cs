#version 460 core

layout (local_size_x = 16, local_size_y = 16) in;

// MANUAL
layout(rgba16f, set = 0, binding = 0) uniform image2D image;

#ifdef VULKAN
layout(push_constant) uniform constants
{
	ivec2 mousePos;
};
#endif
#ifdef OPENGL
layout (location = 0) uniform ivec2 mousePos;
#endif

void main() {
	ivec2 texelCoord = ivec2(gl_GlobalInvocationID.xy);
	ivec2 size = imageSize(image);

	if (texelCoord.x < size.x && texelCoord.y < size.y){

		float dist = length(vec2(mousePos - texelCoord));
		float l = dist < 32 ? 1.0 : (dist > 64 ? 0.0 : ((64.0 - dist) / 32.0));
		vec4 color = vec4(l, l, l, 1.0);

		imageStore(image, texelCoord, color);
	}
}
