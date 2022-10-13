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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.subscription;

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants;
import org.silverpeas.core.subscription.service.AbstractSubscription;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.ComponentSubscriptionResource;
import org.silverpeas.core.subscription.service.NodeSubscription;
import org.silverpeas.core.subscription.service.NodeSubscriptionResource;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.*;

/**
 * Register of all subscription resource type available into Silverpeas.
 * <p>
 * By default, all {@link CommonSubscriptionResourceConstants} constants are registered.
 * </p>
 */
@Provider
@Singleton
public class SubscriptionFactory {

  private final Map<String, SubscriptionResourceType> registry = new ConcurrentHashMap<>();
  private final Map<String, SubscriptionResourceConstructor> subscriptionResourceConstructorRegistry = new ConcurrentHashMap<>();
  @SuppressWarnings("rawtypes")
  private final Map<String, SubscriptionConstructor> subscriptionConstructorRegistry = new ConcurrentHashMap<>();

  public static SubscriptionFactory get() {
    return ServiceProvider.getSingleton(SubscriptionFactory.class);
  }

  @PostConstruct
  protected void setupDefaults() {
    register(COMPONENT,
        (r, s, i) -> ComponentSubscriptionResource.from(i),
        (s, r, c) -> new ComponentSubscription(s, (ComponentSubscriptionResource) r, c));
    register(NODE,
        (r, s, i) -> NodeSubscriptionResource.from(new NodePK(r, s, i)),
        (s, r, c) -> new NodeSubscription(s, (NodeSubscriptionResource) r, c));
  }

  private SubscriptionFactory() {
    // Registry management
  }

  /**
   * Adds a {@link SubscriptionResourceType} instance.
   * <p>Only valid instance are registered.</p>
   * @param type a subscription resource type to add.
   * @param srConstructor a constructor of {@link SubscriptionResource} of type
   * {@link SubscriptionResourceType}.
   * @param sConstructor a constructor of {@link Subscription} of a {@link SubscriptionResource}.
   */
  @SuppressWarnings("rawtypes")
  public void register(final SubscriptionResourceType type,
      final SubscriptionResourceConstructor srConstructor,
      final SubscriptionConstructor sConstructor) {
    if (type.isValid()) {
      registry.put(type.getName(), type);
      subscriptionResourceConstructorRegistry.put(type.getName(), srConstructor);
      subscriptionConstructorRegistry.put(type.getName(), sConstructor);
    }
  }

  /**
   * Gets a registered subscription type by its name if any.
   * @param name the name of a subscription type.
   * @return the corresponding {@link SubscriptionResourceType} instance if any,
   * {@link CommonSubscriptionResourceConstants#UNKNOWN} otherwise.
   */
  public SubscriptionResourceType getSubscriptionResourceTypeByName(final String name) {
    return StringUtil.isDefined(name) ? registry.getOrDefault(name, UNKNOWN) : UNKNOWN;
  }

  /**
   * Creates a {@link SubscriptionResource} instance from given data.
   * @param type a {@link SubscriptionResourceType} instance.
   * @param resourceId an identifier of resource (the local one)
   * @param space a space reference into which the resource is handled (legacy management).
   * @param instanceId an identifier of a component instance into which the resource is hosted.
   * @return an initialized {@link SubscriptionResource} instance.
   */
  public SubscriptionResource createSubscriptionResourceInstance(
      final SubscriptionResourceType type, final String resourceId, final String space,
      final String instanceId) {
    return ofNullable(subscriptionResourceConstructorRegistry.get(type.getName()))
        .map(c -> c.create(resourceId, space, instanceId))
        .orElse(null);
  }

  /**
   * Creates a {@link Subscription} instance from given data.
   * @param subscriber a {@link SubscriptionSubscriber} instance.
   * @param resource a {@link SubscriptionResource} instance.
   * @param creatorId the user id that has handled the subscription.
   * @return an initialized {@link Subscription} instance.
   */
  @SuppressWarnings("unchecked")
  public <R extends SubscriptionResource> AbstractSubscription<R> createSubscriptionInstance(
      final SubscriptionSubscriber subscriber, final R resource, final String creatorId) {
    return ofNullable(subscriptionConstructorRegistry.get(resource.getType().getName()))
        .map(c -> c.create(subscriber, resource, creatorId))
        .orElse(null);
  }

  /**
   * Gets all registered subscription resource types.
   * <p>
   * Data are sorted first by {@link SubscriptionResourceType#priority()} then by
   * {@link SubscriptionResourceType#getName()}.
   * </p>
   * @return a stream of registered subscription resource type.
   */
  public Stream<SubscriptionResourceType> streamAll() {
    return registry.values()
        .stream()
        .sorted(comparingInt(SubscriptionResourceType::priority).thenComparing(
            SubscriptionResourceType::getName));
  }

  @FunctionalInterface
  public interface SubscriptionResourceConstructor {
    SubscriptionResource create(final String resourceId, final String space,
        final String instanceId);
  }

  @FunctionalInterface
  public interface SubscriptionConstructor<R extends SubscriptionResource> {
    AbstractSubscription<R> create(final SubscriptionSubscriber subscriber, final R resource,
        final String creatorId);
  }
}
