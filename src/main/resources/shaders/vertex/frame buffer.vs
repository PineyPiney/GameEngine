// VERTEX SHADER INFORMATION
#version 460 core
layout (location = 0) in vec3 aPos;
layout (location = 2) in vec2 aTexCoords;

out vec2 texCoords;

void main(){
	gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
	texCoords = aTexCoords;
}