// GEOMETRY SHADER INFORMATION
#version 460 core
layout (points) in;
layout (triangle_strip, max_vertices = 5) out;

void buildHouse(vec4 pos);

void main() {    
    buildHouse(gl_in[0].gl_Position + vec4(0.0, -0.5, 0.0, 0.0));
    
}  

void buildHouse(vec4 pos){
    gl_Position = pos + vec4(-0.2, -0.2, 0.0, 0.0);
    EmitVertex();
    gl_Position = pos + vec4(0.2, -0.2, 0.0, 0.0);
    EmitVertex();
    gl_Position = pos + vec4(-0.2, 0.2, 0.0, 0.0);
    EmitVertex();
    gl_Position = pos + vec4(0.2, 0.2, 0.0, 0.0);
    EmitVertex();
    gl_Position = pos + vec4(0.0, 0.4, 0.0, 0.0);
    EmitVertex();
    EndPrimitive();
}