package com.gundi.decorator.test.online;

import javax.naming.InitialContext;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by pai on 15.02.18.
 */
public class OnlineUnitIntegrationTestInitializer {

    private static OnlineUnitIntegrationTestEnvironment testEnvironment =
            OnlineUnitIntegrationTestEnvironment.DEV;
    private static boolean IsInitialized = false;
    private static boolean IsConnected = false;


    /**
     * Gets the TestEnvironment: DEV or UAT
     * @return
     */
    public static OnlineUnitIntegrationTestEnvironment getTestEnvironment() {
        return testEnvironment;
    }

    /**
     * Set the OnlineTestEnvironement
     * @param testEnvironment
     */
    public static void setTestEnvironment(OnlineUnitIntegrationTestEnvironment testEnvironment) {
        OnlineUnitIntegrationTestInitializer.testEnvironment = testEnvironment;
    }


    /**
     * Helper method to connect to the Container
     *
     * @throws Exception
     */
    public synchronized static void start() throws Exception {
        if (!IsInitialized) {
            connect(testEnvironment.providerURL());
            IsInitialized = true;
        }
    }


    /**
     * Helper method to stop the connect to the Container
     *
     * @throws Exception
     */

    public synchronized static void stop() throws Exception {
        if (IsInitialized) {
            disconnect();
            IsInitialized = false;
        }
        disconnect();
    }

    /**
     * Helper Method which connects using the pid and URL mentioned in server.properties
     *
     * @param serverURL
     * @throws Exception
     */
    private synchronized static void connect(String serverURL) throws Exception {
        IsConnected = true;

    }

    /**
     * Helper method to disconnect from the Container
     * @throws Exception
     */
    protected synchronized static void disconnect() throws Exception {
        if (IsConnected) {
            IsConnected = false;
        }
    }

    private final static ReadWriteLock lock = new ReentrantReadWriteLock();
    private static Map<String, Object> services = new HashMap<String, Object>();

    /**
     * Helper Class to Look Up Remote EJBs
     * @param jndi
     * @return
     * @throws Exception
     * "java:global/web/MySimpleEJBBean!com.gundi.decorator.example.services.MySimpleEJB";  Full descriptor for OpenEJB binding
     */
    public final static Object lookupEBJ3(String jndi) throws Exception {

        Object serviceReference = services.get(jndi);
        System.out.println("JNDI lookup String " + jndi);


        if (null == serviceReference) {
            lock.writeLock().lock();

            try {
                Hashtable<Object,Object> env = new Hashtable<Object,Object>();
                env.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
                env.put("java.naming.provider.url", testEnvironment.providerURL());
                //env.put("java.naming.provider.url", "ejbd://localhost:8080");
                //env.put("java.naming.provider.url", "http://localhost:8080/tomee/ejb");

                InitialContext ctx = new InitialContext(env);
                serviceReference = ctx.lookup(jndi);
                services.put(jndi, serviceReference);
            } catch(Exception e) {
                e.printStackTrace();
            }
            finally {
                lock.writeLock().unlock();
            }
        }

        return serviceReference;
    }
}



