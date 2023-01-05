// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec2 pos;

void main(){
	gl_Position = projection * view * model * vec4(aPos, 0.0, 1.0);
	pos = vec2(aPos);
}