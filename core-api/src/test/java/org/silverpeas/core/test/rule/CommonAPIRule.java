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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.internal.util.MockUtil;
import org.silverpeas.core.test.TestBeanContainer;
import org.silverpeas.core.test.TestSystemWrapper;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.LoggerConfigurationManager;
import org.silverpeas.core.util.logging.SilverLoggerProvider;

import javax.enterprise.concurrent.ManagedThreadFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
public class CommonAPIRule implements TestRule {

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
  public <T> T injectIntoMockedBeanContainer(T bean, Annotation... qualifiers) {
    final Class<T> clazz;
    if (MockUtil.isMock(bean) || MockUtil.isSpy(bean)) {
      clazz = (Class<T>) MockUtil.getMockHandler(bean).getMockSettings().getTypeToMock();
    } else {
      clazz = (Class<T>) bean.getClass();
    }
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(clazz, qualifiers)).thenReturn(
        bean);
    if (!clazz.isInterface()) {
      Class[] interfaces = clazz.getInterfaces();
      if (interfaces != null) {
        for (Class anInterface : interfaces) {
          when(TestBeanContainer.getMockedBeanContainer().getBeanByType(anInterface, qualifiers))
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
      managedThreadPoolConstructor.setAccessible(true);
      ManagedThreadPool managedThreadPool = managedThreadPoolConstructor.newInstance();
      ManagedThreadFactory managedThreadFactory = Thread::new;
      FieldUtils.writeField(managedThreadPool, "managedThreadFactory", managedThreadFactory, true);
      when(TestBeanContainer.getMockedBeanContainer()
          .getBeanByType(ManagedThreadPool.class)).thenReturn(managedThreadPool);
    } catch (IllegalAccessException | NoSuchMethodException | InstantiationException |
        InvocationTargetException e) {
      throw new RuntimeException(e);
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

}
