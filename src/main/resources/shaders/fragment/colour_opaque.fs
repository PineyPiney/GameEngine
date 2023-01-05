// FRAGMENT SHADER INFORMATION
#version 400 core

uniform vec3 colour;

out vec4 FragColour;

void main(){
	FragColour = vec4(colour, 1.0);
}