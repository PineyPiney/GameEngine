// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 model;
uniform mat4 guiProjection;

out vec2 texCoords;

void main(){
	gl_Position = guiProjection * model * vec4(aPos, 0.0, 1.0);
	texCoords = aTexCoord;
}