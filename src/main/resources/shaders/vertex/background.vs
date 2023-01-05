// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec3 aPos;
layout (location = 2) in vec2 aTexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec2 pos;

void main(){
	gl_Position = projection * view * model * vec4(aPos, 1.0);
	pos = vec2(aPos) + vec2(0.5, 0.5);
}