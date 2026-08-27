// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 texCoords;

uniform float red;
uniform float green;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 1) in vec2 texCoords;

layout(push_constant) uniform Data {
	layout(offset = 80) float red;
	float green;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	FragColour = vec4(red, green, texCoords.x, 1.0);
}