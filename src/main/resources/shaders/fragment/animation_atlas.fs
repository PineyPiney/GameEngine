// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 texCoords;

uniform sampler2D tex;
uniform float origin;

out vec4 FragColour;
#endif

#ifdef VULKAN
layout(location = 1) in vec2 texCoords;

layout(set = 1, binding = 0) uniform sampler2D tex;
layout(push_constant) uniform Origin {
	layout(offset = 80) float origin;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	vec4 texColour = texture(tex, vec2(texCoords.x + origin, texCoords.y));
	if(texColour.a <= 0.2) discard;
	FragColour = texColour;
}