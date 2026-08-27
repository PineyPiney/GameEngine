// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec2 texCoords;

uniform vec4 backgroundColour;
uniform sampler2D tex;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 1) in vec2 texCoords;

layout(set = 1, binding = 0) uniform sampler2D tex;
layout(push_constant) uniform Colour {
	layout(offset = 80) vec4 backgroundColour;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	vec4 colour = texture(tex, texCoords);
	FragColour = (colour * colour.a) + (backgroundColour * (1.0 - colour.a));
}