// FRAGMENT SHADER INFORMATION
#version 430 core

struct Curve {
	vec2 a;
	vec2 b;
	vec2 c;
};

#ifdef OPENGL
in vec2 texCoords;

layout(std430, binding = 0) buffer BezierCurves {
	Curve curves[];
};

uniform vec4 colour;

out vec4 FragColour;
#endif

#ifdef VULKAN

#extension GL_EXT_buffer_reference : require

layout(location = 1) in vec2 texCoords;

layout(std430, set = 1, binding = 0) readonly buffer BezierCurves {
	Curve curves[];
};

layout(push_constant) uniform Origin {
	layout(offset = 80) vec4 colour;
};

layout(location = 0) out vec4 FragColour;
#endif

/*
	If the x value of this curve is the closest measured so far,
	then set inside if the curve is travelling downward.
*/
void testX(float x, inout bool inside, inout float distance, bool down);
/*
	Test the x value at t on a quadratic curve
*/
void testAtT(Curve curve, float t, inout bool inside, inout float distance);

void main(){

	bool inside = false;
	float distance = 1e38;
	for (int i = 0; i < curves.length(); i++){
		Curve curve = curves[i];

		// x changes linearly with t
		if (curve.a.x == 0) {
			// p3 = (f + s - 2m) + (2m - 2s) + (s) = f AKA the last point
			vec2 p3 = curve.a + curve.b + curve.c;
			bool down = p3.y < curve.c.y;
			// Check 0 < t < 1
			if ((down && p3.y < texCoords.y && texCoords.y < curve.c.y) || (!down && curve.c.y < texCoords.y && texCoords.y < p3.y)){

				// Straight Vertical Line
				if (curve.b.x == 0) {
					testX(curve.c.x, inside, distance, down);
					continue;
				}
				// Straight Line bisected by the off-curve point
				else if (curve.a.y == 0){
					vec2 vec = p3 - curve.c;
					// x = my + c
					float m = vec.x / vec.y;
					float c = p3.x - m * p3.y;
					float x = m * texCoords.y + c;
					testX(x, inside, distance, down);
					continue;
				}

			}
		}
		// y changes linearly with t
		else if (curve.a.y == 0){
			// Horizontal line, gradient == 0
			if (curve.b.y == 0) continue;

			// p3 = (f + s - 2m) + (2m - 2s) + (s) = f AKA the last point
			vec2 p3 = curve.a + curve.b + curve.c;
			float t = (texCoords.y - curve.c.y) / (p3.y - curve.c.y);
			if (0 <= t && t <= 1){
				float x = curve.a.x * t * t + curve.b.x * t + curve.c.x;
				testX(x, inside, distance, p3.y < curve.c.y);
			}
			continue;
		}

		float rt = curve.b.y * curve.b.y - (4.0 * curve.a.y * (curve.c.y - texCoords.y));
		if (rt < 0) continue;

		float t1 = (-curve.b.y + sqrt(rt)) * .5 / curve.a.y;
		testAtT(curve, t1, inside, distance);

		float t2 = (-curve.b.y - sqrt(rt)) * .5 / curve.a.y;
		testAtT(curve, t2, inside, distance);
	}

	vec3 c = vec3(inside);
	FragColour = vec4(c, 1.0);
}

void testX(float x, inout bool inside, inout float distance, bool down){
	float v = x - texCoords.x;
	if (0 < v && v <= distance){
		inside = down;
		distance = v;
	}
}

void testAtT(Curve curve, float t, inout bool inside, inout float distance){
	if (0 < t && t < 1){
		float x = curve.a.x * t * t + curve.b.x * t + curve.c.x;
		float dydt = 2 * curve.a.y * t + curve.b.y;
		testX(x, inside, distance, dydt < 0);
	}
}