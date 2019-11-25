package org.brijframework.container.impl.module;

import java.util.LinkedHashSet;

import org.brijframework.container.impl.AbstractContainer;
import org.brijframework.container.module.ModuleContainer;
import org.brijframework.factories.module.ModuleFactory;
import org.brijframework.support.config.DepandOn;
import org.brijframework.support.config.OrderOn;
import org.brijframework.util.asserts.Assertion;
import org.brijframework.util.printer.ConsolePrint;
import org.brijframework.util.reflect.AnnotationUtil;

public abstract class AbstractModuleContainer extends AbstractContainer implements ModuleContainer{

	private LinkedHashSet<Class<? extends ModuleFactory<?,?>>> classList = new LinkedHashSet<>();
	
	@Override
	public AbstractModuleContainer loadContainer() {
		ConsolePrint.screen("ModuleContainer -> "+this.getClass().getSimpleName() , "Strating to lunch the container for "+this.getClass().getSimpleName());
		this.init();
		getDepandOnSortedFactoryList(getModuleFactories()).forEach((metaFactory) -> {
			ConsolePrint.screen("ModuleFactory -> "+metaFactory.getSimpleName() , "Lunching the module factory for "+metaFactory.getSimpleName());
			loadFactory((Class<? extends ModuleFactory<?,?>>)metaFactory); 
			ConsolePrint.screen("ModuleFactory -> "+metaFactory.getSimpleName() , "Lunched the module factory for "+metaFactory.getSimpleName());
		});
		ConsolePrint.screen("ModuleContainer -> "+this.getClass().getSimpleName()  , "Successfully lunch the container for "+this.getClass().getSimpleName());
		return this;
	}
	
	protected LinkedHashSet<Class<? extends ModuleFactory<?,?>>> getModuleFactories() {
		return classList;
	}

	protected void register(Class<? extends ModuleFactory<?,?>> container) {
		Assertion.notNull(container, "ModuleFactory class should not be null.");
		getModuleFactories().add(container);
	}
	
	protected LinkedHashSet<Class<? extends ModuleFactory<?,?>>> getDepandOnSortedFactoryList(LinkedHashSet<Class<? extends ModuleFactory<?,?>>> linkedHashSet) {
		LinkedHashSet<Class<? extends ModuleFactory<?,?>>> list=new LinkedHashSet<Class<? extends ModuleFactory<?,?>>>();
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
	private void fillDepandOnFactory(LinkedHashSet<Class<? extends ModuleFactory<?,?>>> list, Class<? extends ModuleFactory<?,?>> factory) {
		if(factory==null) {
			return;
		}
		if (factory.isAnnotationPresent(DepandOn.class)) {
			DepandOn depandOn = factory.getAnnotation(DepandOn.class);
			fillDepandOnFactory(list,( Class<? extends ModuleFactory<?,?>> ) depandOn.depand());
		}
		if(!list.contains(factory)) {
			list.add(factory);
		}
	}

	public Iterable<Class<? extends ModuleFactory<?,?>>> getOrderOnSortedFactoryList(LinkedHashSet<Class<? extends ModuleFactory<?,?>>> classList) {
		LinkedHashSet<Class<? extends ModuleFactory<?,?>>> list=new LinkedHashSet<Class<? extends ModuleFactory<?,?>>>();
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
