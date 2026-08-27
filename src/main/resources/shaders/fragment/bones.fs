// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 pos;

uniform vec4 colour;
uniform mat4 model;

out vec4 FragColour;
#endif

#ifdef VULKAN
layout(location = 0) in vec2 pos;

layout(push_constant) uniform Data {
	layout(offset = 80) mat4 model;
	vec4 colour;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	float sizeX = model[0][0];
	float sizeY = model[1][1];
	float ratio = sizeX / sizeY;

	if((0.5 - abs(pos.x)) < 0.1 || (0.5 - abs(pos.y)) < (0.1 * ratio)) FragColour = vec4(0.0, 0.0, 0.0, 1.0);
	else FragColour = colour;
}