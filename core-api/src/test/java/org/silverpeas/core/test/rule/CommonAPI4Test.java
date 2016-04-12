/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.test.rule;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.internal.util.MockUtil;
import org.silverpeas.core.test.TestBeanContainer;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.LoggerConfigurationManager;

import javax.enterprise.concurrent.ManagedThreadFactory;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
public class CommonAPI4Test implements TestRule {

  @Override
  public Statement apply(final Statement base, final Description description) {

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          beforeEvaluate();
          base.evaluate();
        } finally {
          afterEvaluate();
        }
      }
    };
  }

  protected void beforeEvaluate() {
    reset(TestBeanContainer.getMockedBeanContainer());
    systemWrapper();
    loggerConfigurationManager();
    managedThreadFactory();
  }

  protected void afterEvaluate() {
  }

  @SuppressWarnings({"unchecked", "Duplicates"})
  public <T> T injectIntoMockedBeanContainer(T bean) {
    final MockUtil mockUtil = new MockUtil();
    final Class<T> clazz;
    if (mockUtil.isMock(bean) || mockUtil.isSpy(bean)) {
      clazz = mockUtil.getMockHandler(bean).getMockSettings().getTypeToMock();
    } else {
      clazz = (Class<T>) bean.getClass();
    }
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(clazz)).thenReturn(bean);
    if (!clazz.isInterface()) {
      Class[] interfaces = clazz.getInterfaces();
      if (interfaces != null) {
        for (Class anInterface : interfaces) {
          when(TestBeanContainer.getMockedBeanContainer().getBeanByType(anInterface))
              .thenReturn(bean);
        }
      }
    }
    return bean;
  }

  private void systemWrapper() {
    TestSystemWrapper testSystemWrapper = new TestSystemWrapper();
    testSystemWrapper.setupDefaultParameters();
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(SystemWrapper.class))
        .thenReturn(testSystemWrapper);
  }

  private void loggerConfigurationManager() {
    StubbedLoggerConfigurationManager stub = new StubbedLoggerConfigurationManager();
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(LoggerConfigurationManager.class))
        .thenReturn(stub);
  }

  private void managedThreadFactory() {
    ManagedThreadPool managedThreadPool = new ManagedThreadPool();
    try {
      ManagedThreadFactory managedThreadFactory = Thread::new;
      FieldUtils.writeField(managedThreadPool, "managedThreadFactory", managedThreadFactory, true);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(ManagedThreadPool.class))
        .thenReturn(managedThreadPool);
  }

  private class StubbedLoggerConfigurationManager extends LoggerConfigurationManager {
    public StubbedLoggerConfigurationManager() {
      super();
      loadAllConfigurationFiles();
    }
  }

  /**
   * Default implementation that is nothing more than a delegate of {@link System} class.
   * @author Yohann Chastagnier
   */
  private static class TestSystemWrapper implements SystemWrapper {

    private Map<String, String> env = null;

    public void setupDefaultParameters() {

      // Adding by default this environment parameter
      env = new HashMap<>();
      env.putAll(System.getenv());
      env.put("SILVERPEAS_HOME", "SilverpeasHome4Tests");

      // Adding by default this system parameter
      setProperty("SILVERPEAS_DATA_HOME", "SilverpeasDataHome4Tests");
    }

    @Override
    public String getenv(final String name) {
      return getenv().get(name);
    }

    @Override
    public Map<String, String> getenv() {
      return env;
    }

    @Override
    public Properties getProperties() {
      return System.getProperties();
    }

    @Override
    public void setProperties(final Properties props) {
      Enumeration<?> propertyNames = props.propertyNames();
      while (propertyNames.hasMoreElements()) {
        String key = (String) propertyNames.nextElement();
        System.setProperty(key, props.getProperty(key));
      }
    }

    @Override
    public String setProperty(final String key, final String value) {
      if (value != null && !value.trim().isEmpty()) {
        return System.setProperty(key, value);
      }
      return null;
    }

    @Override
    public String getProperty(final String key) {
      return System.getProperty(key);
    }

    @Override
    public String getProperty(final String key, final String def) {
      return System.getProperty(key, def);
    }
  }
}
