// GEOMETRY SHADER INFORMATION
#version 460 core
layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

in VECS{
    vec3 fragPos;
    vec3 normal;
    vec2 texCoords;
} vecsIn[];

uniform float time;

out VECS{
    vec3 fragPos;
    vec3 normal;
    vec2 texCoords;
} vecsOut;

vec3 getNormal();
vec4 explode(vec4 pos, vec3 normal);

void main() {    
    vec3 normal = getNormal();
    for(int i = 0; i < 3; i++){
        gl_Position = explode(gl_in[i].gl_Position, normal);
        vecsOut.fragPos = vecsIn[i].fragPos;
        vecsOut.normal = vecsIn[i].normal;
        vecsOut.texCoords = vecsIn[i].texCoords;
        EmitVertex();
    }
    EndPrimitive();
}  

vec3 getNormal(){
    // a and b are two of the edges that make up this triangle
    vec3 a = vec3(gl_in[0].gl_Position - gl_in[1].gl_Position);
    vec3 b = vec3(gl_in[1].gl_Position - gl_in[2].gl_Position);
    return normalize(cross(a, b));
}

vec4 explode(vec4 pos, vec3 normal){
    float mag = 1.0;
    vec3 dir = normal * ((sin(time) + 1.0) / 2.0) * mag;
    return pos - vec4(dir, 0.0);
}