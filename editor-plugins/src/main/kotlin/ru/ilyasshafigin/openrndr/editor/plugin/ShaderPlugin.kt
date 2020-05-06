package ru.ilyasshafigin.openrndr.editor.plugin

import ru.ilyasshafigin.openrndr.editor.Canvas
import ru.ilyasshafigin.openrndr.editor.Editor
import ru.ilyasshafigin.openrndr.editor.EditorPlugin
import mu.KotlinLogging
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ArrayTexture
import org.openrndr.draw.BlendMode
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Cubemap
import org.openrndr.draw.DepthBuffer
import org.openrndr.draw.DepthTestPass
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.DrawStyle
import org.openrndr.draw.Drawer
import org.openrndr.draw.RenderTarget
import org.openrndr.draw.Session
import org.openrndr.draw.Shader
import org.openrndr.draw.StencilTest
import org.openrndr.draw.VertexBuffer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.draw.vertexFormat
import org.openrndr.extra.glslify.preprocessGlslify
import org.openrndr.extra.glslify.preprocessGlslifyFromUrl
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.internal.Driver
import org.openrndr.math.Matrix44
import org.openrndr.math.Matrix55
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4
import org.openrndr.math.transforms.ortho

/**
 * Plugin for shader drawing.
 *
 * Available shader parameters and attributes:
 * - `uniform mat4 projectionMatrix;`
 * - `uniform vec2 resolution;` - viewport resolution in pixels
 * - `uniform float time;` - current running time in seconds
 * - `uniform vec2 mouse;` - mouse pixel coords. xy: current, zw: mouse button down position, otherwise negative values
 * - `uniform sampler2D canvas;` - current canvas texture
 * - `uniform sampler2D source;` - current source texture
 * - `uniform vec2 sourceSize;`
 * - `uniform vec2 sourceOffset;`
 * - `in vec2 texCoord;` - pixel uv position (0..1)
 */
class ShaderPlugin : EditorPlugin {

    override val settings = Settings()

    /** Path to GLSL shader files */
    var glslPath = "data/shader"
    var glslifyPath = "data/shader/glslify"

    /** If [realTime] is true, then [time] will be real time, otherwise [time] will be calculated from [frameRate] */
    var realTime = true

    /** Frame rate (fps). It applies if [realTime] is true */
    var frameRate = 60

    private var time: Double = 0.0
    private var previewScale: Double = 1.0
    private var mouseButtonDownPosition: Vector2? = null

    private val logger = KotlinLogging.logger { }
    private val shaders = LinkedHashMap<String, FragmentShader>()

    private lateinit var canvas: Canvas

    override fun setup(editor: Editor<*>) {
        logger.info { "Setup ShaderPlugin" }

        canvas = editor.canvas
        previewScale = editor.config.previewScale

        editor.mouse.buttonDown.listen {
            mouseButtonDownPosition = it.position
        }
        editor.mouse.buttonUp.listen {
            mouseButtonDownPosition = null
        }
    }

    override fun reset(editor: Editor<*>) {
        time = 0.0
    }

    override fun beforeDraw(drawer: Drawer, editor: Editor<*>) {
        if (settings.isUpdateEnabled) {
            time += if (realTime) editor.deltaTime else (1.0 / frameRate)
        }

        val sourcePlugin = if (editor.isPluginInstalled<SourcePlugin>()) editor.source else null
        val canvasTarget = canvas.target
        val sourceColorBuffer = sourcePlugin?.image?.colorBuffer ?: canvasTarget.colorBuffer(0)
        val sourceOffset = sourcePlugin?.area?.corner ?: Vector2.ZERO
        val sourceSize = Vector2(sourceColorBuffer.width.toDouble(), sourceColorBuffer.height.toDouble())
        val resolution = Vector2(canvasTarget.width.toDouble(), canvasTarget.height.toDouble())
        val projectionMatrix = ortho(0.0, resolution.x, resolution.y, 0.0, -1.0, 1.0)
        val mouseDownPosition = mouseButtonDownPosition?.let { position ->
            Vector2(position.x / previewScale, resolution.y - position.y / previewScale)
        }
        val mousePosition = Vector4(
            x = editor.mouse.position.x / previewScale,
            y = resolution.y - editor.mouse.position.y / previewScale,
            z = mouseDownPosition?.x ?: -1.0,
            w = mouseDownPosition?.y ?: -1.0
        )
        shaders.values.forEach { shader ->
            shader.projectionMatrix = projectionMatrix
            shader.resolution = resolution
            shader.targetSize = resolution
            shader.time = time
            shader.mouse = mousePosition
            shader.source = sourceColorBuffer
            shader.sourceSize = sourceSize
            shader.sourceOffset = sourceOffset
            shader.beforeDraw(canvas.width, canvas.height)
        }
    }

    override fun afterDraw(drawer: Drawer, editor: Editor<*>) {
        shaders.values.reversed().forEach { shader ->
            shader.afterDraw(drawer, canvas.target, editor.width, editor.height)
        }
    }

    fun addShader(name: String, shader: FragmentShader): FragmentShader {
        shaders[name] = shader

        logger.info { "Added shader '$name' from instance '$shader'" }

        return shader
    }

    fun addShader(name: String, shader: FragmentShader, config: FragmentShader.() -> Unit) {
        addShader(name, shader).config()
    }

    fun addShaderFromPath(name: String, path: String): FragmentShader {
        val shaderUrl = shaderUrl(path)
        val shaderCode = preprocessGlslifyFromUrl(shaderUrl, glslifyPath = glslifyPath)
        val shader = FragmentShader(name, shaderCode)
        shaders[name] = shader

        logger.info { "Added shader '$name' from path '$path'" }

        return shader
    }

    fun addShaderFromPath(name: String, path: String, config: FragmentShader.() -> Unit) {
        addShaderFromPath(name, path).config()
    }

    fun addShaderFromCode(name: String, code: String): FragmentShader {
        val shaderCode = preprocessGlslify(code, glslifyPath = glslifyPath)
        val shader = FragmentShader(name, shaderCode)
        shaders[name] = shader

        logger.info { "Added shader '$name' from code" }

        return shader
    }

    fun addShaderFromCode(name: String, code: String, config: FragmentShader.() -> Unit) {
        addShaderFromCode(name, code).config()
    }

    private fun shaderUrl(path: String) = "file:$glslPath/$path"

    @Description(title = "Shader")
    class Settings {

        @BooleanParameter(label = "Update")
        var isUpdateEnabled = true
    }
}

open class FragmentShader(private val shader: Shader) {

    constructor(name: String, fragmentCode: String) :
        this(Shader.createFromCode(filterVertexCode, fragmentCode, name))

    constructor(name: String, vertexCode: String, fragmentCode: String) :
        this(Shader.createFromCode(vertexCode, fragmentCode, name))

    /**
     * parameter map
     */
    val parameters = mutableMapOf<String, Any>()

    var projectionMatrix: Matrix44 by parameters
    var resolution: Vector2 by parameters
    var targetSize: Vector2 by parameters
    var time: Double by parameters
    var mouse: Vector4 by parameters
    var source: ColorBuffer by parameters
    var sourceSize: Vector2 by parameters
    var sourceOffset: Vector2 by parameters

    private var filteredTarget: RenderTarget? = null

    private fun apply(canvas: ColorBuffer, target: RenderTarget) {
        var textureIndex = 0

        target.bind()
        shader.begin()
        canvas.bind(textureIndex)
        shader.uniform("canvas", textureIndex++)

        parameters.forEach { (uniform, value) ->
            @Suppress("UNCHECKED_CAST")
            when (value) {
                is Boolean -> shader.uniform(uniform, value)
                is Float -> shader.uniform(uniform, value)
                is Double -> shader.uniform(uniform, value.toFloat())
                is Matrix44 -> shader.uniform(uniform, value)
                is Vector2 -> shader.uniform(uniform, value)
                is Vector3 -> shader.uniform(uniform, value)
                is Vector4 -> shader.uniform(uniform, value)
                is ColorRGBa -> shader.uniform(uniform, value)
                is Int -> shader.uniform(uniform, value)
                is Matrix55 -> shader.uniform(uniform, value.floatArray)
                is FloatArray -> shader.uniform(uniform, value)

                // EJ: this is not so nice but I have no other ideas for this
                is Array<*> -> if (value.size > 0) when (value[0]) {
                    is Vector2 -> shader.uniform(uniform, value as Array<Vector2>)
                    is Vector3 -> shader.uniform(uniform, value as Array<Vector3>)
                    is Vector4 -> shader.uniform(uniform, value as Array<Vector4>)
                    else -> throw IllegalArgumentException("unsupported array value: ${value[0]!!::class.java}")
                }

                is DepthBuffer -> {
                    shader.uniform(uniform, textureIndex)
                    value.bind(textureIndex++)
                }

                is ColorBuffer -> {
                    shader.uniform(uniform, textureIndex)
                    value.bind(textureIndex++)
                }

                is Cubemap -> {
                    shader.uniform(uniform, textureIndex)
                    value.bind(textureIndex++)
                }

                is ArrayTexture -> {
                    shader.uniform(uniform, textureIndex)
                    value.bind(textureIndex++)
                }
            }
        }

        Driver.instance.setState(filterDrawStyle)
        Driver.instance.drawVertexBuffer(shader, listOf(filterQuad), DrawPrimitive.TRIANGLES, 0, 6)
        shader.end()
        target.unbind()
    }

    fun beforeDraw(canvasWidth: Int, canvasHeight: Int) {
        if (filteredTarget == null ||
            filteredTarget?.width != canvasWidth ||
            filteredTarget?.height != canvasHeight
        ) {
            filteredTarget?.detachColorBuffers()
            filteredTarget?.detachDepthBuffer()
            filteredTarget?.destroy()
            filteredTarget = renderTarget(canvasWidth, canvasHeight) {
                colorBuffer()
                depthBuffer()
            }
        }
    }

    fun afterDraw(drawer: Drawer, canvasTarget: RenderTarget, editorWidth: Int, editorHeight: Int) {
        filteredTarget?.let { filtered ->
            apply(canvasTarget.colorBuffer(0), filtered)

            drawer.isolatedWithTarget(canvasTarget) {
                ortho()
                view = Matrix44.IDENTITY
                model = Matrix44.IDENTITY
                image(filtered.colorBuffer(0), 0.0, 0.0, editorWidth * 1.0, editorHeight * 1.0)
            }
        }
    }

    fun untrack() {
        Session.active.untrack(shader)
    }

    companion object {

        private val filterDrawStyle = DrawStyle().apply {
            blendMode = BlendMode.REPLACE
            depthWrite = false
            depthTestPass = DepthTestPass.ALWAYS
            stencil.stencilTest = StencilTest.DISABLED
        }

        private val filterQuad: VertexBuffer by lazy {
            VertexBuffer.createDynamic(filterQuadFormat, 6, Session.root).apply {
                shadow.writer().apply {
                    write(Vector2(0.0, 1.0)); write(Vector2(0.0, 0.0))
                    write(Vector2(0.0, 0.0)); write(Vector2(0.0, 1.0))
                    write(Vector2(1.0, 0.0)); write(Vector2(1.0, 1.0))

                    write(Vector2(0.0, 1.0)); write(Vector2(0.0, 0.0))
                    write(Vector2(1.0, 1.0)); write(Vector2(1.0, 0.0))
                    write(Vector2(1.0, 0.0)); write(Vector2(1.0, 1.0))
                }
                shadow.upload()
                shadow.destroy()
            }
        }

        private val filterQuadFormat = vertexFormat {
            position(2)
            textureCoordinate(2)
        }

        private val filterVertexCode = """
            |#version 330
            |
            |in vec2 a_texCoord0;
            |in vec2 a_position;
            |
            |uniform vec2 targetSize;
            |uniform mat4 projectionMatrix;
            |
            |out vec2 texCoord;
            |
            |void main() {
            |    texCoord = a_texCoord0;
            |    vec2 transformed = a_position * targetSize;
            |    gl_Position = projectionMatrix * vec4(transformed, 0.0, 1.0);
            |}
        """.trimMargin()
    }
}

val Editor<*>.shaders: ShaderPlugin
    get() = getPlugin()
