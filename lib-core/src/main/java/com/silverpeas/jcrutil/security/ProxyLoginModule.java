package com.silverpeas.jcrutil.security;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.silverpeas.jcrutil.security.impl.RepositoryHelper;

public class ProxyLoginModule implements LoginModule {
  private LoginModule realModule;

  public ProxyLoginModule() {
    this.realModule = RepositoryHelper.getJcrLoginModule();
  }

  public boolean abort() throws LoginException {
    return realModule.abort();
  }

  public boolean commit() throws LoginException {
    return realModule.commit();
  }

  @SuppressWarnings("unchecked")
  public void initialize(Subject subject, CallbackHandler callbackHandler,
      Map sharedState, Map options) {
    realModule.initialize(subject, callbackHandler, sharedState, options);
  }

  public boolean login() throws LoginException {
    return realModule.login();
  }

  public boolean logout() throws LoginException {
    return realModule.logout();
  }

}
