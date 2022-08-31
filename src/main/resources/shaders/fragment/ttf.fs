// FRAGMENT SHADER INFORMATION
#version 130 core

#define NUM_BEZIERS 128u

uniform vec2 points[NUM_BEZIERS];

// An array of points that are the end of a bezier point
// The order of each curve is the difference between one and the previous index
uniform uint bezierEnds[NUM_BEZIERS - 1u];

uniform vec2 windowSize;
uniform vec4 colour;
uniform float width;

out vec4 FragColour;

vec2 pos = (gl_FragCoord.xy / windowSize) - vec2(1);
float aspect = windowSize.x / windowSize.y;

bool isInRange(float t){
	return 0 <= t && t <= 1;
}

float length2(vec2 vec){
	vec2 prop = vec2(vec.x * aspect, vec.y);
	return (prop.x * prop.x) + (prop.y * prop.y);
}

bool drawPoints(){
	for(uint i = 0u; i < NUM_BEZIERS; i++){
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

vec3 solveQuad(float p0, float p1, float p2, float pos){
	float a = p0 - 2 * p1 + p2;
	float b = 2 * (p1 - p0);
	float c = p0 - pos;

	return vec3(a, b, c);
}

bool getLinearRoot(float a, float b, out float res){

	// Unsolvable
	if(a == 0) return false;

	res = -b / a;
	return true;
}

bool getQuadRoots(float a, float b, float c, out vec2 res){
	float r = b*b - 4 * a * c;

	// Unsolvable
	if(r < 0 || a == 0) return false;

	float root = sqrt(r);
	res.x = (-b + root) / (2*a);
	res.y = (-b - root) / (2*a);
	return true;
}

vec2 getLinearValue(float t, vec2 p0, vec2 p1){
	return p0 + (p1 - p0) * t;
}

vec2 getQuadValue(float t, vec2 p0, vec2 p1, vec2 p2){
	return p1 + (pow(1 - t, 2) * (p0 - p1)) + (pow(t, 2) * (p2 - p1));
}

bool measure(float t, uint order, vec2 points[4]){
	if(isInRange(t)){
		vec2 point1 = getLinearValue(t, points[0], points[1]);
		switch(order){
			case 1:{
				point1 = getLinearValue(t, points[0], points[1]);
				break;
			}
			case 2: {
				point1 = getQuadValue(t, points[0], points[1], points[2]);
				break;
			}
			case 3: point1 = getQuadValue(t, points[0], points[1], points[2]);
		}
		if(length2(point1 - pos) < width){
			FragColour = colour;
			return true;
		}
	}
	return false;
}

void main(){

	if(drawPoints()) return;

	uint pointIndex = 0u;
	for(uint i = 0u; i < NUM_BEZIERS; i++){

		// The order of this curve
		// 1 for linear, 2 for quadratic, 3 for cubic
		uint order = bezierEnds[i];
		if(order == 0u){
			pointIndex++;
			continue;
		}
		else if(order == 4u){
			discard;
		}

		//
		bool x = true;
		bool y = true;

		vec2 roots[3];

		vec2 p0 = points[pointIndex];
		vec2 p1 = points[pointIndex + 1u];
		vec2 p2 = points[pointIndex + 2u];
		vec2 p3 = points[pointIndex + 3u];

		switch(order){
			case 1u: {
				float root1 = 0;
				float root2 = 0;

				vec2 sols = solveLinear(p0.x, p1.x, pos.x);
				if(!getLinearRoot(sols.x, sols.y, root1)) x = false;
				else roots[0].x = root1;

				sols = solveLinear(p0.y, p1.y, pos.y);
				if(!getLinearRoot(sols.x, sols.y, root2)) y = false;
				else roots[0].y = root2;

				break;
			}
			case 2u: {
				vec2 root1 = vec2(0);
				vec2 root2 = vec2(0);

				vec3 sols = solveQuad(p0.x, p1.x, p2.x, pos.x);
				if(!getQuadRoots(sols.x, sols.y, sols.z, root1)) x = false;
				else{
					roots[0].x = root1.x;
					roots[1].x = root1.y;
				}

				sols = solveQuad(p0.y, p1.y, p2.y, pos.y);
				if(!getQuadRoots(sols.x, sols.y, sols.z, root2)) y = false;
				else{
					roots[0].y = root2.x;
					roots[1].y = root2.y;
				}

				break;
			}
		}

		if(!(x || y)) continue;

		vec2 bezierPoints[4] = vec2[](p0, p1, p2, p3);

		// There are [order] solutions to the equation, and they must be checked in the x and y direction
		for(uint j = 0u; j < order; j++){
			if(x && measure(roots[j].x, order, bezierPoints)) return;
			if(y && measure(roots[j].y, order, bezierPoints)) return;
		}

		pointIndex += order;
	}
	discard;
}
