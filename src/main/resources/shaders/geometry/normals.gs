// GEOMETRY SHADER INFORMATION
#version 460 core
layout (triangles) in;
layout (line_strip, max_vertices = 6) out;

in vec3 normal[3];

uniform mat4 projection;

const float MAG = 0.4;

void generateLine(int index);

void main() {    
    generateLine(0);
    generateLine(1);
    generateLine(2);
    
}  

void generateLine(int index){
    gl_Position = projection * gl_in[index].gl_Position;
    EmitVertex();
    gl_Position = projection * (gl_in[index].gl_Position - vec4(normal[index], 0.0) * MAG);
    EmitVertex();
    EndPrimitive();
}