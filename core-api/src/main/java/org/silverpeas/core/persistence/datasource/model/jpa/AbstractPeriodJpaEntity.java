/*
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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.persistence.datasource.model.jpa;

import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.datasource.model.Entity;
import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static java.time.OffsetDateTime.ofInstant;

/**
 * This JPA entity abstraction that handles begin and end dates, both persisted as long values.
 * These two dates are represented by a period object.
 * Persist dates as long values can improve significantly treatment loads, on a technical side.
 * <p/>
 * @param <ENTITY> specify the class name of the entity itself which is handled by a repository
 * manager.
 * @param <IDENTIFIER_TYPE> the identifier class name used by {@link ENTITY} for its primary key
 * definition.
 * @author Yohann Chastagnier
 */
@MappedSuperclass
public abstract class AbstractPeriodJpaEntity<ENTITY extends Entity<ENTITY,
    IDENTIFIER_TYPE>, IDENTIFIER_TYPE extends EntityIdentifier>
    extends AbstractJpaEntity<ENTITY, IDENTIFIER_TYPE> {

  @Column(name = "inDays", nullable = false)
  private boolean inDays = false;

  @Column(name = "startDate", nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  @NotNull
  private LocalDateTime startDate;

  @Column(name = "endDate", nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  @NotNull
  private LocalDateTime endDate;

  @Transient
  private Period period;

  public Period getPeriod() {
    if (startDate != null && endDate != null) {
      if (period == null) {
        OffsetDateTime start = startDate.atOffset(ZoneOffset.UTC);
        OffsetDateTime end = startDate.atOffset(ZoneOffset.UTC);
        if (inDays) {
          period = Period.between(start.toLocalDate(), end.toLocalDate());
        } else {
          period = Period.between(start, end);
        }
      }
    } else {
      period = null;
    }
    return period;
  }

  @SuppressWarnings("unchecked")
  public ENTITY setPeriod(final Period period) {
    this.period = period;
    inDays = period.isInDays();
    startDate = period.getStartDateTime().toLocalDateTime();
    endDate = period.getStartDateTime().toLocalDateTime();
    return (ENTITY) this;
  }
}
