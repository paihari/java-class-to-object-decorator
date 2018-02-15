package com.gundi.decorator.test.online;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by pai on 15.02.18.
 */
public enum OnlineUnitIntegrationTestEnvironment {
    DEV("DEV"), UAT("UAT");

    /**
     * Array of Servers mentioned for a defined environment in server.properties
     */
    private String[] servers;

    /**
     * The Server Port mentioned in server.properties
     */
    private String serverPort;

    /**
     * The Server properties
     */

    private Properties serverProperties;


    OnlineUnitIntegrationTestEnvironment(String environmentName) {
        fetchServerProperties();
        servers = getServers(environmentName);
        serverPort = getServerPort(environmentName);

    }


    /**
     * Helper class to fetch all the properties from server.properties
     */
    private void fetchServerProperties() {
        if (null == serverProperties) {
            serverProperties = new Properties();
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("resources.properties");
            try {
                if (null != in) {
                    serverProperties.load(in);
                } else {
                    throw new IllegalStateException("resources.properties containing server properties not present in the class path ");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * Returns the array of servers from server.properties
     * @param environmentName
     * @return
     */
    private String[] getServers(String environmentName) {
        List<String> serverNames = new ArrayList<String>();
        for (Map.Entry<Object, Object> entry : serverProperties.entrySet()) {
            String key = (String)entry.getKey();
            if(key.startsWith(environmentName)) {
                if(key.equals(environmentName+ ".server.name")) {
                    serverNames.add((String)entry.getValue());
                }
            }
        }
        return serverNames.toArray(new String[serverNames.size()]);
    }


    /**
     * Return the server port from server.properties
     * @param environmentName
     * @return
     */
    private String getServerPort(String environmentName) {
        String serverPort = null;
        for (Map.Entry<Object, Object> entry : serverProperties.entrySet()) {
            String key = (String)entry.getKey();
            if(key.startsWith(environmentName)) {
                if(key.equals(environmentName+ ".server.port")) {
                    serverPort = (String)entry.getValue();
                    break;
                }
            }
        }
        return serverPort;
    }


    /**
     * The T3S URL used to connect to the container
     * @return
     */
    public String providerURL() {
        if(servers.length == 0 || serverPort == null || serverPort.isEmpty() ) {
            throw new IllegalStateException("Server properties for Environment not present in the server.properties ");
        }
        return "http://" + serverName() + ":" + serverPort() + "/tomee/ejb";
    }

    /**
     * The gets the server name
     * if there are more than one servers a
     * random load balancing is done
     * @return
     */
    private String serverName() {
        int size = servers.length;
        if (size > 1) {
            int rand = new Random().nextInt((size));
            return servers[rand];
        } else
            return servers[0];
    }

    /**
     * Return the port of the server to connect to
     * @return
     */
    private String serverPort() {
        return serverPort;
    }




}
