#version 430 core

#ifdef OPENGL
in vec2 texCoords;

uniform sampler2D tex;
uniform vec4 colour;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 1) in vec2 texCoords;

layout(set = 1, binding = 0) uniform sampler2D tex;
layout(push_constant) uniform Data {
	layout(offset = 80) vec4 colour;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	vec4 texture = texture(tex, texCoords);
    if(texture.r < 0.02) discard;
    else FragColour = vec4(colour.r, colour.g, colour.b, colour.a * texture.r);
}
