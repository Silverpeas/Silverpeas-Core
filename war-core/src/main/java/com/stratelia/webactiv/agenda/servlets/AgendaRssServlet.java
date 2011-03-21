/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.agenda.servlets;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.peasUtil.RssServlet;
import com.silverpeas.personalization.UserPreferences;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.agenda.control.AgendaAccess;
import com.stratelia.webactiv.agenda.control.AgendaException;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.util.ResourceLocator;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

public class AgendaRssServlet extends RssServlet {

  private static final long serialVersionUID = -1303827067989958404L;

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#isComponentRss(java.lang.String)
   */
  public boolean isComponentRss(String userIdAgenda) {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#isComponentAvailable(java.lang.String,
   * java.lang.String)
   */
  public boolean isComponentAvailable(AdminController admin,
      String userIdAgenda, String currentUserId) {
    return true;
  }

  public String getChannelTitle(String userId) {
    UserPreferences preferences = SilverpeasServiceProvider.getPersonalizationService().getUserSettings(userId);
    OrganizationController orga = new OrganizationController();
    UserDetail user = orga.getUserDetail(userId);
    ResourceLocator message = new ResourceLocator("com.stratelia.webactiv.agenda.multilang.agenda",
        preferences.getLanguage());
    return message.getStringWithParam("agenda.userAgenda", user.getLastName());
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getListElements(java.lang.String, int)
   */
  public Collection<JournalHeader> getListElements(String userIdAgenda, int nbReturned)
      throws RemoteException {
    try {
      return AgendaAccess.getJournalHeadersForUserAfterDate(userIdAgenda, new Date(), nbReturned);
    } catch (AgendaException e) {
      throw new RemoteException("AgendaRssServlet.getListElements()", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementTitle(java.lang.Object, java.lang.String)
   */
  public String getElementTitle(Object element, String currentUserId) {
    JournalHeader event = (JournalHeader) element;
    String name = event.getName();
    if (event.getClassification().isPrivate()
        && !event.getDelegatorId().equals(currentUserId)) {
      ResourceLocator messageFrench = new ResourceLocator(
          "com.stratelia.webactiv.agenda.multilang.agenda", "fr");
      ResourceLocator messageEnglish = new ResourceLocator(
          "com.stratelia.webactiv.agenda.multilang.agenda", "en");
      name = messageFrench.getString("agenda.rssPrivateEvent") + " ("
          + messageEnglish.getString("agenda.rssPrivateEvent") + ")";
    }
    return name;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementLink(java.lang.Object, java.lang.String)
   */
  public String getElementLink(Object element, String currentUserId) {
    JournalHeader event = (JournalHeader) element;
    String eventUrl = URLManager.getApplicationURL()
        + "/Ragenda/jsp/journal.jsp?Action=Update&JournalId=" + event.getId(); // par
    // défaut,
    // lien
    // sur
    // l'événement
    // en
    // lui-meme
    // URL eventUrl = new
    // URL(getServerURL()+URLManager.getApplicationURL()+"/Journal/"+event.getId());
    if (event.getClassification().isPrivate()
        && !event.getDelegatorId().equals(currentUserId)) {
      // lien sur le calendrier à la date de l'événement
      eventUrl = URLManager.getApplicationURL() + "/Agenda/"
          + event.getDelegatorId();
    }

    return eventUrl;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementDescription(java.lang.Object,
   * java.lang.String)
   */
  public String getElementDescription(Object element, String currentUserId) {
    JournalHeader event = (JournalHeader) element;
    String description = event.getDescription();
    if (event.getClassification().isPrivate()
        && !event.getDelegatorId().equals(currentUserId)) {
      description = "";
    }
    return description;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementDate(java.lang.Object)
   */
  public Date getElementDate(Object element) {
    JournalHeader event = (JournalHeader) element;
    Calendar calElement = GregorianCalendar.getInstance();
    calElement.setTime(event.getStartDate());
    String hourMinute = event.getStartHour(); // hh:mm
    if (hourMinute != null && hourMinute.trim().length() > 0) {
      int hour = Integer.parseInt(hourMinute.substring(0, 2));
      int minute = Integer.parseInt(hourMinute.substring(3));
      calElement.set(Calendar.HOUR_OF_DAY, hour);
      calElement.set(Calendar.MINUTE, minute);
    } else {
      calElement.set(Calendar.HOUR_OF_DAY, 0);
      calElement.set(Calendar.MINUTE, 0);
    }
    return calElement.getTime();
  }

  public String getElementCreatorId(Object element) {
    JournalHeader event = (JournalHeader) element;
    return event.getDelegatorId();
  }
}