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
package org.silverpeas.core.notification.user.delayed.synthese;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency;

/**
 * @author Yohann Chastagnier
 */
public class DelayedNotificationSyntheseData {
  private final Collection<Long> delayedNotificationIdProceeded = new ArrayList<>();
  private Integer userId;
  private DelayedNotificationFrequency frequency;
  private String language;
  private int nbNotifications = 0;
  private final Collection<SyntheseResource> resources = new ArrayList<>();
  private String subject;
  private String message;

  public Collection<Long> getDelayedNotificationIdProceeded() {
    return delayedNotificationIdProceeded;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(final Integer userId) {
    this.userId = userId;
  }

  public DelayedNotificationFrequency getFrequency() {
    return frequency;
  }

  public void setFrequency(final DelayedNotificationFrequency frequency) {
    this.frequency = frequency;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(final String language) {
    this.language = language;
  }

  public int getNbResources() {
    return resources.size();
  }

  public int getNbNotifications() {
    return nbNotifications;
  }

  public void addNbNotifications(final int nb) {
    nbNotifications += nb;
  }

  public Collection<SyntheseResource> getResources() {
    return resources;
  }

  public void addResource(final SyntheseResource resource) {
    resources.add(resource);
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(final String subject) {
    this.subject = subject;
  }

  public String getMessage() {
    if (message == null) {
      message = "";
    }
    return message;
  }

  public boolean isHtmlMessage() {
    return StringUtils.isNotBlank(getMessage()) &&
        (getMessage().indexOf("<html") >= 0 || getMessage().indexOf("<body") >= 0);
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}
