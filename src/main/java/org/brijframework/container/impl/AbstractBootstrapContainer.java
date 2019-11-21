package org.brijframework.container.impl;

import org.brijframework.container.BootstrapContainer;
import org.brijframework.container.Container;
import org.brijframework.factories.Factory;
import org.brijframework.support.util.SupportUtil;
import org.brijframework.util.printer.ConsolePrint;

public abstract class AbstractBootstrapContainer extends AbstractContainer implements BootstrapContainer{

	@Override
	public Container loadContainer() {
		ConsolePrint.screen("BootstrapContainer -> "+this.getClass().getSimpleName() , "Loading the bootstrap container for "+this.getClass().getSimpleName());
		SupportUtil.getOrderOnSortedFactoryList(getClassList()).forEach((metaFactory) -> {
			loadFactory((Class<? extends Factory>)metaFactory); 
		});
		ConsolePrint.screen("BootstrapContainer -> "+this.getClass().getSimpleName() , "Loaded the bootstrap container for "+this.getClass().getSimpleName());
		return this;
	}
}
