/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */


package com.silverpeas.portlets.portal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 * PropertiesContext loads the content of driverconfig.properties
 */
public class PropertiesContext {
    
    private static Properties configProperties;
    //private static String CONFIG_FILE = "DriverConfig.properties";
    private static String CONFIG_FILE = "DriverConfig";
    // Constants for properties defined in the config file
    private static final String PORTLET_RENDER_MODE_PARALLEL = "portletRenderModeParallel";
    private static final String ENABLE_AUTODEPLOY = "enableAutodeploy";
    private static final String AUTODEPLOY_DIR_WATCH_INTERVAL = "autodeployDirWatchInterval";
    
    public static void init() {
        InputStream defaultConfigBundle = null;
        Properties defaultProperties = new Properties();
        try {
        	ResourceLocator properties = new ResourceLocator("com.silverpeas.portlets."+CONFIG_FILE, "");
        	defaultProperties = properties.getProperties();
            /*String configFile = PortletRegistryHelper.getConfigFileLocation() + File.separator + CONFIG_FILE;
            defaultConfigBundle = new FileInputStream(configFile);
            defaultProperties.load(defaultConfigBundle);*/
        //} catch (IOException e) {
        //    System.out.println(e);
        } finally {
            if (defaultConfigBundle != null) {
                try {
                    defaultConfigBundle.close();
                } catch (IOException e) {
                    //drop through
                }
            }
        }
        configProperties = new Properties(defaultProperties);
    }
    
    public static boolean isPortletRenderModeParallel() {
        String value = configProperties.getProperty(PORTLET_RENDER_MODE_PARALLEL);
        if("true".equals(value))
            return true;
        return false;
    }
    
    public static boolean enableAutodeploy() {
        String value = configProperties.getProperty(ENABLE_AUTODEPLOY);
        if("true".equals(value))
            return true;
        return false;
    }
    
    public static long getAutodeployDirWatchInterval() {
        String value = configProperties.getProperty(AUTODEPLOY_DIR_WATCH_INTERVAL);
        long watchInterval;
        try {
            watchInterval = Long.parseLong(value);
        } catch (NumberFormatException nfe){
            watchInterval = -1;
        }
        if(watchInterval <= 0) {
            watchInterval = 5;
        }
        return (watchInterval*1000);
    }
    
}
