// FRAGMENT SHADER INFORMATION
#version 400 core

struct Material{
	sampler2D ambient;
	sampler2D diffuse;
	sampler2D specular;
	sampler2D normals;
	uint textureMask;
	bool ambDiff;
	float shininess;
	float alpha;
};

struct Light{
	vec3 position;

	vec3 ambient;
	vec3 diffuse;
	vec3 specular;
};

in vec3 fragPos;
in vec2 texCoords;
in vec3 normal;

uniform vec3 viewPos;
uniform Material material;
uniform Light light;

out vec4 FragColour;

bool hasTexture(uint texture);

void main(){

	//ambient
	vec3 ambient = light.ambient;
	if(hasTexture(1u)) ambient *= texture(material.ambient, texCoords).xyz;
	else if(material.ambDiff && hasTexture(2u)) ambient *= texture(material.diffuse, texCoords);

	// diffuse
	vec3 norm = hasTexture(8u) ? texture(material.normals, texCoords).xyz : normal;
	vec3 lightDir = normalize(light.position - fragPos);
	float diff = max(dot(norm, lightDir), 0.0);
	vec3 diffuse = light.diffuse * diff;
	if(hasTexture(2u)) diffuse *= texture(material.diffuse, texCoords).xyz;

	//specular
	vec3 viewDir = normalize(viewPos - fragPos);
	vec3 reflectDir = reflect(-lightDir, norm);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
	vec3 specular = light.specular * spec;
	if(hasTexture(4u)) specular *= texture(material.specular, texCoords).xyz;

	vec3 result = ambient + diffuse + specular;
	FragColour = vec4(result, texture(material.diffuse, texCoords).a * material.alpha);
}

bool hasTexture(uint texture){
	return (material.textureMask & texture) == texture;
}