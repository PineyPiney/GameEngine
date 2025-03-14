// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 pos;

uniform ivec2 viewport;
uniform mat4 model;

uniform vec4 colour;
uniform float outlineThickness;
uniform vec4 outlineColour;

out vec4 FragColour;

void main(){
	float sizeX = model[0][0];
	float sizeY = model[1][1];
	float ratio = (viewport.y * sizeY) / (viewport.x * sizeX);

	if((abs(0.5 - pos.x)) > 0.5 - (outlineThickness * ratio) || abs(0.5 - pos.y) > 0.5 - outlineThickness) FragColour = outlineColour;
	else FragColour = colour;
}