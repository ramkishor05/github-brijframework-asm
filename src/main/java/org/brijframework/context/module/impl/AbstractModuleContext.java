package org.brijframework.context.module.impl;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.container.Container;
import org.brijframework.context.Context;
import org.brijframework.context.ModuleContext;
import org.brijframework.context.impl.AbstractContext;
import org.brijframework.context.impl.Stages;
import org.brijframework.support.config.Assignable;
import org.brijframework.support.util.SupportUtil;
import org.brijframework.util.asserts.Assertion;
import org.brijframework.util.reflect.InstanceUtil;
import org.brijframework.util.reflect.MethodUtil;

public abstract class AbstractModuleContext extends AbstractContext implements ModuleContext {

	private ConcurrentHashMap<Object, Container> cache = new ConcurrentHashMap<Object, Container>();

	private Context context;
	
	private LinkedHashSet<Class<? extends Container>> classList = new LinkedHashSet<>();
	
	@Override
	public void initialize(Context context) {
		this.context = context;
	}

	@Override
	public Context getParent() {
		return context;
	}

	@Override
	public ConcurrentHashMap<Object, Container> getContainers() {
		return cache;
	}
	
	@Override
	public void start() {
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
			break;
		case START:
			System.err.println("Context already started.");
		default:
			break;
		}
	}
	
	private void ready() {
		SupportUtil.getDepandOnSortedContainerClassList(getClassList()).forEach((container) -> {
			System.err.println("---------------------Container------------------");
			System.err.println(container.getSimpleName());
			System.err.println("------------------------------------------------");
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
		SupportUtil.getDepandOnSortedContainerClassList(getClassList()).forEach((container) -> {
			destoryContainer(container);
		});
	}
	
	protected void loadContainer(Class<? extends Container>cls) {
		if(!InstanceUtil.isAssignable(cls)) {
			return ;
		}
		if(!this.invokeFactoryMethod(cls)) {
			this.invokeInstanceMethod(cls);
		}
	}

	private boolean invokeFactoryMethod(Class<? extends Container> cls) {
		for(Method method :MethodUtil.getAllMethod(cls)) {
			if(method.isAnnotationPresent(Assignable.class)) {
				try {
					System.err.println("Container    : "+cls.getSimpleName());
					Container container=(Container) method.invoke(null);
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

	private void invokeInstanceMethod(Class<? extends Container> cls) {
		try {
			System.err.println("Container    : "+cls.getSimpleName());
			Container container = (Container) cls.newInstance();
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
		System.err.println("Destorying Container    : "+cls.getSimpleName());
		Container container = getContainers().remove(cls.getName());
		System.err.println("Destoryed Container     : "+cls.getSimpleName());
		container.clearContainer();
		System.gc();
	}
	
	protected LinkedHashSet<Class<? extends Container>> getClassList(){
		if(classList==null) {
			classList=new LinkedHashSet<>();
		}
		return classList;
	}
	
	protected void register(Class<? extends Container> container) {
		Assertion.notNull(container, "Container should not be null.");
		getClassList().add(container);
	}
	
}

