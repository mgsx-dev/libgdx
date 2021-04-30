package com.badlogic.gdx.graphics;

public interface GL31 extends GL30 {
	public static final int GL_TEXTURE_2D_MULTISAMPLE                       = 0x9100;
	
	public void glTexImage2DMultisample(int target, int samples, int internalformat, int width, int height, boolean fixedsamplelocations);
}
