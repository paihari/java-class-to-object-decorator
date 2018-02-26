package com.gundi.decorator.test.resource.persistence;

import com.gundi.decorator.test.resource.TestResourceDecorator;
import com.gundi.decorator.test.resource.TestResourceDecoratorBase;
import com.gundi.decorator.test.util.JPATestUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pai on 25.02.18.
 */
public class PersistenceTestResourceDecorator extends TestResourceDecoratorBase{

    public PersistenceTestResourceDecorator(
            TestResourceDecorator testResourceDecorator) {
        super(testResourceDecorator);
    }

    public PersistenceTestResourceDecorator() {
        super();
    }


    /**
     * Injects the Database EntityManager to the Test Object/EJB object
     * This is used from the Decorator Framework
     */
    @Override
    public void injectTestResource(Object obj, Map<Class<?>, Object> overRidenObjects) throws Exception {
        testResourceDecorator.injectTestResource(obj, overRidenObjects);
        injectTestResource(obj);
    }

    /**
     * Private method to inject DB EntityManager to Test Object/EJB Object
     * @param obj
     * @throws Exception
     */
    private void injectTestResource(Object obj) throws Exception {
        injectTestResource(obj, getTestClass());
    }

    /**
     * Injects the Database EntityManager to the Test Object/EJB object
     * Used also by other decorators
     * This is used from the Decorator Framework
     */

    public void injectTestResource(Object obj, Class<?> testClass) throws Exception {
        if (null != obj ) {
            JPATestUtil.TestEntityMangerFactory factory = JPATestUtil.getEntityManagerFactory();

            List<Class<?>> classes = new ArrayList<Class<?>>();
            classes.add(0, obj.getClass());

            Class<?> superCls = obj.getClass().getSuperclass();
            while(superCls != null) {
                classes.add(0, superCls);
                superCls = superCls.getSuperclass();
            }
            for(Class<?> classToInject : classes) {
                for (final Field field : classToInject.getDeclaredFields()) {
                    PersistenceContext persistenceContext = field.getAnnotation(PersistenceContext.class);

                    if (persistenceContext != null) {
                        // set access
                        field.setAccessible(true);

                        if (field.get(obj) == null) {
                            field.set(obj, factory.getEntityManagerByName(persistenceContext.unitName()));
                        }
                    }
                }
                for(final Method method: classToInject.getDeclaredMethods()) {
                    PersistenceContext persistenceContext = method.getAnnotation(PersistenceContext.class);
                    if(persistenceContext != null) {
                        if(method.getName().startsWith("set") && method.getParameterTypes().length == 1
                                && EntityManager.class.isAssignableFrom(method.getParameterTypes()[0])) {
                            method.setAccessible(true);
                            method.invoke(obj, new Object[] {factory.getEntityManagerByName(persistenceContext.unitName())});


                        }
                    }

                }
            }
        }
    }


}
