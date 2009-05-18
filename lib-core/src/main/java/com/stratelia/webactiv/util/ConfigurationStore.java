/*
 * ConfigurationStore.java
 *
 * Created on 17 novembre 2000, 14:08
 */
 
package com.stratelia.webactiv.util;
import java.io.FileNotFoundException;
import java.io.IOException;

/** 
 *
 * @author  jpouyadou
 * @version 
 */
public interface ConfigurationStore {
/*
 * ConfigurationStore.java
 *
 * Created on 17 novembre 2000, 13:44
 */
 
/** 
 *
 * @author  jpouyadou
 * @version 
 */
	public void serialize() throws FileNotFoundException,IOException;
	public void putProperty(String key,String value);
	public void put(String key,String value);
	public String getProperty(String key,String defaultValue);
	public String getProperty(String key);
	public String getString(String key);
	public String get(String key,String defaultValue);
	public String[] getAllNames();
}
