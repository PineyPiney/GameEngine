// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 pos;

uniform ivec2 viewport;
uniform mat4 model;

uniform vec4 colour;
uniform float outlineThickness;
uniform vec4 outlineColour;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec2 pos;

layout(push_constant) uniform Data {
	mat4 model;
	layout(offset = 80) vec4 colour;
	vec4 outlineColour;
	ivec2 viewport;
	float outlineThickness;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	float sizeX = model[0][0];
	float sizeY = model[1][1];
	float ratio = (viewport.y * sizeY) / (viewport.x * sizeX);

	if((abs(0.5 - pos.x)) > 0.5 - (outlineThickness * ratio) || abs(0.5 - pos.y) > 0.5 - outlineThickness) FragColour = outlineColour;
	else FragColour = colour;
}