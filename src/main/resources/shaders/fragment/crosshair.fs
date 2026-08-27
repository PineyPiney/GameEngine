// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 texCoords;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 1) in vec2 texCoords;

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	if(abs(texCoords.x) > 0.015 && abs(texCoords.y) > 0.015) discard;
	else FragColour = vec4(0.7, 0.7, 0.7, 0.9);
}