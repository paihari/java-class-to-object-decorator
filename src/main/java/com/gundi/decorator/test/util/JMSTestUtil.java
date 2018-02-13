package com.gundi.decorator.test.util;

import com.gundi.decorator.test.annotations.TestRuntimeConfig;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.IllegalStateException;
import java.util.*;
import java.util.Queue;


/**
 * Created by pai on 12.02.18.
 */
public class JMSTestUtil {

    public static boolean useJMSMock = false;

    private static Properties jmsProperties = null;

    //private static final Logger LOGGER = LoggerHelper.getLogger(JMSTestUtil.class);

    /**
     * Gets the JMS Destination Object from the name specified
     * as per the configuration specified in the .bindings file
     * @param fileName
     * @param name
     * @return
     */
    public static Destination getDestinationByName(String fileName, String name) {

        if (useJMSMock) {
            return getMockDestinationByName(fileName, name);
        }

        Destination destination = null;
        InitialContext ctx = null;
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        properties.put(Context.PROVIDER_URL, fileName);

        try {
            ctx = new InitialContext(properties);
            destination = (Destination) ctx.lookup(name);
            if (destination == null) {
                throw new IllegalStateException("Error setting up destination " + name);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new IllegalStateException("Error setting up destination " + name, e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
        }
        return destination;
    }

    /**
     * Gets a Mock Destination
     * @param fileName
     * @param name
     * @return
     */
    protected static Destination getMockDestinationByName(final String fileName, final String name) {
        return new Destination() {
        };
    }

    /**
     * Gets the ConnectionFactory
     * @param testClass: The Annotation parameter is the test class will be used to know the environment type
     * @return
     */
    public static ConnectionFactory getConnectionfactoryByTestEnvironment(Class<?> testClass) {
        return getConnectionFactoryByEnvironment(getTestRuntimeConfiguration(testClass).environment());
    }


    /**
     * Gets the ConnectionFactory
     * @param testClass: The Annotation parameter is the test class will be used to know the environment type
     * @return
     */
    public static XAConnectionFactory getXAConnectionfactoryByTestEnvironment(Class<?> testClass) {
        return getXAConnectionFactoryByEnvironment(getTestRuntimeConfiguration(testClass).environment());
    }


    /**
     * Gets the ConnectionFactory for the specific environment
     * @param environment
     * @return
     */
    protected static ConnectionFactory getConnectionFactoryByEnvironment(String environment) {
        String ldapServerURL = null;
        String name = null;
        Properties props = JMSTestUtil.getJMSProperties();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String)entry.getKey();
            if(key.startsWith(environment)) {
                if(key.equals(environment+ ".ldap.sever")) {
                    ldapServerURL = (String)entry.getValue();
                } else if (key.equals(environment+ ".ldap.cn")) {
                    name = (String)entry.getValue();
                }
            }
        }

        return getConnectionFactoryByName(ldapServerURL,
                name);
    }


    /**
     * Gets the ConnectionFactory for the specific environment
     * @param environment
     * @return
     */
    protected static XAConnectionFactory getXAConnectionFactoryByEnvironment(String environment) {
        String ldapServerURL = null;
        String name = null;
        Properties props = JMSTestUtil.getJMSProperties();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String)entry.getKey();
            if(key.startsWith(environment)) {
                if(key.equals(environment+ ".ldap.sever")) {
                    ldapServerURL = (String)entry.getValue();
                } else if (key.equals(environment+ ".ldap.cn")) {
                    name = (String)entry.getValue();
                }
            }
        }

        return getXAConnectionFactoryByName(ldapServerURL,
                name);
    }


    /**
     * Gets the access to the Annoation TestRuntimeConfig mentioned in the Unit Test
     * @param testClass
     * @return
     */
    protected static TestRuntimeConfig getTestRuntimeConfiguration(Class<?> testClass) {
        TestRuntimeConfig testRuntimeCfg = testClass.getAnnotation(TestRuntimeConfig.class);
        Class<?> currentClass = testClass;
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


    /**
     * Gets the coonection factory as per the information for the specific environment in jms.properties
     * @param ldapServerUrl
     * @param name
     * @return
     */
    protected static ConnectionFactory getConnectionFactoryByName(String ldapServerUrl, String name) {

        if (useJMSMock) {
            return getMockConnectionFactoryByName(ldapServerUrl, name);
        }

        if (ldapServerUrl == null || name == null ) {
            throw new IllegalStateException("JMS Property jms.properties for the enviorment is not set ");
        }

        ConnectionFactory factory = null;

        InitialContext ctx = null;
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put(Context.SECURITY_AUTHENTICATION, "none");
        properties.put(Context.PROVIDER_URL, ldapServerUrl);
        try {
            ctx = new InitialContext(properties);
            factory = (ConnectionFactory) ctx.lookup(name);
            //factory =
            if (factory == null) {
                //log.error("Error setting up connection factory " + name + " at " + ldapServerUrl);
                throw new IllegalStateException("Error setting up connection factory " + name + " at "
                        + ldapServerUrl + " Check the JMS Configuration/JMS Resource Annotation Parameters ");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            //log.error("Error setting up connection factory " + name + " at " + ldapServerUrl, e);
            throw new IllegalStateException("Error setting up connection factory "
                    + name + " at " + ldapServerUrl
                    + " Check the JMS Configuration/JMS Resource Annotation Parameters ", e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    //log.error("NamingException", e);
                    e.printStackTrace();
                }
            }
        }

        return factory;
    }


    /**
     * Gets the coonection factory as per the information for the specific environment in jms.properties
     * @param ldapServerUrl
     * @param name
     * @return
     */
    protected static XAConnectionFactory getXAConnectionFactoryByName(String ldapServerUrl, String name) {


        if (ldapServerUrl == null || name == null ) {
            throw new IllegalStateException("JMS Property jms.properties for the enviorment is not set ");
        }

        XAConnectionFactory factory = null;

        InitialContext ctx = null;
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put(Context.SECURITY_AUTHENTICATION, "none");
        properties.put(Context.PROVIDER_URL, ldapServerUrl);
        try {
            ctx = new InitialContext(properties);
            factory = (XAConnectionFactory) ctx.lookup(name);

            if (factory == null) {
                //log.error("Error setting up connection factory " + name + " at " + ldapServerUrl);
                throw new IllegalStateException("Error setting up connection factory " + name + " at "
                        + ldapServerUrl + " Check the JMS Configuration/JMS Resource Annotation Parameters ");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            //log.error("Error setting up connection factory " + name + " at " + ldapServerUrl, e);
            throw new IllegalStateException("Error setting up connection factory "
                    + name + " at " + ldapServerUrl
                    + " Check the JMS Configuration/JMS Resource Annotation Parameters ", e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    //log.error("NamingException", e);
                    e.printStackTrace();
                }
            }
        }

        return factory;
    }


    /**
     * Gets a Mock Connection Factory
     * @param ldapServerUrl
     * @param name
     * @return
     */
    protected static ConnectionFactory getMockConnectionFactoryByName(final String ldapServerUrl, final String name) {
        return new ConnectionFactory() {
            @Override
            public Connection createConnection() throws JMSException {
                return new Connection() {
                    @Override
                    public void close() throws JMSException {
                    }

                    @Override
                    public ConnectionConsumer createConnectionConsumer(Destination arg0, String arg1,
                                                                       ServerSessionPool arg2, int arg3) throws JMSException {
                        return null;
                    }

                    @Override
                    public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
                        return null;
                    }

                    @Override
                    public ConnectionConsumer createDurableConnectionConsumer(Topic arg0, String arg1, String arg2,
                                                                              ServerSessionPool arg3, int arg4) throws JMSException {
                        return null;
                    }

                    @Override
                    public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
                        return null;
                    }

                    @Override
                    public Session createSession(boolean arg0, int arg1) throws JMSException {
                        return new Session() {
                            @Override
                            public void close() throws JMSException {
                            }

                            @Override
                            public void commit() throws JMSException {
                            }

                            //@Override
                            public QueueBrowser createBrowser(Queue arg0) throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                           // @Override
                            public QueueBrowser createBrowser(Queue arg0, String arg1) throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public BytesMessage createBytesMessage() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public MessageConsumer createConsumer(Destination arg0) throws JMSException {
                                return null;
                            }

                            @Override
                            public MessageConsumer createConsumer(Destination arg0, String arg1) throws JMSException {
                                return null;
                            }

                            @Override
                            public MessageConsumer createConsumer(Destination arg0, String arg1, boolean arg2)
                                    throws JMSException {
                                return null;
                            }

                            @Override
                            public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) throws JMSException {
                                return null;
                            }

                            @Override
                            public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) throws JMSException {
                                return null;
                            }

                            @Override
                            public javax.jms.Queue createQueue(String queueName) throws JMSException {
                                return null;
                            }

                            @Override
                            public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1) throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1, String arg2,
                                                                           boolean arg3) throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public MessageConsumer createDurableConsumer(Topic topic, String name) throws JMSException {
                                return null;
                            }

                            @Override
                            public MessageConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
                                return null;
                            }

                            @Override
                            public MessageConsumer createSharedDurableConsumer(Topic topic, String name) throws JMSException {
                                return null;
                            }

                            @Override
                            public MessageConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) throws JMSException {
                                return null;
                            }

                            @Override
                            public QueueBrowser createBrowser(javax.jms.Queue queue) throws JMSException {
                                return null;
                            }

                            @Override
                            public QueueBrowser createBrowser(javax.jms.Queue queue, String messageSelector) throws JMSException {
                                return null;
                            }

                            @Override
                            public MapMessage createMapMessage() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public Message createMessage() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public ObjectMessage createObjectMessage() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public ObjectMessage createObjectMessage(Serializable arg0) throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public MessageProducer createProducer(final Destination destination) throws JMSException {
                                return new MessageProducer() {
                                    @Override
                                    public void close() throws JMSException {
                                    }

                                    @Override
                                    public int getDeliveryMode() throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public Destination getDestination() throws JMSException {
                                        return destination;
                                    }

                                    @Override
                                    public boolean getDisableMessageID() throws JMSException {
                                        return false;
                                    }

                                    @Override
                                    public boolean getDisableMessageTimestamp() throws JMSException {
                                        return false;
                                    }

                                    @Override
                                    public int getPriority() throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public long getTimeToLive() throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public void setDeliveryDelay(long deliveryDelay) throws JMSException {

                                    }

                                    @Override
                                    public long getDeliveryDelay() throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public void send(Message arg0) throws JMSException {
                                    }

                                    @Override
                                    public void send(Destination arg0, Message arg1) throws JMSException {
                                    }

                                    @Override
                                    public void send(Message arg0, int arg1, int arg2, long arg3) throws JMSException {
                                    }

                                    @Override
                                    public void send(Destination arg0, Message arg1, int arg2, int arg3, long arg4)
                                            throws JMSException {
                                    }

                                    @Override
                                    public void send(Message message, CompletionListener completionListener) throws JMSException {

                                    }

                                    @Override
                                    public void send(Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {

                                    }

                                    @Override
                                    public void send(Destination destination, Message message, CompletionListener completionListener) throws JMSException {

                                    }

                                    @Override
                                    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {

                                    }

                                    @Override
                                    public void setDeliveryMode(int arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setDisableMessageID(boolean arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setDisableMessageTimestamp(boolean arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setPriority(int arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setTimeToLive(long arg0) throws JMSException {
                                    }
                                };
                            }

//                            @Override
//                            public Queue createQueue(String arg0) throws JMSException {
//                                throw new UnsupportedOperationException();
//                            }

                            @Override
                            public StreamMessage createStreamMessage() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public TemporaryQueue createTemporaryQueue() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public TemporaryTopic createTemporaryTopic() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public TextMessage createTextMessage() throws JMSException {
                                return new TextMessage() {
                                    @Override
                                    public void acknowledge() throws JMSException {
                                    }

                                    @Override
                                    public void clearBody() throws JMSException {
                                    }

                                    @Override
                                    public <T> T getBody(Class<T> c) throws JMSException {
                                        return null;
                                    }

                                    @Override
                                    public boolean isBodyAssignableTo(Class c) throws JMSException {
                                        return false;
                                    }

                                    @Override
                                    public void clearProperties() throws JMSException {
                                    }

                                    @Override
                                    public boolean getBooleanProperty(String arg0) throws JMSException {
                                        return false;
                                    }

                                    @Override
                                    public byte getByteProperty(String arg0) throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public double getDoubleProperty(String arg0) throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public float getFloatProperty(String arg0) throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public int getIntProperty(String arg0) throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public String getJMSCorrelationID() throws JMSException {
                                        return null;
                                    }

                                    @Override
                                    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
                                        return null;
                                    }

                                    @Override
                                    public int getJMSDeliveryMode() throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public Destination getJMSDestination() throws JMSException {
                                        return null;
                                    }

                                    @Override
                                    public long getJMSExpiration() throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public String getJMSMessageID() throws JMSException {
                                        return null;
                                    }

                                    @Override
                                    public int getJMSPriority() throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public boolean getJMSRedelivered() throws JMSException {
                                        return false;
                                    }

                                    @Override
                                    public Destination getJMSReplyTo() throws JMSException {
                                        return null;
                                    }

                                    @Override
                                    public long getJMSTimestamp() throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public String getJMSType() throws JMSException {
                                        return null;
                                    }

                                    @Override
                                    public long getLongProperty(String arg0) throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public Object getObjectProperty(String arg0) throws JMSException {
                                        return null;
                                    }


                                    @SuppressWarnings("unchecked")
                                    @Override
                                    public Enumeration getPropertyNames() throws JMSException {
                                        return null;
                                    }

                                    @Override
                                    public short getShortProperty(String arg0) throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public String getStringProperty(String arg0) throws JMSException {
                                        return null;
                                    }

                                    @Override
                                    public boolean propertyExists(String arg0) throws JMSException {
                                        return false;
                                    }

                                    @Override
                                    public void setBooleanProperty(String arg0, boolean arg1) throws JMSException {
                                    }

                                    @Override
                                    public void setByteProperty(String arg0, byte arg1) throws JMSException {
                                    }

                                    @Override
                                    public void setDoubleProperty(String arg0, double arg1) throws JMSException {
                                    }

                                    @Override
                                    public void setFloatProperty(String arg0, float arg1) throws JMSException {
                                    }

                                    @Override
                                    public void setIntProperty(String arg0, int arg1) throws JMSException {
                                    }

                                    @Override
                                    public void setJMSCorrelationID(String arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setJMSCorrelationIDAsBytes(byte[] arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setJMSDeliveryMode(int arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setJMSDestination(Destination arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setJMSExpiration(long arg0) throws JMSException {
                                    }

                                    @Override
                                    public long getJMSDeliveryTime() throws JMSException {
                                        return 0;
                                    }

                                    @Override
                                    public void setJMSDeliveryTime(long deliveryTime) throws JMSException {

                                    }

                                    @Override
                                    public void setJMSMessageID(String arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setJMSPriority(int arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setJMSRedelivered(boolean arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setJMSReplyTo(Destination arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setJMSTimestamp(long arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setJMSType(String arg0) throws JMSException {
                                    }

                                    @Override
                                    public void setLongProperty(String arg0, long arg1) throws JMSException {
                                    }

                                    @Override
                                    public void setObjectProperty(String arg0, Object arg1) throws JMSException {
                                    }

                                    @Override
                                    public void setShortProperty(String arg0, short arg1) throws JMSException {
                                    }

                                    @Override
                                    public void setStringProperty(String arg0, String arg1) throws JMSException {
                                    }

                                    @Override
                                    public String getText() throws JMSException {
                                        return null;
                                    }

                                    @Override
                                    public void setText(String arg0) throws JMSException {
                                    }
                                };
                            }

                            @Override
                            public TextMessage createTextMessage(String arg0) throws JMSException {
                                return null;
                            }

                            @Override
                            public Topic createTopic(String arg0) throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public int getAcknowledgeMode() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public MessageListener getMessageListener() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public boolean getTransacted() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public void recover() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public void rollback() throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public void run() {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public void setMessageListener(MessageListener arg0) throws JMSException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public void unsubscribe(String arg0) throws JMSException {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }

                    @Override
                    public Session createSession(int sessionMode) throws JMSException {
                        return null;
                    }

                    @Override
                    public Session createSession() throws JMSException {
                        return null;
                    }

                    @Override
                    public String getClientID() throws JMSException {
                        return null;
                    }

                    @Override
                    public ExceptionListener getExceptionListener() throws JMSException {
                        return null;
                    }

                    @Override
                    public ConnectionMetaData getMetaData() throws JMSException {
                        return null;
                    }

                    @Override
                    public void setClientID(String arg0) throws JMSException {
                    }

                    @Override
                    public void setExceptionListener(ExceptionListener arg0) throws JMSException {
                    }

                    @Override
                    public void start() throws JMSException {
                    }

                    @Override
                    public void stop() throws JMSException {
                    }
                };
            }

            @Override
            public Connection createConnection(String arg0, String arg1) throws JMSException {
                return null;
            }

            @Override
            public JMSContext createContext() {
                return null;
            }

            @Override
            public JMSContext createContext(String userName, String password) {
                return null;
            }

            @Override
            public JMSContext createContext(String userName, String password, int sessionMode) {
                return null;
            }

            @Override
            public JMSContext createContext(int sessionMode) {
                return null;
            }
        };
    }

    /**
     * Gets the properties from the jms.properties file
     * @return
     */
    protected static Properties getJMSProperties() {
        if (null == jmsProperties) {
            jmsProperties = new Properties();
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("jms.properties");
            try {
                if (null != in) {
                    jmsProperties.load(in);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return jmsProperties;
    }


    /**
     * Cleans up the resources
     * @param connection
     * @param session
     * @param consumer
     */
    public static void cleanupResources(final Connection connection, final Session session,
                                        final MessageConsumer consumer) {
        try {
            if (consumer != null) {
                consumer.close();
            }
        } catch (Throwable bad) {
            //LOGGER.fine("Error closing consumer [" + consumer + "]", bad);
        } finally {
            // cannot stop in container
            cleanupResources(connection, session, true);
        }
    }

    /**¨
     * Cleans up the resources
     * @param connection
     * @param session
     * @param stop
     */
    public static void cleanupResources(final Connection connection, final Session session, final boolean stop) {
        try {
            if (session != null) {
                session.close();
            }
        } catch (Throwable bad) {
           // LOGGER.fine("Error closing session [" + session + "]", bad);
        } finally {
            try {
                if (connection != null) {
                    if (stop) {
                        try {
                            connection.stop();
                        } finally {
                            connection.close();
                        }
                    } else {
                        connection.close();
                    }
                }
            } catch (Throwable bad) {
               // LOGGER.fine("Error closing connection [" + connection + "]", bad);
            }
        }
    }

}