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
package org.silverpeas.core;

import com.google.common.base.Objects;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Reference to a resource managed by a component instance in Silverpeas. Such a reference is a
 * {@link ComponentResourceIdentifier} used to identify any kinds of resources in Silverpeas without
 * having knowledge on what this resource is. It is dedicated to be used to refer a given resource
 * without having to pass it explicitly; information about the resource can be then passed from
 * service to service without any coupling between them or with the underlying resource.
 * <p>
 * A resource is referred by such a reference by both its local identifier and the
 * identifier of the component instance it belongs to.
 * </p>
 * <p>
 * For compatibility reason, {@link ResourceReference} extends {@link WAPrimaryKey} but this
 * inheritance will be removed in the future.
 * </p>
 *
 * @author Nicolas Eysseric
 * @author mmoquillon
 */
@SuppressWarnings("deprecation")
public class ResourceReference extends WAPrimaryKey
    implements ComponentResourceIdentifier, Serializable {

  private static final long serialVersionUID = 1551181996404764039L;
  private static final String ABSOLUTE_ID_FORMAT = "{0}:{1}";

  /**
   * Identifier referring that the resource behind is unknown and doesn't require to be known.
   */
  public static final String UNKNOWN_ID = "";

  /**
   * Constructs a new reference to the specified contribution.
   *
   * @param identifier the unique identifier of a contribution.
   */
  public ResourceReference(ContributionIdentifier identifier) {
    super(identifier.getLocalId(), identifier.getComponentInstanceId());
  }

  /**
   * Constructs a new reference to a resource with the specified identifier. The resource doesn't
   * belong to a component instance or that component instance isn't required to refer uniquely the
   * resource.
   *
   * @param id the unique identifier of a resource from which it can be explicitly and uniquely
   * referred in the whole Silverpeas.
   */
  public ResourceReference(String id) {
    this(id, UNKNOWN_ID);
  }

  /**
   * Constructs a new reference to a resource with the specified identifier and that is managed by
   * the specified component instance.
   *
   * @param id the identifier of the resource, unique in the given component instance.
   * @param componentInstanceId the unique identifier of the component instance.
   */
  public ResourceReference(String id, String componentInstanceId) {
    super(id, componentInstanceId);
  }

  /**
   * Constructs a new reference to a resource from the old and deprecated WAPrimaryKey object.
   *
   * @param pk a WAPrimaryKey object used to identify uniquely a resource in Silverpeas.
   */
  public ResourceReference(WAPrimaryKey pk) {
    this(pk.getId(), pk.getInstanceId());
  }

  /**
   * Gets a reference to the specified resource managed by a component instance.
   *
   * @param resource the unique identifier of a resource managed by a component.
   * @return a {@link ResourceReference} instance.
   */
  public static ResourceReference to(final ComponentResourceIdentifier resource) {
    return new ResourceReference(resource.getLocalId(), resource.getComponentInstanceId());
  }

  /**
   * Is this reference refers the same resource than the specified other reference? Two
   * references refer the same resource if the pair local identifier of the referred
   * resource and identifier of the application instance the resource belongs to are equal in the
   * references.
   *
   * @param other the object to compare to this reference.
   * @return true if other is equals to this reference. False otherwise.
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ResourceReference)) {
      return false;
    }
    final ResourceReference otherRef = (ResourceReference) other;
    return Objects.equal(getLocalId(), otherRef.getLocalId())
        && Objects.equal(getComponentInstanceId(), otherRef.getComponentInstanceId());
  }

  /**
   * Gets the hash code of this reference. It is computed on the local identifier of the referred
   * resource and on the identifier of the component instance the resource belongs to.
   *
   * @return the hash code of this reference.
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(getLocalId(), getComponentInstanceId());
  }

  @Override
  public String getLocalId() {
    return this.getId();
  }

  @Override
  public String getComponentInstanceId() {
    return this.getInstanceId();
  }

  @Override
  public String asString() {
    return MessageFormat.format(ABSOLUTE_ID_FORMAT, getComponentInstanceId(), getLocalId());
  }


}