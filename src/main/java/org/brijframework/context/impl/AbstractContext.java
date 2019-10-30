package org.brijframework.context.impl;

import java.util.Properties;

import org.brijframework.context.Context;

public abstract class AbstractContext implements Context{

	private Context context;
	
	private Properties properties;
	
	private boolean isStarted;
	
	private boolean isStoped;
	
	private boolean loadContext;
	
	private boolean init;

	private boolean configred;
	
	public  boolean isInit() {
		return init;
	}
	
	public void setInit(boolean init) {
		this.init = init;
	}

	@Override
	public Properties getProperties() {
		if(properties==null) {
			properties=new Properties();
		}
		return properties;
	}

	@Override
	public Context getParent() {
		return context;
	}

	@Override
	public void initialize(Context context) {
		this.context = context;
	}
	
	protected void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}
	
	@Override
	public boolean isStarted() {
		return this.isStarted;
	}
	
	protected void setStoped(boolean isStoped) {
		this.isStoped = isStoped;
	}
	
	@Override
	public boolean isStoped() {
		return isStoped;
	}

	protected void setLoadContext(boolean loadContext) {
		this.loadContext = loadContext;
	}
	
	public boolean isLoadContext() {
		return this.loadContext;
	}
	
	public void setProperty(String key, String value) {
		getProperties().setProperty(key, value);
	}
	
	public String getProperty(String key) {
		return getProperties().getProperty(key);
	}

	public boolean isConfigred() {
		return configred;
	}
	
	public void setConfigred(boolean configred) {
		this.configred = configred;
	}

	protected void load() {
		
	}
}
