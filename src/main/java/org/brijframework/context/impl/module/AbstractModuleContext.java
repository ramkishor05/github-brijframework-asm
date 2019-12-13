package org.brijframework.context.impl.module;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.container.Container;
import org.brijframework.container.module.ModuleContainer;
import org.brijframework.context.Context;
import org.brijframework.context.impl.AbstractContext;
import org.brijframework.context.impl.Stages;
import org.brijframework.context.module.ModuleContext;
import org.brijframework.support.factories.SingletonFactory;
import org.brijframework.support.util.SupportUtil;
import org.brijframework.util.asserts.Assertion;
import org.brijframework.util.printer.LoggerConsole;
import org.brijframework.util.reflect.InstanceUtil;
import org.brijframework.util.reflect.MethodUtil;

public abstract class AbstractModuleContext extends AbstractContext implements ModuleContext {

	private ConcurrentHashMap<Object, ModuleContainer> cache = new ConcurrentHashMap<Object, ModuleContainer>();

	private Context context;
	
	private LinkedHashSet<Class<? extends ModuleContainer>> classList = new LinkedHashSet<>();
	
	@Override
	public void initialize(Context context) {
		this.context = context;
	}

	@Override
	public Context getParent() {
		return context;
	}

	@Override
	public ConcurrentHashMap<Object, ModuleContainer> getContainers() {
		return cache;
	}
	
	@Override
	public void start() {
		LoggerConsole.screen("ModuleContext -> "+this.getClass().getSimpleName(), "Starting to lunch the module context for "+this.getClass().getSimpleName());
		Stages stages = getStages();
		switch (stages) {
		case INIT:
			this.init();
			this.setStages(Stages.LOAD);
		case LOAD:
			this.load();
			this.setStages(Stages.READY);
		case READY:
			this.ready();
			this.setStages(Stages.START);
		default:
			break;
		}
		LoggerConsole.screen("ModuleContext -> "+this.getClass().getSimpleName(), "Started to lunch the module context for "+this.getClass().getSimpleName());
	}
	
	private void ready() {
		if(Stages.START.equals(getStages())) {
			System.err.println("Context already stoped.");
			return;
		}
		if(getClassList()==null || getClassList().isEmpty()) {
			System.err.println("Container register should not be empty. please register context into @Override init method for :"+this.getClass().getSimpleName());
			return;
		}
		LoggerConsole.screen("ModuleContext -> "+this.getClass().getSimpleName(), "Ready container for module container to lunch the related factories");
		SupportUtil.getDepandOnSortedModuleContainerList(getClassList()).forEach((container) -> {
			loadContainer(container);
		});
	}

	@Override
	public void stop() {
		if(Stages.STOPED.equals(getStages())) {
			System.err.println("Context already stoped.");
			return;
		}
		if(getClassList()==null || getClassList().isEmpty()) {
			System.err.println("Container register should not be empty. please register context into @Override init method for :"+this.getClass().getSimpleName());
			return;
		}
		SupportUtil.getDepandOnSortedModuleContainerList(getClassList()).forEach((container) -> {
			destoryContainer(container);
		});
	}
	
	protected void loadContainer(Class<? extends ModuleContainer>cls) {
		if(!InstanceUtil.isAssignable(cls)) {
			return ;
		}
		LoggerConsole.screen("ModuleContainer -> "+cls.getSimpleName(), "Loading container for module container to lunch the related factories");
		if(!this.invokeFactoryMethod(cls)) {
			this.invokeInstanceMethod(cls);
		}
		LoggerConsole.screen("ModuleContainer -> "+cls.getSimpleName(), "Loaded container for module container to lunch the related factories");
	}

	protected boolean invokeFactoryMethod(Class<? extends ModuleContainer> cls) {
		for(Method method :MethodUtil.getAllMethod(cls)) {
			if(method.isAnnotationPresent(SingletonFactory.class)) {
				try {
					ModuleContainer container=(ModuleContainer) method.invoke(null);
					container.setContext(this);
					container.init();
					container.loadContainer();
					getContainers().put(cls.getSimpleName(), container);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	protected void invokeInstanceMethod(Class<? extends ModuleContainer> cls) {
		try {
			ModuleContainer container = (ModuleContainer) cls.newInstance();
			container.setContext(this);
			container.init();
			container.loadContainer();
			getContainers().put(cls.getSimpleName(), container);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	protected void destoryContainer(Class<? extends Container> cls) {
		if(!InstanceUtil.isAssignable(cls)) {
			return ;
		}
		Container container = getContainers().remove(cls.getName());
		container.clearContainer();
		System.gc();
	}
	
	protected LinkedHashSet<Class<? extends ModuleContainer>> getClassList(){
		if(classList==null) {
			classList=new LinkedHashSet<>();
		}
		return classList;
	}
	
	protected void register(Class<? extends ModuleContainer> container) {
		Assertion.notNull(container, "Container should not be null.");
		getClassList().add(container);
	}
	
}

