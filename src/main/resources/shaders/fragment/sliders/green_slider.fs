// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 texCoords;

uniform float red;
uniform float blue;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 1) in vec2 texCoords;

layout(push_constant) uniform Data {
	layout(offset = 80) float red;
	float blue;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	FragColour = vec4(red, texCoords.x, blue, 1.0);
}