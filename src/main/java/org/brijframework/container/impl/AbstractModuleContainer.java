package org.brijframework.container.impl;

import org.brijframework.container.Container;
import org.brijframework.container.ModuleContainer;
import org.brijframework.factories.Factory;
import org.brijframework.support.util.SupportUtil;
import org.brijframework.util.printer.ConsolePrint;

public abstract class AbstractModuleContainer extends AbstractContainer implements ModuleContainer{

	@Override
	public Container loadContainer() {
		ConsolePrint.screen("ModuleContainer -> "+this.getClass().getSimpleName() , "Strating to lunch the container for "+this.getClass().getSimpleName());
		this.init();
		SupportUtil.getDepandOnSortedFactoryList(getClassList()).forEach((metaFactory) -> {
			ConsolePrint.screen("ModuleFactory -> "+metaFactory.getSimpleName() , "Lunching the module factory for "+metaFactory.getSimpleName());
			
			loadFactory((Class<? extends Factory>)metaFactory); 
			ConsolePrint.screen("ModuleFactory -> "+metaFactory.getSimpleName() , "Lunched the module factory for "+metaFactory.getSimpleName());
			
		});
		ConsolePrint.screen("ModuleContainer -> "+this.getClass().getSimpleName()  , "Successfully lunch the container for "+this.getClass().getSimpleName());
		return this;
	}
}
