// FRAGMENT SHADER INFORMATION
#version 400 core

uniform vec2 points[2];
uniform vec2 windowSize;

uniform vec4 colour;
uniform float width;

out vec4 FragColour;

float length2(vec2 vec);
bool drawPoints();
vec2 solveLinear(float p0, float p1, float pos);
bool getRoot(float a, float b, out float res);
bool isInRange(float t);
vec2 getValue(float t);
bool measure(float t);

vec2 p0 = points[0];
vec2 p1 = points[1];

vec2 pos = (gl_FragCoord.xy / windowSize) - vec2(1);
float aspect = windowSize.x / windowSize.y;

void main(){

	if(drawPoints()) return;
	// https://en.wikipedia.org/wiki/B%C3%A9zier_curve
	// pos = 	(1-t) * p0 + t * p1
	// (0, 0) = (p1 - p0) * t + (p0 - pos)

	bool x = true;
	bool y = true;

	vec2 sols = solveLinear(p0.x, p1.x, pos.x);
	float a = sols.x;
	float b = sols.y;

	float root = 0;
	if(!getRoot(a, b, root)) x = false;

	sols = solveLinear(p0.y, p1.y, pos.y);
	a = sols.x;
	b = sols.y;

	float root2 = 0;
	if(!getRoot(a, b, root2)) y = false;

	if(!(x || y)) discard;

	if(measure(root)) return;
	else if(measure(root2)) return;
	else discard;
}

bool drawPoints(){
	vec2[] points = vec2[](p0, p1);
	for(uint i = 0u; i < 2u; i++){
		if(length2(pos - points[i]) < (width * 0.6)){
			FragColour = vec4(0, 1, 0, 1);
			return true;
		}
	}
	return false;
}

vec2 solveLinear(float p0, float p1, float pos){
	float a = p1 - p0;
	float b = p0 - pos;

	return vec2(a, b);
}

// 'out' is used to effectively make res passed by reference rather than value
// https://stackoverflow.com/questions/13633586/can-you-pass-a-matrix-by-reference-in-a-glsl-shader
bool getRoot(float a, float b, out float res){

	// Unsolvable
	if(a == 0) return false;

	res = -b / a;
	return true;
}

bool isInRange(float t){
	return 0 <= t && t <= 1;
}

vec2 getValue(float t){
	return p0 + (p1 - p0) * t;
}

float length2(vec2 vec){
	vec2 prop = vec2(vec.x * aspect, vec.y);
	return (prop.x * prop.x) + (prop.y * prop.y);
}

bool measure(float t){
	if(isInRange(t)){
		vec2 point1 = getValue(t);
		if(length2(point1 - pos) < width){
			FragColour = colour;
			return true;
		}
	}
	return false;
}