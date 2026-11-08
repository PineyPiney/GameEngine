// VERTEX SHADER INFORMATION
#version 430 core

layout (location = 0) in vec2 posIn;

#ifdef OPENGL

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec2 pos;
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

layout (location = 0) out vec2 pos;
#endif

void main(){
	gl_Position = projection * view * model * vec4(posIn, 0.0, 1.0);
	pos = vec2(posIn);
}