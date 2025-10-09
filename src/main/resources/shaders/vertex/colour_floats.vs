// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColour;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec4 colour;

const float _255 = 0.00392156862;

void main(){
	gl_Position = projection * view * model * vec4(aPos, 1.0);
	colour = aColour;
}