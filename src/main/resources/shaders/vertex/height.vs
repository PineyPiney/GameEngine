// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec3 aPos;
layout (location = 2) in vec2 aTexCoord;

uniform mat4 model;

out vec2 texCoords;
out mat4 Fmodel;

void main(){
	Fmodel = model;
	texCoords = aTexCoord;
	gl_Position = model * vec4(aPos, 1.0);
}