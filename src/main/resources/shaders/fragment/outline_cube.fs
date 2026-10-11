// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec3 pos;

uniform mat4 model;
uniform vec4 colour;
uniform vec4 outlineColour;
uniform float width;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec3 pos;

layout(push_constant) uniform Data {
	mat4 model;
	layout(offset = 80) vec4 colour;
	vec4 outlineColour;
	float width;
};

layout(location = 0) out vec4 FragColour;
#endif


void main(){
	float sizeX = model[0][0];
	float sizeY = model[1][1];
	float sizeZ = model[2][2];

	int i = 0;
	if ((.5 - abs(pos.x)) * sizeX < width) i++;
	if ((.5 - abs(pos.y)) * sizeY < width) i++;
	if ((.5 - abs(pos.z)) * sizeZ < width) i++;

	if (i >= 2) FragColour = vec4(vec3(.5) + pos, 1.0);
	else if (colour.a > 0) FragColour = colour;
	else discard;
}