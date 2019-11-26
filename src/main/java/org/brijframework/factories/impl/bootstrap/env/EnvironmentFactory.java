package org.brijframework.factories.impl.bootstrap.env;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.brijframework.env.Environment;
import org.brijframework.env.impl.EnvironmentImpl;
import org.brijframework.factories.impl.bootstrap.AbstractBootstrapFactory;
import org.brijframework.support.config.SingletonFactory;
import org.brijframework.support.config.OrderOn;
import org.brijframework.support.config.SupportConstants;
import org.brijframework.support.config.application.EnvironmentResource;
import org.brijframework.support.enums.ResourceType;
import org.brijframework.util.objects.PropertiesUtil;
import org.brijframework.util.printer.ConsolePrint;
import org.brijframework.util.reflect.ReflectionUtils;
import org.brijframework.util.resouces.ResourcesUtil;
import org.brijframework.util.resouces.YamlUtil;
import org.brijframework.util.text.StringUtil;

@OrderOn(0)
public class EnvironmentFactory extends AbstractBootstrapFactory<String,Environment>{

	private static final String prefix_filepath = "filepath:";
	private static final String prefix_classpath = "classpath:";
	private static EnvironmentFactory factory;
	
	@SingletonFactory
	public static EnvironmentFactory getFactory() {
		if(factory==null) {
			factory=new EnvironmentFactory();
		}
		return factory;
	}
	
	@Override
	public EnvironmentFactory loadFactory() {
		try {
		ConsolePrint.screen("BootstrapFactory -> "+this.getClass().getSimpleName(), "Lunching the factory for ResourceContext");
		String environmentLocation = System.getProperty(SupportConstants.APPLICATION_ENVIRONMENT_RESOURCE_PATH_KEY);
		String environmentFiles = System.getProperty(SupportConstants.APPLICATION_ENVIRONMENT_RESOURCE_FILE_NAMES);
		List<String> environmentPaths=new ArrayList<String>();
		if(StringUtil.isNonEmpty(environmentFiles)) {
			loadEnviroment(environmentLocation, environmentFiles, environmentPaths);
		}else {
			ReflectionUtils.getClassListFromInternal().forEach(cls->{
				if(cls.isAnnotationPresent(EnvironmentResource.class)) {
					EnvironmentResource resource=cls.getAnnotation(EnvironmentResource.class);
					loadEnviroment(resource.location(), resource.value(), environmentPaths);
				}
			});
		}
		for(String environmentPath:environmentPaths) {
			try {
				if(environmentPath.startsWith(prefix_classpath)) {
					loadClassPathResource(environmentPath);
				}
				if(environmentPath.startsWith(prefix_filepath)) {
					loadFilePathResource(environmentPath);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		setActiveProfile();
		Environment environment=getEnvironment();
		ConsolePrint.screen("APPLICATION_ENVIRONMENT_RESOURCE",environmentPaths.toString());
		ConsolePrint.screen("environment", "Active profile :"+environment.getName());
		ConsolePrint.screen("BootstrapFactory -> "+this.getClass().getSimpleName(), "Lunched the factory for ResourceContext");
	} catch (Exception e) {
		ConsolePrint.screen("BootstrapFactory -> "+this.getClass().getSimpleName(), "Error to lunch the factory for ResourceContext");
	}
		return this;
	}

	private void loadFilePathResource(String environmentPath) {
		
	}

	private void loadClassPathResource(String environmentPath) {
		URL resource = ResourcesUtil.getResource(environmentPath.replace(prefix_classpath, ""));
		if(resource==null) {
			return;
		}
		if (environmentPath.toString().endsWith(ResourceType.PROP)) {
			Properties envProperties = PropertiesUtil.getProperties(new File(resource.getFile()));
			environment(envProperties);
		}
		if (environmentPath.toString().endsWith(ResourceType.YML) || environmentPath.toString().endsWith(ResourceType.YAML)) {
			try {
				String[] profiles=new String(Files.readAllBytes(new File(resource.getFile()).toPath())).split("---");
				for (String profile : profiles) {
					Properties envProperties = YamlUtil.getEnvProperties(profile);
					environment(envProperties);
				}
				}catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	private void environment(Properties envProperties) {
		String profileName=envProperties.getProperty("application.profiles");
		if(profileName==null) {
			profileName="default";
		}
		EnvironmentImpl environment=new EnvironmentImpl();
		environment.setActive(false);
		environment.setName(profileName);
		environment.setProperties(envProperties);
		register(profileName, environment);
	}
	
	private void setActiveProfile() {
		for (Environment environment : getCache().values()) {
			String activePrfile=(String) environment.getProperties().get("application.profiles.active");
			if (activePrfile!=null) {
				EnvironmentImpl environmentImpl=(EnvironmentImpl)getCache().get(activePrfile);
				if(environmentImpl!=null) {
				   environmentImpl.setActive(true);
				 }
			}
		}
		
	}

	private void loadEnviroment(String environmentLocation, String environmentFiles, List<String> environmentPaths) {
		for(String environmentFile: environmentFiles.split("\\|")) {
			environmentPaths.add(StringUtil.isNonEmpty(environmentLocation)? environmentLocation+""+environmentFile : environmentFile);
		}
	}

	@Override
	protected void preregister(String key, Environment value) {
	}

	@Override
	protected void postregister(String key, Environment value) {
		
	}

	public  Environment getEnvironment() {
		for (Environment environment : getCache().values()) {
			if(environment.isActive()) {
				return environment;
			}
		}
		return getCache().get("default");
	}
}
