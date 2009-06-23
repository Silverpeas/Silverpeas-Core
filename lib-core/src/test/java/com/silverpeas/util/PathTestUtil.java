/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ehugonnet
 */
public class PathTestUtil {

  public static final char SEPARATOR = File.separatorChar;
  private final static Properties TESTS_PROPS = new Properties();
  public static String BUILD_PATH = "";
  static {
    try {
      TESTS_PROPS.load(PathTestUtil.class.getClassLoader().
          getResourceAsStream("maven.properties"));
      BUILD_PATH = TESTS_PROPS.getProperty("build.dir").replace('/',
          SEPARATOR);
    } catch (IOException ex) {
      Logger.getLogger(PathTestUtil.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
