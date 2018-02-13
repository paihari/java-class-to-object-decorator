package com.gundi.decorator.test.resource;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import javax.ejb.EJB;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Created by pai on 12.02.18.
 */
public interface TestResourceDecorator {

        /**
     * The Main Method that are used by individual decorators
     * @param obj
     * @param overRidenObjects
     * @throws Exception
     */
    public void injectTestResource(Object obj, Map<Class<?>, Object> overRidenObjects) throws Exception;

    /**
     * Method to get the Running Unit Test Class
     * Implemented by the TestResourceDecoratorBase
     * @return
     */
    public Class<?> getTestClass();

    /**
     * The Factory Method, which returns the type of decorator
     * This uses relection to glance the TestClass and identifies
     * the type of resources to be injected
     * @author A895680
     *
     */
    public static class Factory {
        public static TestResourceDecorator getInstance(Class<?> testClass) {
            TestResourceDecorator decorator = new TestResourceDecoratorBase(testClass);
            List<Class<?>> classes = new ArrayList<Class<?>>();
            classes.add(0, testClass);

            Class<?> superCls = testClass.getSuperclass();
            while(superCls != null) {
                classes.add(0, superCls);
                superCls = superCls.getSuperclass();
            }

            for(Class<?> classToInject : classes) {

                for (final Field field : classToInject.getDeclaredFields()) {
                    EJB ejb = field.getAnnotation(EJB.class);
                    if(ejb != null) {
                        decorator = null; //new EJBTestResourceDecorator(decorator);
                        break;
                    }
                }
                for (final Field field : classToInject.getDeclaredFields()) {
                    Resource resource = field.getAnnotation(Resource.class);
                    if(resource != null) {
                        decorator = null; //new JMSTestResourceDecorator(decorator);
                        break;
                    }
                }
                for (final Field field : classToInject.getDeclaredFields()) {
                    PersistenceContext persistenceContext = field.getAnnotation(PersistenceContext.class);
                    if(persistenceContext != null) {
                        decorator = null; //new PersistenceTestResourceDecorator(decorator);
                        break;
                    }
                }
                for (final Field field : classToInject.getDeclaredFields()) {
//                    MDB mdb = field.getAnnotation(MDB.class);
//                    if(mdb != null) {
//                        decorator = null ; //new MDBTestResourceDecorator(decorator);
//                        break;
//                    }
                }

            }
            return decorator;
        }
    }


}
