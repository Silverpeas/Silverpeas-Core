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

package org.silverpeas.core.test.extention;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
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
import org.silverpeas.core.test.util.MavenTestEnv;
import org.silverpeas.core.test.util.lang.TestSystemWrapper;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.LoggerConfigurationManager;
import org.silverpeas.core.util.logging.SilverLoggerProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
 * mock in the bean container (otherwise it is mocked and registered as for fields). Any methods
 * annotated with {@link TestManagedBean} are resolved in last.
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
 * <p>
 *  The ordering of the declaration of the different such annotated fields in the test class is
 *  very important as they are treated sequentially in their declaration ordering. So, any bean that
 *  is required by others beans has to be declared before those others beans.
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
        constructAndRegisterBean(type);
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
      Method[] methods = type.getDeclaredMethods();
      for (Method method : methods) {
        processTestManagedBeanAnnotation(method, testInstance);
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
        parameterContext.isAnnotated(TestManagedBean.class) ||
        parameterContext.getParameter().getType().equals(MavenTestEnv.class);
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
    if (parameter.getType().equals(MavenTestEnv.class)) {
      bean = new MavenTestEnv(extensionContext.getRequiredTestInstance());
    } else if (parameterContext.isAnnotated(TestManagedMock.class)) {
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
    Class<?> test = context.getRequiredTestClass();
    UserProvider mock = TestBeanContainer.getMockedBeanContainer().getBeanByType(UserProvider.class);
    Method requesterProvider = recursivelyFindRequesterProvider(test);
    if (requesterProvider != null) {
      requesterProvider.trySetAccessible();
      User requester = (User) requesterProvider.invoke(context.getRequiredTestInstance());
      when(mock.getCurrentRequester()).thenReturn(requester);
    }
  }

  private Method recursivelyFindRequesterProvider(final Class<?> testClass) {
    Method[] methods = testClass.getDeclaredMethods();
    return  Stream.of(methods)
        .filter(m -> m.getAnnotation(RequesterProvider.class) != null)
        .filter(m -> User.class.isAssignableFrom(m.getReturnType()))
        .findFirst()
        .orElseGet(() -> {
          Class<?> superclass = testClass.getSuperclass();
          return superclass == null ? null : recursivelyFindRequesterProvider(superclass);
        });
  }

  private void processTestManagedBeanAnnotation(final Field field, final Object testInstance)
      throws ReflectiveOperationException {
    if (field.isAnnotationPresent(TestManagedBean.class)) {
      final Object bean = setupInstanceField(field, testInstance);
      manageBean(bean, field.getType());
      if (field.isAnnotationPresent(Named.class)) {
        Named namedQualifier = field.getAnnotation(Named.class);
        String name = namedQualifier.value();
        when(TestBeanContainer.getMockedBeanContainer().getBeanByName(name)).thenReturn(bean);
      }
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
      field.trySetAccessible();
      field.set(testInstance, bean);
      registerInBeanContainer(bean);
      if (field.isAnnotationPresent(Named.class)) {
        Named namedQualifier = field.getAnnotation(Named.class);
        String name = namedQualifier.value();
        when(TestBeanContainer.getMockedBeanContainer().getBeanByName(name)).thenReturn(bean);
      }
    }
  }

  private void processTestedBeanAnnotation(final Field field, final Object testInstance)
      throws ReflectiveOperationException {
    if (field.isAnnotationPresent(TestedBean.class)) {
      final Object bean = setupInstanceField(field, testInstance);
      registerInBeanContainer(bean);
    }
  }

  private void processTestManagedBeanAnnotation(final Method method, final Object testInstance)
      throws ReflectiveOperationException {
    if (method.isAnnotationPresent(TestManagedBean.class)) {
      method.trySetAccessible();
      Class<?>[] classes = (Class<?>[]) method.invoke(testInstance);
      for (Class<?> clazz : classes) {
        constructAndRegisterBean(clazz);
      }
    }
  }

  private Object setupInstanceField(final Field field, final Object testInstance)
      throws ReflectiveOperationException {
    field.trySetAccessible();
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

  private void constructAndRegisterBean(final Class<?> type) throws ReflectiveOperationException {
    Object bean = instantiate(type);
    Objects.requireNonNull(bean);
    mockInjectedDependency(bean);
    invokePostConstruction(bean);
    manageBean(bean, type);
  }

  private void mockInjectedDependency(final Object bean)
      throws ReflectiveOperationException {
    loopInheritance(bean.getClass(), typeToLookup -> {
      Field[] beanFields = typeToLookup.getDeclaredFields();
      for (Field dependency : beanFields) {
        if (dependency.isAnnotationPresent(Inject.class)) {
          Object mock;
          if (dependency.getType().equals(Instance.class)) {
            ParameterizedType type = (ParameterizedType) dependency.getGenericType();
            Type beanType = type.getActualTypeArguments()[0];
            mock = new InstanceImpl<>((Class<?>)beanType);
          } else {
            mock = TestBeanContainer.getMockedBeanContainer().getBeanByType(dependency.getType());
            if (mock == null) {
              mock = mock(dependency.getType());
            }
          }
          dependency.trySetAccessible();
          dependency.set(bean, mock);
        }
      }
    });
  }

  private  <T> T instantiate(final Class<? extends T> beanType) {
    T bean;
    try {
      Constructor<? extends T> constructor = beanType.getDeclaredConstructor();
      constructor.trySetAccessible();
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
    mockSystemWrapper(testInstance);
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

  private void mockSystemWrapper(final Object testInstance) {
    TestSystemWrapper testSystemWrapper = new TestSystemWrapper();
    testSystemWrapper.initFor(testInstance);
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(SystemWrapper.class)).thenReturn(
        testSystemWrapper);
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
      managedThreadPoolConstructor.trySetAccessible();
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

    TypeConsumer registerer = t -> {
      putInTestBeanContainer(bean, t, qualifiers);
      if (qualifiers.length > 1) {
        Stream.of(qualifiers).forEach(q -> putInTestBeanContainer(bean, t, q));
      }
    };

    Function<Class<?>, Class<?>[]> typesFinder = c -> {
      Class<?>[] interfaces = c.getInterfaces();
      Class<?>[] types = Arrays.copyOf(interfaces, interfaces.length + 1);
      types[types.length - 1] = c;
      return types;
    };

    try {
      Stream.of(typesFinder.apply(clazz))
          .filter(t -> t.getTypeParameters().length == 0)
          .forEach(t -> {
            try {
              registerer.consume(t);
            } catch (ReflectiveOperationException e) {
              throw new SilverpeasRuntimeException(e);
            }
          });

      if (!clazz.isInterface()) {
        loopInheritance(clazz.getSuperclass(), c ->
          Stream.of(typesFinder.apply(c))
              .filter(t -> Modifier.isAbstract(t.getModifiers()))
              .filter(t -> t.getTypeParameters().length == 0)
              .forEach(t -> {
                try {
                  registerer.consume(t);
                } catch (ReflectiveOperationException e) {
                  throw new SilverpeasRuntimeException(e);
                }
              })
        );
      }
    } catch (ReflectiveOperationException e) {
      throw new SilverpeasRuntimeException(e);
    }
    return bean;
  }

  @SuppressWarnings({"unchecked"})
  private <T> void putInTestBeanContainer(final T bean, final Class type,
      Annotation... qualifiers) {
    Set existing = TestBeanContainer.getMockedBeanContainer().getAllBeansByType(type, qualifiers);
    if (!existing.isEmpty()) {
      final HashSet all = new HashSet<>(existing);
      all.add(bean);
      when(TestBeanContainer.getMockedBeanContainer()
          .getAllBeansByType(type, qualifiers)).thenReturn(all);
      if (existing.size() == 1) {
        when(TestBeanContainer.getMockedBeanContainer().getBeanByType(type, qualifiers)).thenThrow(
            new AmbiguousResolutionException("A bean of type " + type + " already exist!"));
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
      throws ReflectiveOperationException {
    Class<?> type = fromType;
    while (type != null && !type.isInterface() &&
        !type.equals(Object.class)) {
      consumer.consume(type);
      type = type.getSuperclass();
    }
  }

  @FunctionalInterface
  private interface TypeConsumer {
    void consume(final Class<?> type) throws ReflectiveOperationException;
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

  private class InstanceImpl<T> implements Instance<T> {

    private final Class<T> beansType;

    public InstanceImpl(Class<T> beansType) {
      this.beansType = beansType;
    }

    @Override
    public Instance<T> select(final Annotation... qualifiers) {
      return null;
    }

    @Override
    public <U extends T> Instance<U> select(final Class<U> subtype,
        final Annotation... qualifiers) {
      return null;
    }

    @Override
    public <U extends T> Instance<U> select(final TypeLiteral<U> subtype,
        final Annotation... qualifiers) {
      return null;
    }

    @Override
    public boolean isUnsatisfied() {
      return TestBeanContainer.getMockedBeanContainer().getAllBeansByType(beansType).isEmpty();
    }

    @Override
    public boolean isAmbiguous() {
      return false;
    }

    @Override
    public void destroy(final T instance) {
      // nothing to do
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
      Set<T> beans = TestBeanContainer.getMockedBeanContainer().getAllBeansByType(beansType);
      if (beans.isEmpty()) {
        return Collections.singleton(mock(beansType)).iterator();
      } else {
        return beans.iterator();
      }
    }

    @Override
    public T get() {
      Set<T> beans = TestBeanContainer.getMockedBeanContainer().getAllBeansByType(beansType);
      if (!beans.isEmpty()) {
        return beans.iterator().next();
      } else {
        return mock(beansType);
      }
    }
  }
}
