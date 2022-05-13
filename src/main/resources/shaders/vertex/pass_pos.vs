// VERTEX SHADER INFORMATION
#version 460 core
layout (location = 0) in vec3 aPos;
layout (location = 2) in vec2 aTexCoord;

uniform mat4 model;
uniform mat4 vp;

out vec2 pos;

void main(){
	gl_Position = vp * model * vec4(aPos, 1.0);
	pos = vec2(aPos);
}