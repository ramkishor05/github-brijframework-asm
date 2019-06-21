package org.brijframework.asm.context;

import java.util.Properties;

import org.brijframework.context.Context;

public abstract class AbstractContext implements Context{

	private Context context;
	
	private Properties properties;
	
	private boolean isStarted;
	
	private boolean isStoped;
	
	private boolean loadContext;

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
	

}
