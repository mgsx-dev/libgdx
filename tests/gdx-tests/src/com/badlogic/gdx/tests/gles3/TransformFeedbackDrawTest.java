package com.badlogic.gdx.tests.gles3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.IndexData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.TransformFeedback;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

@GdxTestConfig(requireGL30=true)
public class TransformFeedbackDrawTest extends GdxTest {

	private Mesh mesh;
	private ShaderProgram shader;
	private Matrix4 matrix;
	private Mesh transformedMesh;
	private GLProfiler profiler;

	@Override
	public void create () {
		
		profiler = new GLProfiler(Gdx.graphics);
		profiler.enable();
		profiler.setListener(new GLErrorListener() {
			@Override
			public void onError (int error) {
				System.err.println("GL error " + error);
			}
		});
		
		ShaderProgram.prependVertexCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 140\n" : "#version 300 es\n";
		ShaderProgram.prependFragmentCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 140\n" : "#version 300 es\n";
		
		int w = 10;
		int h = 10;
		matrix = new Matrix4().setToOrtho2D(-0.5f, -0.5f, w, h);
		
		// create a line grid
		MeshBuilder mb = new MeshBuilder();
		mb.begin(new VertexAttributes(VertexAttribute.Position()), GL20.GL_LINES);
		for(int y=0 ; y<h ; y++){
			for(int x=0 ; x<w ; x++){
				mb.vertex(x, y, 0);
			}
		}
		for(int y=0 ; y<h ; y++){
			for(int x=0 ; x<w ; x++){
				if(x < w-1){
					mb.index((short)(y*w+x));
					mb.index((short)(y*w+x+1));
				}
				if(y < h-1){
					mb.index((short)(y*w+x));
					mb.index((short)((y+1)*w+x));
				}
			}
		}
		mesh = mb.end();
		
		String vertexCode = "attribute vec4 a_position;\n"
		+ "uniform mat4 u_projModelView;\n"
		+ "void main(){ gl_Position = u_projModelView * a_position; }\n";
		
		String fragmentCode = "void main(){ gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0); }\n";
		
		shader = new ShaderProgram(vertexCode, fragmentCode);
		if(!shader.isCompiled()){
			throw new GdxRuntimeException(shader.getLog());
		}
		
		// based off https://open.gl/feedback
		
		IntBuffer intbuf = BufferUtils.newIntBuffer(1);
		
		// create the shader
		final String transformCode = ShaderProgram.prependVertexCode
			+ "in vec4 a_position;\n"
			+ "out vec3 o_position;\n"
			+ "void main(){ o_position = vec3(a_position.x * 0.5, a_position.y, 0.0); }";
	   
		int transformShader = Gdx.gl.glCreateShader(GL20.GL_VERTEX_SHADER);
		Gdx.gl.glShaderSource(transformShader, transformCode);
		Gdx.gl.glCompileShader(transformShader);
		Gdx.gl.glGetShaderiv(transformShader, GL20.GL_COMPILE_STATUS, intbuf);
		if(intbuf.get(0) == 0){
			throw new GdxRuntimeException(Gdx.gl.glGetShaderInfoLog(transformShader));
		}

		int program = Gdx.gl.glCreateProgram();
		Gdx.gl.glAttachShader(program, transformShader);
		
		Gdx.gl30.glTransformFeedbackVaryings(program, new String[]{"o_position"}, GL30.GL_INTERLEAVED_ATTRIBS);
		
		Gdx.gl.glLinkProgram(program);
		
		intbuf.clear();
		Gdx.gl.glGetProgramiv(program, GL20.GL_LINK_STATUS, intbuf);
		if(intbuf.get(0) == 0){
			throw new GdxRuntimeException(Gdx.gl.glGetProgramInfoLog(program));
		}
		
		ShaderProgram trShader = new ShaderProgram(program);

		// create transform mesh
		short [] indices = new short[mesh.getNumIndices()];
		mesh.getIndices(indices);
			
		transformedMesh = new Mesh(true, mesh.getNumVertices(), mesh.getNumIndices(), new VertexAttributes(VertexAttribute.Position()));
		transformedMesh.setIndices(indices);
		
		TransformFeedback.capture(trShader, transformedMesh, mesh, GL20.GL_POINTS, GL20.GL_POINTS, 0, mesh.getNumVertices(), true);
	}
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shader.bind();
		shader.setUniformMatrix("u_projModelView", matrix);
		transformedMesh.render(shader, GL20.GL_LINES);
	}
}
