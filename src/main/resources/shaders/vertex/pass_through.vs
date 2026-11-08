// VERTEX SHADER INFORMATION
#version 430 core

layout (location = 0) in vec3 posIn;
layout (location = 1) in vec2 texIn;

#ifdef OPENGL
out vec2 texCoords;
#endif

#ifdef VULKAN
layout (location = 0) out vec2 texCoords;
#endif

void main(){
	gl_Position = vec4(posIn, 1.0);
	texCoords = texIn;
}