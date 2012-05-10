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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.notification.repository;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.StringUtil;

/**
 * @author Yohann Chastagnier
 */
public class NotificationResourceRepositoryImpl implements NotificationResourceRepositoryCustom {

  @Inject
  private EntityManagerFactory emf;

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.repository.NotificationResourceRepository#findResource(com.silverpeas.
   * notification.model.NotificationResourceData)
   */
  @Override
  public List<NotificationResourceData> findResource(final NotificationResourceData notificationResourceData) {

    // Query
    final StringBuffer query = new StringBuffer("from NotificationResourceData where");
    query.append(" resourceId = :resourceId");
    query.append(" and resourceType = :resourceType");
    query.append(" and resourceName = :resourceName");
    query.append(" and resourceLocation = :resourceLocation");
    query.append(" and resourceUrl = :resourceUrl");

    // resourceDescription parameter
    if (isDefined(notificationResourceData.getResourceDescription())) {
      query.append(" and resourceDescription = :resourceDescription");
    } else {
      query.append(" and resourceDescription is null");
    }

    // Typed query
    final TypedQuery<NotificationResourceData> tq =
        emf.createEntityManager().createQuery(query.toString(), NotificationResourceData.class);

    tq.setParameter("resourceId", notificationResourceData.getResourceId());
    tq.setParameter("resourceType", notificationResourceData.getResourceType());
    tq.setParameter("resourceName", notificationResourceData.getResourceName());
    tq.setParameter("resourceLocation", notificationResourceData.getResourceLocation());
    tq.setParameter("resourceUrl", notificationResourceData.getResourceUrl());
    // resourceDescription parameter
    if (isDefined(notificationResourceData.getResourceDescription())) {
      tq.setParameter("resourceDescription", notificationResourceData.getResourceDescription());
    }

    // Result
    return tq.getResultList();
  }

  private static boolean isDefined(final String string) {
    return string != null && StringUtil.isDefined(string.trim());
  }
}
