package org.brijframework.env.impl;

import static org.brijframework.support.config.SupportConstants.APPLICATION_CONFIGRATION_FILE_NAMES;
import static org.brijframework.support.config.SupportConstants.APPLICATION_CONFIGRATION_PATH_KEY;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.brijframework.env.Environment;
import org.brijframework.factories.impl.FileFactory;
import org.brijframework.support.config.EnvironmentConfig;
import org.brijframework.support.enums.ResourceType;
import org.brijframework.util.objects.PropertiesUtil;
import org.brijframework.util.reflect.AnnotationUtil;
import org.brijframework.util.reflect.ReflectionUtils;
import org.brijframework.util.resouces.YamlUtil;

public class EnvironmentImpl extends Properties implements Environment {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EnvironmentImpl() {
		setProperties(System.getProperties());
	}

	@Override
	public void init() {
		findAnnotationConfig();
		findFileLocateConfig();
		loadFileLocateConfig();
		this.entrySet().stream()
		.sorted((entry1, entry2) -> ((String) entry1.getKey()).compareToIgnoreCase(((String) entry2.getKey())))
		;
	}
	
	@Override
	public void setProperties(Properties properties) {
		this.putAll(properties);
	}

	protected void findAnnotationConfig() {
		if (this.containsKey(APPLICATION_CONFIGRATION_PATH_KEY)) {
			return;
		}
		try {
			ReflectionUtils.getClassListFromInternal().forEach(cls -> {
				if (cls.isAnnotationPresent(EnvironmentConfig.class)) {
					EnvironmentConfig config = (EnvironmentConfig) AnnotationUtil.getAnnotation(cls, EnvironmentConfig.class);
					List<String> files = new ArrayList<>();
					FileFactory.getResources(Arrays.asList(config.paths().split("\\|"))).forEach(file -> {
						files.add(file.getAbsolutePath());
					});
					this.put(APPLICATION_CONFIGRATION_PATH_KEY, files);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void findFileLocateConfig() {
		if (this.containsKey(APPLICATION_CONFIGRATION_PATH_KEY)) {
			return;
		}
		try {
			List<String> files = new ArrayList<>();
			FileFactory.getResources(Arrays.asList(APPLICATION_CONFIGRATION_FILE_NAMES.split("\\|"))).forEach(file -> {
				files.add(file.getAbsolutePath());
			});
			this.put(APPLICATION_CONFIGRATION_PATH_KEY,files);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void loadFileLocateConfig() {
		try {
			@SuppressWarnings("unchecked")
			List<String> files = (List<String>) this.get(APPLICATION_CONFIGRATION_PATH_KEY);
			for (String filePath : files) {
				File file=new File(filePath);
				if (!file.exists()) {
					System.err.println("Env configration file not found.");
					continue;
				}
				if (filePath.toString().endsWith(ResourceType.PROP)) {
					this.putAll(PropertiesUtil.getProperties(file));
				}
				if (filePath.toString().endsWith(ResourceType.YML) || filePath.toString().endsWith(ResourceType.YAML)) {
					this.putAll(YamlUtil.getEnvProperties(file));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
