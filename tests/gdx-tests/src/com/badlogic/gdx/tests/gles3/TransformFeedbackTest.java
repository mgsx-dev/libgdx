package com.badlogic.gdx.tests.gles3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.BufferUtils;

@GdxTestConfig(requireGL30=true)
public class TransformFeedbackTest extends GdxTest {

	@Override
	public void create () {
		
		// based off https://open.gl/feedback
		
		// create the shader
		final String vertexShaderCode = "in float inValue;\n"
			+ "out float outValue;\n"
			+ "void main(){ outValue = sqrt(inValue); }";
	   
		int shader = Gdx.gl.glCreateShader(GL20.GL_VERTEX_SHADER);
		Gdx.gl.glShaderSource(shader, vertexShaderCode);
		Gdx.gl.glCompileShader(shader);

		int program = Gdx.gl.glCreateProgram();
		Gdx.gl.glAttachShader(program, shader);
		
		Gdx.gl30.glTransformFeedbackVaryings(program, new String[]{}, GL30.GL_INTERLEAVED_ATTRIBS);
		
		Gdx.gl.glLinkProgram(program);
		Gdx.gl.glUseProgram(program);
		
		// create the input buffer
		IntBuffer buffer = BufferUtils.newIntBuffer(1);
		Gdx.gl30.glGenVertexArrays(1, buffer);
		int vao = buffer.get(0);
		Gdx.gl30.glBindVertexArray(vao);
		
		float data[] = { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f };
		FloatBuffer fb = BufferUtils.newFloatBuffer(data.length);
		BufferUtils.copy(data, fb, data.length, 0);
		

		buffer.clear();
		Gdx.gl.glGenBuffers(1, buffer);
		int vbo = buffer.get(0);
		Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, vbo);
		Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, data.length, fb, GL20.GL_STATIC_DRAW);
		
		// bind attributes
		int inputAttrib = Gdx.gl.glGetAttribLocation(program, "inValue");
		Gdx.gl.glEnableVertexAttribArray(inputAttrib);
		Gdx.gl.glVertexAttribPointer(inputAttrib, 1, GL20.GL_FLOAT, false, 0, 0);
		
		// create tansform buffer
		buffer.clear();
		Gdx.gl.glGenBuffers(1, buffer);
		int tbo = buffer.get(0);
		Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, tbo);
		Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, data.length * 4, null, GL30.GL_STATIC_READ);
		
		// disable rasterization in our case (no rendering, simply transform)
		Gdx.gl.glEnable(GL30.GL_RASTERIZER_DISCARD);
		
		// perform transformation
		Gdx.gl30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, tbo);
		Gdx.gl30.glBeginTransformFeedback(GL20.GL_POINTS);
		Gdx.gl.glDrawArrays(GL20.GL_POINTS, 0, 5);
		Gdx.gl30.glEndTransformFeedback();
		Gdx.gl.glFlush();
		
		Gdx.gl.glDisable(GL30.GL_RASTERIZER_DISCARD);
		
		// get the result
		/*
		XXX OpenGL only
		FloatBuffer fbOut = BufferUtils.newFloatBuffer(data.length);
		Gdx.gl30.glGetBufferSubData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, data.length * 4, fbOut);
		*/
		
		/*
		XXX GLES way for reading feedback (compatible with OpenGL) but needs glMapBufferRange method
		ByteBuffer b = Gdx.gl30.glMapBufferRange(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, data.length * 4, null);
		FloatBuffer fbOut = b.asFloatBuffer();
		*/
		
		// Test the result :
		// TODO fbOut.get() ...
	}
}
