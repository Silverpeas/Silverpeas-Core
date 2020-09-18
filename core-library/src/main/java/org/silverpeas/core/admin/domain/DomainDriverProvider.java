/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain;

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.util.ServiceProvider;

/**
 * @author ehugonnet
 */
@Provider
public class DomainDriverProvider {

  private DomainDriverProvider() {
  }

  @SuppressWarnings("unchecked")
  private static DomainDriver loadDomainDriver(String name) throws ClassNotFoundException {
    try {
      return ServiceProvider.getService(name);
    } catch (IllegalStateException e) {
      return ServiceProvider.getService((Class<? extends DomainDriver>) Class.forName(name));
    }
  }

  @SuppressWarnings("unchecked")
  public static DomainDriver getDriver(String name)
      throws IllegalAccessException, InstantiationException, ClassNotFoundException {

    DomainDriver domainDriver = loadDomainDriver(name);
    if (domainDriver == null) {
      Class<? extends DomainDriver> driverClass =
          (Class<? extends DomainDriver>) Class.forName(name);
      domainDriver = driverClass.newInstance();
    }
    return domainDriver;
  }

}
