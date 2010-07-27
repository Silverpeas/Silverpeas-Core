/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * ResourceLocator.java
 *
 * Created on 19 octobre 2000, 09:54
 */
package com.stratelia.webactiv.util;

import com.silverpeas.util.ConfigurationClassLoader;
import com.silverpeas.util.StringUtil;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourceBundleWrapper;
import java.util.HashMap;
import java.util.Map;

public class ResourceLocator implements Serializable {

  private static final long serialVersionUID = -2389291572691404932L;
  private static ClassLoader loader =
      new ConfigurationClassLoader(ResourceLocator.class.getClassLoader());
  final static String m_DefaultExtension = ".properties";
  private String m_sPropertiesFile = null;
  private Locale m_sPropertiesLocale = null;
  static private Map<String, Map<Locale, ResourceBundle>> m_hPropertiesCache =
      new HashMap<String, Map<Locale, ResourceBundle>>();

  // --------------------------------------------------------------------------------------------
  // METHODS for .properties
  // --------------------------------------------------------------------------------------------
  /** Creates new ResourceLocator */
  public ResourceLocator() {
  }

  /**
   * Create a resource locator with the given property file (Ex: com.stratelia.webactiv.util.util)
   * Use the function getString to get the parameters from this instance
   */
  public ResourceLocator(String sPropertyFile, String sLanguage) {
    m_sPropertiesFile = sPropertyFile;
    if (sLanguage != null) {
      m_sPropertiesLocale = new Locale(sLanguage);
    } else {
      m_sPropertiesLocale = Locale.getDefault();
    }
  }

  /**
   * Create a resource locator with the given property file (Ex: com.stratelia.webactiv.util.util)
   * Use the function getString to get the parameters from this instance
   * @deprecated
   */
  public ResourceLocator(String sPropertyFile, Locale sLocale) {
    m_sPropertiesFile = sPropertyFile;
    if (sLocale != null) {
      m_sPropertiesLocale = sLocale;
    } else {
      m_sPropertiesLocale = Locale.getDefault();
    }
  }

  /**
   * Set properties of a SilverPeas component.
   */
  public void setPropertyLocation(String sPropertyFile, String sLanguage) {
    m_sPropertiesFile = sPropertyFile;
    if (sLanguage != null) {
      m_sPropertiesLocale = new Locale(sLanguage);
    } else {
      m_sPropertiesLocale = Locale.getDefault();
    }
  }

  private ResourceBundle getResourceBundle(String sPropertyFile, Locale locale) {
    boolean bLoadPropertyInCache = true;
    ResourceBundle bundle = null;
    // Print Cache
    // Look in the cache first
    Map<Locale, ResourceBundle> hOneProperty = m_hPropertiesCache.get(sPropertyFile);
    if (hOneProperty != null) {
      // Load the property only if the language is not in the cache
      bundle = hOneProperty.get(m_sPropertiesLocale);
      if (bundle != null) {
        bLoadPropertyInCache = false;
      }
    }

    // Load the property if not in the cache
    if (bLoadPropertyInCache) {
      bundle = new ResourceBundleWrapper(sPropertyFile, m_sPropertiesLocale);
      if (bundle != null) {
        if (hOneProperty == null) {
          // No hash for this property, create it
          Map<Locale, ResourceBundle> hash = new HashMap<Locale, ResourceBundle>();
          m_hPropertiesCache.put(sPropertyFile, hash);
          // Set the bundle for the given language
          hash.put(m_sPropertiesLocale, bundle);
        } else {
          // Set the property for the given language
          hOneProperty.put(m_sPropertiesLocale, bundle);
        }
      } else {
        Exception e = new Exception();
        SilverTrace.error("util", "ResourceLocator.getProperties",
            "util.MSG_NO_PROPERTY_FILE",
            (sPropertyFile + "_" + m_sPropertiesLocale.getLanguage()), e);
        return null;
      }
    }
    return bundle;
  }

  /**
   * Return the value of the given attribut in the Property created with the ResourceLocator
   * constructor
   */
  public String getString(String sAttribut) {
    ResourceBundle bundle = this.getResourceBundle(m_sPropertiesFile,
        m_sPropertiesLocale);
    try {
      return bundle.getString(sAttribut);
    } catch (MissingResourceException msrex) {
      SilverTrace.warn("util", "ResourceLocator.getString",
          "util.MSG_NO_ATTR_VALUE", "File : " + m_sPropertiesFile
          + " | Attribut : " + sAttribut, msrex);
      return null;
    }
  }

  /**
   * Return the value of the given attribut in the Property created with the ResourceLocator
   * constructor
   */
  public String getString(String sAttribut, String defaultValue) {
    String sReturn = getString(sAttribut);
    if (sReturn == null || sReturn.length() == 0) {
      sReturn = defaultValue;
    }
    return sReturn;
  }

  /**
   * Return the value of the given attribut in the Property created with the ResourceLocator
   * constructor
   */
  public boolean getBoolean(String sAttribut, boolean defaultValue) {
    String value = getString(sAttribut);
    if (value == null) {
      return defaultValue;
    }
    return StringUtil.getBooleanValue(value);
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

  /** Return an enumeration of all keys in the property file loaded */
  public Enumeration<String> getKeys() {
    ResourceBundle bundle = this.getResourceBundle(m_sPropertiesFile,
        m_sPropertiesLocale);
    return bundle.getKeys();
  }

  public ResourceBundle getResourceBundle() {
    return this.getResourceBundle(m_sPropertiesFile, m_sPropertiesLocale);
  }

  /** Return the properties * */
  public Properties getProperties() {
    ResourceBundle bundle = this.getResourceBundle(m_sPropertiesFile,
        m_sPropertiesLocale);
    Properties props = new Properties();
    Enumeration<String> keys = bundle.getKeys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      props.setProperty(key, bundle.getString(key));
    }
    return props;
  }

  public String getLanguage() {
    return m_sPropertiesLocale.getLanguage();
  }

  public static void resetResourceLocator() {
    SilverTrace.info("util", "ResourceLocator.resetResourceLocator",
        "root.MSG_GEN_ENTER_METHOD", "Reset Cache Resource Locator");
    m_hPropertiesCache.clear();
  }

  // --------------------------------------------------------------------------------------------
  // METHODS for .XML properties
  // --------------------------------------------------------------------------------------------
  public static URL getResource(Object object, Locale loc, String configFile, String extension) {
    String ext = extension;
    if (ext == null) {
      ext = m_DefaultExtension;
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
            Class clazz = object.getClass();
            url = clazz.getResource(configFile);
            if (url == null) {
              url = clazz.getResource(configFile + ext);
            }
          }
        }
      }
    }
    return url;
  }

  public static InputStream getResourceAsStream(Object object, Locale loc,
      String configFile, String extension) {
    if (extension == null) {
      extension = m_DefaultExtension;
    }
    if (!extension.startsWith(".")) {
      extension = '.' + extension;
    }
    SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
        "Starting with args:Object = " + object + ", loc=" + loc + ", ConfigFile="
        + configFile + ", extension=" + extension);
    InputStream inputStream = locateResourceAsStream(object, loc, configFile, extension);
    if (inputStream == null) {
      if (loc != null) {
        inputStream = locateResourceAsStream(object, null, configFile, extension);
      }
      if (inputStream == null) {
        if (object != null) {
          SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
              "Calling getClass for object '" + object + "'");
          Class clazz = object.getClass();
          SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
              "calling getResourceAsStream(" + configFile + ")");
          inputStream = clazz.getResourceAsStream(configFile);
          if (inputStream == null) {
            String extendedFile = configFile + extension;
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
    SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
        "returning " + inputStream);
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
    Locale lloc = null;
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
    Locale lloc = null;
    if (loc == null) {
      lloc = Locale.getDefault();
    } else {
      lloc = loc;
    }

    String lang = lloc.getLanguage();
    String var = lloc.getVariant();
    String country = lloc.getCountry();

    InputStream is = locateResourceAsStream(o, configFile, extension, lang,
        country, var);
    return (is);
  }

  /**
   * This method returns the URL of a resource file. The algorithm to find it is the same as for
   * getBundle()
   */
  private static URL locateResource(Object object, String configFile,
      String extension, String lang, String country, String var) {
    URL url = null;
    boolean vardone = false;
    if (object != null) {
      Class clazz = object.getClass();
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
  private static InputStream locateResourceAsStream(Object o,
      String configFile, String fileExtension, String lang, String country,
      String var) {
    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
        "Starting with args:Object = " + o + ", ConfigFile=" + configFile
        + ", extension=" + fileExtension + ", lang=" + lang + ", country="
        + country + ", var =" + var);

    if (o == null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "o is null. returning immediately.");
      return null;
    }

    Class clazz = o.getClass();
    if (clazz == null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "getClass() returned null");
      return null;
    }

    InputStream is = null;
    boolean vardone = false;
    String fileName = configFile + "_" + lang;
    if (StringUtil.isDefined(country)) {
      fileName += "_" + country;
      if (StringUtil.isDefined(var)) {
        fileName = fileName + "_" + var;
        vardone = true;
      }
    } else {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "no country specified");
    }

    if (vardone) {
      is = getConfigurationFileInputStream(clazz, fileName, fileExtension);
      if (is != null) {
        return is;
      }
    }
    fileName = configFile + "_" + lang;
    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
        "calling getResourceAsStream", fileName);

    is = getConfigurationFileInputStream(clazz, configFile + "_" + lang, fileExtension);
    if (is != null) {
      return is;
    }
    return getConfigurationFileInputStream(clazz, configFile, fileExtension);
  }

  private static InputStream getConfigurationFileInputStream(Class clazz, String configFile,
      String fileExtension) {
    String fileName = configFile;
    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
        "calling getResourceAsStream", fileName);

    InputStream is = clazz.getResourceAsStream(fileName);
    if (is != null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream", "found resource",
          fileName);
      return is;
    }
    is = loader.getResourceAsStream(fileName);
    if (is != null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream", "found resource",
          fileName);
      return is;
    }
    fileName = fileName + fileExtension;
    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
        "calling getResourceAsStream (with extension)", fileName);
    is = clazz.getResourceAsStream(fileName);
    if (is != null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "found resource", fileName);
      return (is);
    }
    is = loader.getResourceAsStream(fileName);
    if (is != null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "found resource", fileName);
      return (is);
    }
    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
        "Could not find resource", fileName);
    return null;
  }

  private static InputStream getPrivileged(final ClassLoader l, final String s) {
    InputStream stream =
        (InputStream) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Object>() {

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
