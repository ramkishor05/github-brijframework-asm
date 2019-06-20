package org.brijframework.asm.context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.container.Container;
import org.brijframework.context.Context;
import org.brijframework.context.ModuleContext;
import org.brijframework.support.model.Assignable;
import org.brijframework.support.util.SupportUtil;
import org.brijframework.util.asserts.Assertion;
import org.brijframework.util.reflect.InstanceUtil;
import org.brijframework.util.reflect.MethodUtil;

public abstract class AbstractModuleContext implements ModuleContext {

	private ConcurrentHashMap<Object, Container> cache = new ConcurrentHashMap<Object, Container>();

	private Context context;
	
	private LinkedHashSet<Class<? extends Container>> classList = new LinkedHashSet<>();
	
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

	@Override
	public Context getParent() {
		return context;
	}

	@Override
	public ConcurrentHashMap<Object, Container> getContainers() {
		return cache;
	}
	
	@Override
	public void startup() {
		SupportUtil.getDepandOnSortedContainerClassList(getClassList()).forEach((container) -> {
			System.err.println("---------------------Container------------------");
			System.err.println(container.getSimpleName());
			System.err.println("------------------------------------------------");
			loadContainer(container);
			
		});
	}
	
	@Override
	public void destory() {
		SupportUtil.getDepandOnSortedContainerClassList(getClassList()).forEach((container) -> {
			destoryContainer(container);
		});
	}
	
	protected void loadContainer(Class<? extends Container>cls) {
		if(!InstanceUtil.isAssignable(cls)) {
			return ;
		}
		boolean called=false;
		for(Method method:MethodUtil.getAllMethod(cls)) {
			if(method.isAnnotationPresent(Assignable.class)) {
				try {
					System.err.println("Container    : "+cls.getSimpleName());
					Container container=(Container) method.invoke(null);
					container.setContext(this);
					container.init();
					container.loadContainer();
					getContainers().put(cls.getSimpleName(), container);
					called=true;
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		if(!called) {
			try {
				System.err.println("Container    : "+cls.getSimpleName());
				Container container=(Container) cls.newInstance();
				container.setContext(this);
				container.init();
				container.loadContainer();
				getContainers().put(cls.getSimpleName(), container);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	protected void destoryContainer(Class<? extends Container> cls) {
		if(!InstanceUtil.isAssignable(cls)) {
			return ;
		}
		System.err.println("Destorying Container    : "+cls.getSimpleName());
		Container container=getContainers().remove(cls.getName());
		System.err.println("Destoryed Container     : "+cls.getSimpleName());
		container.clearContainer();
		System.gc();
	}
	
	protected LinkedHashSet<Class<? extends Container>> getClassList(){
		return classList;
	}
	
	protected void register(Class<? extends Container> container) {
		Assertion.notNull(container, "Container should not be null.");
		getClassList().add(container);
	}
	
}

