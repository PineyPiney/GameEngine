// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 pos;

uniform mat4 model;
uniform vec4 colour;
uniform vec4 outlineColour;
uniform float width;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec2 pos;

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

	if ((.5 - abs(pos.x)) * sizeX < width || (.5 - abs(pos.y)) * sizeY < width) FragColour = outlineColour;
	else if (colour.a > 0) FragColour = colour;
	else discard;
}