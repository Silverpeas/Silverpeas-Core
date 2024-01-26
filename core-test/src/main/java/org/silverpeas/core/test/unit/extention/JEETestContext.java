package org.silverpeas.core.test.unit.extention;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.GroupProvider;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.TestManagedBeanFeeder;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.test.extension.SilverTestEnvContext;
import org.silverpeas.kernel.test.util.Reflections;

import javax.enterprise.concurrent.ManagedThreadFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

/**
 * Context of the unit test environment to use in Silverpeas Core. It pre-registers into the bean
 * container dedicated to tests the following beans and mocks:
 * <ul>
 *   <li>A {@link I18n} mock</li>
 *   <li>A {@link UserProvider} mock</li>
 *   <li>A {@link GroupProvider} mock</li>
 *   <li>A {@link ManagedThreadFactory} bean</li>
 *   <li>A {@link ManagedThreadPool} bean</li>
 * </ul>
 * Additionally to these mocks and beans, the system user is pre-created and the current requester
 * can be also declared in the unit test by providing it through a method annotated with the
 * {@link RequesterProvider} annotation.
 * @author mmoquillon
 */
public class JEETestContext extends SilverTestEnvContext {

  private static final String SYSTEM = "SYSTEM";

  private final I18n i18n = mock(I18n.class);
  private final UserProvider userProvider = mock(UserProvider.class);

  private final GroupProvider groupProvider = mock(GroupProvider.class);

  @Override
  public void init() {
    mockI18n();
    mockUserProvider();
    initManagedThreadPool();
  }

  @Override
  public void beforeTest(TestExecutionContext context) {
    Class<?> test = context.getType();
    Method requesterProvider = recursivelyFindRequesterProvider(test);
    if (requesterProvider != null) {
      requesterProvider.trySetAccessible();
      User requester;
      try {
        requester = (User) requesterProvider.invoke(context.getInstance());
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new SilverpeasRuntimeException(e);
      }
      when(userProvider.getCurrentRequester()).thenReturn(requester);
    }
  }

  @Override
  public void afterTest(TestExecutionContext context) {
    // nothing to do after the test
  }

  @Override
  @NonNull
  public List<Object> getMocksToManage() {
    return List.of(i18n, userProvider, groupProvider);
  }

  @Override
  @NonNull
  public List<Class<?>> getBeansToManage() {
    return List.of();
  }

  @Override
  public void clear() {
    // clear all the caches
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    CacheAccessorProvider.getApplicationCacheAccessor().getCache().clear();
  }

  private void mockI18n() {
    when(i18n.getDefaultLanguage()).thenReturn("fr");
    when(i18n.getSupportedLanguages()).thenReturn(Set.of("fr", "en", "de"));
  }

  private void mockUserProvider() {
    User systemUser = mock(User.class);
    when(systemUser.getId()).thenReturn("-1");
    when(systemUser.getLastName()).thenReturn(SYSTEM);
    when(systemUser.getFirstName()).thenReturn(SYSTEM);
    doCallRealMethod().when(userProvider).getCurrentRequester();
    when(userProvider.getSystemUser()).thenReturn(systemUser);
  }

  private void initManagedThreadPool() {
    // ManagedThreadFactory is required by ManagedThreadPool: publishes it first into the bean
    // container for the dependency of ManagedThreadPool on it to be resolved.
    TestManagedBeanFeeder feeder = new TestManagedBeanFeeder();
    ManagedThreadFactory factory = Thread::new;
    ManagedThreadPool pool = Reflections.instantiate(ManagedThreadPool.class);
    feeder.manageBean(factory, ManagedThreadFactory.class);
    feeder.manageBean(pool, ManagedThreadPool.class);
  }

  private Method recursivelyFindRequesterProvider(final Class<?> testClass) {
    Method[] methods = testClass.getDeclaredMethods();
    return Stream.of(methods)
        .filter(m -> m.getAnnotation(RequesterProvider.class) != null)
        .filter(m -> User.class.isAssignableFrom(m.getReturnType()))
        .findFirst()
        .orElseGet(() -> {
          Class<?> superclass = testClass.getSuperclass();
          return superclass == null ? null : recursivelyFindRequesterProvider(superclass);
        });
  }
}
  