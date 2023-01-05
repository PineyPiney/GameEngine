// FRAGMENT SHADER INFORMATION
#version 400 core

const float offset = 1.0 / 1536.0;
const vec2 offsets[25] = vec2[](
	vec2(-2 * offset, 	-2 * offset), // bottom-left
	vec2(-offset, 		-2 * offset), // bottom-left
	vec2( 0.0f,   		-2 * offset), // bottom-center
	vec2( offset, 		-2 * offset),  // bottom-right
	vec2(-2 * offset, 	-2 * offset), // bottom-left

	vec2(-2 * offset, 	-offset), // bottom-left
	vec2(-offset, 		-offset), // bottom-left
	vec2( 0.0f,   		-offset), // bottom-center
	vec2( offset, 		-offset),  // bottom-right
	vec2(-2 * offset, 	-offset), // bottom-left

	vec2(-2 * offset, 	0.0), // bottom-left
	vec2(-offset, 		0.0), // bottom-left
	vec2( 0.0f,   		0.0), // bottom-center
	vec2( offset, 		0.0),  // bottom-right
	vec2(-2 * offset, 	0.0), // bottom-left

	vec2(-2 * offset, 	offset), // bottom-left
	vec2(-offset, 		offset), // bottom-left
	vec2( 0.0f,   		offset), // bottom-center
	vec2( offset, 		offset),  // bottom-right
	vec2(-2 * offset, 	offset), // bottom-left

	vec2(-2 * offset, 	2 * offset), // bottom-left
	vec2(-offset, 		2 * offset), // bottom-left
	vec2( 0.0f,   		2 * offset), // bottom-center
	vec2( offset, 		2 * offset),  // bottom-right
	vec2(-2 * offset, 	2 * offset) // bottom-left

);

in vec2 texCoords;

uniform sampler2D ourTexture;
uniform sampler2D background;
uniform float translucency;

out vec4 FragColour;

void main(){
	float total = 100.0;
	float kernel[25] = float[](
		1.0 / total, 2.0 / total, 4.0 / total, 2.0 / total, 1.0 / total,
		2.0 / total, 4.0 / total, 8.0 / total, 4.0 / total, 2.0 / total,
		4.0 / total, 8.0 / total, 16.0 /total, 8.0 / total, 4.0 /total,
		2.0 / total, 4.0 / total, 8.0 / total, 4.0 / total, 2.0 / total,
		1.0 / total, 2.0 / total, 4.0 / total, 2.0 / total, 1.0 / total
	);

	vec3 sampleTex[25];
	for(int i = 0; i < 25; i++)
	{
		sampleTex[i] = vec3(texture(background, texCoords.st + offsets[i]));
	}
	vec3 col = vec3(0.0);
	for(int i = 0; i < 25; i++)
	col += sampleTex[i] * kernel[i];

	FragColour = vec4(col, 1.0);
}