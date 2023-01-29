#version 400 core

out vec4 FragColour;

in vec2 texCoords;

uniform sampler2D ourTexture;
uniform vec4 colour;

void main(){
    vec4 texture = texture(ourTexture, texCoords);
    if(texture.r < 0.02) discard;
    else FragColour = vec4(colour.r, colour.g, colour.b, colour.a * texture.r);
}
