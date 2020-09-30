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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProjectEmitterZip implements ProjectEmitter {

	protected ZipOutputStream zipOutput;
	protected ByteArrayOutputStream byteArrayOutput;
	protected String outputDir;

	@Override
	public void begin (String outputDir) {
		this.outputDir = outputDir;
		byteArrayOutput = new ByteArrayOutputStream();
		zipOutput = new ZipOutputStream(byteArrayOutput);
	}

	@Override
	public void writeFile (File outFile, byte[] bytes) {
		try {
			zipOutput.putNextEntry(new ZipEntry(outFile.getPath()));
			zipOutput.write(bytes);
			zipOutput.closeEntry();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public File resolveFile (ProjectFile file) {
		return new File(file.outputName);
	}

	@Override
	public void end () {
		try {
			zipOutput.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		save();
	}

	/** save zip content to a file. Subclass may override it in order to output it differently (eg. from server) */
	protected void save () {
		File out = new File(outputDir, "project.zip");
		try {
			FileOutputStream str = new FileOutputStream(out);
			str.write(byteArrayOutput.toByteArray());
			str.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isBuildable () {
		return false;
	}

}
