package org.brijframework.container.impl.bootstrap;

import java.util.LinkedHashSet;

import org.brijframework.container.Container;
import org.brijframework.container.bootstrap.BootstrapContainer;
import org.brijframework.container.impl.AbstractContainer;
import org.brijframework.factories.Factory;
import org.brijframework.factories.bootstrap.BootstrapFactory;
import org.brijframework.support.config.DepandOn;
import org.brijframework.support.config.OrderOn;
import org.brijframework.util.asserts.Assertion;
import org.brijframework.util.printer.ConsolePrint;
import org.brijframework.util.reflect.AnnotationUtil;

public abstract class AbstractBootstrapContainer extends AbstractContainer implements BootstrapContainer{

	private LinkedHashSet<Class<? extends BootstrapFactory<?, ?>>> bootstrapFactories=new LinkedHashSet<Class<? extends BootstrapFactory<?, ?>>>();

	@Override
	public Container loadContainer() {
		ConsolePrint.screen("BootstrapContainer -> "+this.getClass().getSimpleName() , "Loading the bootstrap container for "+this.getClass().getSimpleName());
		getOrderOnSortedFactoryList(getBootstrapFactories()).forEach((metaFactory) -> {
			loadFactory((Class<? extends Factory<?, ?>>)metaFactory); 
		});
		ConsolePrint.screen("BootstrapContainer -> "+this.getClass().getSimpleName() , "Loaded the bootstrap container for "+this.getClass().getSimpleName());
		return this;
	}

	protected LinkedHashSet<Class<? extends BootstrapFactory<?, ?>>> getBootstrapFactories() {
		return bootstrapFactories;
	}
	
	protected void register(Class<? extends BootstrapFactory<?,?>> container) {
		Assertion.notNull(container, "ModuleFactory class should not be null.");
		getBootstrapFactories().add(container);
	}
	
	public LinkedHashSet<Class<? extends BootstrapFactory<?, ?>>> getDepandOnSortedFactoryList(LinkedHashSet<Class<? extends BootstrapFactory<?, ?>>> linkedHashSet) {
		LinkedHashSet<Class<? extends BootstrapFactory<?, ?>>> list=new LinkedHashSet<Class<? extends BootstrapFactory<?, ?>>>();
		linkedHashSet.stream().sorted((c1,c2)->{
			if(c1.isAnnotationPresent(DepandOn.class)) {
				return -1;
			}
			if(c2.isAnnotationPresent(DepandOn.class)) {
				return  1;
			}
			return 0;
		}).forEach(factory->{
			fillDepandOnFactory(list,factory);
		});
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private void fillDepandOnFactory(LinkedHashSet<Class<? extends BootstrapFactory<?, ?>>> list, Class<? extends BootstrapFactory<?, ?>> factory) {
		if(factory==null) {
			return;
		}
		if (factory.isAnnotationPresent(DepandOn.class)) {
			DepandOn depandOn = factory.getAnnotation(DepandOn.class);
			fillDepandOnFactory(list,( Class<? extends BootstrapFactory<?, ?>> ) depandOn.depand());
		}
		if(!list.contains(factory)) {
			list.add(factory);
		}
	}

	public Iterable<Class<? extends BootstrapFactory<?, ?>>> getOrderOnSortedFactoryList(LinkedHashSet<Class<? extends BootstrapFactory<?, ?>>> classList) {
		LinkedHashSet<Class<? extends BootstrapFactory<?, ?>>> list=new LinkedHashSet<Class<? extends BootstrapFactory<?, ?>>>();
		classList.stream().filter(c->c.isAnnotationPresent(OrderOn.class)).sorted((c1,c2)->{
			OrderOn orderOn1=(OrderOn) AnnotationUtil.getAnnotation(c1, OrderOn.class);
			OrderOn orderOn2=(OrderOn) AnnotationUtil.getAnnotation(c2, OrderOn.class);
			return Integer.compare(orderOn1.value(), orderOn2.value());
		}).forEach(factory->{
			list.add(factory);
		});
		
		classList.stream().filter(c-> !c.isAnnotationPresent(OrderOn.class)).forEach(factory->{
			list.add(factory);
		});
		return list;
	}
	
}
