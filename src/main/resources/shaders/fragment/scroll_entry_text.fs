// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform mat4 model;
uniform sampler2D ourTexture;
uniform vec4 colour;
uniform vec2 limits;

out vec4 FragColour;

void main(){

	float y = gl_FragCoord.y;

	if(y < limits[0] || y > limits[1]) discard;

    vec4 texture = texture(ourTexture, texCoords);
    if(texture.r < 0.02) discard;
    else FragColour = vec4(colour.r, colour.g, colour.b, colour.a * texture.r);
}