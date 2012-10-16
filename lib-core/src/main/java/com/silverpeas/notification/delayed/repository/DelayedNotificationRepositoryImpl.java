/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.notification.delayed.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.silverpeas.notification.delayed.constant.DelayedNotificationFrequency;
import com.silverpeas.notification.delayed.model.DelayedNotificationData;
import com.silverpeas.util.persistence.TypedParameter;
import com.silverpeas.util.persistence.TypedParameterUtil;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;

/**
 * @author Yohann Chastagnier
 */
public class DelayedNotificationRepositoryImpl implements DelayedNotificationRepositoryCustom {

  @Inject
  private EntityManagerFactory emf;

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.delayed.repository.DelayedNotificationRepositoryCustom#
   * findUsersToBeNotified(java.util.Set, java.util.Set, boolean)
   */
  @Override
  public List<Integer> findUsersToBeNotified(final Set<NotifChannel> aimedChannels,
      final Set<DelayedNotificationFrequency> aimedFrequencies,
      final boolean isThatUsersWithNoSettingHaveToBeNotified) {

    // Parameters
    final List<TypedParameter<?>> parameters = new ArrayList<TypedParameter<?>>();

    // Query
    final StringBuilder query = new StringBuilder();
    query.append("select distinct d.userId from DelayedNotificationData d ");
    query.append("  left outer join d.delayedNotificationUserSetting p ");
    query.append("where d.channel in (:");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "channels", NotifChannel.toIds(aimedChannels)));
    query.append(") and ( ");
    query.append("  (p.id is not null and p.frequency in (:");
    query.append(
        TypedParameterUtil.addNamedParameter(parameters, "frequencies",
            DelayedNotificationFrequency.toCodes(aimedFrequencies))).append(")) ");
    if (isThatUsersWithNoSettingHaveToBeNotified) {
      query.append("  or p.id is null ");
    }
    query.append(") ");

    // Typed query
    final TypedQuery<Integer> typedQuery = emf.createEntityManager().createQuery(query.toString(), Integer.class);

    // Parameters
    TypedParameterUtil.computeNamedParameters(typedQuery, parameters);

    // Result
    return typedQuery.getResultList();
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.delayed.repository.DelayedNotificationRepositoryCustom#findResource(com.silverpeas.
   * notification.model.NotificationResourceData)
   */
  @Override
  public List<DelayedNotificationData> findDelayedNotification(final DelayedNotificationData delayedNotification) {

    // Parameters
    final List<TypedParameter<?>> parameters = new ArrayList<TypedParameter<?>>();

    // Query
    final StringBuilder query = new StringBuilder("from DelayedNotificationData where");
    query.append(" userId = :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "userId", delayedNotification.getUserId()));
    query.append(" and fromUserId = :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "fromUserId", delayedNotification.getFromUserId()));
    query.append(" and channel = :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "channel", delayedNotification.getChannel().getId()));
    query.append(" and action = :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "action", delayedNotification.getAction().getId()));
    query.append(" and language = :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "language", delayedNotification.getLanguage()));
    Date date = delayedNotification.getCreationDate();
    if (date == null) {
      date = new Date();
    }
    query.append(" and creationDate between :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "creationDateMin",
        DateUtils.setSeconds(DateUtils.setMilliseconds(date, 0), 0), TemporalType.TIMESTAMP));
    query.append(" and :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "creationDateMax",
        DateUtils.setSeconds(DateUtils.setMilliseconds(date, 999), 59), TemporalType.TIMESTAMP));
    query.append(" and notificationResourceId = :");
    query.append(TypedParameterUtil.addNamedParameter(parameters, "resourceId", delayedNotification.getResource()));

    // resourceDescription parameter
    if (StringUtils.isNotBlank(delayedNotification.getMessage())) {
      query.append(" and message = :");
      query.append(TypedParameterUtil.addNamedParameter(parameters, "message", delayedNotification.getMessage()));
    } else {
      query.append(" and message is null");
    }

    // Typed query
    final TypedQuery<DelayedNotificationData> typedQuery =
        emf.createEntityManager().createQuery(query.toString(), DelayedNotificationData.class);

    // Parameters
    TypedParameterUtil.computeNamedParameters(typedQuery, parameters);

    // Result
    return typedQuery.getResultList();
  }
}
