// FRAGMENT SHADER INFORMATION
#version 400 core

uniform vec4 colour;

out vec4 FragColour;

void main(){
	FragColour = colour;
}