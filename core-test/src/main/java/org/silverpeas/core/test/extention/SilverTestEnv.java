/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.test.extention;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.mockito.internal.util.MockUtil;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.GroupProvider;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.silvertrace.SilverpeasTrace;
import org.silverpeas.core.test.TestBeanContainer;
import org.silverpeas.core.test.util.MavenTestEnv;
import org.silverpeas.core.test.util.lang.TestSystemWrapper;
import org.silverpeas.core.test.util.log.TestSilverpeasTrace;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.LoggerConfigurationManager;
import org.silverpeas.core.util.logging.SilverLoggerProvider;

import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

/**
 * Prepares the environment to run unit tests. It mocks the container of beans and set ups it
 * for the tests with some of the common beans in Silverpeas. It scans for {@link TestManagedBean}
 * and {@link MockedBean} annotated fields and for {@link MockedBean} annotated parameters to
 * register them automatically into the bean container used in tests. If the class of
 * {@link TestManagedBean} annotated field is qualified by a {@link Qualifier} annotated annotation,
 * then it is registered under that qualifier.
 * The mocked bean container can be injected in the parameters of a test.
 * @author mmoquillon
 */
public class SilverTestEnv implements TestInstancePostProcessor, ParameterResolver, BeforeEachCallback {

  /**
   * Injects in the unit test class all the fields that are annotated with {@link TestManagedBean}
   * or {@link MockedBean}. The first will be automatically instantiated if not yet. The second
   * will be mocked. All of these beans will be then registered into the mocked bean container
   * used for unit tests.
   * @param testInstance the instance of the test class.
   * @param context the context of the extension.
   * @throws Exception if an error occurs while injecting the fields.
   */
  @Override
  public void postProcessTestInstance(final Object testInstance, final ExtensionContext context)
      throws Exception {
    reset(TestBeanContainer.getMockedBeanContainer());
    Field[] fields = testInstance.getClass().getDeclaredFields();
    for (Field field : fields) {
      TestManagedBean managedBean = field.getAnnotation(TestManagedBean.class);
      MockedBean mockedBean = field.getAnnotation(MockedBean.class);
      if (managedBean != null) {
        field.setAccessible(true);
        Object bean = field.get(testInstance);
        if (bean == null) {
          bean = instantiate(field.getType());
          field.set(testInstance, bean);
        }
        manageBean(bean, field.getType());
      } else if (mockedBean != null) {
        field.setAccessible(true);
        Object bean = mock(field.getType());
        field.setAccessible(true);
        field.set(testInstance, bean);
        registerInBeanContainer(bean);
      }
    }
  }

  /**
   * Is the parameter in a test's method is supported by this extension for value injection?
   * @param parameterContext the context of the parameter.
   * @param extensionContext the context of the extension.
   * @return true if the parameter is either annotated with @{@link TestManagedBean} or with
   * {@link MockedBean}
   */
  @Override
  public boolean supportsParameter(final ParameterContext parameterContext,
      final ExtensionContext extensionContext) {
    return parameterContext.isAnnotated(MockedBean.class) ||
        parameterContext.isAnnotated(TestManagedBean.class) ||
        parameterContext.getParameter().getType().equals(MavenTestEnv.class);
  }

  /**
   * Resolves the parameter referred by the parameter context by valuing it according to its
   * annotation: if annotated with {@link TestManagedBean}, the parameter will be instantiated with
   * its default constructor; if annotated with {@link MockedBean}, the parameter will be mocked.
   * @param parameterContext the context of the parameter.
   * @param extensionContext the context of the extension.
   * @return the value of the parameter to inject.
   */
  @Override
  public Object resolveParameter(final ParameterContext parameterContext,
      final ExtensionContext extensionContext) {
    final Object bean;
    final Parameter parameter = parameterContext.getParameter();
    if (parameter.getType().equals(MavenTestEnv.class)) {
      bean = new MavenTestEnv(extensionContext.getRequiredTestInstance());
    } else if (parameterContext.isAnnotated(MockedBean.class)) {
      bean = mock(parameter.getType());
      registerInBeanContainer(bean);
    } else if (parameterContext.isAnnotated(TestManagedBean.class)) {
      bean = instantiate(parameter.getType());
      manageBean(bean, parameter.getType());
    } else {
      bean = null;
    }
    return bean;
  }

  /**
   * Prepares the unit test environment before executing any test. Some beans are mocked by default
   * ({@link GroupProvider}, {@link UserProvider}, {@link ManagedThreadFactory}, and so on.)
   * If the unit test defines a method annotated with {@link RequesterProvider}, then it is
   * invoked to get the user to set as the default requester.
   * @param context the context of the extension.
   * @throws Exception if an error occurs while preparing the test environement.
   */
  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    mockCommonBeans();
    Method[] methods = context.getRequiredTestClass().getDeclaredMethods();
    for(Method method: methods) {
      RequesterProvider requesterProvider = method.getAnnotation(RequesterProvider.class);
      if (requesterProvider != null && User.class.isAssignableFrom(method.getReturnType())) {
        method.setAccessible(true);
        User requester = (User) method.invoke(context.getRequiredTestInstance());
        UserProvider mock = TestBeanContainer.getMockedBeanContainer().getBeanByType(UserProvider.class);
        when(mock.getCurrentRequester()).thenReturn(requester);
        break;
      }
    }
  }

  private static Object instantiate(final Class<?> beanType) {
    Object bean;
    try {
      Constructor constructor = beanType.getDeclaredConstructor();
      constructor.setAccessible(true);
      bean = constructor.newInstance();
    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
        IllegalAccessException e) {
      bean = null;
    }
    return bean;
  }

  private void manageBean(final Object bean, final Class<?> beanType) {
    Annotation[] qualifiers = Stream.of(beanType.getDeclaredAnnotations())
        .filter(a -> a.annotationType().getAnnotationsByType(Qualifier.class).length > 0)
        .toArray(Annotation[]::new);
    registerInBeanContainer(bean, qualifiers);
  }

  private void mockCommonBeans() {
    mockUserProvider();
    mockGroupProvider();
    mockSystemWrapper();
    mockLoggingSystem();
    mockManagedThreadFactory();
  }

  private void mockUserProvider() {
    UserProvider userProvider = mock(UserProvider.class);
    doCallRealMethod().when(userProvider).getCurrentRequester();
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(UserProvider.class)).thenReturn(
        userProvider);
  }

  private void mockGroupProvider() {
    GroupProvider groupProvider = mock(GroupProvider.class);
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(GroupProvider.class)).thenReturn(
        groupProvider);
  }

  private void mockSystemWrapper() {
    TestSystemWrapper testSystemWrapper = new TestSystemWrapper();
    testSystemWrapper.setupDefaultParameters();
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(SystemWrapper.class)).thenReturn(
        testSystemWrapper);
  }

  private void mockLoggingSystem() {
    when(
        TestBeanContainer.getMockedBeanContainer().getBeanByType(SilverpeasTrace.class)).thenReturn(
        new TestSilverpeasTrace());

    StubbedLoggerConfigurationManager configurationManager =
        new StubbedLoggerConfigurationManager();
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(LoggerConfigurationManager.class))
        .thenReturn(configurationManager);

    StubbedSilverLoggerProvider loggerProvider =
        new StubbedSilverLoggerProvider(configurationManager);
    when(TestBeanContainer.getMockedBeanContainer()
        .getBeanByType(SilverLoggerProvider.class)).thenReturn(loggerProvider);
  }

  private void mockManagedThreadFactory() {
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
      throw new SilverpeasRuntimeException(e);
    }
  }

  @SuppressWarnings({"unchecked"})
  private <T> T registerInBeanContainer(T bean, Annotation... qualifiers) {
    final Class<T> clazz;
    if (MockUtil.isMock(bean) || MockUtil.isSpy(bean)) {
      clazz = MockUtil.getMockHandler(bean).getMockSettings().getTypeToMock();
    } else {
      clazz = (Class<T>) bean.getClass();
    }
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(clazz, qualifiers)).thenReturn(
        bean);
    if (qualifiers.length > 1) {
      Stream.of(qualifiers)
          .forEach(q -> when(
              TestBeanContainer.getMockedBeanContainer().getBeanByType(clazz, q)).thenReturn(bean));
    }
    if (!clazz.isInterface()) {
      Class[] interfaces = clazz.getInterfaces();
      if (interfaces != null) {
        for (Class anInterface : interfaces) {
          when(TestBeanContainer.getMockedBeanContainer()
              .getBeanByType(anInterface, qualifiers)).thenReturn(bean);
        }
      }
    }
    return bean;
  }

  private class StubbedLoggerConfigurationManager extends LoggerConfigurationManager {
    StubbedLoggerConfigurationManager() {
      super();
      loadAllConfigurationFiles();
    }
  }

  private class StubbedSilverLoggerProvider extends SilverLoggerProvider {

    StubbedSilverLoggerProvider(final LoggerConfigurationManager loggerConfigurationManager) {
      super(loggerConfigurationManager);
    }
  }
}
