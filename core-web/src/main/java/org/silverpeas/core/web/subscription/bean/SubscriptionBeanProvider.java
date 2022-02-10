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
package org.silverpeas.core.web.subscription.bean;

import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResourceType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.silverpeas.core.subscription.SubscriptionServiceProvider.getSubscribeService;

/**
 * This common subscription bean provider can return results from a default {@link
 * SubscriptionBeanService} implementations that compute basic
 * operations or from an implementation provided by the component itself in order to compute a
 * result that takes into account specific component rules.
 * @author silveryocha
 */
public class SubscriptionBeanProvider {

  private static final String DEFAULT_IMPLEMENTATION_ID = "default";
  private static final Map<String, SubscriptionBeanService> typeImplementations = new HashMap<>();

  private SubscriptionBeanProvider() {
    // Provider class
  }

  /**
   * Registers a new implementation of {@link SubscriptionBeanService}
   * to manage and to provide.
   * @param service the service instance to register.
   */
  public static void registerSubscriptionBeanService(AbstractSubscriptionBeanService service) {
    final List<SubscriptionResourceType> types = service.getHandledSubscriptionResourceTypes();
    if (types.isEmpty()) {
      typeImplementations.put(DEFAULT_IMPLEMENTATION_ID, service);
    } else {
      types.forEach(t -> typeImplementations.put(t.getName(), service));
    }
  }

  /**
   * @see SubscriptionBeanService#getSubscriptionTypeListLabel(SubscriptionResourceType, String)
   */
  public static String getSubscriptionTypeListLabel(final SubscriptionResourceType type,
      final String language) {
    return getService(type).getSubscriptionTypeListLabel(type, language);
  }

  /**
   * @see SubscriptionBeanService#toSubscriptionBean(Collection, String)
   */
  public static List<AbstractSubscriptionBean> getByUserSubscriberAndSubscriptionResourceType(
      final SubscriptionResourceType type, final String userId, final String language) {
    final Collection<Subscription> list = getSubscribeService().getByUserSubscriber(userId).stream()
        .filter(s -> s.getResource().getType() == type)
        .collect(Collectors.toList());
    return getService(type).toSubscriptionBean(list, language);
  }

  /**
   * @see SubscriptionBeanService#toSubscriptionBean(Collection, String)
   */
  public static Optional<AbstractSubscriptionBean> getBySubscription(
      final Subscription subscription, final String language) {
    final Collection<Subscription> list = Collections.singletonList(subscription);
    return getService(subscription.getResource().getType()).toSubscriptionBean(list, language)
        .stream().findFirst();
  }

  /**
   * Gets the service implemented for the type if any, the default one otherwise.
   * @param type a {@link SubscriptionResourceType} instance.
   * @return an instance of {@link SubscriptionBeanService}.
   */
  private static SubscriptionBeanService getService(final SubscriptionResourceType type) {
    return Optional
        .ofNullable(typeImplementations.get(type.getName()))
        .orElse(typeImplementations.get(DEFAULT_IMPLEMENTATION_ID));
  }
}
