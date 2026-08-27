// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 texCoords;

uniform sampler2D tex;

out vec4 FragColour;
#endif

#ifdef VULKAN
layout(location = 1) in vec2 texCoords;

layout(set = 1, binding = 0) uniform sampler2D tex;

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	if(abs(0.5 - texCoords.x) > 0.48 || abs(0.5 - texCoords.y) > 0.48){
		FragColour = vec4(0.0);
	}
	else{
		vec4 colour = texture(tex, texCoords);
		if(colour.a == 0) discard;
		FragColour = colour;
	}

}