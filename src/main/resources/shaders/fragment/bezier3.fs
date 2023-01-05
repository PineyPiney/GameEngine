// FRAGMENT SHADER INFORMATION
#version 400 core

uniform vec2 points[4];
uniform vec2 windowSize;

uniform vec4 colour;
uniform float width;

out vec4 FragColour;

bool drawPoints();
vec4 solveQuad(float p0, float p1, float p2, float p3, float pos);
bool getRoots(float a, float b, float c, float d, out vec3 res);
bool isInRange(float t);
vec2 getValue(float t);
float length2(vec2 vec);
bool measure(float t, vec2 pos);

vec2 p0 = points[0];
vec2 p1 = points[1];
vec2 p2 = points[2];
vec2 p3 = points[3];

vec2 pos = (gl_FragCoord.xy / windowSize) - vec2(1);
float aspect = windowSize.x / windowSize.y;

void main(){

	if(drawPoints()) return;
	// https://en.wikipedia.org/wiki/B%C3%A9zier_curve
	// pos = 	(1-t)^3 * p0 + 3(1-t)^2 * t * p1 + 3(1-t)t^2 * p2 + t^3 * p3
	// (0, 0) = (-p0 + 3p1 - 3p2 + p3) * t^3 +
	//			(3*p0 - 6*p1 + 3*p2) * t^2 +
	//			(3*p0 - 3*p1) * t +
	//			(p0 - pos)

	bool x = true;
	bool y = true;

	vec4 sols = solveQuad(p0.x, p1.x, p2.x, p3.x, pos.x);
	float a = sols.x;
	float b = sols.y;
	float c = sols.z;
	float d = sols.w;

	vec3 roots = vec3(0);
	if(!getRoots(a, b, c, d, roots)) x = false;

	sols = solveQuad(p0.y, p1.y, p2.y, p3.y, pos.y);
	a = sols.x;
	b = sols.y;
	c = sols.z;
	d = sols.w;

	vec3 roots2 = vec3(0);
	if(!getRoots(a, b, c, d, roots2)) y = false;

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

vec4 solveQuad(float p0, float p1, float p2, float p3, float pos){
	float a = -p0 + 3 * p1 - 3 * p2 + p3;
	float b = 3 * (p0 - 2*p1 + p2);
	float c = 3 * (p0 - p1);
	float d = p0 - pos;

	return vec4(a, b, c, d);
}

// 'out' is used to effectively make res passed by reference rather than value
// https://stackoverflow.com/questions/13633586/can-you-pass-a-matrix-by-reference-in-a-glsl-shader
bool getRoots(float a, float b, float c, float d, out vec3 res){
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