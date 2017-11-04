/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.g2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShaderPart;
import com.badlogic.gdx.graphics.glutils.ShaderStage;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SimpleComputeShaderTest extends GdxTest {
	private SpriteBatch batch;
	private Drawable drawable;
	private Texture texture;

	private ShaderProgram computeShader;
	
	@Override
	public void create () {
		
		if(Gdx.gl30 == null){
			throw new GdxRuntimeException("compute shader require GL30");
		}
		
		batch = new SpriteBatch();
		
		String source = 
				"#version 430\n"
				+ "layout(local_size_x = 1, local_size_y = 1) in;"
				+ "layout(rgba8, binding = 0) uniform image2D img_output;"
				+ ""
				+ "void main() {"
				+ "	vec4 pixel = vec4(0.0, 0.0, 0.0, 1.0);"
				+ "	ivec2 pixel_coords = ivec2(gl_GlobalInvocationID.xy);"
				+ "	pixel.r = float(pixel_coords.x) / 256.0;"
				+ "	pixel.g = float(pixel_coords.y) / 256.0;"
				+ "	imageStore(img_output, pixel_coords, pixel);"
				+ "}"
				+ ""
				+ ""
				+ ""
			;
		
		// from http://antongerdelan.net/opengl/compute.html
		computeShader = new ShaderProgram(new ShaderPart(ShaderStage.compute, source));

		if(!computeShader.isCompiled()){
			throw new GdxRuntimeException(computeShader.getLog());
		}
		
		int width = 256;
		int height = 256;
		
		texture = new Texture(width, height, Format.RGBA8888);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		drawable = new TextureRegionDrawable(new TextureRegion(texture));
		
		int GL_WRITE_ONLY = 0x88B9;
		
		Gdx.gl30.glBindImageTexture(0, texture.getTextureObjectHandle(), 0, false, 0, GL_WRITE_ONLY, GL30.GL_RGBA8);
		
		// draw !
		computeShader.begin();
		Gdx.gl30.glDispatchCompute(width, height, 1);
		computeShader.end();
		
		Gdx.gl30.glMemoryBarrier(0x20); // AKA GL_SHADER_IMAGE_ACCESS_BARRIER_BIT
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		drawable.draw(batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
	}

	@Override
	public void dispose () {
		texture.dispose();
		batch.dispose();
	}
}
