package org.brijframework.asm.context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.context.BootstrapContext;
import org.brijframework.context.Context;
import org.brijframework.support.model.Assignable;
import org.brijframework.support.util.SupportUtil;
import org.brijframework.util.asserts.Assertion;
import org.brijframework.util.reflect.InstanceUtil;

public abstract class AbstractBootstrapContext extends AbstractContext implements BootstrapContext {

	private ConcurrentHashMap<Object, Context> cache = new ConcurrentHashMap<Object, Context>();

	private LinkedHashSet<Class<? extends Context>> classList;
	
	protected void loadContext(Class<? extends Context> contextClass) {
		if(contextClass==null) {
			System.err.println("Context should not be null.");
			return;
		}
		if(this.isLoadContext()) {
			System.err.println("Context already loaded.");
			return;
		}
		Method assignable=null;
		for (Method method : contextClass.getMethods()) {
			if (Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Assignable.class)) {
				assignable=method;
			}
		}
		if (assignable!=null) {
			System.err.println("---------------------------------------------------------------------");
			System.err.println("Context      : " + contextClass.getSimpleName());
			try {
				Context context = (Context) assignable.invoke(null);
				context.getProperties().putAll(this.getProperties());
				register(context);
				context.startup();
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}else if(InstanceUtil.isAssignable(contextClass)){
			System.err.println("---------------------------------------------------------------------");
			System.err.println("Context      : " + contextClass.getSimpleName());
			try {
				Context context = InstanceUtil.getInstance(contextClass);
				if(context!=null) {
					context.getProperties().putAll(this.getProperties());
					register(context);
					context.startup();
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
	public void startup() {
		if(this.isStarted()) {
			System.err.println("Context already started.");
			return;
		}
		if(getRegisteredList()==null || getRegisteredList().isEmpty()) {
			System.err.println("Context should not be empty. please register context into @Override init method for :"+this.getClass().getSimpleName());
			return;
		}
		getRegisteredList().forEach((Context) ->{ loadContext(Context);});
		this.setStarted(true);
	}
	
	@Override
	public void destory() {
		if(this.isStarted()) {
			System.err.println("Context already stoped.");
			return;
		}
		if(getRegisteredList()==null || getRegisteredList().isEmpty()) {
			System.err.println("Context should not be empty. please register context into @Override init method for :"+this.getClass().getSimpleName());
			return;
		}
		getRegisteredList().forEach((Context) ->{ destoryContext(Context);});
		this.setStoped(true);
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
}
