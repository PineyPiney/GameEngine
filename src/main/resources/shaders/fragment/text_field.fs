// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform sampler2D ourTexture;
uniform vec4 colour;

// The horizontal edges of the field
// Ranges from -1 to 1
uniform vec2 limits;
uniform ivec2 viewport;

out vec4 FragColour;

void main(){

	float x = (gl_FragCoord.x * 2.0 / viewport.x) - 1.0;

	if(x < limits[0] || x > limits[1]) discard;

	vec4 texture = texture(ourTexture, texCoords);
	if(texture.r + texture.g + texture.b < 0.05) discard;
	else FragColour = vec4(colour.r, colour.g, colour.b, colour.a * texture.r);
}