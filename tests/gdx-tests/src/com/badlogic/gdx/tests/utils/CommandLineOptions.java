package com.badlogic.gdx.tests.utils;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Array;

/**
 * Shared class for desktop launchers.
 * 
 * options:
 * --gl30 enable GLES 3.2 (default is GLES 2.0)
 * --glErrors enable GLProfiler and log any GL errors. (default is disabled)
 */
public class CommandLineOptions {
	
	public String startupTestName = null;
	public int glApi = 20;
	public boolean logGLErrors = false;
	
	public CommandLineOptions (String [] argv) {
		Array<String> args = new Array<String>(argv);
		for(String arg : args){
			if(arg.startsWith("-")){
				if(arg.equals("--gl30")) glApi = 30;
				else if(arg.equals("--gl31")) glApi = 31;
				else if(arg.equals("--gl32")) glApi = 32;
				else if(arg.equals("--glErrors")) logGLErrors = true;
				else System.err.println("skip unrecognized option " + arg);
			}else{
				startupTestName = arg;
			}
		}
	}

	public boolean isTestCompatible (String testName) {
		final Class<? extends GdxTest> clazz = GdxTests.forName(testName);
		GdxTestConfig config = clazz.getAnnotation(GdxTestConfig.class);
		if(config != null){
			if(glApi < config.minGlApi()) return false;
		}
		return true;
	}

	public Object[] getCompatibleTests () {
		List<String> names = new ArrayList<>();
		for(String name : GdxTests.getNames()){
			if(isTestCompatible(name)) names.add(name);
		}
		return names.toArray();
	}
}
