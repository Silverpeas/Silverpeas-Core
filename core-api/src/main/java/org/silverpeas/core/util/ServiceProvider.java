/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import org.silverpeas.core.admin.component.model.SilverpeasComponentDataProvider;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.kernel.ManagedBeanProvider;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.util.Mutable;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A provider of Silverpeas services such as repositories, controllers, transactional services, and
 * so on.
 * <p>
 * This provider is an adaptor to the underlying IoC by IOD subsystem used to manage the life-cycle
 * and the dependencies of some objects in Silverpeas. Usually, defining injection points is enough
 * for a bean to access the services in Silverpeas, but these injection points work only for beans
 * managed themselves by the IoC subsystem. Hence, this provider is for unmanaged beans in order
 * they have access all of the services available in Silverpeas. Such unmanaged beans are, for
 * example, the entities fetched from a data source.
 * </p>
 * <p>
 * The provider can be either used directly by the unmanaged beans or through a more typed-specific
 * provider. This latest solution is our preferred way.
 * </p>
 *
 * @author mmoquillon
 * @apiNote The {@link ServiceProvider} is a delegator to the {@link ManagedBeanProvider} single
 * instance.
 * @see ManagedBeanProvider
 */
public final class ServiceProvider {

  private ServiceProvider() {
  }

  /**
   * Gets an instance of the single implementation matching the specified service type and
   * satisfying the given qualifiers if any. The service is first looked for in the cache of the
   * current thead before asking it to the IOC subsystem. If the implementation is a singleton and
   * it's not yet cached, then the single instance is put in the cache of the current thread for
   * further retrieval.
   * <p>
   * Be attentive about that this method does not return result when trying to get the instance of
   * an implementation of services which are dealing with typed types (see
   * <code>ApplicationService&lt;T&gt;</code> for example). To deal with this kind of services,
   * please use the {@link javax.enterprise.inject.Instance} annotation in a managed bean.
   * </p>
   *
   * @param type the type of the service.
   * @param qualifiers zero, one or more qualifiers annotating the service to look for.
   * @param <T> the type of the service to return.
   * @return the service satisfying the expected type and, if any, the expected qualifiers.
   * @throws org.silverpeas.kernel.SilverpeasRuntimeException if no bean can be obtained for any
   * reasons. The reason is given in the wrapped exception.
   * @see ManagedBeanProvider#getManagedBean(Class, Annotation...)
   */
  public static <T> T getService(Class<T> type, Annotation... qualifiers) {
    return provide(() -> beanProvider().getManagedBean(type, qualifiers));
  }

  /**
   * Gets an object of the service qualified with the specified name. The service is first looked
   * for in the cache of the current thead before asking it to the IoC subsystem. If the
   * implementation is a singleton and it's not yet cached, then the single instance is put in the
   * cache of the current thread for further retrieval.
   * <p>
   * Be attentive about that this method does not return result when trying to get the instance of
   * an implementation of services which are dealing with typed types (see
   * <code>ApplicationService&lt;T&gt;</code> for example). To deal with this kind of services,
   * please use the {@link javax.enterprise.inject.Instance} annotation in a managed bean.
   * </p>
   *
   * @param name the name of the service.
   * @param <T> the type of the service to return.
   * @return the service matching the specified name.
   * @throws org.silverpeas.kernel.SilverpeasRuntimeException if no bean can be obtained for any
   * reasons. The reason is given in the wrapped exception.
   * @see ManagedBeanProvider#getManagedBean(String)
   */
  public static <T> T getService(String name) {
    return provide(() -> beanProvider().getManagedBean(name));
  }

  /**
   * Gets an instance of each service implementing the specified type and satisfying the given
   * qualifiers if any.
   * <p>
   * Be attentive about that this method does not return result when trying to get the instance of
   * an implementation of services which are dealing with typed types (see
   * <code>ApplicationService&lt;T&gt;</code> for example). To deal with this kind of services,
   * please use the {@link javax.enterprise.inject.Instance} annotation in a managed bean.
   * </p>
   *
   * @param type the common type of the services.
   * @param qualifiers zero, one or more qualifiers annotating the services to look for.
   * @param <T> the type of the services to return.
   * @return a set of services satisfying the expected type and, if any, the expected qualifiers, or
   * an empty set otherwise.
   * @throws org.silverpeas.kernel.SilverpeasRuntimeException if no bean can be obtained for any
   * reasons. The reason is given in the wrapped exception.
   * @see ManagedBeanProvider#getAllManagedBeans(Class, Annotation...)
   */
  public static <T> Set<T> getAllServices(Class<T> type, Annotation... qualifiers) {
    return provide(() -> beanProvider().getAllManagedBeans(type, qualifiers));
  }

  /**
   * <p>
   * Some APIs are defined at a high level and they can be implemented by several transverse or
   * application services. Such APIs observes a convention naming for the services supporting them.
   * Those APIs expects then each implementation follows this convention naming so that they can be
   * retrieved by the engine providing the API to perform some specific tasks. The convention naming
   * is:
   * <code>[COMPONENT NAME][API NAME SUFFIX]</code>. For example, the Component Instance Post
   * Construction API expects to find any implementations that have, in their name, the
   * <code>InstancePostConstruction</code> suffix. By doing so, the Component Instance Management
   * Engine can invoke the service related to the application for which an instance has just been
   * created. <code>kmeliaInstancePostConstruction</code> is an example of such a service name for
   * the Kmelia application.
   * </p>
   * <p>
   * In order to be retrieved by the APIs, the implementation has to be qualified with a name
   * following the expected naming rule by using the {@link Named} annotation. To be provided by
   * this way, an implementation of an API must use {@link Named} annotation and fill
   * {@code Named#value()} in case where the implementation class name does not correspond to
   * <code>[COMPONENT NAME][SERVICE NAME SUFFIX]</code> concatenation.
   * </p>
   * <p>
   * Silverpeas components which supports the Workflow API are taken in charge by this method. In
   * case of a workflow component, the implementation has to satisfies the following more specific
   * naming convention: <code>processManager[WORKFLOW API NAME SUFFIX]</code>.
   * </p>
   *
   * @param applicationId a component instance identifier or a component name.
   * @param apiName a service name suffix.
   * @param <T> the concrete type of the service to return.
   * @return the service implementing the given API for the specified component (a transverse
   * component or an application instance).
   * @throws org.silverpeas.kernel.SilverpeasRuntimeException if no bean can be obtained for any
   * reasons. The reason is given in the wrapped exception.
   * @see ManagedBeanProvider#getManagedBean(String)
   */
  public static <T> T getService(final String applicationId, final String apiName) {
    final Mutable<String> componentName = Mutable.ofNullable(
        SilverpeasComponentInstance.getComponentName(applicationId));
    if (!componentName.isPresent()) {
      componentName.set(applicationId);
    }
    final String serviceName;
    boolean workflow = false;
    try {
      workflow = SilverpeasComponentDataProvider.get().isWorkflow(componentName.get());
    } catch (SilverpeasRuntimeException ignore) {
      // it is not a workflow
    }
    if (workflow) {
      serviceName = "processManager" + apiName;
    } else {
      serviceName = componentName.get().substring(0, 1).toLowerCase() +
          componentName.get().substring(1) + apiName;
    }
    return getService(serviceName);
  }

  private static <T> T provide(Supplier<T> beanSupplier) {
    try {
      return beanSupplier.get();
    } catch (RuntimeException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  private static ManagedBeanProvider beanProvider() {
    return ManagedBeanProvider.getInstance();
  }

}
