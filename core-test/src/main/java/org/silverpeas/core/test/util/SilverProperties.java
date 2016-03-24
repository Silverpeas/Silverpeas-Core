package org.silverpeas.core.test.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Yohann Chastagnier
 */
public class SilverProperties extends Properties {

  private final Class baseClass;

  /**
   * Loads properties of given property file path from a class.<br/>
   * Property file has to exist into resources of the project.
   * @param fromClass the class from which the properties are requested.
   * @param propertyFilePaths the paths of files that contains the aimed properties.
   * @return an instance of {@link SilverProperties} that contains requested properties.
   */
  public static SilverProperties load(Class fromClass, String... propertyFilePaths) {
    SilverProperties properties = new SilverProperties(fromClass);
    return properties.load(propertyFilePaths);
  }

  private SilverProperties(final Class baseClass) {
    this.baseClass = baseClass;
  }

  /**
   * Loads properties of given property file path from a class and add them to the currents.<br/>
   * Property file has to exist into resources of the project.
   * @param propertyFilePaths the paths of files that contains the aimed properties.
   * @return an instance of {@link SilverProperties} that contains requested properties.
   */
  public SilverProperties load(String... propertyFilePaths) {
    for (String propertyFilePath : propertyFilePaths) {
      try (InputStream is = baseClass.getClassLoader().getResourceAsStream(propertyFilePath)) {
        load(is);
      } catch (Exception ex) {
        Logger.getLogger(baseClass.getName()).log(Level.SEVERE, "Class " + baseClass, ex);
        throw new RuntimeException(ex);
      }
    }
    return this;
  }
}
