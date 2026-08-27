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
	FragColour = texture(tex, texCoords);
}