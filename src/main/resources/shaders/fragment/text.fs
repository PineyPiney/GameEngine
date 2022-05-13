#version 460 core

out vec4 FragColour;

in vec2 texCoords;

uniform sampler2D ourTexture;
uniform vec4 colour;

void main(){
    vec4 texture = texture(ourTexture, texCoords);
    if(texture.r + texture.g + texture.b < 0.05) discard;

    FragColour = vec4(texture.r * colour.r, texture.g * colour.g, texture.b * colour.b, texture.a * colour.a);
}
