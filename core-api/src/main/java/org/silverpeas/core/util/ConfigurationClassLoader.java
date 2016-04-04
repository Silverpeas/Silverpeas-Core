/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util;

import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

/**
 * The resource bundles and the properties files and all located into a particular directory
 * in the Silverpeas home directory that isn't in the classpath of the running JEE application.
 * Therefore this class loader aims to manage the access to the resources in this particular
 * location; it acts as a bridge between the current hierarchy of class loaders and this particular
 * unmanaged location.
 * </p>
 * By default, when a resource is asked, it looks for in the current hierarchy of class loaders
 * before to seek the resource into the resources directory in the Silverpeas home directory.
 * @author ehugonnet
 */
public class ConfigurationClassLoader extends ClassLoader {

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
    if (resource == null && name != null) {
      File file = new File(bundlesBaseDirectory(), name);
      if (file.exists() && file.isFile()) {
        try {
          resource = file.toURI().toURL();
        } catch (MalformedURLException ex) {
          SilverLogger.getLogger(this).error("Malformed URL for resource " + name, ex);
        }
      }
    }
    return resource;
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    InputStream inputStream = super.getResourceAsStream(name);
    if (inputStream == null && name != null) {
      File file = new File(bundlesBaseDirectory(), name);
      if (file.exists() && file.isFile()) {
        try {
          inputStream = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
          SilverLogger.getLogger(this).error("Resource " + name + " not found", ex);
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
    super(parent);
  }

  protected File bundlesBaseDirectory() {
    return new File(SystemWrapper.get().getenv("SILVERPEAS_HOME"), "properties");
  }

}
