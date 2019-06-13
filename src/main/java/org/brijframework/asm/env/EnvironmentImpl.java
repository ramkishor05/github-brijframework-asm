package org.brijframework.asm.env;

import java.util.Properties;

import org.brijframework.env.Environment;

public abstract class EnvironmentImpl implements Environment {
	
	private Properties properties;

	public EnvironmentImpl() {
		this.init();
	}

	public abstract void init();
	
	public Properties getProperties() {
		if(properties==null) {
			properties= new Properties();
		}
		return properties;
	}
	
}
