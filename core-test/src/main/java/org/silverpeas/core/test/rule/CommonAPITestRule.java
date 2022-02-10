/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.test.rule;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.internal.util.MockUtil;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.GroupProvider;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.test.TestBeanContainer;
import org.silverpeas.core.test.util.lang.TestSystemWrapper;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.LoggerConfigurationManager;
import org.silverpeas.core.util.logging.SilverLoggerProvider;

import javax.enterprise.concurrent.ManagedThreadFactory;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.mockito.Mockito.*;
import static org.silverpeas.core.util.logging.SilverLoggerProvider.ROOT_NAMESPACE;

/**
 * @author Yohann Chastagnier
 */
public class CommonAPITestRule implements TestRule {

  private TestContext testContext;
  private UserProvider userProvider;

  @Override
  public Statement apply(final Statement base, final Description description) {

    File testTempData = new File(new File(
        description.getTestClass().getProtectionDomain().getCodeSource().getLocation().getFile()),
        "test-temp-data");
    testContext = new TestContext(description, testTempData);

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          beforeEvaluate();
          base.evaluate();
        } finally {
          try {
            afterEvaluate();
          } finally {
            FileUtils.deleteQuietly(testTempData);
          }
        }
      }
    };
  }

  protected void beforeEvaluate() {
    reset(TestBeanContainer.getMockedBeanContainer());
    userProvider();
    groupProvider();
    systemWrapper();
    loggerConfigurationManager();
    managedThreadFactory();
  }

  protected void afterEvaluate() {
    // nothing to do
  }

  public TestContext getTestContext() {
    return testContext;
  }

  public void setLoggerLevel(Level level) {
    final ConsoleHandler handler = new ConsoleHandler();
    setLoggerHandler(handler);
    handler.setFormatter(new SimpleFormatter());
    switch (level) {
      case INFO:
        Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.INFO);
        handler.setLevel(java.util.logging.Level.INFO);
        break;
      case DEBUG:
        Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.FINE);
        handler.setLevel(java.util.logging.Level.FINE);
        break;
      case WARNING:
        Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.WARNING);
        handler.setLevel(java.util.logging.Level.WARNING);
        break;
      case ERROR:
        Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.SEVERE);
        handler.setLevel(java.util.logging.Level.SEVERE);
        break;
    }
  }

  private void setLoggerHandler(final Handler handler) {
    Logger.getLogger(ROOT_NAMESPACE).setUseParentHandlers(false);
    if (Arrays.stream(Logger.getLogger(ROOT_NAMESPACE).getHandlers())
        .noneMatch(h -> handler.getClass().isInstance(h))) {
      Logger.getLogger(ROOT_NAMESPACE).addHandler(handler);
    }
  }

  public void setCurrentRequester(final User user) {
    when(userProvider.getCurrentRequester()).thenReturn(user);
  }

  @SuppressWarnings("unchecked")
  public <T> T injectIntoMockedBeanContainer(T bean, Annotation ... qualifiers) {
    final Class<T> clazz;
    if (MockUtil.isMock(bean) || MockUtil.isSpy(bean)) {
      clazz = (Class<T>) MockUtil.getMockHandler(bean).getMockSettings().getTypeToMock();
    } else {
      clazz = (Class<T>) bean.getClass();
    }
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(clazz, qualifiers)).thenReturn(bean);
    if (!clazz.isInterface()) {
      Class<T>[] interfaces = (Class<T>[]) clazz.getInterfaces();
      if (interfaces != null) {
        for(Class<T> anInterface : interfaces) {
          when(TestBeanContainer.getMockedBeanContainer().getBeanByType(anInterface, qualifiers))
              .thenReturn(bean);
        }
      }
      if (clazz.getSimpleName().endsWith("4Test") && clazz.getGenericSuperclass() instanceof Class) {
        when(TestBeanContainer.getMockedBeanContainer()
            .getBeanByType((Class<T>) clazz.getGenericSuperclass(), qualifiers)).thenReturn(bean);
      }
    }
    return bean;
  }

  private void userProvider() {
    userProvider = mock(StubbedUserProvider.class);
    doCallRealMethod().when(userProvider).getCurrentRequester();
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(UserProvider.class))
        .thenReturn(userProvider);
  }

  private void groupProvider() {
    GroupProvider groupProvider = mock(GroupProvider.class);
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(GroupProvider.class))
        .thenReturn(groupProvider);
  }

  private void systemWrapper() {
    TestSystemWrapper testSystemWrapper = new TestSystemWrapper();
    testSystemWrapper.setupDefaultParameters();
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(SystemWrapper.class))
        .thenReturn(testSystemWrapper);
  }

  private void loggerConfigurationManager() {
    StubbedLoggerConfigurationManager configurationManager =
        new StubbedLoggerConfigurationManager();
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(LoggerConfigurationManager.class))
        .thenReturn(configurationManager);

    StubbedSilverLoggerProvider loggerProvider =
        new StubbedSilverLoggerProvider(configurationManager);
    when(TestBeanContainer.getMockedBeanContainer()
        .getBeanByType(SilverLoggerProvider.class)).thenReturn(loggerProvider);
  }

  private void managedThreadFactory() {
    try {
      Constructor<ManagedThreadPool> managedThreadPoolConstructor =
          ManagedThreadPool.class.getDeclaredConstructor();
      managedThreadPoolConstructor.trySetAccessible();
      ManagedThreadPool managedThreadPool = managedThreadPoolConstructor.newInstance();
      ManagedThreadFactory managedThreadFactory = Thread::new;
      FieldUtils.writeField(managedThreadPool, "managedThreadFactory",
          managedThreadFactory, true);
      when(TestBeanContainer.getMockedBeanContainer()
          .getBeanByType(ManagedThreadPool.class)).thenReturn(managedThreadPool);
    } catch (IllegalAccessException | NoSuchMethodException | InstantiationException |
        InvocationTargetException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  protected class TestContext {
    private final Description description;
    private final File tempData;

    public TestContext(final Description description, final File tempData) {
      this.description = description;
      this.tempData = tempData;
    }

    public Description getDescription() {
      return description;
    }

    public File getTempData() {
      return tempData;
    }
  }

  private class StubbedLoggerConfigurationManager extends LoggerConfigurationManager {
    public StubbedLoggerConfigurationManager() {
      super();
      loadAllConfigurationFiles();
    }
  }

  private class StubbedSilverLoggerProvider extends SilverLoggerProvider {

    protected StubbedSilverLoggerProvider(
        final LoggerConfigurationManager loggerConfigurationManager) {
      super(loggerConfigurationManager);
    }
  }

  private abstract class StubbedUserProvider implements UserProvider {
    @Override
    public User getCurrentRequester() {
      return UserProvider.super.getCurrentRequester();
    }
  }
}
