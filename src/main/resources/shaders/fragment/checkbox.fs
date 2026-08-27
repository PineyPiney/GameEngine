// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 texCoords;

uniform bool ticked;
uniform vec4 colour;

out vec4 FragColour;
#endif

#ifdef VULKAN
layout(location = 1) in vec2 texCoords;

layout(push_constant) uniform Data {
	layout(offset = 80) vec4 colour;
	bool ticked;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	float x = abs(0.5 - texCoords.x);
	float y = abs(0.5 - texCoords.y);
	if(x > 0.45 || y > 0.45){
		FragColour = vec4(vec3(0.8), 1.0);
	}
	else{
		bool inCross = abs(x - y) < 0.1;
		FragColour = (ticked && inCross) ? colour : vec4(vec3(0.5), 1.0);
	}

}