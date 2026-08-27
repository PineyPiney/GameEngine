// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 pos;

uniform vec4 xColour;
uniform vec4 yColour;
uniform vec4 zColour;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec2 pos;

layout(push_constant) uniform Data {
	vec4 xColour;
	vec4 yColour;
	vec4 zColour;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	float rad2 = (pos.x * pos.x + pos.y * pos.y);
	if(abs(rad2 - .23) < .02) FragColour = zColour;
	else if(abs(pos.x) < .02) FragColour = xColour;
	else if(abs(pos.y) < .02) FragColour = yColour;
	else FragColour = vec4(0);
}