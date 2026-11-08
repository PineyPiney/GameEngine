// VERTEX SHADER INFORMATION
#version 430 core

layout (location = 0) in vec3 posIn;

#ifdef OPENGL

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 pos;
#endif

#ifdef VULKAN

layout(binding = 0, set = 0) uniform Matrices {
	mat4 view;
	mat4 projection;
};

//push constants block
layout(push_constant) uniform constants
{
	mat4 model;
};

layout (location = 0) out vec3 pos;
#endif

void main(){
	gl_Position = projection * view * model * vec4(posIn, 1.0);
	pos = posIn;
}