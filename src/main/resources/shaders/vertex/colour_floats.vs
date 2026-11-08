// VERTEX SHADER INFORMATION
#version 430 core

layout (location = 0) in vec3 posIn;
layout (location = 1) in vec4 colourIn;

#ifdef OPENGL

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec4 colour;
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

layout (location = 0) out vec4 colour;
#endif

const float _255 = 0.00392156862;

void main(){
	gl_Position = projection * view * model * vec4(posIn, 1.0);
	colour = colourIn;
}