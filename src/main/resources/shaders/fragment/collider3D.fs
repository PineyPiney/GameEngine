// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec3 pos;

uniform vec4 colour;
uniform mat4 model;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec3 pos;

layout(push_constant) uniform Data {
	layout(offset = 80) mat4 model;
	vec4 colour;
};

layout(location = 0) out vec4 FragColour;
#endif


void main(){
	float sizeX = model[0][0];
	float sizeY = model[1][1];
	float sizeZ = model[2][2];

	int i = 0;
	if(abs(pos.x) > .5 - (.05 / sizeX)) i++;
	if(abs(pos.y) > .5 - (.05 / sizeY)) i++;
	if(abs(pos.z) > .5 - (.05 / sizeZ)) i++;

	if(i >= 2) FragColour = vec4(vec3(1.0) - pos, 1.0);
	else discard;
}