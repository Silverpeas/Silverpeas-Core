/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.test.extension;

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
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.test.TestBeanContainer;
import org.silverpeas.core.test.TestSystemWrapper;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.LoggerConfigurationManager;
import org.silverpeas.core.util.logging.SilverLoggerProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

/**
 * Prepares the environment specific to Silverpeas to run unit tests.
 * <p>Firstly, it mocks the container of beans and set ups it for the tests with some of the
 * common beans in Silverpeas: {@link UserProvider}, {@link GroupProvider}, {@link SystemWrapper},
 * {@link ManagedThreadPool}, and the logging system.
 * </p>
 * <p>
 * Secondly it scans for fields and parameters annotated with {@link TestManagedBean} and
 * {@link TestManagedMock} to register them automatically into the bean container used in tests.
 * If the class of a {@link TestManagedBean} annotated field is qualified by a {@link Qualifier}
 * annotated annotation, then it is registered under that qualifier also. For any parameter
 * annotated with {@link TestManagedMock}, it is first resolved by looking for an already registered
 * mock in the bean container (otherwise it is mocked and registered as for fields).
 * </p>
 * <p>
 * Thirdly it scans for fields annotated with {@link TestedBean} to scan it for injection point in
 * order to resolve those dependencies either by settings them with a bean already registered into
 * the bean container or by mocking it.
 * </p>
 * <p>
 *  Finally it looks for a method in the test class that is annotated with {@link RequesterProvider}
 *  to execute it and to set the returned {@link User} instance as the default requester to use
 *  in all the tests of the class.
 * </p>
 * @author mmoquillon
 */
public class SilverTestEnv implements TestInstancePostProcessor, ParameterResolver, BeforeEachCallback {

  /**
   * Injects in the unit test class all the fields that are annotated with one of the supported
   * annotations by {@link SilverTestEnv} extension ({@link TestManagedMock}, {@link TestManagedBean},
   * {@link TestedBean}, ...). Each of such annotated beans will be either mocked or instantiated
   * with their default constructor and then registered into the bean container used for the unit
   * tests.
   * <p>
   * <strong>Be caution:</strong> any {@link TestedBean} annotated fields should be declared
   * lastly for their dependencies to have a change to be set with any previous declared
   * {@link TestManagedMock} and {@link TestManagedBean} annotated field values.
   * </p>
   * @param testInstance the instance of the test class.
   * @param context the context of the extension.
   * @throws Exception if an error occurs while injecting the fields.
   */
  @Override
  public void postProcessTestInstance(final Object testInstance, final ExtensionContext context)
      throws Exception {
    reset(TestBeanContainer.getMockedBeanContainer());
    mockCommonBeans(testInstance);
    TestManagedBeans testManagedBeans =
        testInstance.getClass().getAnnotation(TestManagedBeans.class);
    if (testManagedBeans != null) {
      for (Class<?> type : testManagedBeans.value()) {
        Object bean = instantiate(type);
        Objects.requireNonNull(bean);
        mockInjectedDependency(bean);
        invokePostConstruction(bean);
        manageBean(bean, type);
      }
    }
    TestManagedMocks testManagedMocks = testInstance.getClass().getAnnotation(TestManagedMocks.class);
    if (testManagedMocks != null) {
      for (Class<?> type : testManagedMocks.value()) {
        Object mock = mock(type);
        registerInBeanContainer(mock);
      }
    }
    loopInheritance(testInstance.getClass(), type -> {
      Field[] fields = type.getDeclaredFields();
      for (Field field : fields) {
        processTestManagedBeanAnnotation(field, testInstance);
        processMockedBeanAnnotation(field, testInstance);
        processTestedBeanAnnotation(field, testInstance);
      }
    });
  }

  /**
   * Is the parameter in a test's method is supported by this extension for value injection?
   * @param parameterContext the context of the parameter.
   * @param extensionContext the context of the extension.
   * @return true if the parameter is either annotated with @{@link TestManagedBean} or with
   * {@link TestManagedMock}
   */
  @Override
  public boolean supportsParameter(final ParameterContext parameterContext,
      final ExtensionContext extensionContext) {
    return parameterContext.isAnnotated(TestManagedMock.class) ||
        parameterContext.isAnnotated(TestManagedBean.class);
  }

  /**
   * Resolves the parameter referred by the parameter context by valuing it according to its
   * annotation: if annotated with {@link TestManagedBean}, the parameter will be instantiated with
   * its default constructor; if annotated with {@link TestManagedMock}, the parameter will be mocked.
   * @param parameterContext the context of the parameter.
   * @param extensionContext the context of the extension.
   * @return the value of the parameter to inject.
   */
  @Override
  public Object resolveParameter(final ParameterContext parameterContext,
      final ExtensionContext extensionContext) {
    Object bean;
    final Parameter parameter = parameterContext.getParameter();
    if (parameterContext.isAnnotated(TestManagedMock.class)) {
      bean = TestBeanContainer.getMockedBeanContainer().getBeanByType(parameter.getType());
      if (bean == null) {
        TestManagedMock annotation = parameterContext.findAnnotation(TestManagedMock.class)
            .orElseThrow(RuntimeException::new);
        if (annotation.stubbed()) {
          bean = mock(parameter.getType());
        } else {
          bean = spy(parameter.getType());
        }
        registerInBeanContainer(bean);
      }
    } else if (parameterContext.isAnnotated(TestManagedBean.class)) {
      bean = TestBeanContainer.getMockedBeanContainer().getBeanByType(parameter.getType());
      if (bean == null) {
        bean = instantiate(parameter.getType());
        manageBean(bean, parameter.getType());
      }
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
    CacheServiceProvider.clearAllThreadCaches();
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

  private void processTestManagedBeanAnnotation(final Field field, final Object testInstance)
      throws IllegalAccessException {
    if (field.isAnnotationPresent(TestManagedBean.class)) {
      final Object bean = setupInstanceField(field, testInstance);
      manageBean(bean, field.getType());
    }
  }

  private void processMockedBeanAnnotation(final Field field, final Object testInstance)
      throws IllegalAccessException {
    if (field.isAnnotationPresent(TestManagedMock.class)) {
      TestManagedMock annotation = field.getAnnotation(TestManagedMock.class);
      Object bean;
      if (annotation.stubbed()) {
        bean = mock(field.getType());
      } else {
        bean = spy(field.getType());
      }
      field.setAccessible(true);
      field.set(testInstance, bean);
      registerInBeanContainer(bean);
    }
  }

  private void processTestedBeanAnnotation(final Field field, final Object testInstance)
      throws IllegalAccessException {
    if (field.isAnnotationPresent(TestedBean.class)) {
      final Object bean = setupInstanceField(field, testInstance);
      registerInBeanContainer(bean);
    }
  }

  private Object setupInstanceField(final Field field, final Object testInstance)
      throws IllegalAccessException {
    field.setAccessible(true);
    Object bean = field.get(testInstance);
    if (bean == null) {
      bean = instantiate(field.getType());
      field.set(testInstance, bean);
    }
    Objects.requireNonNull(bean);
    mockInjectedDependency(bean);
    invokePostConstruction(bean);
    return bean;
  }

  private void mockInjectedDependency(final Object bean)
      throws IllegalAccessException {
    loopInheritance(bean.getClass(), typeToLookup -> {
      Field[] beanFields = typeToLookup.getDeclaredFields();
      for (Field dependency : beanFields) {
        if (dependency.isAnnotationPresent(Inject.class)) {
          Object mock =
              TestBeanContainer.getMockedBeanContainer().getBeanByType(dependency.getType());
          if (mock == null) {
            mock = mock(dependency.getType());
          }
          dependency.setAccessible(true);
          dependency.set(bean, mock);
        }
      }
    });
  }

  private  <T> T instantiate(final Class<? extends T> beanType) {
    T bean;
    try {
      Constructor<? extends T> constructor = beanType.getDeclaredConstructor();
      constructor.setAccessible(true);
      bean = constructor.newInstance();
    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
        IllegalAccessException e) {
      bean = null;
    }
    return bean;
  }

  private void invokePostConstruction(final Object bean) {
    try {
      Method[] methods = bean.getClass().getDeclaredMethods();
      for (Method method : methods) {
        if (method.isAnnotationPresent(PostConstruct.class)) {
          method.setAccessible(true);
          method.invoke(bean);
          break;
        }
      }
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  private void manageBean(final Object bean, final Class<?> beanType) {
    Annotation[] qualifiers = Stream.of(beanType.getDeclaredAnnotations())
        .filter(a -> a.annotationType().getAnnotationsByType(Qualifier.class).length > 0)
        .toArray(Annotation[]::new);
    registerInBeanContainer(bean, qualifiers);
  }

  private void mockCommonBeans(final Object testInstance) {
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
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(SystemWrapper.class))
        .thenReturn(testSystemWrapper);
  }

  private void mockLoggingSystem() {
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
      clazz = (Class<T>) MockUtil.getMockHandler(bean).getMockSettings().getTypeToMock();
    } else {
      clazz = (Class<T>) bean.getClass();
    }
    Class<?>[] types = Arrays.copyOf(clazz.getInterfaces(), clazz.getInterfaces().length + 1);
    types[types.length - 1] = clazz;

    for (Class type : types) {
      putInTestBeanContainer(bean, type, qualifiers);
      if (qualifiers.length > 1) {
        Stream.of(qualifiers).forEach(q -> putInTestBeanContainer(bean, type, q));
      }
    }
    return bean;
  }

  @SuppressWarnings({"unchecked"})
  private <T> void putInTestBeanContainer(final T bean, final Class type,
      Annotation... qualifiers) {
    Set existing = TestBeanContainer.getMockedBeanContainer().getAllBeansByType(type, qualifiers);
    if (!existing.isEmpty()) {
      if (!type.isAnnotationPresent(Singleton.class)) {
        final HashSet all = new HashSet(existing);
        all.add(bean);
        when(TestBeanContainer.getMockedBeanContainer()
            .getAllBeansByType(type, qualifiers)).thenReturn(all);
        if (existing.size() == 1) {
          when(TestBeanContainer.getMockedBeanContainer().getBeanByType(type, qualifiers))
              .thenThrow(
                  new AmbiguousResolutionException("A bean of type " + type + " already exist!"));
        }
      }
    } else {
      when(TestBeanContainer.getMockedBeanContainer().getBeanByType(type, qualifiers)).thenReturn(
          bean);
      when(TestBeanContainer.getMockedBeanContainer()
          .getAllBeansByType(type, qualifiers)).thenReturn(
          Stream.of(bean).collect(Collectors.toSet()));
    }
  }

  private void loopInheritance(final Class<?> fromType, final TypeConsumer consumer)
      throws IllegalAccessException {
    Class<?> type = fromType;
    while (type != null && !type.isInterface() &&
        !type.equals(Object.class)) {
      consumer.consume(type);
      type = type.getSuperclass();
    }
  }

  @FunctionalInterface
  private interface TypeConsumer {
    void consume(final Class<?> type) throws IllegalAccessException;
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
