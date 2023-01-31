// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform float red;
uniform float blue;

out vec4 FragColour;

void main(){
	FragColour = vec4(red, texCoords.x, blue, 1.0);
}