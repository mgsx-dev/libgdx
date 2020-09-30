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

package com.badlogic.gdx.setup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ProjectEmitterFileSystem implements ProjectEmitter {

	protected File out;

	@Override
	public void begin (String outputDir) {
		out = new File(outputDir);
		if (!out.exists() && !out.mkdirs()) {
			throw new RuntimeException("Couldn't create output directory '" + out.getAbsolutePath() + "'");
		}
	}

	@Override
	public void writeFile (File outFile, byte[] bytes) {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(outFile));
			out.write(bytes);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't write file '" + outFile.getAbsolutePath() + "'", e);
		} finally {
			if (out != null) try {
				out.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public File resolveFile (ProjectFile file) {
		File outFile = new File(out, file.outputName);
		if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
			throw new RuntimeException("Couldn't create dir '" + outFile.getAbsolutePath() + "'");
		}
		return outFile;
	}

	@Override
	public void end () {

	}

	@Override
	public boolean isBuildable () {
		return true;
	}

}
