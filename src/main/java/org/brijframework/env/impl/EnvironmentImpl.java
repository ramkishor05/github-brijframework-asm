package org.brijframework.env.impl;

import static org.brijframework.support.config.SupportConstants.APPLICATION_BOOTSTRAP_CONFIG_FILES;
import static org.brijframework.support.config.SupportConstants.APPLICATION_BOOTSTRAP_CONFIG_PATHS;

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
		.forEach(entry -> {
			System.err.println(entry.getKey() + "=" + entry.getValue());
		});
	}
	
	@Override
	public void setProperties(Properties properties) {
		this.putAll(properties);
	}

	protected void findAnnotationConfig() {
		if (this.containsKey(APPLICATION_BOOTSTRAP_CONFIG_PATHS)) {
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
					this.put(APPLICATION_BOOTSTRAP_CONFIG_PATHS, files);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void findFileLocateConfig() {
		if (this.containsKey(APPLICATION_BOOTSTRAP_CONFIG_PATHS)) {
			return;
		}
		try {
			List<String> files = new ArrayList<>();
			FileFactory.getResources(Arrays.asList(APPLICATION_BOOTSTRAP_CONFIG_FILES.split("\\|"))).forEach(file -> {
				files.add(file.getAbsolutePath());
			});
			this.put(APPLICATION_BOOTSTRAP_CONFIG_PATHS,files);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void loadFileLocateConfig() {
		try {
			@SuppressWarnings("unchecked")
			List<String> files = (List<String>) this.get(APPLICATION_BOOTSTRAP_CONFIG_PATHS);
			for (String filePath : files) {
				System.err.println(APPLICATION_BOOTSTRAP_CONFIG_PATHS + "=" + filePath);
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
