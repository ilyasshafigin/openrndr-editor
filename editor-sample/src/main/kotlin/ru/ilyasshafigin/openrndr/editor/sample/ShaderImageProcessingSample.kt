package ru.ilyasshafigin.openrndr.editor.sample

import org.openrndr.color.ColorRGBa
import ru.ilyasshafigin.openrndr.editor.editor
import ru.ilyasshafigin.openrndr.editor.plugin.ExportImagePlugin
import ru.ilyasshafigin.openrndr.editor.plugin.FpsMonitorPlugin
import ru.ilyasshafigin.openrndr.editor.plugin.ShaderPlugin
import ru.ilyasshafigin.openrndr.editor.plugin.VideoRecorderPlugin

fun main() = editor {
    install(FpsMonitorPlugin())
    install(VideoRecorderPlugin())
    install(ExportImagePlugin())
    install(ShaderPlugin()) {
        addShaderFromCode(
            name = "imageprocessing",
            code = """
                |#version 330
                |#pragma glslify: noise = require(glsl-noise/simplex/3d)
                |
                |#define PI 3.14159265359
                |
                |uniform vec2 resolution;
                |uniform vec4 mouse;
                |uniform float time;
                |uniform sampler2D canvas;
                |in vec2 texCoord;
                |out vec4 outColor;
                |
                |const vec3 c1 = vec3(3.54585104, 2.93225262, 2.41593945);
                |const vec3 x1 = vec3(0.69549072, 0.49228336, 0.27699880);
                |const vec3 y1 = vec3(0.02312639, 0.15225084, 0.52607955);
                |
                |const vec3 c2 = vec3(3.90307140, 3.21182957, 3.96587128);
                |const vec3 x2 = vec3(0.11748627, 0.86755042, 0.66077860);
                |const vec3 y2 = vec3(0.84897130, 0.88445281, 0.73949448);
                |
                |float saturate(float x) {
                |    return min(1.0, max(0.0, x));
                |}
                |
                |vec3 saturate(vec3 x) {
                |    return min(vec3(1.0, 1.0, 1.0), max(vec3(0.0, 0.0, 0.0), x));
                |}
                |
                |vec3 bump3y(vec3 x, vec3 yoffset) {
                |    vec3 y = vec3(1.0, 1.0, 1.0) - x * x;
                |    y = saturate(y - yoffset);
                |    return y;
                |}
                |
                |vec3 spectral_zucconi6(float x) {
                |    return bump3y(c1 * (x - x1), y1) + bump3y(c2 * (x - x2), y2);
                |}
                |
                |void main() {
                |    vec2 center = vec2(0.5);
                |    vec2 pa = texCoord;
                |
                |    float angle = noise(vec3(pa, time * 0.1)) * 2.0 * PI;
                |    vec2 delta = vec2(0.0);
                |    delta -= 0.003 * (pa - center);
                |    delta += 0.001 * vec2(cos(angle), sin(angle));
                |
                |    vec2 pb = pa + delta;
                |    vec3 ca = texture(canvas, pa).rgb;
                |    vec3 cb = texture(canvas, pb).rgb;
                |
                |    vec3 color = mix(mix(ca, cb, 0.9) * 0.99, spectral_zucconi6(fract(angle * 0.1)), 0.01);
                |    outColor = vec4(color, 1.0);
                |}
                """.trimMargin()
        )
    }

    draw { drawer ->
        canvas.draw(drawer) {
            fill = null
            stroke = ColorRGBa.WHITE
            strokeWeight = 10.0
            circle(center, 100.0)
        }
    }
}
