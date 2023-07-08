// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec2 aPos;

uniform mat4 model;
uniform mat4 projection;

out vec2 texCoords;

void main(){
	gl_Position = projection * model * vec4(aPos, -1.0, 1.0);
	texCoords = aPos;
}