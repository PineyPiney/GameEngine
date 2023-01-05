// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec2 aPos;

uniform mat4 model;

out vec2 pos;

void main(){
	gl_Position = model * vec4(aPos, 0.0, 1.0);
	pos = aPos;
}