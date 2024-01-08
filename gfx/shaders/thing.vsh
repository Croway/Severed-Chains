#version 330 core

layout(location = 0) in vec3 inPos;

layout(std140) uniform transforms {
  mat4 camera;
  mat4 projection;
};

layout(std140) uniform transforms2 {
  mat4 model;
};

void main() {
  gl_Position = projection * camera * model * vec4(inPos, 1.0);
}
