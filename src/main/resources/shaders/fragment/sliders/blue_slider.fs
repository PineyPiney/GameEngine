// FRAGMENT SHADER INFORMATION
#version 460 core

const float WIDTH = 0.02;

in vec2 pos;

uniform float red;
uniform float green;

uniform mat4 model;

out vec4 FragColour;

void main(){
	float sizeX = model[0][0];
	float sizeY = model[1][1];
	float ratio = sizeX / sizeY;

	if((abs(0.5 - pos.x)) > 0.5 - WIDTH || abs(0.5 - pos.y) > 0.5 - (WIDTH * ratio)) FragColour = vec4(0.0, 0.0, 0.0, 1.0);
	else FragColour = vec4(red, green, pos.x, 1.0);
}