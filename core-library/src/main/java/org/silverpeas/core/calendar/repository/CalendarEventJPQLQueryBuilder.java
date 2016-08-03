/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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
package org.silverpeas.core.calendar.repository;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.repository.CalendarEventCriteria.JOIN_DATA_APPLY;
import org.silverpeas.core.calendar.repository.CalendarEventCriteria.QUERY_ORDER_BY;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.repository.QueryCriteria;
import org.silverpeas.core.persistence.datasource.repository.SimpleQueryCriteria;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * A dynamic builder of a JPQL query.
 * @author Yohann Chastagnier
 */
public class CalendarEventJPQLQueryBuilder implements CalendarEventCriteriaProcessor {

  private StringBuilder orderBy = null;
  private boolean done = false;
  private final SimpleQueryCriteria jpqlCriteria;
  private String conjunction;

  public CalendarEventJPQLQueryBuilder(final NamedParameters parameters) {
    this.jpqlCriteria = new SimpleQueryCriteria(parameters);
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void endProcessing() {
    if (orderBy != null && orderBy.length() > 0) {
      jpqlCriteria.clause().add(orderBy.toString());
    }
    done = true;
  }

  @Override
  public QueryCriteria result() {
    return this.jpqlCriteria;
  }

  @Override
  public CalendarEventCriteriaProcessor then() {
    if (!done) {
      conjunction = "and";
    }
    return this;
  }

  @Override
  public CalendarEventCriteriaProcessor processCalendars(List<Calendar> calendars) {
    if (!done) {
      jpqlCriteria.clause().add(conjunction).add("calendar in :calendars").parameters()
          .add("calendars", calendars);
      conjunction = null;
    }
    return this;
  }

  @Override
  public CalendarEventCriteriaProcessor processPeriod(Period period) {
    if (!done) {
      jpqlCriteria.clause()
          .add(conjunction)
          .add("startDate <= :endDate and endDate >= :startDate")
          .parameters()
          .add("startDate", period.getStartDateTime()
              .toLocalDateTime()) //new Date(period.getStartDateTime().toInstant().toEpochMilli()))
          .add("endDate", period.getEndDateTime()
              .toLocalDateTime());//new Date(period.getEndDateTime().toInstant().toEpochMilli()));
      conjunction = null;
    }
    return this;
  }

  @Override
  public CalendarEventCriteriaProcessor processCreator(User creator) {
    if (!done) {
      jpqlCriteria.clause().add(conjunction).add("createdBy = :createdBy").parameters()
          .add("createdBy", creator.getId());
      conjunction = null;
    }
    return this;
  }

  @Override
  public CalendarEventCriteriaProcessor processJoinDataApply(
      final List<JOIN_DATA_APPLY> joinDataApplies) {
    return this;
  }

  @Override
  public CalendarEventCriteriaProcessor processOrdering(List<QUERY_ORDER_BY> orderings) {
    if (!done) {
      for (QUERY_ORDER_BY anOrdering : orderings) {
        if (!anOrdering.isApplicableOnJpaQuery()) {
          continue;
        }
        if (orderBy == null) {
          orderBy = new StringBuilder("order by ");
        } else {
          orderBy.append(", ");
        }
        orderBy.append(anOrdering.getPropertyName());
        orderBy.append(" ");
        orderBy.append(anOrdering.isAsc() ? "asc" : "desc");
      }
      conjunction = null;
    }
    return this;
  }

  @Override
  public CalendarEventCriteriaProcessor processIdentifiers(List<String> identifiers) {
    if (!done) {
      List<UuidIdentifier> uuids = new ArrayList<UuidIdentifier>(identifiers.size());
      for (String id : identifiers) {
        uuids.add(new UuidIdentifier().fromString(id));
      }
      jpqlCriteria.clause().add(conjunction).add("id in :ids").parameters().add("ids", uuids);
      conjunction = null;
    }
    return this;
  }
}
