package org.brijframework.context.bootstrap.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.context.BootstrapContext;
import org.brijframework.context.Context;
import org.brijframework.context.impl.AbstractContext;
import org.brijframework.context.impl.Stages;
import org.brijframework.support.util.SupportUtil;
import org.brijframework.util.asserts.Assertion;
import org.brijframework.util.reflect.InstanceUtil;

public abstract class AbstractBootstrapContext extends AbstractContext implements BootstrapContext {

	private ConcurrentHashMap<Object, Context> cache = new ConcurrentHashMap<Object, Context>();

	private LinkedHashSet<Class<? extends Context>> classList;
	
	protected void loadContext(Class<? extends Context> contextClass) {
		if(Stages.LOAD.equals(this.getStages())) {
			System.err.println("Context already loaded.");
			return;
		}
		if(contextClass==null) {
			System.err.println("Context should not be null.");
			return;
		}
		Method assignable= findFactoryMethod(contextClass);
		if (assignable!=null) {
			System.err.println("---------------------------------------------------------------------");
			System.err.println("Context      : " + contextClass.getSimpleName());
			try {
				Context context = (Context) assignable.invoke(null);
				register(context);
				context.start();
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}else if(InstanceUtil.isAssignable(contextClass)){
			System.err.println("---------------------------------------------------------------------");
			System.err.println("Context      : " + contextClass.getSimpleName());
			try {
				Context context = InstanceUtil.getInstance(contextClass);
				if(context!=null) {
					register(context);
					context.start();
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void destoryContext(Class<? extends Context> contextClass) {
		if(!InstanceUtil.isAssignable(contextClass)) {
			return ;
		}
		System.err.println("Context Destorying  : "+contextClass.getSimpleName());
		getContexts().remove(contextClass.getName());
		System.gc();
		System.err.println("Destoryed Container  : "+contextClass.getSimpleName());
	}
	
	@Override
	public void start() {
		if(Stages.START.equals(this.getStages())) {
			System.err.println("Context already started.");
			return;
		}
		if(getRegisteredList()==null || getRegisteredList().isEmpty()) {
			System.err.println("Context should not be empty. please register context into @Override init method for :"+this.getClass().getSimpleName());
			return;
		}
		getRegisteredList().forEach((Context) ->{ loadContext(Context);});
		this.setStages(Stages.START);
	}
	
	@Override
	public void stop() {
		if(Stages.STOPED.equals(this.getStages())) {
			System.err.println("Context already stoped.");
			return;
		}
		if(getRegisteredList()==null || getRegisteredList().isEmpty()) {
			System.err.println("Context should not be empty. please register context into @Override init method for :"+this.getClass().getSimpleName());
			return;
		}
		getRegisteredList().forEach((Context) ->{ destoryContext(Context);});
		this.setStages(Stages.STOPED);
	}

	@Override
	public ConcurrentHashMap<Object, Context> getContexts() {
		if(this.cache==null) {
			this.cache = new ConcurrentHashMap<Object, Context>();
		}
		return this.cache;
	}
	
	protected LinkedHashSet<Class<? extends Context>> getRegisteredList(){
		return SupportUtil.getDepandOnSortedClassList(classList);
	}
	
	public LinkedHashSet<Class<? extends Context>> getClassList() {
		if(classList==null) {
			classList=new LinkedHashSet<>();
		}
		return classList;
	}
	
	protected void register(Class<? extends Context> context) {
		Assertion.notNull(context, "Context class should not be null.");
		getClassList().add(context);
	}
	
	protected void register(Context context) {
		Assertion.notNull(context, "Context should not be null.");
		context.initialize(this);
		context.init();
		getContexts().put(context.getClass().getName(),context);
	}
	
	public Context getContext(String key) {
		return cache.get(key);
	}
	
	public List<Context> getContexts(Class<? extends Context> contextClass) {
		List<Context> contexts=new ArrayList<>();
		for(Context context:cache.values()) {
			if(contextClass.isAssignableFrom(context.getClass())) {
				contexts.add(context);
			}
		}
		return contexts;
	}
	
}
