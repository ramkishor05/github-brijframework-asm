package org.brijframework.context.impl.bootstrap;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.container.bootstrap.BootstrapContainer;
import org.brijframework.context.bootstrap.BootstrapContext;
import org.brijframework.context.impl.AbstractContext;
import org.brijframework.context.impl.Stages;
import org.brijframework.support.factories.SingletonFactory;
import org.brijframework.support.util.SupportUtil;
import org.brijframework.util.asserts.Assertion;
import org.brijframework.util.printer.LoggerConsole;
import org.brijframework.util.reflect.InstanceUtil;
import org.brijframework.util.reflect.MethodUtil;

public abstract class AbstractBootstrapContext extends AbstractContext implements BootstrapContext {

	private ConcurrentHashMap<Object, BootstrapContainer> cache = new ConcurrentHashMap<Object, BootstrapContainer>();

	private LinkedHashSet<Class<? extends BootstrapContainer>> classList;
	
	protected void loadContainer(Class<? extends BootstrapContainer> bootstrapContainerClass) {
		if(!InstanceUtil.isAssignable(bootstrapContainerClass)) {
			return ;
		}
		if(!this.invokeFactoryMethod(bootstrapContainerClass)) {
			this.invokeInstanceMethod(bootstrapContainerClass);
		}
	}

	protected boolean invokeFactoryMethod(Class<? extends BootstrapContainer> bootstrapContainerClass) {
		for(Method method :MethodUtil.getAllMethod(bootstrapContainerClass)) {
			if(method.isAnnotationPresent(SingletonFactory.class)) {
				try {
					BootstrapContainer bootstrapContainer=(BootstrapContainer) method.invoke(null);
					register(bootstrapContainer);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	protected void invokeInstanceMethod(Class<? extends BootstrapContainer> bootstrapContainerClass) {
		try {
			BootstrapContainer bootstrapContainer = (BootstrapContainer) bootstrapContainerClass.newInstance();
			register(bootstrapContainer);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	protected void destoryContext(Class<? extends BootstrapContainer> containerClass) {
		if(!InstanceUtil.isAssignable(containerClass)) {
			return ;
		}
		getContainers().remove(containerClass.getName());
		System.gc();
	}
	
	@Override
	public void start() {
		if(Stages.START.equals(this.getStages())) {
			System.err.println("Bootstrap container already started.");
			return;
		}
		this.init();
		if(getRegisteredList()==null || getRegisteredList().isEmpty()) {
			System.err.println("Bootstrap container should not be empty. please register context into @Override init method for :"+this.getClass().getSimpleName());
			return;
		}
		LoggerConsole.screen("BootstrapContext -> "+this.getClass().getSimpleName(), "Starting bootstrap context for loading the bootstrap container");
		getRegisteredList().forEach((Context) ->{ 
			loadContainer(Context);
		});
		this.setStages(Stages.START);
		LoggerConsole.screen("BootstrapContext -> "+this.getClass().getSimpleName(), "Started bootstrap context for loading the bootstrap container");
	}
	
	@Override
	public void stop() {
		if(Stages.STOPED.equals(this.getStages())) {
			System.err.println("Bootstrap container already stoped.");
			return;
		}
		if(getRegisteredList()==null || getRegisteredList().isEmpty()) {
			System.err.println("Bootstrap container should not be empty. please register context into @Override init method for :"+this.getClass().getSimpleName());
			return;
		}
		getRegisteredList().forEach((Context) ->{ 
			destoryContext(Context);
		});
		this.setStages(Stages.STOPED);
	}

	@Override
	public ConcurrentHashMap<Object, BootstrapContainer> getContainers() {
		if(this.cache==null) {
			this.cache = new ConcurrentHashMap<Object, BootstrapContainer>();
		}
		return this.cache;
	}
	
	protected LinkedHashSet<Class<? extends BootstrapContainer>> getRegisteredList(){
		return SupportUtil.getDepandOnSortedBootstrapContainerList(getClassList());
	}
	
	public LinkedHashSet<Class<? extends BootstrapContainer>> getClassList() {
		if(classList==null) {
			classList=new LinkedHashSet<>();
		}
		return classList;
	}
	
	protected void register(Class<? extends BootstrapContainer> context) {
		Assertion.notNull(context, "Context class should not be null.");
		getClassList().add(context);
	}
	
	protected void register(BootstrapContainer bootstrapContainer) {
		Assertion.notNull(bootstrapContainer, "bootstrap container should not be null.");
		bootstrapContainer.setContext(this);
		bootstrapContainer.init();
		bootstrapContainer.loadContainer();
		getContainers().put(bootstrapContainer.getClass().getSimpleName(), bootstrapContainer);
	}
	
	@Override
	public void poststart(BootstrapContainer container) {
		
	}
	
	@Override
	public void prestart(BootstrapContainer container) {
		
	}
	
}
