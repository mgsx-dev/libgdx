package com.badlogic.gdx.tests.gles3;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

@GdxTestConfig(minGlApi=32)
public class FrameBufferMultisampleTest extends GdxTest
{
	private ShapeRenderer shapes;
	private SpriteBatch batch;
	private FrameBuffer fbo;
	private ShaderProgram shader;
	
	int w = 64;
	int h = 64;
	
	int segments = 64;
	
	@Override
	public void create () {
		
		ShaderProgram.prependVertexCode = "#version 150\n#define varying out\n#define attribute in\n";
		ShaderProgram.prependFragmentCode = "#version 150\n#define varying in\n#define texture2D texture\n#define gl_FragColor fragColor\nout vec4 fragColor;\n";
		
		String vert = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
			+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";
		
		String frag = "" + "\n"
		+ "uniform int texSamples;" + "\n"
		+ "uniform sampler2DMS u_texture;" + "\n"
		+ "" + "\n"
		+ "varying vec2 v_texCoords;" + "\n"
		+ "" + "\n"
		+ "" + "\n"
		+ "void main(){" + "\n"
		+ "" + "\n"
		+ "ivec2 texSize = textureSize(u_texture);" + "\n"
		+ "ivec2 coord = ivec2(v_texCoords * texSize);" + "\n"
		+ "" + "\n"
		+ "" + "\n"
		+ "vec4 color = vec4(0.0);" + "\n"
		+ "for (int i = 0; i < texSamples; i++) color += texelFetch(u_texture, coord, i);" + "\n"
		+ "color /= float(texSamples);" + "\n"
		+ "gl_FragColor = color;" + "\n"
		+ "" + "\n"
		+ "}" + "\n"
		;

		shader = new ShaderProgram(vert, frag);
		if(!shader.isCompiled()) throw new GdxRuntimeException(shader.getLog());
		
		shapes = new ShapeRenderer();
		batch = new SpriteBatch();
		
		
		IntBuffer buf = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL30.GL_MAX_SAMPLES, buf);
		int maxSamples = buf.get();
		int nbCols = 4;
		int nbRows = 4;
		
		fbo = new FrameBuffer(Format.RGBA8888, w * nbCols, h * nbRows, false);
		float l = 0;
		fbo.begin();
		ScreenUtils.clear(l,l,l,0,true);
		fbo.end();
		
		for(int y=0 ; y<nbRows ; y++){
			for(int x=0 ; x<nbCols ; x++){
				int level = y*nbCols+x;
				int samples = 1 << level;
				if(samples <= maxSamples){
					drawCircleMultisample((float)x/(float)nbCols, (float)y/(float)nbRows, samples);
				}
			}
		}
		
		fbo.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}
	
	private void drawCircleMultisample (float x, float y, int samples) 
	{
		float ellipseW = w / 1.2f;
		float ellipseH = h / 1.2f;

		shapes.setColor(Color.ORANGE);
		
		if(samples > 1)
		{
			FrameBufferBuilder b = new FrameBufferBuilder(w, h, samples);
			b.addBasicColorTextureAttachment(Format.RGBA8888);
			// b.addBasicDepthRenderBuffer();
			FrameBuffer msFbo = b.build();
			
			
			msFbo.begin();
			ScreenUtils.clear(0,0,0,0,false);
			shapes.getProjectionMatrix().setToOrtho2D(0, 0, msFbo.getWidth(), msFbo.getHeight());
			shapes.setProjectionMatrix(shapes.getProjectionMatrix());
			shapes.begin(ShapeType.Filled);
			shapes.ellipse(w/2-ellipseW/2, h/2-ellipseH/2, ellipseW, ellipseH, segments);
			shapes.end();
			msFbo.end();
			
			fbo.begin();
			batch.getProjectionMatrix().setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
			batch.begin();
			batch.setShader(shader);
			shader.setUniformi("texSamples", samples);
			batch.draw(msFbo.getColorBufferTexture(), x * fbo.getWidth() , y * fbo.getHeight() , msFbo.getWidth(), msFbo.getHeight(), 0, 0, 1, 1);
			batch.setShader(null);
			batch.end();
			fbo.end();
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			
			msFbo.dispose();
		}
		else{
			fbo.begin();
			shapes.getProjectionMatrix().setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());
			shapes.setProjectionMatrix(shapes.getProjectionMatrix());
			shapes.begin(ShapeType.Filled);
			shapes.ellipse(x * fbo.getWidth() + + w/2 - ellipseW/2, y * fbo.getHeight() + h/2 - ellipseH/2, ellipseW, ellipseH, segments);
			shapes.end();
			fbo.end();
		}
	}

	@Override
	public void render () {
		int overSize = 4;
		float l = 0f;
		ScreenUtils.clear(l,l,l,1,true);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		batch.draw(fbo.getColorBufferTexture(), 0, 0, fbo.getWidth() * overSize, fbo.getHeight() * overSize, 0, 0, 1, 1);
		batch.end();
	}
	
	@Override
	public void dispose () {
		fbo.dispose();
		shapes.dispose();
		batch.dispose();
		shader.dispose();
	}
}
