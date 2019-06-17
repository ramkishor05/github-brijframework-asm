package org.brijframework.asm.container;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.container.Container;
import org.brijframework.context.Context;
import org.brijframework.factories.Factory;
import org.brijframework.group.Group;
import org.brijframework.support.model.Assignable;
import org.brijframework.support.util.SupportUtil;
import org.brijframework.util.asserts.Assertion;
import org.brijframework.util.reflect.MethodUtil;

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
		boolean called = false;
		for (Method method : MethodUtil.getAllMethod(cls)) {
			if (method.isAnnotationPresent(Assignable.class)) {
				try {
					System.err.println("Factory      : " + cls.getName());
					Factory factory = (Factory) method.invoke(null);
					factory.setContainer(this);
					factory.loadFactory();
					called = true;
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		if (!called) {
			try {
				System.err.println("Factory      : " + cls.getName());
				Factory factory  = (Factory) cls.newInstance();
				factory.setContainer(this);
				factory.loadFactory();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	

	@Override
	public Container loadContainer() {
		SupportUtil.getDepandOnSortedClassFactoryList(getClassList()).forEach((metaFactory) -> {
			System.out.println(metaFactory);
			/*loadFactory((Class<? extends Factory>)metaFactory);*/
		});
		return this;
	}
	
	@Override
	public Container clearContainer() {
		getCache().clear();
		return this;
	}
}
