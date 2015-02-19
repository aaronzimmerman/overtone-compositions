// Displays a constant color.

uniform vec3 iRGB;

void main(void)
{
    vec3 c = iRGB;
    gl_FragColor = vec4(c, 1.0);
}
