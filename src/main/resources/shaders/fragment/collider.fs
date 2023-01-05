// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 pos;

uniform vec4 colour;
uniform mat4 model;

out vec4 FragColour;

void main(){
	float sizeX = model[0][0];
	float sizeY = model[1][1];
	float ratio = abs(sizeX / sizeY);

	if(abs(0.5 - pos.x) > 0.45 || abs(0.5 - pos.y) > (0.5 - (0.05 * ratio))) FragColour = colour;
	else discard;
}