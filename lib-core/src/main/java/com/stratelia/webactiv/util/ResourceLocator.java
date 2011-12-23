/**
 * Copyright (C) 2000 - 2011 Silverpeas
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p/>
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of
 * the text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * ResourceLocator.java
 * <p/>
 * Created on 19 octobre 2000, 09:54
 */
package com.stratelia.webactiv.util;

import com.silverpeas.util.ConfigurationClassLoader;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourceBundleWrapper;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class ResourceLocator implements Serializable {

  private static final long serialVersionUID = -2389291572691404932L;
  private static ClassLoader loader =
          new ConfigurationClassLoader(ResourceLocator.class.getClassLoader());
  final static String DEFAULT_EXTENSION = ".properties";
  private String propertyFile = null;
  private Locale propertyLocale = null;
  private ResourceLocator defaultResource = null;

  // --------------------------------------------------------------------------------------------
  // METHODS for .properties
  // --------------------------------------------------------------------------------------------
  /** Creates new ResourceLocator */
  public ResourceLocator() {
  }

  /**
   * Create a resource locator with the given property file (Ex: com.stratelia.webactiv.util.util)
   * Use the function getString to get the parameters from this instance
   * @param sPropertyFile
   * @param sLanguage
   */
  public ResourceLocator(String sPropertyFile, String sLanguage) {
    this(sPropertyFile, sLanguage, null);
  }

  public ResourceLocator(String sPropertyFile, String sLanguage, ResourceLocator defaultResource) {
    this.defaultResource = defaultResource;
    propertyFile = sPropertyFile;
    if (StringUtil.isDefined(sLanguage)) {
      propertyLocale = new Locale(sLanguage);
    } else {
      propertyLocale = Locale.getDefault();
    }
  }

  /**
   * Create a resource locator with the given property file (Ex: com.stratelia.webactiv.util.util)
   * Use the function getString to get the parameters from this instance
   * @deprecated
   */
  public ResourceLocator(String sPropertyFile, Locale sLocale) {
    propertyFile = sPropertyFile;
    if (sLocale != null) {
      propertyLocale = sLocale;
    } else {
      propertyLocale = Locale.getDefault();
    }
  }

  /**
   * Set properties of a SilverPeas component.
   * @param sPropertyFile
   * @param sLanguage
   */
  public void setPropertyLocation(String sPropertyFile, String sLanguage) {
    propertyFile = sPropertyFile;
    if (sLanguage != null) {
      propertyLocale = new Locale(sLanguage);
    } else {
      propertyLocale = Locale.getDefault();
    }
  }

  /**
   * Switchs this resource locator to the specified language for the same refered property file.
   * @param sLanguage the language to use for getting property values.
   */
  public void setLanguage(final String sLanguage) {
    if (sLanguage != null) {
      propertyLocale = new Locale(sLanguage);
    } else {
      propertyLocale = Locale.getDefault();
    }
  }

  private ResourceBundle getResourceBundle(String sPropertyFile, Locale locale) {
    return new ResourceBundleWrapper(sPropertyFile, propertyLocale);
  }

  /**
   * Return the value of the given attribut in the Property created with the ResourceLocator
   * constructor
   * @param sAttribut
   * @return
   */
  public String getString(String sAttribut) {
    ResourceBundle bundle = this.getResourceBundle(propertyFile, propertyLocale);
    try {
      if (!bundle.containsKey(sAttribut) && this.defaultResource != null) {
        return this.defaultResource.getString(sAttribut);
      }
      return bundle.getString(sAttribut);
    } catch (MissingResourceException msrex) {
      SilverTrace.warn("util", "ResourceLocator.getString", "util.MSG_NO_ATTR_VALUE",
              "File : " + propertyFile + " | Attribut : " + sAttribut);
      return null;
    }
  }

  /**
   * Return the value of the given attribut in the Property created with the ResourceLocator
   * constructor
   * @param sAttribut
   * @param defaultValue
   * @return
   */
  public String getString(String sAttribut, String defaultValue) {
    String sReturn = getString(sAttribut);
    if (!StringUtil.isDefined(sReturn)) {
      sReturn = defaultValue;
    }
    return sReturn;
  }

  /**
   * Return the value of the given attribut in the Property created with the ResourceLocator
   * constructor
   * @param sAttribut
   * @param defaultValue
   * @return
   */
  public boolean getBoolean(String sAttribut, boolean defaultValue) {
    String value = getString(sAttribut);
    if (value == null || value.trim().isEmpty()) {
      return defaultValue;
    }
    return StringUtil.getBooleanValue(value);
  }

  /**
   * Gets the value as a long of the specified attributes in the resource bundle located by this
   * ResourceLocator.
   * It no such attribute exists or has no value, then returns the specified default value.
   * @param sAttribute the attribute to look up in the resource bundle.
   * @param defaultValue the default value to return wether no such property exists in the resource
   * bundle.
   * @return the value as a long.
   */
  public long getLong(final String sAttribute, long defaultValue) {
    String value = getString(sAttribute);
    if (value == null || value.trim().isEmpty() && !StringUtil.isLong(value)) {
      return defaultValue;
    }
    return Long.parseLong(value);
  }

  /**
   * Gets the value as a float of the specified attributes in the resource bundle located by this
   * ResourceLocator.
   * It no such attribute exists or has no value, then returns the specified default value.
   * @param sAttribute the attribute to look up in the resource bundle.
   * @param defaultValue the default value to return wether no such property exists in the resource
   * bundle.
   * @return the value as a float.
   */
  public float getFloat(final String sAttribute, float defaultValue) {
    String value = getString(sAttribute);
    if (value == null || value.trim().isEmpty() && !StringUtil.isFloat(value)) {
      return defaultValue;
    }
    return Float.parseFloat(value);
  }

  /**
   * Gets the value as an integer of the specified attributes in the resource bundle located by this
   * ResourceLocator.
   * It no such attribute exists or has no value, then returns the specified default value.
   * @param sAttribute the attribute to look up in the resource bundle.
   * @param defaultValue the default value to return wether no such property exists in the resource
   * bundle.
   * @return the value as an integer.
   */
  public int getInteger(final String sAttribute, int defaultValue) {
    String value = getString(sAttribute);
    if (value == null || value.trim().isEmpty() && !StringUtil.isInteger(value)) {
      return defaultValue;
    }
    return Integer.parseInt(value);
  }

  public String getStringWithParam(String resName, String param) {
    String[] params = {param};
    return getStringWithParams(resName, params);
  }

  public String getStringWithParams(String resName, String[] params) {
    String theSt = getString(resName);
    if (theSt != null) {
      StringBuilder theResult = new StringBuilder();
      int theStarIndex = -1;
      int theParamIndex = 0;
      int thePreviousIndex = 0;

      theStarIndex = theSt.indexOf('*');
      while ((theStarIndex >= 0) && (theParamIndex < params.length)) {
        theResult.append(theSt.substring(thePreviousIndex, theStarIndex));
        theResult.append(params[theParamIndex++]);
        thePreviousIndex = theStarIndex + 1;
        if (thePreviousIndex < theSt.length()) {
          theStarIndex = theSt.indexOf('*', thePreviousIndex);
        } else {
          theStarIndex = -1;
        }
      }
      theResult.append(theSt.substring(thePreviousIndex));
      return theResult.toString();
    }
    return null;
  }

  /**
   * Read a String-List from a Settings-file with indexes from 1 to n If max is -1, the functions
   * reads until the propertie's Id is not found. If max >= 1, the functions returns an array of
   * 'max' elements (the elements not found are set to "")
   * @param propNamePrefix
   * @param propNameSufix
   * @param max the maximum index (-1 for no maximum value)
   * @return
   * @see
   */
  public String[] getStringArray(String propNamePrefix, String propNameSufix, int max) {
    int i = 1;
    List<String> valret = new ArrayList<String>();
    while ((i <= max) || (max == -1)) {
      String s = getString(propNamePrefix + java.lang.Integer.toString(i) + propNameSufix, null);
      if (s != null) {
        valret.add(s);
      } else {
        if (max == -1) {
          max = i;
        } else {
          valret.add("");
        }
      }
      i++;
    }
    return valret.toArray(new String[valret.size()]);
  }

  /** Return an enumeration of all keys in the property file loaded
   * @return
   */
  public Enumeration<String> getKeys() {
    ResourceBundle bundle = this.getResourceBundle(propertyFile, propertyLocale);
    return bundle.getKeys();
  }

  public ResourceBundle getResourceBundle() {
    return this.getResourceBundle(propertyFile, propertyLocale);
  }

  /** Return the properties *
   * @return
   */
  public Properties getProperties() {
    ResourceBundle bundle = this.getResourceBundle(propertyFile, propertyLocale);
    Properties props;
    if (this.defaultResource != null) {
      props = new Properties(this.defaultResource.getProperties());
    } else {
      props = new Properties();
    }
    Enumeration<String> keys = bundle.getKeys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      props.setProperty(key, bundle.getString(key));
    }
    return props;
  }

  public String getLanguage() {
    return propertyLocale.getLanguage();
  }

  public static void resetResourceLocator() {
    SilverTrace.info("util", "ResourceLocator.resetResourceLocator",
            "root.MSG_GEN_ENTER_METHOD", "Reset Cache Resource Locator");
    ResourceBundle.clearCache();
  }

  // --------------------------------------------------------------------------------------------
  // METHODS for .XML properties
  // --------------------------------------------------------------------------------------------
  public static URL getResource(Object object, Locale loc, String configFile, String extension) {
    String ext = extension;
    if (ext == null) {
      ext = DEFAULT_EXTENSION;
    }
    if (!ext.startsWith(".")) {
      ext = '.' + ext;
    }
    URL url = locateResource(object, loc, configFile, ext);
    if (url == null) {
      if (loc != null) {
        url = locateResource(object, null, configFile, ext);
        if (url == null) {
          if (object != null) {
            url = object.getClass().getResource(configFile);
            if (url == null) {
              url = object.getClass().getResource(configFile + ext);
            }
          }
        }
      }
    }
    return url;
  }

  public static InputStream getResourceAsStream(Object object, Locale loc, String configFile,
          String extension) {
    String fileExtension = extension;
    if (extension == null) {
      fileExtension = DEFAULT_EXTENSION;
    }
    if (!fileExtension.startsWith(".")) {
      fileExtension = '.' + fileExtension;
    }
    SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
            "Starting with args:Object = " + object + ", loc=" + loc + ", ConfigFile="
            + configFile + ", extension=" + fileExtension);
    InputStream inputStream = locateResourceAsStream(object, loc, configFile, fileExtension);
    if (inputStream == null) {
      if (loc != null) {
        inputStream = locateResourceAsStream(object, null, configFile, fileExtension);
      }
      if (inputStream == null) {
        if (object != null) {
          SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
                  "Calling getClass for object '" + object + "'");
          Class<?> clazz = object.getClass();
          SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
                  "calling getResourceAsStream(" + configFile + ")");
          inputStream = clazz.getResourceAsStream(configFile);
          if (inputStream == null) {
            String extendedFile = configFile + fileExtension;
            SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
                    "calling getResourceAsStream(" + extendedFile + ")");
            inputStream = clazz.getResourceAsStream(extendedFile);
            if (inputStream == null) {
              inputStream = loadResourceAsStream(configFile, extendedFile, clazz.getClassLoader());
              if (inputStream == null) {
                SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
                        "calling getSystemResourceAsStream", extendedFile);
                inputStream = ClassLoader.getSystemResourceAsStream(extendedFile);
                if (inputStream == null) {
                  SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
                          "resource not found. Trying doPriviledged(" + extendedFile + ")"
                          + inputStream);
                  inputStream = getPrivileged(clazz.getClassLoader(), extendedFile);
                }
              }
            }
          }
        }
      }
    }
    SilverTrace.debug("util", "ResourceLocator.getResourceAsStream", "returning " + inputStream);
    return inputStream;
  }

  private static InputStream loadResourceAsStream(String configFile, String extendedFile,
          ClassLoader loader) {
    InputStream inputStream = loader.getResourceAsStream(configFile);
    if (inputStream == null) {
      inputStream = loader.getResourceAsStream(extendedFile);
    }
    return inputStream;
  }

  private static URL locateResource(Object o, Locale loc, String ConfigFile,
          String Extension) {
    Locale lloc;
    if (loc == null) {
      lloc = Locale.getDefault();
    } else {
      lloc = loc;
    }
    String lang = lloc.getLanguage();
    String var = lloc.getVariant();
    String country = lloc.getCountry();
    URL u = locateResource(o, ConfigFile, Extension, lang, country, var);
    return (u);
  }

  private static InputStream locateResourceAsStream(Object o, Locale loc,
          String configFile, String extension) {
    Locale lloc;
    if (loc == null) {
      lloc = Locale.getDefault();
    } else {
      lloc = loc;
    }
    String lang = lloc.getLanguage();
    String var = lloc.getVariant();
    String country = lloc.getCountry();
    InputStream is = locateResourceAsStream(o, configFile, extension, lang, country, var);
    return (is);
  }

  /**
   * This method returns the URL of a resource file. The algorithm to find it is the same as for
   * getBundle()
   */
  private static URL locateResource(Object object, String configFile, String extension, String lang,
          String country, String var) {
    URL url = null;
    boolean vardone = false;
    if (object != null) {
      Class<?> clazz = object.getClass();
      if (clazz != null) {
        String fileName = configFile + "_" + lang + "_" + country;
        if (StringUtil.isDefined(var)) {
          fileName = fileName + "_" + var;
          vardone = true;
        }
        url = clazz.getResource(fileName);
        if (url == null) {
          fileName = fileName + extension;
          url = clazz.getResource(fileName);
          if (url == null) {
            if (vardone) {
              fileName = configFile + "_" + lang + "_" + country;
              url = clazz.getResource(fileName);
              if (url == null) {
                fileName = fileName + extension;
                url = clazz.getResource(fileName);
              }
              if (url != null) {
                return (url);
              }
            }
            fileName = configFile + "_" + lang;
            url = clazz.getResource(fileName);
            if (url == null) {
              fileName = fileName + extension;
              url = clazz.getResource(fileName);
            }
          }
        }
      }
    }
    return url;

  }

  /**
   * This method returns an input stream on a resource file. The algorithm to find it is the same as
   * for getBundle()
   */
  private static InputStream locateResourceAsStream(Object o, String configFile,
          String fileExtension, String lang, String country, String var) {
    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
            "Starting with args:Object = " + o + ", ConfigFile=" + configFile + ", extension="
            + fileExtension + ", lang=" + lang + ", country=" + country + ", var =" + var);
    if (o == null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
              "o is null. returning immediately.");
      return null;
    }
    Class<?> clazz = o.getClass();
    if (clazz == null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream", "getClass() returned null");
      return null;
    }
    boolean vardone = false;
    String fileName = configFile + "_" + lang;
    if (StringUtil.isDefined(country)) {
      fileName += "_" + country;
      if (StringUtil.isDefined(var)) {
        fileName = fileName + "_" + var;
        vardone = true;
      }
    } else {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream", "no country specified");
    }

    if (vardone) {
      InputStream is = loadResourceAsStream(clazz, fileName, fileExtension);
      if (is != null) {
        return is;
      }
    }

    fileName = configFile + "_" + lang;
    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
            "calling getResourceAsStream", fileName);
    InputStream is = loadResourceAsStream(clazz, configFile + "_" + lang, fileExtension);
    if (is != null) {
      return is;
    }
    return loadResourceAsStream(clazz, configFile, fileExtension);
  }

  private static InputStream loadResourceAsStream(Class<?> clazz, String configFile,
          String fileExtension) {
    InputStream is = loadResourceAsStream(clazz, configFile);
    if (is == null) {
      String fileName = configFile + fileExtension;
      is = loadResourceAsStream(clazz, fileName);
      if (is == null) {
        fileName = clazz.getPackage().getName().replace('.', '/') + '/' + fileName;
        is = loadResourceAsStream(clazz, fileName);
      }
    }
    return is;
  }

  private static InputStream loadResourceAsStream(Class<?> clazz, String fileName) {
    InputStream is = clazz.getResourceAsStream(fileName);
    if (is != null) {
      SilverTrace.debug("util", "ResourceLocator.getFileInputStream", "found resource", fileName);
      return is;
    }
    is = loader.getResourceAsStream(fileName);
    if (is != null) {
      SilverTrace.debug("util", "ResourceLocator.getFileInputStream", "found resource", fileName);
    }
    return is;
  }

  private static InputStream getPrivileged(final ClassLoader l, final String s) {
    InputStream stream = (InputStream) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Object>() {

      @Override
      public Object run() {
        if (l != null) {
          return l.getResourceAsStream(s);
        } else {
          return ClassLoader.getSystemResourceAsStream(s);
        }
      }
    });
    return (stream);
  }
}
