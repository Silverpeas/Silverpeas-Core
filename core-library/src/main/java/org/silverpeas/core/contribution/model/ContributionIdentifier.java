/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.model;

import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.WAPrimaryKey;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.text.MessageFormat;

/**
 * A contribution identifier is an identifier that identifies uniquely a contribution in Silverpeas
 * whatever its type and the application instance or tools to which it belongs.
 * For doing, it is made up of two parts: one part that identify uniquely a contribution among all
 * of the contributions of the same type, and a last part that identify the component instance the
 * contribution belongs to.
 * The contribution identifier is serializable and can it can be transmitted in JSON or in XML.
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ContributionIdentifier implements ResourceIdentifier, Serializable {

  private static final String ABSOLUTE_ID_FORMAT = "{0}:{1}";

  @XmlElement(required = true)
  @NotNull
  private String instanceId;
  @XmlElement(required = true)
  @NotNull
  private String localId;

  /**
   * Constructs a new contribution identifier from the specified component instance identifier and
   * from the local identifier of the contribution.
   * @param instanceId the unique identifier of the instance.
   * @param localId the local identifier of the contribution.
   * @return an contribution identifier.
   */
  public static final ContributionIdentifier from(String instanceId, String localId) {
    return new ContributionIdentifier(instanceId, localId);
  }

  /**
   * Constructs a new contribution identifier from the specified component instance identifier and
   * from the local identifier of the contribution.
   * @param key an old and deprecated representation of an identifier of Silverpeas.
   * @return an contribution identifier.
   */
  public static final ContributionIdentifier from(WAPrimaryKey key) {
    return new ContributionIdentifier(key.getInstanceId(), key.getId());
  }

  protected ContributionIdentifier() {

  }

  /**
   * Constructs a new contribution identifier from the specified component instance identifier and
   * from the local identifier of the contribution.
   * @param instanceId the unique identifier of the instance.
   * @param localId the local identifier of the contribution.
   */
  public ContributionIdentifier(String instanceId, String localId) {
    this.localId = localId;
    this.instanceId = instanceId;
  }

  /**
   * Gets the absolute value of this identifier.
   * <p>Either it is an explicit absolute unique value or, in the case no absolute value is defined
   * for this contribution, it is made up of both the component instance identifier and the
   * contribution identifier. In latter, the component instance identifier defines the namespace
   * in which the local identifier exists.
   * @return the String representation of this identifier either as a value in itself or in the
   * form of [component instance identifier]:[local contribution identifier]
   */
  @Override
  public String asString() {
    return MessageFormat.format(ABSOLUTE_ID_FORMAT, getComponentInstanceId(), getLocalId());
  }

  /**
   * Gets the local identifier of the contribution. A local identifier is in fact unique only among
   * all the contributions of the same type; it is why it's said local to the same contribution
   * types.
   * @return the local contribution identifier.
   */
  public String getLocalId() {
    return localId;
  }

  /**
   * Gets the unique identifier of the component instance to which the contribution belongs. It
   * acts as a namespace of the unique identifier.
   * @return the component instance identifier (either an application instance or a tool instance
   * identifier).
   */
  public String getComponentInstanceId() {
    return instanceId;
  }
}
