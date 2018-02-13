package com.gundi.decorator.test.resource.ejb;

import com.gundi.decorator.test.resource.TestResourceDecorator;
import com.gundi.decorator.test.resource.TestResourceDecoratorBase;
import org.reflections.Reflections;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;




/**
 * Created by pai on 12.02.18.
 */
public class EJBTestResourceDecorator extends TestResourceDecoratorBase {

    //private static final Logger LOGGER = LoggerHelper.getLogger(EJBTestResourceDecorator.class);

    private Map<String, Object> ejbCache = new HashMap<String, Object>();

    public EJBTestResourceDecorator(TestResourceDecorator testResourceDecorator) {

        super(testResourceDecorator);
    }

    public EJBTestResourceDecorator() {

        super();
    }

    /**
     * Injects the EJB service object to the Test Object/EJB object
     * This is used from the Decorator Framework
     */
    @Override
    public void injectTestResource(Object obj, Map<Class<?>, Object> overRidenObjects) throws Exception {
        testResourceDecorator.injectTestResource(obj, overRidenObjects);
        if(getTestRuntimeConfiguration().testOnContainer()) {
            injectOnlineEJBResources(obj);
        } else {
            injectLocalEJBResource(obj, overRidenObjects);
        }
    }

    /**
     * Injects Online Remote EJBs to the Test Class Object
     * @param obj: The Test Class Object
     */
    private void injectOnlineEJBResources(Object obj) {
/*
        try {
            OnlineUnitIntegrationTestInitializer.setTestEnvironment(getOnlineUnitIntegrationTestEnvironment());
            OnlineUnitIntegrationTestInitializer.start();
            OnlineUnitIntegrationTestInitializer.stop();

            if (null != obj ) {

                List<Class<?>> classes = new ArrayList<Class<?>>();
                classes.add(0, obj.getClass());

                Class<?> superCls = obj.getClass().getSuperclass();
                while (superCls != null) {
                    classes.add(0, superCls);
                    superCls = superCls.getSuperclass();
                }

                for (Class<?> classToInject : classes) {
                    for (final Field field : classToInject.getDeclaredFields()) {
                        EJB ejb = field.getAnnotation(EJB.class);
                        if(ejb != null) {
                            field.setAccessible(true);
                            String lookUpString = ejb.mappedName() + "#" + field.getType().getName();
                            Object service = OnlineUnitIntegrationTestInitializer.lookupEBJ3(lookUpString);
                            field.set(obj, service);

                        }
                    }
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not connect to the container mentioned in server.properties "  );
        }
*/
    }

    /**
     * Injects the Locally created EJB Implementation on Object which is either the test class object or EJBs
     * This is a recursive method, which traverses through the complete EJB composition
     * @param obj: Test Class Object/EJB Implementation Object
     * @param overRidenObjects: If the Unit Test has specified any overriden Objects, these are passed to be injected.
     * @throws Exception
     */
    protected void injectLocalEJBResource(Object obj, Map<Class<?>, Object> overRidenObjects) throws Exception {
        if (null != obj ) {
            List<Class<?>> classes = new ArrayList<Class<?>>();
            classes.add(0, obj.getClass());

            Class<?> superCls = obj.getClass().getSuperclass();
            while(superCls != null) {
                classes.add(0, superCls);
                superCls = superCls.getSuperclass();
            }

            for(Class<?> classToInject : classes) {
                injectEJBsToObject(classToInject, obj, overRidenObjects);

            }
        }
    }

    /**
     * Used to Inject Local EJB Resources
     * @param refClass
     * @param obj
     * @param overRidenObjects
     * @throws Exception
     */
    private void injectEJBsToObject(Class<?> refClass, Object obj, Map<Class<?>, Object> overRidenObjects) throws Exception {

        for (final Field field : refClass.getDeclaredFields()) {
            Object serviceObject = null;
            EJB ejb = field.getAnnotation(EJB.class);

            if(ejb != null) {
                Class<?> interfaceClass = field.getType();
                if(ejbCache.containsKey(interfaceClass.getName()))  {
                    serviceObject = ejbCache.get(interfaceClass.getName());
                } else {
                    serviceObject = getServiceObject(overRidenObjects, interfaceClass, field);
                    ejbCache.put(interfaceClass.getName() + ejb.mappedName(), serviceObject);
                }

                if(serviceObject == null) {
                    String message = "The EJB object for the Interface " + interfaceClass + " could not be obtained";
                    //LOGGER.error(message);
                    throw new IllegalStateException(message);
                } else {
                    injectLocalEJBResource(serviceObject, overRidenObjects);
                    injectEntityManager(serviceObject);
                    injectJMSDestination(serviceObject);
                    field.setAccessible(true);
                    field.set(obj, serviceObject);
                }
            }
        }

        //for(final Method method: obj.getClass().getMethods()) {
        for(final Method method: obj.getClass().getDeclaredMethods()) {
            PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);

            if(postConstruct != null) {
                try {
                    method.setAccessible(true);
                    method.invoke(obj);
                } catch(Throwable t) {
                    t.printStackTrace();
                    String message = "The EJB Post Construct invocation failed on class: " + obj.getClass().getName();
                    //LOGGER.error(message);
                    throw new IllegalStateException(message);
                }
            }
        }
    }

    /**
     * Gets the EJB Object either from the cache or by Google reflection lookup
     * @param overRidenObjects
     * @param interfaceClass
     * @param field
     * @return
     * @throws Exception
     */
    private Object getServiceObject(Map<Class<?>, Object> overRidenObjects, Class<?> interfaceClass, Field field) throws Exception {
        Object serviceObject = null;
        if(overRidenObjects.containsKey(interfaceClass)) {
            serviceObject = overRidenObjects.get(interfaceClass);
        } else {
            try {
                serviceObject = getImplementationServiceObject(interfaceClass, field);
            } catch(Exception e) {
                String message = "Finding the Implementation object for : " + interfaceClass.getName() + " failed, Hint: Check the package structure";
                //LOGGER.error(message);
                throw new IllegalStateException(message);
            }

        }
        return serviceObject;

    }

    /**
     * Creates a EJB Implementation Object from Google Reflection lookup
     * @param refClass
     * @param field
     * @return
     * @throws Exception
     */
    private Object getImplementationServiceObject(Class<?> refClass, Field field) throws Exception {
        Class<?> implementationServiceClass = getImplementationServiceClass(refClass.getPackage(), refClass, field);
        if(implementationServiceClass == null) {
            return null;
        }
        return implementationServiceClass.newInstance();
    }

    /**
     * Gets EJB Implementation class from Google Reflection lookup
     * @param pkg
     * @param refClass
     * @param field
     * @return
     */
    @SuppressWarnings("unchecked")
    private Class<?> getImplementationServiceClass(Package pkg, Class<?> refClass, Field field) {
        Reflections reflections = new Reflections(pkg.getName());
        Set referenceImplClasses =  reflections.getSubTypesOf(refClass);
        if(referenceImplClasses.size() == 0) {
            return null;
        } else {
            return getEJBBean(referenceImplClasses, field);
        }
    }

    /**
     * Internal method which checks if the EJB implementation matches the EJB declaration on the Test class/EJB class
     * @param referenceImplClasses
     * @param field
     * @return
     */
    @SuppressWarnings("unchecked")
    private Class<?>  getEJBBean(Set referenceImplClasses, Field field) {
        Class<?> implClass = null;
        for(Object referenceImplClass : referenceImplClasses) {
            implClass = (Class<?>)referenceImplClass;
            EJB ejb = field.getAnnotation(EJB.class);
            String mappedName = ejb.mappedName();
            Stateless stateless = implClass.getAnnotation(Stateless.class);
            if(stateless != null) {
                if(mappedName.equals("")) {
                    return implClass;
                } else {
                    if(stateless.mappedName().equals(mappedName)) {
                        return implClass;
                    }
                }
            }
            Stateful stateful = implClass.getAnnotation(Stateful.class);
            if(stateful != null) {
                if(mappedName.equals("")) {
                    return implClass;
                } else {
                    if(stateful.mappedName().equals(mappedName)) {
                        return implClass;
                    }
                }
            }

        }
        return implClass;

    }
    /**
     * Injects the EJB service object to the Test Object/EJB object
     * Used in Control Test Framework
     * @param obj
     * @param service
     * @throws Exception
     */
    public void injectSingleService(Object obj, Object service) throws Exception {
        if (null != obj && null != service) {

            List<Class<?>> classes = new ArrayList<Class<?>>();
            classes.add(0, obj.getClass());

            Class<?> superCls = obj.getClass().getSuperclass();
            while (superCls != null) {
                classes.add(0, superCls);
                superCls = superCls.getSuperclass();
            }

            for (Class<?> classToInject : classes) {
                for (final Field field : classToInject.getDeclaredFields()) {
                    EJB ejb = field.getAnnotation(EJB.class);

                    if (ejb != null) {
                        // set access

                        if (field.getType().isAssignableFrom(service.getClass())) {
                            field.setAccessible(true);
                            if (field.get(obj) == null) {
                                field.set(obj, service);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Injects JMS Destinations, If any present in the EJB by calling the JMSResourceDecorator
     * @param obj
     * @throws Exception
     */
    protected void injectJMSDestination(Object obj) throws Exception {
//        if (null != obj ) {
//            JMSTestResourceDecorator JMSTestResourceDecorator = new JMSTestResourceDecorator();
//            JMSTestResourceDecorator.injectTestResource(obj, getTestClass());
//        }
    }

    /**
     * Injects JMS Resources, If any present in the EJB by calling the JMSResourceDecorator
     *
     * @param obj
     * @throws Exception
     */
    protected void injectEntityManager(Object obj) throws Exception {
//        if (null != obj ) {
//            PersistenceTestResourceDecorator persistenceTestResourceDecorator =
//                    new PersistenceTestResourceDecorator();
//            persistenceTestResourceDecorator.injectTestResource(obj, getTestClass());
//        }
    }

}