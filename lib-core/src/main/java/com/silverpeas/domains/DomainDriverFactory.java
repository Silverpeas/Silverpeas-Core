/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.domains;

import com.stratelia.webactiv.beans.admin.DomainDriver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * @author ehugonnet
 */
public class DomainDriverFactory implements ApplicationContextAware {

  private static DomainDriverFactory instance;
  private ApplicationContext context;

  private DomainDriverFactory() {
  }

  static DomainDriverFactory getDomainDriverFactory() {
    synchronized (DomainDriverFactory.class) {
      if (instance == null) {
        instance = new DomainDriverFactory();
      }
    }
    return instance;
  }

  private static DomainDriver loadDomainDriverFromSpring(String name)
      throws IllegalAccessException, InstantiationException, ClassNotFoundException {
    if (getDomainDriverFactory() == null || getDomainDriverFactory().getApplicationContext() == null) {
      return null;
    }
    if (getDomainDriverFactory().getApplicationContext().containsBean(name)) {
      return (DomainDriver) getDomainDriverFactory().getApplicationContext().getBean(name, DomainDriver.class);
    }
    Class<? extends DomainDriver> driverClass = (Class<? extends DomainDriver>) Class.forName(name);
    String[] names = getDomainDriverFactory().getApplicationContext().getBeanNamesForType(driverClass);
    if (names.length > 0) {
      return getDomainDriverFactory().getApplicationContext().getBean(names[0], driverClass);
    }
    return null;
  }

  public static DomainDriver getDriver(String name)
      throws IllegalAccessException, InstantiationException, ClassNotFoundException {

    DomainDriver domainDriver = loadDomainDriverFromSpring(name);
    if (domainDriver == null) {
      Class<? extends DomainDriver> driverClass = (Class<? extends DomainDriver>) Class.forName(name);
      domainDriver = driverClass.newInstance();
    }
    return domainDriver;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.context = applicationContext;
  }

  protected ApplicationContext getApplicationContext() {
    return context;
  }
}
