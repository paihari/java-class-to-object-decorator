package com.gundi.decorator.test.base;

import com.gundi.decorator.test.annotations.TestRuntimeConfig;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Created by pai on 13.02.18.
 */

@TestRuntimeConfig(name="component_test_base", applicationID = "component_test_base_app")
public class ComponentTestBase {

    @BeforeClass
    public final static void setUpClass() throws Exception {

    }

    @Before
    public void setupLogging() {

    }

    protected void initRunTime() {

    }

    protected TestRuntimeConfig getTestRuntimeConfig() {
        TestRuntimeConfig testRuntimeConfig = getClass().getAnnotation(TestRuntimeConfig.class);
        Class<?> currentClass = getClass();
        while (testRuntimeConfig == null) {
            if(currentClass != null) {
                testRuntimeConfig = currentClass.getAnnotation(TestRuntimeConfig.class);
                currentClass = currentClass.getSuperclass();
            } else {
                throw new IllegalStateException("TestRuntimeConfig is a required annotation");
            }
        }
        return testRuntimeConfig;
    }

    protected String getTestRoot() {
        return getTestRuntimeConfig().testRoot();

    }

    protected String getTestApplicationId() {
        return getTestRuntimeConfig().applicationID();
    }

    protected boolean isRollBackAfterExecution() {
        return getTestRuntimeConfig().rollbackAfterExcecution();
    }



}
