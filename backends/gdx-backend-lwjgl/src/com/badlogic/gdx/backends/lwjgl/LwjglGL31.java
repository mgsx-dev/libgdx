package com.badlogic.gdx.backends.lwjgl;

import org.lwjgl.opengl.GL32;

import com.badlogic.gdx.graphics.GL31;

public class LwjglGL31 extends LwjglGL30 implements GL31
{

	@Override
	public void glTexImage2DMultisample (int target, int samples, int internalformat, int width, int height,
		boolean fixedsamplelocations) {
		GL32.glTexImage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
	}

}
