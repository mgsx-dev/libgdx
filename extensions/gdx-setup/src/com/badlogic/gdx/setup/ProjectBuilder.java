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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.setup.DependencyBank.ProjectType;

public class ProjectBuilder {

	DependencyBank bank;
	List<ProjectType> modules = new ArrayList<ProjectType>();
	List<Dependency> dependencies = new ArrayList<Dependency>();
	String buildContents;
	String settingsContents;

	public ProjectBuilder(DependencyBank bank) {
		this.bank = bank;
	}

	public List<String> buildProject(List<ProjectType> projects, List<Dependency> dependencies) {
		List<String> incompatibilities = new ArrayList<String>();
		for (Dependency dep : dependencies) {
			for (ProjectType type : projects) {
				dep.getDependencies(type);
				incompatibilities.addAll(dep.getIncompatibilities(type));
			}
		}
		this.modules = projects;
		this.dependencies = dependencies;
		return incompatibilities;
	}

	public void build(Language language) throws IOException {
		settingsContents = "include ";
		for (ProjectType module : modules) {
			settingsContents += "'" + module.getName() + "'";
			if (modules.indexOf(module) != modules.size() - 1) {
				settingsContents += ", ";
			}
		}

		StringWriter buildWriter = new StringWriter();
		BufferedWriter buildBw = new BufferedWriter(buildWriter);
		BuildScriptHelper.addBuildScript(language, modules, buildBw);
		BuildScriptHelper.addAllProjects(buildBw);
		for (ProjectType module : modules) {
			BuildScriptHelper.addProject(language, module, dependencies, buildBw);
		}
		buildBw.close();
		buildWriter.close();
		buildContents = buildWriter.getBuffer().toString();
	}

}
