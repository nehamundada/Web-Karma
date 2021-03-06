/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.rdf;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.Command.CommandTag;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.modeling.ModelingConfiguration;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.WorkspaceRegistry;

public abstract class RdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(RdfGenerator.class);
	
	protected Workspace initializeWorkspace() {
		
		Workspace workspace = WorkspaceManager.getInstance().createWorkspace();
        WorkspaceRegistry.getInstance().register(new ExecutionController(workspace));
        ModelingConfiguration.load();
        ModelingConfiguration.setManualAlignment(true);
		return workspace;
	}

	protected void removeWorkspace(Workspace workspace) {
		WorkspaceManager.getInstance().removeWorkspace(workspace.getId());
	    WorkspaceRegistry.getInstance().deregister(workspace.getId());
	}

	protected void applyHistoryToWorksheet(Workspace workspace, Worksheet worksheet,
			KR2RMLMapping mapping) throws JSONException {
		WorksheetCommandHistoryExecutor wchr = new WorksheetCommandHistoryExecutor(worksheet.getId(), workspace);
		try
		{
			List<CommandTag> tags = new ArrayList<CommandTag>();
			tags.add(CommandTag.Transformation);
			wchr.executeCommandsByTags(tags, mapping.getWorksheetHistory());
		}
		catch (CommandException | KarmaException e)
		{
			logger.error("Unable to execute column transformations", e);
		}
	}
	
}
