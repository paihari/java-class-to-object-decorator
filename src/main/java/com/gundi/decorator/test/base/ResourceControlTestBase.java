package com.gundi.decorator.test.base;

import com.gundi.decorator.test.annotations.TestRuntimeConfig;
import com.gundi.decorator.test.resource.ejb.EJBTestResourceDecorator;

/**
 * Created by pai on 13.02.18.
 */

@TestRuntimeConfig(name = "resource_control_test_base")
public class ResourceControlTestBase extends ComponentTestBase{

    EJBTestResourceDecorator ejbTestResourceDecorator = new EJBTestResourceDecorator();

    /**
     * Injects EJB to Test Object/EJB Object
     * @param obj
     * @param service
     * @throws Exception
     */
    protected void injectService(Object obj, Object service) throws Exception {
        ejbTestResourceDecorator.injectSingleService(obj, service);
    }


}

