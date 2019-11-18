package org.brijframework.factories.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.brijframework.container.Container;
import org.brijframework.factories.Factory;

public abstract class AbstractFactory<K,T> implements Factory {
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

	@Override
	public Factory clear() {
		getCache().clear();
		return this;
	}

	public void register(K key, T value) {
		preregister(key, value);
		getCache().put(key, value);
		postregister(key, value);
	}
	
	protected abstract void preregister(K key, T value) ;
	
	protected abstract void postregister(K key, T value);
	
}
