package com.mobilesorcery.sdk.core.templates;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mobilesorcery.sdk.core.templates.SectionedPropertiesFile.Section;

/**
 * Given a "project template description file",
 * returns all meta data necessary to create a new
 * template
 * @author Mattias Bybro, mattias.bybro@purplescout.com, mattias@bybro.com
 *
 */
public class ProjectTemplateDescription {

	private List<String> templateFiles;
	private Map<String, String> settings;
	private String id;
	private String name;
	private List<String> generatedFiles;
	private String description;

	public static ProjectTemplateDescription parse(File descFile) throws IOException {
		return init(SectionedPropertiesFile.parse(descFile));
	}

	public static ProjectTemplateDescription parse(Reader reader) throws IOException {
		return init(SectionedPropertiesFile.parse(reader));
	}
	
	private static ProjectTemplateDescription init(SectionedPropertiesFile file) {
		ProjectTemplateDescription desc = new ProjectTemplateDescription();
		Section files = file.getFirstSection("Files"); //$NON-NLS-1$
		String[] templateFileDescs = files == null ? new String[0] : files.getValues();
		ArrayList<String> templateFilesRW = new ArrayList<String>();
		ArrayList<String> generatedFilesRW = new ArrayList<String>();
		
		if (templateFileDescs != null) {
			for (int i = 0; i < templateFileDescs.length; i++) {
				String[] templateFileDesc = templateFileDescs[i].split("->", 2); //$NON-NLS-1$
				if (templateFileDesc.length > 0) {
					 templateFilesRW.add(templateFileDesc[0].trim());
					 int generatedFileIndex = templateFileDesc.length == 1 ? 0 : 1;
					 generatedFilesRW.add(templateFileDesc[generatedFileIndex].trim());
				}
			}
		}
		
		desc.templateFiles = Collections.unmodifiableList(templateFilesRW);
		desc.generatedFiles = Collections.unmodifiableList(generatedFilesRW);
		
		Section settings = file.getFirstSection("Settings"); //$NON-NLS-1$
		desc.settings = settings == null ? new HashMap<String, String>() : settings.getEntriesAsMap();
		
		Map<String, String> defaultEntries = file.getDefaultSection().getEntriesAsMap();
		desc.id = defaultEntries.get("id"); //$NON-NLS-1$
		desc.name = defaultEntries.get("name"); //$NON-NLS-1$
		desc.description = defaultEntries.get("description"); //$NON-NLS-1$
		
		return desc;
	}
	
	private ProjectTemplateDescription() {
	}
		
	/**
	 * <p>Returns a list of template file paths,
	 * relative to the project root. The result may
	 * contain parameters (see Template.replace)</p>
	 * <p>Example format:<code>test.c</code>,<code>subdir/test.c</code>.</p>
	 * @return
	 */
	public List<String> getTemplateFiles() {
		return templateFiles;  
	}

	public Map<String, String> getSettings() {
		return settings;
	}

	public String getId() {
		return id; 
	}

	public String getName() {
		return name;
	}

	public List<String> getGeneratedFiles() {
		return generatedFiles;
	}

	public String getDescriptionText() {
		return description;
	}

}
