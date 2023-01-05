// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform sampler2D ourTexture;

out vec4 FragColour;

void main(){
	if(abs(0.5 - texCoords.x) > 0.48 || abs(0.5 - texCoords.y) > 0.48){
		FragColour = vec4(0.0);
	}
	else{
		vec4 colour = texture(ourTexture, texCoords);
		if(colour.a == 0) discard;
		FragColour = colour;
	}

}