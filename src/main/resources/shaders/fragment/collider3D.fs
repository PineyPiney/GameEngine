// FRAGMENT SHADER INFORMATION
#version 400 core

in vec3 pos;

uniform vec4 colour;
uniform mat4 model;

out vec4 FragColour;

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