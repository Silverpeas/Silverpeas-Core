/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.util;

import org.silverpeas.core.admin.component.model.SilverpeasComponentDataProvider;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;

import javax.inject.Named;
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
   * Gets an instance of the single implementation of mandatory given type and qualifier if any.<br>
   * Please be attentive about that this method does not return result when trying to get
   * instance of an implementation of interfaces which are dealing with typed types
   * (<code>ApplicationService&lt;T&gt;</code> for example).<br>
   * To deal with this kind of interfaces, please use the
   * {@link javax.enterprise.inject.Instance} method. An example can be found into core-library
   * project at ApplicationServiceTest class.
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return the bean satisfying the expected type and, if any, the expected qualifiers.
   * @throws java.lang.IllegalStateException if no bean of the specified type and with the
   * specified qualifiers can be found.
   * @see BeanContainer#getBeanByType(Class, java.lang.annotation.Annotation...)
   */
  public static <T> T getService(Class<T> type, Annotation... qualifiers) {
    return beanContainer().getBeanByType(type, qualifiers);
  }

  /**
   * Gets an instance of the single implementation that is qualified by the specified name.<br>
   * Please be attentive about that this method does not return result when trying to get
   * instance of an implementation of interfaces which are dealing with typed types
   * (<code>ApplicationService&lt;T&gt;</code> for example).<br>
   * To deal with this kind of interfaces, please use the
   * {@link javax.enterprise.inject.Instance} method. An example can be found into core-library
   * project at ApplicationServiceTest class.
   * @param name the name of the bean.
   * @param <T> the type of the bean to return.
   * @return the bean matching the specified name.
   * @throws java.lang.IllegalStateException if no bean can be found with the specified name.
   * @see BeanContainer#getBeanByName(String)
   */
  public static <T> T getService(String name) {
    return beanContainer().getBeanByName(name);
  }

  /**
   * Gets an instance of all implementations of mandatory given type and qualifier if any.<br>
   * Please be attentive about that this method does not return result when trying to get
   * instance implementations of interfaces which are dealing with typed types
   * (<code>ApplicationService&lt;T&gt;</code> for example).<br>
   * To deal with this kind of interfaces, please use the
   * {@link javax.enterprise.inject.Instance} method. An example can be found into core-library
   * project at ApplicationServiceTest class.
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return a set of beans satisfying the expected type and, if any, the expected qualifiers, or
   * an empty set otherwise.
   * @see BeanContainer#getAllBeansByType(Class, java.lang.annotation.Annotation...)
   */
  public static <T> Set<T> getAllServices(Class<T> type, Annotation... qualifiers) {
    return beanContainer().getAllBeansByType(type, qualifiers);
  }

  /**
   * <p>
   * Some API are defined at a high level, core-api for example, and be implemented by several
   * services or component services.
   * </p>
   * <p>
   * This method offers to provide an implementation of an API by observing following convention of
   * service naming: <br><code>[COMPONENT NAME][SERVICE NAME
   * SUFFIX]</code><br><code>kmeliaInstancePostConstruction</code> for example, where
   * <code>kmelia</code> the component name and <code>InstancePostConstruction</code> the service
   * name suffix.
   * </p>
   * <p>
   * To be provided by this way, an implementation of an API must use {@link Named}
   * annotation and fill {@code Named#value()} in case where the implementation class name does not
   * correspond to <code>[COMPONENT NAME][SERVICE NAME SUFFIX]</code> concatenation.
   * </p>
   * <p>
   * This way of service implementation getting take care about silverpeas components which have a
   * workflow behavior. In case of workflow component, the implementation provided will be the one
   * satisfying the service name <code>processManager[SERVICE NAME SUFFIX]</code>.
   * </p>
   * @param componentInstanceIdOrComponentName a component instance identifier of a component name.
   * @param nameSuffix a service name suffix.
   * @param <T> the type of the bean to return.
   * @return the bean matching the specified name.
   * @throws java.lang.IllegalStateException if no bean can be found with the specified name.
   * @throws java.lang.IllegalArgumentException if componentInstanceIdOrComponentName is not found.
   * @see BeanContainer#getBeanByName(String)
   */
  public static <T> T getServiceByComponentInstanceAndNameSuffix(
      final String componentInstanceIdOrComponentName, final String nameSuffix) {
    final Mutable<String> componentName = Mutable.ofNullable(
        SilverpeasComponentInstance.getComponentName(componentInstanceIdOrComponentName));
    if (!componentName.isPresent()) {
      componentName.set(componentInstanceIdOrComponentName);
    }
    final String serviceName;
    boolean workflow = false;
    try {
      workflow = SilverpeasComponentDataProvider.get().isWorkflow(componentName.get());
    } catch (IllegalStateException ignore) {
      // it is not a workflow
    }
    if (workflow) {
      serviceName = "processManager" + nameSuffix;
    } else {
      serviceName =
          componentName.get().substring(0, 1).toLowerCase() + componentName.get().substring(1) +
              nameSuffix;
    }
    return getService(serviceName);
  }

  private static BeanContainer beanContainer() {
    return _currentContainer;
  }

}
