package org.brijframework.asm.factories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.brijframework.util.resouces.ResourcesUtil;

public interface FileFactory {

	public static final String MANIFEST_MF = "MANIFEST.MF";
	public static final String POM_PROPERTIES = "pom.properties";
	public static final String POM_XML = "pom.xml";
	public static final String META_INF = "META-INF";
	public static final String All_INF = "";
	public static final String COM_INF = "comman";
	
	public static  boolean isIgnoreFile(File file) {
		if (MANIFEST_MF.equalsIgnoreCase(file.getName()) || POM_PROPERTIES.equalsIgnoreCase(file.getName())
				|| POM_XML.equalsIgnoreCase(file.getName())) {
			return true;
		}
		return false;
	}
	
	public static List<File> getResources(List<String> types) {
		List<File>  list=new ArrayList<File>();
		try {
			for (File file : ResourcesUtil.getResources(All_INF)) {
				if(!isIgnoreFile(file) && types.contains(file.getName())) {
					list.add(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
}
