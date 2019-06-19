package org.brijframework.asm.context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.context.BootstrapContext;
import org.brijframework.context.Context;
import org.brijframework.support.model.Assignable;
import org.brijframework.support.util.SupportUtil;
import org.brijframework.util.asserts.Assertion;
import org.brijframework.util.reflect.InstanceUtil;
import org.brijframework.util.reflect.MethodUtil;

public abstract class AbstractBootstrapContext implements BootstrapContext {

	private ConcurrentHashMap<Object, Context> cache = new ConcurrentHashMap<Object, Context>();

	private LinkedHashSet<Class<? extends Context>> classList = new LinkedHashSet<>();

	private Context context;
	
	private Properties properties;

	@Override
	public Properties getProperties() {
		if(properties==null) {
			properties=new Properties();
		}
		return properties;
	}


	@Override
	public void initialize(Context context) {
		this.context = context;
	}

	public void loadContext(Class<? extends Context> contextClass) {
		Method assignable=null;
		for (Method method : MethodUtil.getAllMethod(contextClass)) {
			if (method.isAnnotationPresent(Assignable.class)) {
				assignable=method;
			}
		}
		if (assignable!=null) {
			System.err.println("---------------------------------------------------------------------");
			System.err.println("Context       : " + contextClass.getName());
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
			System.err.println("Context       : " + contextClass.getName());
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
	
	protected void destoryContext(Class<? extends Context> clas) {
		if(clas.isInterface() || clas.getModifiers() == Modifier.ABSTRACT) {
			return ;
		}
		System.err.println("Context Destorying     : "+clas.getName());
		Context context=getContexts().remove(clas.getName());
		context=null;
		System.gc();
		System.err.println("Destoryed Container    : "+context);
	}

	@Override
	public Context getParent() {
		return context;
	}

	@Override
	public ConcurrentHashMap<Object, Context> getContexts() {
		return cache;
	}
	
	protected LinkedHashSet<Class<? extends Context>> getClassList(){
		return SupportUtil.getDepandOnSortedClassList(classList);
	}
	
	protected void register(Class<? extends Context> context) {
		Assertion.notNull(context, "Context class should not be null.");
		classList.add(context);
	}
	
	protected void register(Context context) {
		Assertion.notNull(context, "Context should not be null.");
		context.initialize(this);
		context.init();
		getContexts().put(context.getClass().getName(),context);
	}

}
