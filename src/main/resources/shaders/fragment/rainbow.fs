// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 pos;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec2 pos;

layout(location = 0) out vec4 FragColour;
#endif


void main(){
	FragColour = vec4(pos, 1.0, 1.0);
}