package com.gundi.decorator.test.base;

import com.gundi.decorator.test.resource.TestResourceDecorator;
import org.junit.After;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pai on 13.02.18.
 */
public class ResourceDecoratorTestBase extends ResourceControlTestBase {

    /**
     * Map which contains the objects which needs to be overriden
     * These are the objects, which the test class can create and
     * force the framework to use
     */
    private Map<Class<?>, Object> overRidenObjects = new HashMap<Class<?>, Object>();

    /**
     * The initialisation method, which decorates the test class and EJBs
     * @throws Exception
     */
    @Before
    public void initialze() throws Exception {
        overRideObjects();
        TestResourceDecorator decorator = TestResourceDecorator.Factory.getInstance(this.getClass());
        decorator.injectTestResource(this, overRidenObjects);


    }

    /**
     * The last method that is executed by each test.
     * For now the transaction is commited
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {

    }

    /**
     * Helper Method to get the overriden objects
     * @return
     */
    public Map<Class<?>, Object> getOverRidenObjects() {
        return overRidenObjects;
    }


    /**
     * Just a marker method, to be overriden by the Unit test, to override some EJBs
     */
    // This method is overiden in the child classes to override a specific object
    public void overRideObjects() throws Exception{
    }

}
