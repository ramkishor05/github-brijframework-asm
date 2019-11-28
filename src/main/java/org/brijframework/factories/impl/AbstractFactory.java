package org.brijframework.factories.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.container.Container;
import org.brijframework.context.Context;
import org.brijframework.env.Environment;
import org.brijframework.factories.Factory;

public abstract class AbstractFactory<K,T> implements Factory<K,T> {
	Container container;
	ConcurrentHashMap<K, T> cache;

	@Override
	public Container getContainer() {
		return container;
	}

	@Override
	public void setContainer(Container container) {
		this.container = container;
	}

	@Override
	public ConcurrentHashMap<K, T> getCache() {
		if(cache==null) {
			cache=new ConcurrentHashMap<>();
		}
		return cache;
	}
	
	public T getContainer(K modelKey) {
		if (getContainer() == null) {
			return null;
		}
		return getContainer().find(modelKey);
	}

	@Override
	public Factory<K,T> clear() {
		getCache().clear();
		return this;
	}

	public T register(K key, T value) {
		preregister(key, value);
		getCache().put(key, value);
		loadContainer(key, value);
		postregister(key, value);
		return value;
	}

	@Override
	public T find(K key) {
		if(getCache().containsKey(key)) {
			return getCache().get(key);
		}
		return getContainer(key);
	}
	
	
	public Object getEnvProperty(String key) {
		Container container = getContainer();
		if(container==null) {
			return null;
		}
		Context context = container.getContext();
		if(context==null) {
			return null;
		}
		Environment environment = context.getEnvironment();
		if(environment==null) {
			return null;
		}
		return environment.get(key);
	}

	@Override
	public boolean contains(K key) {
		if(getCache().containsKey(key)) {
			return true;
		}
		return getContainer().containsObject(key);
	}
	
	protected abstract void loadContainer(K key, T value);

	protected abstract void preregister(K key, T value) ;
	
	protected abstract void postregister(K key, T value);
	
	
}
