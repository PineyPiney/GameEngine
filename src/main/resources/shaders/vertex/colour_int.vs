// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in int aColour;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec4 colour;

const float _255 = 0.00392156862;

void main(){
	gl_Position = projection * view * model * vec4(aPos, 1.0);
	colour = vec4(float(aColour >> 24 & 255) * _255, float(aColour >> 16 & 255) * _255, float(aColour >> 8 & 255) * _255, float(aColour & 255) * _255);
}