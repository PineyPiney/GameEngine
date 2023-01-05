// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 pos;

out vec4 FragColour;

void main(){
	FragColour = vec4(pos, 1.0, 1.0);
}