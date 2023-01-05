// FRAGMENT SHADER INFORMATION
#version 400 core

uniform vec2 points[3];

uniform vec2 windowSize;
uniform vec4 colour;
uniform float width;

out vec4 FragColour;

bool drawPoints();
vec3 solveQuad(float p0, float p1, float p2, float pos);
bool getRoots(float a, float b, float c, out vec2 res);
bool isInRange(float t);
vec2 getValue(float t);
float length2(vec2 vec);
bool measure(float t, vec2 pos);

vec2 p0 = points[0];
vec2 p1 = points[1];
vec2 p2 = points[2];

vec2 pos = (gl_FragCoord.xy / windowSize) - vec2(1);
float aspect = windowSize.x / windowSize.y;

void main(){

	if(drawPoints()) return;
	// https://en.wikipedia.org/wiki/B%C3%A9zier_curve
	// pos = 	(1-t)^2 * p0 + 2(1-t)t * p1 + t^2 * p2
	// (0, 0) = (p0 - 2*p1 + p2) * t^2 + (p1 - p0) * t + (p0 - pos)

	bool x = true;
	bool y = true;

	vec3 sols = solveQuad(p0.x, p1.x, p2.x, pos.x);
	float a = sols.x;
	float b = sols.y;
	float c = sols.z;

	vec2 roots = vec2(0);
	if(!getRoots(a, b, c, roots)) x = false;

	sols = solveQuad(p0.y, p1.y, p2.y, pos.y);
	a = sols.x;
	b = sols.y;
	c = sols.z;

	vec2 roots2 = vec2(0);
	if(!getRoots(a, b, c, roots2)) y = false;

	if(!(x || y)) discard;

	if(measure(roots.x, pos)) return;
	else if(measure(roots.y, pos)) return;
	else if(measure(roots2.x, pos)) return;
	else if(measure(roots2.y, pos)) return;
	else discard;
}

bool drawPoints(){
	vec2[] points = vec2[](p0, p1, p2);
	for(uint i = 0u; i < 3u; i++){
		if(length2(pos - points[i]) < (width * 0.6)){
			FragColour = vec4(0, 1, 0, 1);
			return true;
		}
	}
	return false;
}

vec3 solveQuad(float p0, float p1, float p2, float pos){
	float a = p0 - 2 * p1 + p2;
	float b = 2 * (p1 - p0);
	float c = p0 - pos;

	return vec3(a, b, c);
}

// 'out' is used to effectively make res passed by reference rather than value
// https://stackoverflow.com/questions/13633586/can-you-pass-a-matrix-by-reference-in-a-glsl-shader
bool getRoots(float a, float b, float c, out vec2 res){
	float r = b*b - 4 * a * c;

	// Unsolvable
	if(r < 0 || a == 0) return false;

	float root = sqrt(r);
	res.x = (-b + root) / (2*a);
	res.y = (-b - root) / (2*a);
	return true;
}

bool isInRange(float t){
	return 0 <= t && t <= 1;
}

vec2 getValue(float t){
	return p1 + (pow(1 - t, 2) * (p0 - p1)) + (pow(t, 2) * (p2 - p1));
}

float length2(vec2 vec){
	vec2 prop = vec2(vec.x * aspect, vec.y);
	return (prop.x * prop.x) + (prop.y * prop.y);
}

bool measure(float t, vec2 pos){
	if(isInRange(t)){
		vec2 point1 = getValue(t);
		if(length2(point1 - pos) < width){
			FragColour = colour;
			return true;
		}
	}
	return false;
}