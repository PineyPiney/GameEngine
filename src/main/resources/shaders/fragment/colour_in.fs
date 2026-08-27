// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec4 colour;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec4 colour;

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	FragColour = colour;
}