/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.accesscontrol;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * A provider of the different access controllers available in Silverpeas.
 *
 * The different access controllers are all managed in the IoC container and then can be get by
 * dependency injection. Nevertheless, not all objects in Silverpeas are managed by the IoC
 * container and they can need the services of an access controller. The AccessControllerProvider
 * aims to make available the different access controllers to such objects.
 */
public class AccessControllerProvider implements ApplicationContextAware {

  private static final AccessControllerProvider instance = new AccessControllerProvider();

  private ApplicationContext context;

  /**
   * Gets an instance of the AccessControllerProvider class.
   * @return an AccessControllerProvider instance.
   */
  public static AccessControllerProvider getInstance() {
    return instance;
  }

  /**
   * Gets the access controller identified by the specified name.
   * @param name the unique name of the access controller. It is the name under which it has been
   * registered into the IoC container.
   * @param <T> the type of the resource that is used in the access control mechanism.
   * @return the asked access controller.
   */
  public static <T> AccessController<T> getAccessController(String name) {
    return (AccessController<T>) getInstance().context.getBean(name);
  }

  /**
   * Set the ApplicationContext that this object runs in.
   * Normally this call will be used to initialize the object.
   * <p>Invoked after population of normal bean properties but before an init callback such
   * as {@link org.springframework.beans.factory.InitializingBean#afterPropertiesSet()}
   * or a custom init-method. Invoked after {@link org.springframework.context
   * .ResourceLoaderAware#setResourceLoader},
   * {@link org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher}
   * and
   * {@link org.springframework.context.MessageSourceAware}, if applicable.
   * @param applicationContext the ApplicationContext object to be used by this object
   * @throws org.springframework.context.ApplicationContextException in case of context
   * initialization errors
   * @throws org.springframework.beans.BeansException if thrown by application context methods
   * @see org.springframework.beans.factory.BeanInitializationException
   */
  @Override
  public void setApplicationContext(final ApplicationContext applicationContext)
      throws BeansException {
    this.context = applicationContext;
  }
}
