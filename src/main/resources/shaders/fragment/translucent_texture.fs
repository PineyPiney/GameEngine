// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 texCoords;

uniform sampler2D tex;
uniform float alpha;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 1) in vec2 texCoords;

layout(set = 1, binding = 0) uniform sampler2D tex;

layout(push_constant) uniform Data {
	float alpha;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	vec4 colour = texture(tex, texCoords);
	FragColour = vec4(vec3(colour), colour.a * alpha);
}