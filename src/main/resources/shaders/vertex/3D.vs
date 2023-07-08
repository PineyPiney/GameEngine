// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 pos;
out vec3 normal;
out vec2 texCoords;

void main(){
	gl_Position = projection * view * model * vec4(aPos, 1.0);

	pos = aPos;
	normal = aNormal;
	texCoords = aTexCoord;
}