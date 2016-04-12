package org.silverpeas.core.util;

import org.silverpeas.core.util.BeanContainer;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * A provider of Silverpeas services such as repositories, controllers, transactional services, and
 * so on.
 *
 * This provider is an adaptor to the underlying IoD subsystem used to manage the life-cycle
 * and the dependencies of the objects in Silverpeas. Usually, defining injection points is enough
 * for a bean to access the services in Silverpeas, but these injection points work only for beans
 * managed themselves by the IoD subsystem. Hence, this provider is for unmanaged beans in order
 * they have access all of the services available in Silverpeas.
 *
 * The provider can be either used directly by the unmanaged beans or through a more typed-specific
 * provider. This latest solution is our preferred way.
 *
 * The service provider doesn't use directly the IoD subsystem to fetch the asked service. Instead
 * it delegates the service fetching to another object, a {@code BeanContainer} instance that is
 * a wrapper of the actual IoD subsystem in use. The bind between the {@code BeanContainer}
 * interface and its implementation is performed by the Java SPI (Java Service Provider Interface).
 * Only the first available bean container implementation is loaded by the {@code ServiceProvider}
 * class.
 * @author mmoquillon
 */
public final class ServiceProvider {

  private static final BeanContainer _currentContainer;

  static {
    Iterator<BeanContainer> iterator = ServiceLoader.load(BeanContainer.class).iterator();
    if (iterator.hasNext()) {
      _currentContainer = iterator.next();
    } else {
      throw new RuntimeException(
          "No IoD container detected! At least one bean container should be available!");
    }
  }

  private ServiceProvider() {
  }


  /**
   * @see BeanContainer#getBeanByType(Class, java.lang.annotation.Annotation...)
   */
  public static <T> T getService(Class<T> type, Annotation... qualifiers)
      throws IllegalStateException {
    return beanContainer().getBeanByType(type, qualifiers);
  }

  /**
   * @see BeanContainer#getBeanByName(String)
   */
  public static <T> T getService(String name) throws IllegalStateException {
    return beanContainer().getBeanByName(name);
  }

  /**
   * @see BeanContainer#getAllBeansByType(Class, java.lang.annotation.Annotation...)
   */
  public static <T> Set<T> getAllServices(Class<T> type, Annotation... qualifiers) {
    return beanContainer().getAllBeansByType(type, qualifiers);
  }

  private static BeanContainer beanContainer() {
    return _currentContainer;
  }

}
