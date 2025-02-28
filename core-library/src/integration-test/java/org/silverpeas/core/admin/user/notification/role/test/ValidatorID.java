/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.admin.user.notification.role.test;

import org.silverpeas.core.persistence.datasource.model.CompositeEntityIdentifier;

import javax.persistence.Embeddable;

/**
 * Composite identifier of a validator. A validator refers user playing the role of validator for a
 * given resource.
 *
 * @author mmoquillon
 */
@SuppressWarnings("unused")
@Embeddable
public class ValidatorID implements CompositeEntityIdentifier {

  private Long resourceId;

  private String validatorId;

  public ValidatorID() {
    // for JPA
  }

  ValidatorID(String validatorId, Long resource) {
    this.resourceId = resource;
    this.validatorId = validatorId;
  }

  Long getResourceId() {
    return resourceId;
  }

  String getValidatorId() {
    return validatorId;
  }

  @Override
  public CompositeEntityIdentifier fromString(String... values) {
    this.validatorId = values[0];
    this.resourceId = Long.parseLong(values[1]);
    return this;
  }

  @Override
  public String asString() {
    return validatorId + COMPOSITE_SEPARATOR + resourceId;
  }
}
  