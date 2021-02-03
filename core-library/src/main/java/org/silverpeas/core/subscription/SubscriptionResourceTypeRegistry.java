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
package org.silverpeas.core.subscription;

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.*;

/**
 * Register of all subscription resource type available into Silverpeas.
 * <p>
 * By default, all {@link CommonSubscriptionResourceConstants} constants are registered.
 * </p>
 */
@Provider
@Singleton
public class SubscriptionResourceTypeRegistry {

  private final Map<String, SubscriptionResourceType> registry = new ConcurrentHashMap<>();

  public static SubscriptionResourceTypeRegistry get() {
    return ServiceProvider.getSingleton(SubscriptionResourceTypeRegistry.class);
  }

  @PostConstruct
  protected void setupDefaults() {
    Stream.of(COMPONENT, NODE).forEach(this::add);
  }

  private SubscriptionResourceTypeRegistry() {
    // Registry management
  }

  /**
   * Adds a {@link SubscriptionResourceType} instance.
   * <p>Only valid instance are registered.</p>
   * @param type a subscription resource type to add.
   */
  public void add(final SubscriptionResourceType type) {
    if (type.isValid()) {
      registry.put(type.getName(), type);
    }
  }

  /**
   * Gets a registered subscription type by its name if any.
   * @param name the name of a subscription type.
   * @return the corresponding {@link SubscriptionResourceType} instance if any,
   * {@link CommonSubscriptionResourceConstants#UNKNOWN} otherwise.
   */
  public SubscriptionResourceType getByName(final String name) {
    return StringUtil.isDefined(name) ? registry.getOrDefault(name, UNKNOWN) : UNKNOWN;
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
    return registry.values().stream().sorted(comparingInt(SubscriptionResourceType::priority)
        .thenComparing(SubscriptionResourceType::getName));
  }
}
