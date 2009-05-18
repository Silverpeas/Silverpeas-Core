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

  public static LoginModule getJcrLoginModule() {
    return (LoginModule) getInstance().context.getBean(JRC_LOGIN_MODULE);
  }

  public static AccessManager getJcrAccessManager() {
    return (AccessManager) getInstance().context.getBean(JRC_ACCESS_MANAGER);
  }

}
