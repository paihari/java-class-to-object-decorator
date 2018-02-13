package com.gundi.decorator.test.resource;

import com.gundi.decorator.test.annotations.TestRuntimeConfig;

import java.util.Map;

/**
 * Created by pai on 12.02.18.
 */
public class TestResourceDecoratorBase implements TestResourceDecorator {

    protected TestResourceDecorator testResourceDecorator;
    public Class<?> testClass = null;


    public TestResourceDecoratorBase(TestResourceDecorator testResourceDecorator) {
        this.testResourceDecorator = testResourceDecorator;
        this.testClass = testResourceDecorator.getTestClass();

    }

    public TestResourceDecoratorBase(Class<?> testClass) {
        this.testClass = testClass;
    }

    public TestResourceDecoratorBase() {

    }

    /**
     * Gets the Unit Test class for the decorators to work on
     */
    public Class<?> getTestClass() {
        return testClass;
    }


    /**
     * The Base Main Method used by the individual decorators
     */
    @Override
    public void injectTestResource(Object obj, Map<Class<?>, Object> overRidenObjects) throws Exception {
        if(testResourceDecorator != null) {
            testResourceDecorator.injectTestResource(obj, overRidenObjects);
        }
    }

    /**
     * Gets the Annotation object mentioned in the Test Class
     * @return
     */
    protected TestRuntimeConfig getTestRuntimeConfiguration() {
        TestRuntimeConfig testRuntimeCfg = getTestClass().getAnnotation(TestRuntimeConfig.class);
        Class<?> currentClass = getTestClass();
        while (testRuntimeCfg == null) {
            if (currentClass != null) {
                testRuntimeCfg = currentClass.getAnnotation(TestRuntimeConfig.class);
                currentClass = currentClass.getSuperclass();
            } else {
                throw new IllegalStateException("TestRuntimeConfig is a required annotation");
            }
        }

        return testRuntimeCfg;
    }




}