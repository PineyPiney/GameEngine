// FRAGMENT SHADER INFORMATION
#version 400 core

const float offset = 1.0 / 300.0;
const vec2 offsets[9] = vec2[](
	vec2(-offset,  offset), // top-left
	vec2( 0.0f,    offset), // top-center
	vec2( offset,  offset), // top-right
	vec2(-offset,  0.0f),   // center-left
	vec2( 0.0f,    0.0f),   // center-center
	vec2( offset,  0.0f),   // center-right
	vec2(-offset, -offset), // bottom-left
	vec2( 0.0f,   -offset), // bottom-center
	vec2( offset, -offset)  // bottom-right
);

const int EDGE_DETECTION = 1;
const int GREY_SCALE = 2;
const int INVERT = 4;

in vec2 texCoords;

uniform sampler2D screenTexture;
uniform int effects;

out vec4 FragColour;

void main(){
	vec4 c = texture(screenTexture, texCoords);

	if((effects & EDGE_DETECTION) > 0){
		float kernel[9] = float[](
			-1, -1, -1,
			-1,  9, -1,
			-1, -1, -1
		);

		vec3 sampleTex[9];
		for(int i = 0; i < 9; i++){
			sampleTex[i] = vec3(texture(screenTexture, texCoords.st + offsets[i]));
		}
		vec3 col = vec3(0.0);
		for(int i = 0; i < 9; i++) col += sampleTex[i] * kernel[i];

		c = vec4(col, 1.0);
	}
	if((effects & GREY_SCALE) > 0){
		float average = 0.2126 * c.r + 0.7152 * c.g + 0.0722 * c.b;
		c = vec4(average, average, average, 1.0);
	}
	if((effects & INVERT) > 0){
		c = vec4(vec3(1.0 - c), 1.0);
	}

	FragColour = c;

}