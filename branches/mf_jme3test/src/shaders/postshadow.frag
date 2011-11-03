#import "shadow.glsllib"

uniform SHADOWMAP m_ShadowMap;
varying vec4 projCoord;

void main() {
   vec4 coord = projCoord;
   coord.xyz /= coord.w;
   float shad = Shadow_GetShadow(m_ShadowMap, coord) * 0.7 + 0.3;
   if (shad > 0.5)
       shad = 1.0;
   //shad = 1.0 - shad;
   gl_FragColor = vec4(shad,shad,shad,1.0);
}
