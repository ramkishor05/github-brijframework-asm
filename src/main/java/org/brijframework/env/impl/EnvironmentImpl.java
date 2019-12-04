package org.brijframework.env.impl;

import java.util.Properties;

import org.brijframework.env.Environment;
import org.brijframework.support.beans.Bean;
import org.brijframework.support.model.Model;

@Model
@Bean(id="environment" , factoryClass="org.brijframework.factories.impl.bootstrap.env.EnvironmentFactory", factoryMethod="getFactory.getEnvironment")
public class EnvironmentImpl implements Environment {

	private String name;
	private boolean active;
	private Properties properties;
	public EnvironmentImpl() {
	}

	@Override
	public void init() {
		
	}

	@Override
	public void setProperties(Properties properties) {
		getProperties().putAll(properties);
	}

	@Override
	public Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
		}
		return properties;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public Object get(String key) {
		return getProperties().get(key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnvironmentImpl other = (EnvironmentImpl) obj;
		if (active != other.active)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EnvironmentImpl [name=" + name + ", active=" + active + ", properties=" + properties + "]";
	}
	
}
