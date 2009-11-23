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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourceBundleWrapper;

public class ResourceLocator implements Serializable {

  final static String m_DefaultExtension = ".properties";

  private String m_sPropertiesFile = null;
  private Locale m_sPropertiesLocale = null;

  static private Hashtable<String, Hashtable<Locale, ResourceBundle>> m_hPropertiesCache =
      new Hashtable<String, Hashtable<Locale, ResourceBundle>>();

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
    Hashtable<Locale, ResourceBundle> hOneProperty = m_hPropertiesCache
        .get(sPropertyFile);
    if (hOneProperty != null) {
      // Load the property only if the language is not in the cache
      bundle = (ResourceBundle) hOneProperty.get(m_sPropertiesLocale);
      if (bundle != null)
        bLoadPropertyInCache = false;
    }

    // Load the property if not in the cache
    if (bLoadPropertyInCache) {
      bundle = new ResourceBundleWrapper(sPropertyFile, m_sPropertiesLocale);
      if (bundle != null) {
        if (hOneProperty == null) {
          // No hash for this property, create it
          Hashtable<Locale, ResourceBundle> hash = new Hashtable<Locale, ResourceBundle>();
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
    if (value == null)
      return defaultValue;

    return value.equalsIgnoreCase("yes") || value.equals("1")
        || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("oui");
  }

  public String getStringWithParam(String resName, String param) {
    String[] params = { param };
    return getStringWithParams(resName, params);
  }

  public String getStringWithParams(String resName, String[] params) {
    String theSt = getString(resName);
    if (theSt != null) {
      StringBuffer theResult = new StringBuffer();
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
        } else
          theStarIndex = -1;
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
  public static URL getResource(Object o, Locale loc, String ConfigFile,
      String Extension) {
    if (Extension == null)
      Extension = m_DefaultExtension;
    else if (!Extension.startsWith("."))
      Extension = "." + Extension;
    URL u = locateResource(o, loc, ConfigFile, Extension);
    if (u != null)
      return (u);
    if (loc != null) {
      u = locateResource(o, null, ConfigFile, Extension);
      if (u != null)
        return (u);
    }
    String s = ConfigFile;
    Class C = null;

    if (o == null)
      return (null);

    C = o.getClass();
    if (C == null)
      return (null);
    u = C.getResource(s);
    if (u != null)
      return (u);
    s = s + Extension;
    u = C.getResource(s);
    return (u);
  }

  public static InputStream getResourceAsStream(Object o, Locale loc,
      String ConfigFile, String Extension) {
    if (Extension == null)
      Extension = m_DefaultExtension;
    else if (!Extension.startsWith("."))
      Extension = "." + Extension;
    SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
        "Starting with args:Object = " + o + ", loc=" + loc + ", ConfigFile="
        + ConfigFile + ", extension=" + Extension);
    InputStream is = locateResourceAsStream(o, loc, ConfigFile, Extension);
    if (is != null) {
      URL u = locateResource(o, loc, ConfigFile, Extension);
      SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
          "locateResourceAsStream found a resource in '" + u.toString() + "'");
      return (is);
    }
    if (loc != null) {
      is = locateResourceAsStream(o, null, ConfigFile, Extension);
      if (is != null) {
        URL u = locateResource(o, null, ConfigFile, Extension);
        SilverTrace
            .debug("util", "ResourceLocator.getResourceAsStream",
            "locateResourceAsStream found a resource in '" + u.toString()
            + "'");

        return (is);
      }
    }
    String s = ConfigFile;
    Class C = null;

    if (o == null) {
      return (null);
    }
    SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
        "Calling getClass for object '" + o + "'");

    C = o.getClass();
    if (C == null) {
      SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
          "getClass for object '" + o + "' returned null");
      return (null);
    }
    SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
        "calling getResourceAsStream(" + s + ")");
    is = C.getResourceAsStream(s);
    if (is != null) {
      return (is);
    }

    s = s + Extension;

    SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
        "calling getResourceAsStream(" + s + ")");

    is = C.getResourceAsStream(s);

    if (is != null) {
      URL u = C.getResource(s);
      SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
          "found a resource in '" + u.toString() + "'");
      return (is);
    }

    // still not found. Try system resources
    s = ConfigFile;

    ClassLoader l = C.getClassLoader();
    if (l == null) {
      SilverTrace
          .debug("util", "ResourceLocator.getResourceAsStream",
          "getClassLoader returned null. Using system class loader methods instead.");

      l = ClassLoader.getSystemClassLoader();
      if (l == null) {
        SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
            "getSystemClassLoader returned null too!");
      } else {
        SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
            "getSystemClassLoader worked and returned a non-null value");
      }
    }
    SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
        "calling getSystemResourceAsStream", s);

    if (l != null) {
      is = l.getSystemResourceAsStream(s);
    } else {
      is = ClassLoader.getSystemResourceAsStream(s);
    }
    if (is != null) {
      URL u;
      if (l != null) {
        u = l.getSystemResource(s);
      } else {
        u = ClassLoader.getSystemResource(s);
      }
      SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
          "locateResourceAsStream found a resource in '" + u.toString() + "'");

      return (is);
    }

    s = s + Extension;

    SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
        "calling getSystemResourceAsStream", s);

    is = l.getSystemResourceAsStream(s);

    if (is != null) {
      URL u = l.getSystemResource(s);
      SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
          "locateResourceAsStream found a resource in '" + u.toString() + "'");

      return (is);
    }

    if (is == null) {
      SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
          "resource not found. Trying doPriviledged(" + s + ")" + is);
      is = getPrivileged(l, s);
    }
    SilverTrace.debug("util", "ResourceLocator.getResourceAsStream",
        "returning " + is);
    return (is);
  }

  public static void main(String args[]) {
    String ExtensionString = null;
    String ConfigFileName = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].compareToIgnoreCase("-config") == 0) {
        i++;
        if (i >= args.length) {
          System.err.println("ERROR:missing argument after -config");
          System.exit(1);
        }
        ConfigFileName = args[i];
      } else if (args[i].compareToIgnoreCase("-debug") == 0) {
        i++;
        if (i >= args.length) {
          System.err.println("ERROR:missing argument after -debug");
          System.exit(1);
        }
      } else if (args[i].compareToIgnoreCase("-ext") == 0) {
        i++;
        if (i >= args.length) {
          System.err.println("ERROR:missing argument after -ext");
          System.exit(1);
        }
        ExtensionString = args[i];
      } else {
        System.err.println("Invalid argument:" + args[i]);
        System.exit(1);
      }
    }
    // if no configuration file given use default
    if (ConfigFileName == null)
      ConfigFileName = "resources";

    ResourceLocator rl = new ResourceLocator();
    if (ResourceLocator.getResourceAsStream(rl, null, ConfigFileName,
        ExtensionString) == null) {
      System.err.println("Could not open resource stream");
    }
    URL u = ResourceLocator.getResource(rl, null, ConfigFileName,
        ExtensionString);
    if (u == null)
      System.err.println("Could not find resource");
    else
      System.out.println("Resource found! (" + u.toString() + ")");
  }

  private static URL locateResource(Object o, Locale loc, String ConfigFile,
      String Extension) {
    Locale lloc = null;
    if (loc == null)
      lloc = Locale.getDefault();
    else
      lloc = loc;

    String lang = lloc.getLanguage();
    String var = lloc.getVariant();
    String country = lloc.getCountry();
    URL u = locateResource(o, ConfigFile, Extension, lang, country, var);
    return (u);
  }

  private static InputStream locateResourceAsStream(Object o, Locale loc,
      String ConfigFile, String Extension) {
    Locale lloc = null;
    if (loc == null)
      lloc = Locale.getDefault();
    else
      lloc = loc;

    String lang = lloc.getLanguage();
    String var = lloc.getVariant();
    String country = lloc.getCountry();
    InputStream is = locateResourceAsStream(o, ConfigFile, Extension, lang,
        country, var);
    return (is);
  }

  /**
   * This method returns the URL of a resource file. The algorithm to find it is the same as for
   * getBundle()
   */
  private static URL locateResource(Object o, String ConfigFile,
      String Extension, String lang, String country, String var) {
    URL u = null;
    boolean vardone = false;
    Class C = null;

    if (o == null)
      return (null);

    C = o.getClass();
    if (C == null)
      return (null);

    String s = ConfigFile + "_" + lang + "_" + country;
    if (var != null && !var.equals("")) {
      s = s + "_" + var;
      vardone = true;
    }

    u = C.getResource(s);
    if (u != null) {
      // Debug.println("found resource " + s);
      return (u);
    }
    s = s + Extension;
    u = C.getResource(s);
    if (u != null) {
      // Debug.println("found resource " + s);
      return (u);
    }
    if (vardone) {
      s = ConfigFile + "_" + lang + "_" + country;
      u = C.getResource(s);
      if (u != null)
        return (u);
      s = s + Extension;
      u = C.getResource(s);
      if (u != null) {
        // Debug.println("found resource " + s);
        return (u);
      }
    }
    s = ConfigFile + "_" + lang;
    u = C.getResource(s);
    if (u != null) {
      // Debug.println("found resource " + s);
      return (u);
    }
    s = s + Extension;
    u = C.getResource(s);
    if (u != null)
      return (u);
    return (null);
  }

  /**
   * This method returns an input stream on a resource file. The algorithm to find it is the same as
   * for getBundle()
   */
  private static InputStream locateResourceAsStream(Object o,
      String ConfigFile, String Extension, String lang, String country,
      String var) {
    InputStream is = null;
    boolean vardone = false;
    Class C = null;

    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
        "Starting with args:Object = " + o + ", ConfigFile=" + ConfigFile
        + ", extension=" + Extension + ", lang=" + lang + ", country="
        + country + ", var =" + var);

    if (o == null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "o is null. returning immediately.");
      return (null);
    }

    C = o.getClass();
    if (C == null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "getClass() returned null");
      return (null);
    }
    String s = ConfigFile + "_" + lang;
    if (country != null && !country.equals("")) {
      s += "_" + country;
      if (var != null && !var.equals("")) {
        s = s + "_" + var;
        vardone = true;
      }
    } else {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "no country specified");
    }

    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
        "calling getResourceAsStream", s);

    is = C.getResourceAsStream(s);
    if (is != null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "found resource", s);
      return (is);
    }

    s = s + Extension;
    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
        "calling getResourceAsStream (with extension)", s);

    is = C.getResourceAsStream(s);
    if (is != null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "found resource (with extension)", s);
      return (is);
    }
    if (vardone) {
      s = ConfigFile + "_" + lang + "_" + country;
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "calling getResourceAsStream", s);
      is = C.getResourceAsStream(s);
      if (is != null) {
        SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
            "found resource", s);
        return (is);
      }
      s = s + Extension;
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "calling getResourceAsStream", s);

      is = C.getResourceAsStream(s);
      if (is != null) {
        SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
            "found resource", s);
        return (is);
      }
    }
    s = ConfigFile + "_" + lang;
    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
        "calling getResourceAsStream", s);

    is = C.getResourceAsStream(s);
    if (is != null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "found resource", s);
      return (is);
    }
    s = s + Extension;
    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
        "calling getResourceAsStream (with extension)", s);
    is = C.getResourceAsStream(s);
    if (is != null) {
      SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
          "found resource", s);
      return (is);
    }
    SilverTrace.debug("util", "ResourceLocator.locateResourceAsStream",
        "Could not find resource", s);
    return (null);
  }

  private static InputStream getPrivileged(final ClassLoader l, final String s) {
    InputStream stream = (InputStream) java.security.AccessController
        .doPrivileged(new java.security.PrivilegedAction<Object>() {
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