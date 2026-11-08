// VERTEX SHADER INFORMATION
#version 430 core

layout (location = 0) in vec2 posIn;
layout (location = 1) in vec2 texIn;

#ifdef OPENGL

uniform mat4 model;
uniform mat4 guiProjection;

out vec2 texCoords;
#endif

#ifdef VULKAN

layout(binding = 0, set = 0) uniform Matrices {
	mat4 guiProjection;
};

//push constants block
layout(push_constant) uniform constants
{
	mat4 model;
};

layout (location = 1) out vec2 texCoords;
#endif


void main(){
	gl_Position = guiProjection * model * vec4(posIn, 0.0, 1.0);
	texCoords = texIn;
}