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
package org.silverpeas.core.contribution.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.ComponentResourceIdentifier;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.contribution.ContributionLocator;
import org.silverpeas.core.util.Mutable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.util.StringUtil.isDefined;

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
@Embeddable
public class ContributionIdentifier implements ComponentResourceIdentifier, Serializable {
  private static final long serialVersionUID = 2071352191096872217L;

  public static final String MISSING_PART = "?";

  private static final String ABSOLUTE_ID_FORMAT = "{0}:{1}:{2}";
  private static final Pattern ABSOLUTE_ID_PATTERN =
      Pattern.compile("^(?<instanceId>[^:]+):(?<type>[^:]+):(?<localId>.+)$");

  @XmlElement(required = true)
  @NotNull
  @Column(name = "contrib_instanceId", nullable = false, length = 30)
  private String instanceId;
  @XmlElement(required = true)
  @NotNull
  @Column(name = "contrib_type", nullable = false, length = 40)
  private String type;
  @XmlElement(required = true)
  @NotNull
  @Column(name = "contrib_id", nullable = false, length = 40)
  private String localId;

  protected ContributionIdentifier() {
  }

  /**
   * Constructs a new contribution identifier from the specified component instance identifier and
   * from the local identifier of the contribution.
   * @param instanceId the unique identifier of the instance.
   * @param localId the local identifier of the contribution.
   * @param type the type of the contribution.
   */
  protected ContributionIdentifier(String instanceId, String localId, String type) {
    this.localId = localId;
    this.instanceId = instanceId;
    this.type = type;
  }

  /**
   * <p>
   * Decodes the contribution identifier from the given string.
   * </p>
   * <p>
   * If the decoded contribution identifier is partially filled (component instance identifier
   * is missing for example), then the {@link ContributionLocator} is used to attempt to locate
   * the contribution behind the contribution identifier. If it is located, the component
   * instance identifier found is taken into account.
   * </p>
   * @param contributionId a contribution identifier as string.
   * @return a {@link ContributionIdentifier} which represents the given id as string.
   */
  public static ContributionIdentifier decode(String contributionId) {
    final Matcher matcher = ABSOLUTE_ID_PATTERN.matcher(contributionId);
    if (matcher.matches()) {
      final String instanceId = matcher.group("instanceId");
      final String type = matcher.group("type");
      final String localId = matcher.group("localId");
      if (isDefined(instanceId) && isDefined(localId) && isDefined(type)) {
        final Mutable<ContributionIdentifier> contributionIdentifier =
            Mutable.of(ContributionIdentifier.from(instanceId, localId, type));
        if (MISSING_PART.equals(instanceId)) {
          ContributionLocator.get().locateByLocalIdAndType(localId, type)
              .ifPresent(contributionIdentifier::set);
        }
        return contributionIdentifier.get();
      }
    }
    throw new IllegalArgumentException(failureOnGetting("contribution id from", contributionId));
  }

  /**
   * Is the specified identifier a valid contribution identifier?
   * @param id the textual format of an identifier of a resource in Silverpeas.
   * @return true if the given parameter represents correctly the identifier of a contribution in
   * Silverpeas, false otherwise.
   */
  public static boolean isValid(String id) {
    final Matcher matcher = ABSOLUTE_ID_PATTERN.matcher(id);
    return matcher.matches();
  }

  /**
   * Constructs a new contribution identifier from the specified Silverpeas resource identifier.
   * If the resource identifier is actually a {@link ContributionIdentifier} instance, then returns
   * it as such, otherwise decode it from its {@link String} representation (though its
   * {@link ResourceIdentifier#asString()} method
   * @param resourceId a {@link ResourceIdentifier} object.
   * @return an contribution identifier.
   */
  public static ContributionIdentifier from(final ResourceIdentifier resourceId) {
    if (resourceId instanceof ContributionIdentifier) {
      return (ContributionIdentifier) resourceId;
    } else {
      return ContributionIdentifier.decode(resourceId.asString());
    }
  }

  /**
   * Constructs a new contribution identifier from the specified component instance identifier and
   * from the local identifier of the contribution. The type of the contribution is main one
   * handled by the component.
   * @param instanceId the unique identifier of the instance.
   * @param localId the local identifier of the contribution.
   * @param type the type of the contribution.
   * @return an contribution identifier.
   */
  public static ContributionIdentifier from(String instanceId, String localId,
      CoreContributionType type) {
    return from(instanceId, localId, type.name());
  }

  /**
   * Constructs a new contribution identifier from the specified component instance identifier and
   * from the local identifier of the contribution. The type of the contribution is main one
   * handled by the component.
   * @param key an old and deprecated representation of an identifier of Silverpeas.
   * @return an contribution identifier.
   */
  public static ContributionIdentifier from(WAPrimaryKey key) {
    return new ContributionIdentifier(key.getInstanceId(), key.getId(),
        CoreContributionType.UNKNOWN.name());
  }

  /**
   * Constructs a new contribution identifier from the specified component instance identifier and
   * from the local identifier of the contribution.
   * @param instanceId the unique identifier of the instance.
   * @param localId the local identifier of the contribution.
   * @param type the type of the contribution.
   * @return an contribution identifier.
   */
  public static ContributionIdentifier from(String instanceId, String localId, String type) {
    return new ContributionIdentifier(instanceId, localId, type);
  }

  /**
   * Constructs a new contribution identifier from the specified component instance identifier and
   * from the local identifier of the contribution.
   * @param key an old and deprecated representation of an identifier of Silverpeas.
   * @param type the type of the contribution.
   * @return an contribution identifier.
   */
  public static ContributionIdentifier from(WAPrimaryKey key, String type) {
    return new ContributionIdentifier(key.getInstanceId(), key.getId(), type);
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
    return MessageFormat
        .format(ABSOLUTE_ID_FORMAT, getComponentInstanceId(), getType(), getLocalId());
  }

  /**
   * Gets the local identifier of the contribution. A local identifier is in fact unique only among
   * all the contributions of the same type; it is why it's said local to the same contribution
   * types.
   * @return the local contribution identifier.
   */
  @Override
  public String getLocalId() {
    return localId;
  }

  /**
   * Gets the unique identifier of the component instance to which the contribution belongs. It
   * acts as a namespace of the unique identifier.
   * @return the component instance identifier (either an application instance or a tool instance
   * identifier).
   */
  @Override
  public String getComponentInstanceId() {
    return instanceId;
  }

  /**
   * Gets the type of the contribution.
   * @return the type of the contribution as string.
   */
  public String getType() {
    return type;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ContributionIdentifier that = (ContributionIdentifier) o;

    return new EqualsBuilder().append(instanceId, that.instanceId).append(type, that.type)
        .append(localId, that.localId).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(instanceId).append(type).append(localId).toHashCode();
  }
}
