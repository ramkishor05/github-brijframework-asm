package org.brijframework.context.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.brijframework.container.Container;
import org.brijframework.context.Context;
import org.brijframework.env.Environment;
import org.brijframework.factories.impl.bootstrap.env.EnvironmentFactory;
import org.brijframework.support.config.SingletonFactory;

public abstract class AbstractContext implements Context{

	private Context context;
	
	private Environment environment;
	
	private Stages stages;
	
	public Stages getStages() {
		if(stages==null) {
			stages=Stages.INIT;
		}
		return stages;
	}
	
	protected void setStages(Stages stages) {
		this.stages = stages;
	}
	
	@Override
	public Environment getEnvironment() {
		if(environment==null) {
			load();
		}
		return environment;
	}

	@Override
	public Context getParent() {
		return context;
	}

	@Override
	public void initialize(Context context) {
		this.context = context;
	}

	protected void load() {
		environment = EnvironmentFactory.getFactory().getEnvironment();
	}
	
	protected Method findFactoryMethod(Class<? extends Container> contextClass) {
		for (Method method : contextClass.getMethods()) {
			if (Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(SingletonFactory.class)) {
				return method;
			}
		}
		return null;
	}
}
