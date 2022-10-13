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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.reminder;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.text.MessageFormat;
import java.util.UUID;

/**
 * Identifier of a reminder in the persistence context.
 * @author mmoquillon
 */
@Embeddable
public class ReminderIdentifier implements EntityIdentifier {
  private static final long serialVersionUID = -8752448963347095783L;

  private static final String ID_PREFIX = "Reminder#";
  private static final MessageFormat SCHEDULED_JOB_NAME = new MessageFormat(ID_PREFIX + "{0}");

  @Column(name = "id", columnDefinition = "varchar(41)", length = 41)
  private String id;

  @Override
  public ReminderIdentifier fromString(final String id) {
    if (!id.startsWith(ID_PREFIX) && id.length() != 41) {
      throw new IllegalArgumentException("The specified id " + id + " isn't of a reminder's one");
    }
    this.id = id;
    return this;
  }

  @Override
  public ReminderIdentifier generateNewId(final String... parameters) {
    this.id =
        SCHEDULED_JOB_NAME.format(new Object[]{UUID.randomUUID().toString().replaceAll("-", "")});
    return this;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.id).toHashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ReminderIdentifier other = (ReminderIdentifier) obj;
    return new EqualsBuilder().append(this.id, other.id).isEquals();
  }

  @Override
  public String asString() {
    return this.id;
  }
}
  