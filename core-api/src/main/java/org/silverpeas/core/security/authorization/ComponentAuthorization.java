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
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Component security provides a way to check a user have enough rights to access a given object in
 * a Silverpeas component instance. Each Silverpeas component should implements this interface
 * according to the objects or resources it manages.
 */
public interface ComponentAuthorization {

  static Set<ComponentAuthorization> getAll() {
    return ServiceProvider.getAllServices(ComponentAuthorization.class);
  }

  /**
   * Is this service related to the specified component instance. The service is related to the
   * specified instance if it is a service defined by the application from which the instance
   * was spawned.
   * @param instanceId the unique instance identifier of the component.
   * @return true if the instance is spawn from the application to which the service is related.
   * False otherwise.
   */
  boolean isRelatedTo(String instanceId);

  /**
   * Filtering the given resources from user rights.
   * <p>
   * The order into which the given resources are provided is kept.
   * </p>
   * @param <T> the type of a resource contained into resource list.
   * @param resources the resources to filter.
   * @param converter the converter which permits to get the {@link ComponentResourceReference} instance
   * from a {@code T} resource.
   * @param userId the identifier of the user.
   * @param operations the operations to give to AccessControlContext.
   * @return a filtered stream of {@code T} resource, ordered as the given resources are.
   */
  <T> Stream<T> filter(Collection<T> resources, Function<T, ComponentResourceReference> converter,
      String userId, final AccessControlOperation... operations);

  /**
   * Representation of a resource in order to filtered by the API implementations.
   */
  class ComponentResourceReference {
    final String localId;
    final String type;
    final String instanceId;

    public ComponentResourceReference(final String localId, final String type, final String instanceId) {
      this.localId = localId;
      this.type = type;
      this.instanceId = instanceId;
    }

    public String getLocalId() {
      return localId;
    }

    public String getType() {
      return type;
    }

    public String getInstanceId() {
      return instanceId;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final ComponentResourceReference that = (ComponentResourceReference) o;
      return localId.equals(that.localId) &&
          Objects.equals(type, that.type) && instanceId.equals(that.instanceId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(localId, type, instanceId);
    }
  }
}