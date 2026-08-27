// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
uniform vec4 colour;

out vec4 FragColour;
#endif


#ifdef VULKAN

layout(push_constant) uniform Data {
	layout(offset = 80) vec4 colour;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	FragColour = colour;
}