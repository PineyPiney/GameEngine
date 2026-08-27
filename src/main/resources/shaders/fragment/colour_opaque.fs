// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
uniform vec3 colour;

out vec4 FragColour;
#endif


#ifdef VULKAN

layout(push_constant) uniform Data {
	layout(offset = 80) vec3 colour;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	FragColour = vec4(colour, 1.0);
}