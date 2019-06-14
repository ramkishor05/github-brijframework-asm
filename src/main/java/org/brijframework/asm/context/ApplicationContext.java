package org.brijframework.asm.context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.container.Container;
import org.brijframework.context.Context;
import org.brijframework.support.model.Assignable;
import org.brijframework.support.model.DepandOn;
import org.brijframework.util.reflect.MethodUtil;
import org.brijframework.util.reflect.ReflectionUtils;

public class ApplicationContext implements Context {

	private ConcurrentHashMap<Object, Container> cache = new ConcurrentHashMap<Object, Container>();

	private Context context;

	@Override
	public void initialize(Context context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void startup() {
		List<Class<? extends Context>> classes = new ArrayList<>();
		try {
			ReflectionUtils.getClassListFromExternal().forEach(cls -> {
				if (!ApplicationContext.class.isAssignableFrom(cls) && Context.class.isAssignableFrom(cls)
						&& !cls.isInterface() && cls.getModifiers() != Modifier.ABSTRACT) {
					classes.add((Class<? extends Context>) cls);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			ReflectionUtils.getClassListFromInternal().forEach(cls -> {
				if (!ApplicationContext.class.isAssignableFrom(cls) && Context.class.isAssignableFrom(cls)
						&& !cls.isInterface() && cls.getModifiers() != Modifier.ABSTRACT) {
					classes.add((Class<? extends Context>) cls);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		classes.stream().sorted((context1, context2) -> {
			if (context1.isAnnotationPresent(DepandOn.class)) {
				DepandOn depandOn = context1.getAnnotation(DepandOn.class);
				if (depandOn.depand().equals(context2)) {
					return 1;
				}
			}
			if (context2.isAnnotationPresent(DepandOn.class)) {
				DepandOn depandOn = context2.getAnnotation(DepandOn.class);
				if (depandOn.depand().equals(context1)) {
					return -1;
				}
			}
			return 0;
		}).forEach((Context) -> {
			loading(Context);
		});
	}

	private void loading(Class<?> contextClass) {
		System.err.println("Context    : " + contextClass.getName());
		boolean called = false;

		for (Method method : MethodUtil.getAllMethod(contextClass)) {
			if (method.isAnnotationPresent(Assignable.class)) {
				try {
					Context context = (Context) method.invoke(null);
					context.initialize(this);
					context.startup();
					called = true;
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		if (!called) {
			try {
				Context context = (Context) contextClass.newInstance();
				context.initialize(this);
				context.startup();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void destory() {
	}

	@Override
	public Context getParent() {
		return context;
	}

	@Override
	public ConcurrentHashMap<Object, Container> getContainers() {
		return cache;
	}

}
