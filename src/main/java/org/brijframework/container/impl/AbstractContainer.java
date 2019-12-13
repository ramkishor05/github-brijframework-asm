package org.brijframework.container.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.container.Container;
import org.brijframework.context.Context;
import org.brijframework.factories.Factory;
import org.brijframework.group.Group;
import org.brijframework.support.factories.SingletonFactory;

public abstract class AbstractContainer implements DefaultContainer {
	
	private Context context;
	
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
	
	protected void loadFactory(Class<? extends Factory<?, ?>> cls) {
		if (cls==null) {
			return ;
		}
		this.invokeFactoryMethod(cls, findFactoryMethod(cls));
	}

	private void invokeFactoryMethod(Class<? extends Factory<?, ?>> cls, Method target) {
		if (target==null) {
			return ;
		}
		try {
			Factory<?, ?> factory = (Factory<?, ?>)target.invoke(null);
			factory.setContainer(this);
			factory.loadFactory();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private Method findFactoryMethod(Class<? extends Factory<?, ?>> cls) {
		for (Method method : cls.getMethods()) {
			if (Modifier.isStatic(method.getModifiers()) && method.isAnnotationPresent(SingletonFactory.class) && cls.isAssignableFrom(method.getReturnType())  ) {
				try {
					return method;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	@Override
	public Container clearContainer() {
		getCache().clear();
		return this;
	}
}
