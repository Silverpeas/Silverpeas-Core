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

package com.silverpeas.jcrutil.security.impl;

import javax.security.auth.spi.LoginModule;
import org.apache.jackrabbit.core.security.AccessManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class RepositoryHelper implements ApplicationContextAware {
  public static final String JRC_LOGIN_MODULE = "jcrLoginModule";
  public static final String JRC_ACCESS_MANAGER = "jcrAccessManager";

  private ApplicationContext context;
  private static RepositoryHelper instance;

  private RepositoryHelper() {
  }

  @Override
  public void setApplicationContext(ApplicationContext context)
      throws BeansException {
    this.context = context;
  }

  public static RepositoryHelper getInstance() {
    synchronized (RepositoryHelper.class) {
      if (RepositoryHelper.instance == null) {
        RepositoryHelper.instance = new RepositoryHelper();
      }
    }
    return RepositoryHelper.instance;
  }

  private ApplicationContext getContext() {
    return context;
  }

  public static LoginModule getJcrLoginModule() {
    return getInstance().getContext().getBean(JRC_LOGIN_MODULE, LoginModule.class);
  }

  public static AccessManager getJcrAccessManager() {
    return getInstance().getContext().getBean(JRC_ACCESS_MANAGER, AccessManager.class);
  }

}
