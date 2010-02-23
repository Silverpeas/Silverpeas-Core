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

package com.silverpeas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ehugonnet
 */
public class ConfigurationClassLoader extends ClassLoader {

  private String baseDir = System.getenv("SILVERPEAS_HOME") + File.separatorChar;

  @Override
  public synchronized void clearAssertionStatus() {
    super.clearAssertionStatus();
  }

  @Override
  protected Package definePackage(String name, String specTitle, String specVersion,
      String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase)
      throws IllegalArgumentException {
    return super.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion,
        implVendor, sealBase);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    return super.findClass(name);
  }

  @Override
  protected String findLibrary(String libname) {
    return super.findLibrary(libname);
  }

  @Override
  protected URL findResource(String name) {
    return super.findResource(name);
  }

  @Override
  protected Enumeration<URL> findResources(String name) throws IOException {
    return super.findResources(name);
  }

  @Override
  protected Package getPackage(String name) {
    return super.getPackage(name);
  }

  @Override
  protected Package[] getPackages() {
    return super.getPackages();
  }

  @Override
  public URL getResource(String name) {
    URL resource = super.getResource(name);
    if (resource == null && name != null && name.endsWith(".properties")) {
      String fileName = baseDir + name;
      File file = new File(fileName);
      if (file.exists()) {
        try {
          resource = file.toURI().toURL();
        } catch (MalformedURLException ex) {
          Logger.getLogger(ConfigurationClassLoader.class.getName()).log(Level.SEVERE, null, ex);
          resource = super.getResource(name);
        }
      }
    }
    return resource;
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    InputStream inputStream = super.getResourceAsStream(name);
    if (inputStream == null && name != null) {
      String fileName = baseDir + name;
      File file = new File(fileName);
      if (file.exists()) {
        try {
          inputStream = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
          Logger.getLogger(ConfigurationClassLoader.class.getName()).log(Level.SEVERE, null, ex);
          inputStream = super.getResourceAsStream(name);
        }
      }
    }
    return inputStream;
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    return super.getResources(name);
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    return super.loadClass(name);
  }

  @Override
  protected synchronized Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException {
    return super.loadClass(name, resolve);
  }

  @Override
  public synchronized void setClassAssertionStatus(String className, boolean enabled) {
    super.setClassAssertionStatus(className, enabled);
  }

  @Override
  public synchronized void setDefaultAssertionStatus(boolean enabled) {
    super.setDefaultAssertionStatus(enabled);
  }

  @Override
  public synchronized void setPackageAssertionStatus(String packageName, boolean enabled) {
    super.setPackageAssertionStatus(packageName, enabled);
  }

  public ConfigurationClassLoader(ClassLoader parent) {
    this(parent, System.getenv("SILVERPEAS_HOME") + File.separatorChar + "properties");
  }

  public ConfigurationClassLoader(ClassLoader parent, String directory) {
    super(parent);
    assert directory != null;
    if (directory.endsWith(File.separator)) {
      this.baseDir = directory;
    } else {
      this.baseDir = directory + File.separatorChar;
    }
  }
}
