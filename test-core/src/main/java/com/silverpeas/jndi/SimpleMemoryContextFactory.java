/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.jndi;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * A factory of a naming context that uses the memory as dictionary of objects. Useful to tests
 * objets using JNDI to get dependencies.
 */
public class SimpleMemoryContextFactory implements InitialContextFactory {

  private static Map<String, String> savedSystemProperties = new HashMap<String, String>();
  private static SimpleMemoryContext context = new SimpleMemoryContext();

  /**
   * Sets up this class as the default JNDI initial context factory. Useful if some tests uses
   * another JNDI intial context factory.
   */
  public static void setUpAsInitialContext() {
    context.clear();
    savedSystemProperties.put(Context.INITIAL_CONTEXT_FACTORY, System.getProperty(
        Context.INITIAL_CONTEXT_FACTORY));
    savedSystemProperties.put(Context.URL_PKG_PREFIXES, System
        .getProperty(Context.URL_PKG_PREFIXES));
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, SimpleMemoryContextFactory.class.getName());
    System.setProperty(Context.URL_PKG_PREFIXES, SimpleMemoryContextFactory.class.getPackage().
        getName());
  }

  /**
   * Reverts to the previous JNDI initial context factory. This method has to be invoked if the
   * setUpAsInitialContext method was invoked previously.
   */
  public static void tearDownAsInitialContext() {
    String property = savedSystemProperties.get(Context.INITIAL_CONTEXT_FACTORY);
    if (property == null || property.trim().isEmpty()) {
      System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
    } else {
      System.setProperty(Context.INITIAL_CONTEXT_FACTORY, property);
    }

    property = savedSystemProperties.get(Context.URL_PKG_PREFIXES);
    if (property != null) {
      System.setProperty(Context.URL_PKG_PREFIXES, property);
    } else {
      System.clearProperty(Context.URL_PKG_PREFIXES);
    }
  }

  @Override
  public Context getInitialContext(final Hashtable<?, ?> environment) throws NamingException {
    return context;
  }
}
