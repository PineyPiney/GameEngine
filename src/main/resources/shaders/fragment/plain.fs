// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
out vec4 FragColour;
#endif

#ifdef VULKAN
layout(location = 0) out vec4 FragColour;
#endif

void main(){
	FragColour = vec4(0.0, 1.0, 1.0, 1.0);
}