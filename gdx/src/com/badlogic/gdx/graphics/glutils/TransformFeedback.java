package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TransformFeedback {

	/**
	 * Perform transform feedback by capturing source mesh provided.
	 * @param shader a transform feedback shader (vertex shader and optional geometry shader)
	 * @param target the target mesh.
	 * @param source the source mesh.
	 * @param targetPrimitiveType either GL20.GL_POINTS, GL20.GL_LINES or GL20.GL_TRIANGLES
	 * @param sourcePrimitiveType typically GL20.GL_POINTS...
	 * @param sourceOffset typically 0
	 * @param sourceCount typically source.getNumVertices()
	 * @param allocate whether to allocate target vertices GPU side, use false if already allocated.
	 */
	public static void capture(ShaderProgram shader, Mesh target, Mesh source, int targetPrimitiveType, int sourcePrimitiveType, int sourceOffset, int sourceCount, boolean allocate){
		
		GL30 gl = Gdx.gl30;
		if(gl == null) throw new GdxRuntimeException("transform feedback requires GLES 3.0+");
		
		if(!(target.getVertexData() instanceof VertexBufferObjectWithVAO)) throw new GdxRuntimeException("transform feedback requires VertexBufferObjectWithVAO");
		VertexBufferObjectWithVAO targetVertices = (VertexBufferObjectWithVAO)target.getVertexData();

		if(allocate){
			gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, targetVertices.bufferHandle);
			gl.glBufferData(GL20.GL_ARRAY_BUFFER, targetVertices.byteBuffer.capacity(), null, targetVertices.usage);
		}
		
		shader.bind();
		
		source.bind(shader);
		
		gl.glEnable(GL30.GL_RASTERIZER_DISCARD);
		
		gl.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, targetVertices.bufferHandle);
		gl.glBeginTransformFeedback(targetPrimitiveType);
		gl.glDrawArrays(sourcePrimitiveType, sourceOffset, sourceCount);
		gl.glEndTransformFeedback();
		gl.glFlush();
		gl.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, 0);
		
		gl.glDisable(GL30.GL_RASTERIZER_DISCARD);
	}
	
	public static void capture(ShaderProgram shader, Mesh target, Mesh source){
		capture(shader, target, source, GL20.GL_POINTS, GL20.GL_POINTS, 0, source.getNumVertices(), true);
	}
	
}
