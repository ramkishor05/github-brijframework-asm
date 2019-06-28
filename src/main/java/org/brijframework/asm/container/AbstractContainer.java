package org.brijframework.asm.container;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.container.Container;
import org.brijframework.context.Context;
import org.brijframework.factories.Factory;
import org.brijframework.group.Group;
import org.brijframework.support.config.Assignable;
import org.brijframework.support.util.SupportUtil;
import org.brijframework.util.asserts.Assertion;

public abstract class AbstractContainer implements DefaultContainer {
	
	private Context context;
	
	private LinkedHashSet<Class<? extends Factory>> classList = new LinkedHashSet<>();
	
	private ConcurrentHashMap<Object, Group> cache = new ConcurrentHashMap<>();

	@Override
	public ConcurrentHashMap<Object, Group> getCache() {
		return cache;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}
	
	@Override
	public LinkedHashSet<Class<? extends Factory>> getClassList(){
		return classList;
	}
	
	protected void register(Class<? extends Factory> container) {
		Assertion.notNull(container, "Factory class should not be null.");
		getClassList().add(container);
	}
	
	protected void loadFactory(Class<? extends Factory> cls) {
		Method target=null;
		for (Method method : cls.getMethods()) {
			if (Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(Assignable.class) && cls.isAssignableFrom(method.getReturnType())  ) {
				try {
					target=method;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}
		if (target!=null) {
			try {
				System.err.println("Factory      : " + cls.getSimpleName());
				Factory factory = (Factory)target.invoke(null);
				factory.setContainer(this);
				factory.loadFactory();
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	

	@Override
	public Container loadContainer() {
		SupportUtil.getDepandOnSortedClassFactoryList(getClassList()).forEach((metaFactory) -> {
			loadFactory((Class<? extends Factory>)metaFactory); 
		});
		return this;
	}
	
	@Override
	public Container clearContainer() {
		getCache().clear();
		return this;
	}
}
