/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.agenda.control;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.RemoveException;

import org.apache.commons.lang3.CharEncoding;

import com.silverpeas.ical.ExportIcalManager;
import com.silverpeas.ical.ImportIcalManager;
import com.silverpeas.ical.SynchroIcalManager;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.agenda.model.CalendarImportSettings;
import com.stratelia.webactiv.agenda.model.CalendarImportSettingsDao;
import com.stratelia.webactiv.agenda.model.CalendarImportSettingsDaoJdbc;
import com.stratelia.webactiv.agenda.view.AgendaHtmlView;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.calendar.control.CalendarBm;
import com.stratelia.webactiv.calendar.control.CalendarBmHome;
import com.stratelia.webactiv.calendar.control.CalendarException;
import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.calendar.model.Category;
import com.stratelia.webactiv.calendar.model.HolidayDetail;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.calendar.model.ParticipationStatus;
import com.stratelia.webactiv.calendar.model.Schedulable;
import com.stratelia.webactiv.calendar.model.SchedulableCount;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Class declaration
 * @author
 */
public class AgendaSessionController extends AbstractComponentSessionController {

  private int currentDisplayType = AgendaHtmlView.BYDAY;
  private boolean calendarVisible = true;
  private Calendar currentCalendar;
  private CalendarBm calendarBm;
  private Category category = null;
  private ResourceLocator settings;
  private JournalHeader currentJournalHeader = null;
  private Collection<Attendee> currentAttendees = null;
  private Collection<Category> currentCategories = null;
  private ParticipationStatus participationStatus = null;
  private NotificationSender notifSender = null;
  private List<Date> nonSelectableDays = null;
  private List<String> holidaysDates = null;
  private String serverURL = null;
  private String agendaUserId = getUserId();
  private UserDetail agendaUserDetail = getUserDetail();
  private final CalendarImportSettingsDao importSettingsDao = new CalendarImportSettingsDaoJdbc();
  public final static String ICALENDAR_MIME_TYPE = "text/calendar";
  public final static String EXPORT_SUCCEEDED = "0";
  public final static String EXPORT_FAILED = "1";
  public final static String EXPORT_EMPTY = "2";
  public final static String IMPORT_SUCCEEDED = "0";
  public final static String IMPORT_FAILED = "1";
  public final static String SYNCHRO_SUCCEEDED = "0";
  public final static String SYNCHRO_FAILED = "1";
  public final static String AGENDA_FILENAME_PREFIX = "agenda";
  public final static int WORKING_DAY = 0;
  public final static int HOLIDAY_DAY = 1;

  /**
   * Constructor declaration
   * @see
   */
  public AgendaSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context,
        "com.stratelia.webactiv.agenda.multilang.agenda");
    setComponentRootName(URLManager.CMP_AGENDA);
    initEJB();
    AdminController admin = new AdminController("useless");
    Domain defaultDomain = admin.getDomain(getUserDetail().getDomainId());
    serverURL = defaultDomain.getSilverpeasServerURL();
  }

  private boolean isUseRss() {
    return "yes".equals(getSettings().getString("calendarRss"));
  }

  @Override
  public String getRSSUrl() {
    if (isUseRss()) {
      try {
        return "/rssAgenda/" + getAgendaUserId() + "?userId=" + getUserId() + "&amp;login="
            + URLEncoder.encode(getUserDetail().getLogin(), CharEncoding.UTF_8) + "&amp;password="
            + URLEncoder.encode(getOrganizationController().getUserFull(getUserId()).getPassword(),
            CharEncoding.UTF_8);
      } catch (UnsupportedEncodingException e) {
        SilverTrace.error("agenda", "AgendaSessionController.getRSSUrl()",
            "agenda.MSG_CANT_DEFINE_URL", e);
        return null;
      }
    }
    return null;
  }

  /**
   * Method declaration
   * @see
   */
  private void initEJB() {
    // 1 - Remove all data stored by this SessionController (includes ref to
    // EJB)
    calendarBm = null;

    // 2 - Init EJB used by this SessionController
    setCalendarBm();
  }

  /**
   * Method declaration
   * @see
   */
  private void setCalendarBm() {
    if (calendarBm == null) {
      try {
        calendarBm = EJBUtilitaire.getEJBObjectRef(
            JNDINames.CALENDARBM_EJBHOME, CalendarBmHome.class).create();
      } catch (Exception e) {
        throw new AgendaRuntimeException(
            "AgendaSessionController.setCalendarBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
  }

  protected String getComponentInstName() {
    return URLManager.CMP_AGENDA;
  }

  /**
   * methods for Journal
   */
  public String addJournal(String name, String description, String priority,
      String classification, Date startDay, String startHour, Date endDay,
      String endHour) throws RemoteException {
    JournalHeader journal = new JournalHeader(name, getUserId());

    journal.setDescription(description);
    try {
      journal.getPriority().setValue(Integer.parseInt(priority));
    } catch (Exception e) {
      SilverTrace.warn("agenda", "AgendaSessionController.addJournal(String name, "
          + "String description, String priority, String classification, Date startDay, String "
          + "startHour, Date endDay, String endHour)", "agenda.MSG_CANT_GET_PRIORITY", "", e);

    }
    try {
      journal.getClassification().setString(classification);
      journal.setStartDay(DateUtil.date2SQLDate(startDay));
      journal.setStartHour(startHour);
      journal.setEndDay(DateUtil.date2SQLDate(endDay));
      journal.setEndHour(endHour);
    } catch (java.text.ParseException pe) {
      throw new AgendaRuntimeException("AgendaSessionController.addJournal(String name, String "
          + "description, String priority, String classification, Date startDay, String startHour, "
          + "Date endDay, String endHour)", SilverpeasException.ERROR, "agenda.EX_CANT_ADD_JOURNAL",
          pe);
    }
    String result = calendarBm.addJournal(journal);
    return result;
  }

  /**
   * Method declaration
   * @param id
   * @param title
   * @param text
   * @see
   */
  protected void notifyAttendees(String id, String title, String text) {
    notifyAttendees(id, title, text, null);
  }

  protected void notifyAttendees(String id, String title, String text,
      String url) {
    try {
      Collection<Attendee> attendees = getJournalAttendees(id);
      NotificationMetaData notifMetaData = new NotificationMetaData(
          NotificationParameters.NORMAL, title, text);
      notifMetaData.setSender(getUserId());
      notifMetaData.setSource(getString("agenda"));

      for (Attendee attendee : attendees) {
        notifMetaData.addUserRecipient(new UserRecipient(attendee.getUserId()));
      }

      if (url != null) {
        notifMetaData.setLink(url);
      }

      getNotificationSender().notifyUser(notifMetaData);
    } catch (Exception e) {
      SilverTrace.warn("agenda", "AgendaSessionController.notifyAttendees()",
          "agenda.MSG_CANT_SEND_MAILS", "for title=" + title, e);
    }
  }

  /**
   * Method declaration
   * @param id
   * @param name
   * @param description
   * @param priority
   * @param classification
   * @param startDay
   * @param startHour
   * @param endDay
   * @param endHour
   * @throws CalendarException
   * @throws RemoteException
   * @see
   */
  public void updateJournal(String id, String name, String description,
      String priority, String classification, Date startDay, String startHour,
      Date endDay, String endHour) throws RemoteException, CalendarException {
    boolean changed = false;
    JournalHeader journal = getJournalHeader(id);

    journal.setName(name);
    journal.setDescription(description);
    try {
      journal.getPriority().setValue(Integer.parseInt(priority));
    } catch (Exception e) {
      SilverTrace.warn(
          "agenda",
          "AgendaSessionController. updateJournal(String name, String description, String priority, String classification, Date startDay, String startHour, Date endDay, String endHour)",
          "agenda.MSG_CANT_GET_PRIORITY", "", e);
    }

    try {
      changed = eventHasChanged(journal, startDay, startHour, endDay, endHour);

      journal.getClassification().setString(classification);
      journal.setStartDay(DateUtil.date2SQLDate(startDay));
      journal.setStartHour(startHour);
      journal.setEndDay(DateUtil.date2SQLDate(endDay));
      journal.setEndHour(endHour);
    } catch (java.text.ParseException pe) {
      throw new AgendaRuntimeException("AgendaSessionController.updateJournal(String name, "
          + "String description, String priority, String classification, Date startDay, "
          + "String startHour, Date endDay, String endHour)",
          SilverpeasException.ERROR, "agenda.EX_CANT_UPDATE_JOURNAL", pe);
    }

    calendarBm.updateJournal(journal);
    if (changed) {
      UserDetail you = getUserDetail();
      String url = URLManager.getURL(URLManager.CMP_AGENDA)
          + "journal.jsp?JournalId=" + id + "&Action=Update";
      notifyAttendees(id, getString("titleUpdate"), getString("subject")
          + "\n '" + journal.getName() + "' " + getString("with") + " "
          + you.getDisplayedName() + " \n" + getString("delayed") + " "
          + DateUtil.getOutputDate(journal.getStartDate(), getLanguage()) + " "
          + getString("HeureRDV") + " " + journal.getStartHour() + "\n", url);
    }
  }

  private boolean eventHasChanged(JournalHeader journal, Date startDay,
      String startHour, Date endDay, String endHour) {
    boolean hasChanged = true;

    if (DateUtil.datesAreEqual(journal.getStartDate(), startDay)
        && DateUtil.datesAreEqual(journal.getEndDate(), endDay)) {
      if (journal.getStartHour() != null
          && journal.getStartHour().equals(startHour)) {
        if (journal.getEndHour() != null
            && journal.getEndHour().equals(endHour)) {
          hasChanged = false;
        }
      }
    }

    SilverTrace.debug("agenda", "AgendaSessionController.eventHasChanged",
        "root.MSG_GEN_EXIT_METHOD", "dates have changed = " + hasChanged);

    return hasChanged;
  }

  /**
   * Method declaration
   * @param id
   * @throws CalendarException
   * @throws RemoteException
   * @see
   */
  public void removeJournal(String id) throws RemoteException,
      CalendarException {
    JournalHeader journal = getJournalHeader(id);
    UserDetail you = getUserDetail(journal.getDelegatorId());

    notifyAttendees(id, getString("titleDelete"), getString("subject") + " '"
        + journal.getName() + "'\n" + getString("with") + " "
        + you.getDisplayedName() + "\n" + getString("planned") + " "
        + DateUtil.getOutputDate(journal.getStartDate(), getLanguage()) + " "
        + getString("HeureRDV") + " " + journal.getStartHour() + " "
        + getString("removed") + "\n");

    calendarBm.removeJournal(id);
  }

  /**
   * Method declaration
   * @param journalId
   * @return
   * @throws RemoteException
   * @see
   */
  public JournalHeader getJournalHeader(String journalId)
      throws RemoteException {
    return calendarBm.getJournalHeader(journalId);
  }

  /**
   * methods for attendees
   */
  public Collection<Attendee> getJournalAttendees(String journalId)
      throws AgendaException {
    try {
      return calendarBm.getJournalAttendees(journalId);
    } catch (Exception e) {
      throw new AgendaException(
          "AgendaSessionController.getJournalAttendees(String journalId)",
          SilverpeasException.ERROR, "agenda.EX_CANT_GET_ATTENDEES",
          "journalId=" + journalId, e);
    }

  }

  /**
   * Method declaration
   * @param journalId
   * @param userIds
   * @throws AgendaException
   * @see
   */
  public void setJournalAttendees(String journalId, String[] userIds)
      throws AgendaException {
    try {
      calendarBm.setJournalAttendees(journalId, userIds);

      JournalHeader journal = getJournalHeader(journalId);
      UserDetail you = getUserDetail();
      String url = URLManager.getURL(URLManager.CMP_AGENDA)
          + "journal.jsp?JournalId=" + journalId + "&Action=Update";
      notifyAttendees(journalId, getString("NewRDV"), you.getDisplayedName()
          + " " + getString("NewRDVcalled") + " '" + journal.getName() + "' \n"
          + " " + getString("JourRDV") + " "
          + DateUtil.getOutputDate(journal.getStartDate(), getLanguage()) + " "
          + getString("HeureRDV") + " " + journal.getStartHour() + "\n"
          + getString("ThanksToConfirm"), url);
    } catch (Exception e) {
      throw new AgendaException(
          "AgendaSessionController.setJournalAttendees(String journalId, String[] userIds)",
          SilverpeasException.ERROR, "agenda.EX_CANT_SET_ATTENDEES",
          "journalId=" + journalId, e);
    }
  }

  /**
   * Method declaration
   * @param journalId
   * @param userId
   * @param status
   * @throws AgendaException
   * @see
   */
  public void setJournalParticipationStatus(String journalId, String userId,
      String status) throws AgendaException {
    try {
      calendarBm.setJournalParticipationStatus(journalId, userId, status);
    } catch (Exception e) {
      throw new AgendaException(
          "AgendaSessionController.setJournalParticipationStatus(String journalId, String userId, String status)",
          SilverpeasException.ERROR, "agenda.EX_CANT_SET_STATUS", "journalId="
              + journalId + " ,userId=" + getUserId() + ",status=" + status, e);
    }
  }

  /**
   * methods for categories
   */
  public Collection<Category> getAllCategories() throws AgendaException {
    try {
      return calendarBm.getAllCategories();
    } catch (Exception e) {
      throw new AgendaException("AgendaSessionController. getAllCategories()",
          SilverpeasException.ERROR, "agenda.EX_CANT_GET_CATEGORIES", e);
    }
  }

  /**
   * Method declaration
   * @param categoryId
   * @return
   * @throws AgendaException
   * @see
   */
  public Category getCategory(String categoryId) throws AgendaException {
    try {
      return calendarBm.getCategory(categoryId);
    } catch (Exception e) {
      throw new AgendaException(
          "AgendaSessionController. getCategory(String categoryId)",
          SilverpeasException.ERROR, "agenda.EX_CANT_GET_CATEGORY",
          "categoryId=" + categoryId, e);
    }
  }

  /**
   * Method declaration
   * @param journalId
   * @return
   * @throws AgendaException
   * @see
   */
  public Collection<Category> getJournalCategories(String journalId)
      throws AgendaException {
    try {
      return calendarBm.getJournalCategories(journalId);
    } catch (Exception e) {
      throw new AgendaException(
          "AgendaSessionController. getJournalCategories(String journalId)",
          SilverpeasException.ERROR, "agenda.EX_CANT_GET_JOURNAL_CATEGORY",
          "journalId=" + journalId, e);
    }
  }

  /**
   * Method declaration
   * @param journalId
   * @param categoryIds
   * @throws AgendaException
   * @see
   */
  public void setJournalCategories(String journalId, String[] categoryIds)
      throws AgendaException {
    try {
      calendarBm.setJournalCategories(journalId, categoryIds);
    } catch (Exception e) {
      throw new AgendaException(
          "setJournalCategories(String journalId, String[] categoryIds)",
          SilverpeasException.ERROR, "agenda.EX_CANT_SET_JOURNAL_CATEGORY",
          "journalId=" + journalId, e);
    }

  }

  /**
   * Method declaration
   * @throws RemoteException
   * @see
   */
  public void indexAll() throws RemoteException {
    calendarBm.indexAllJournal();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public UserDetail[] getUserList() {
    return getOrganizationController().getAllUsers();
  }

  /**
   * Method declaration
   * @param userId
   * @return
   * @see
   */
  public UserDetail getUserDetail(String userId) {
    return getOrganizationController().getUserDetail(userId);
  }

  /**
   * methods to manage current user state
   */
  public Category getCategory() {
    return category;
  }

  /**
   * Method declaration
   * @param category
   * @see
   */
  public void setCategory(Category category) {
    this.category = category;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public JournalHeader getCurrentJournalHeader() {
    return currentJournalHeader;
  }

  /**
   * Method declaration
   * @param journalHeader
   * @see
   */
  public void setCurrentJournalHeader(JournalHeader journalHeader) {
    currentJournalHeader = journalHeader;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Collection<Attendee> getCurrentAttendees() {
    return currentAttendees;
  }

  /**
   * Method declaration
   * @param attendees
   * @see
   */
  public void setCurrentAttendees(Collection<Attendee> attendees) {
    currentAttendees = attendees;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Collection<Category> getCurrentCategories() {
    return currentCategories;
  }

  /**
   * Method declaration
   * @param categories
   * @see
   */
  public void setCurrentCategories(Collection<Category> categories) {
    currentCategories = categories;
  }

  /**
   * Method declaration
   * @param visible
   * @see
   */
  public void setCalendarVisible(boolean visible) {
    this.calendarVisible = visible;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public ParticipationStatus getParticipationStatus() {
    if (participationStatus == null) {
      participationStatus = new ParticipationStatus(
          ParticipationStatus.ACCEPTED);
    }
    return participationStatus;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public NotificationSender getNotificationSender() {
    if (notifSender == null) {
      notifSender = new NotificationSender(null);
    }
    return notifSender;
  }

  /**
   * @return
   */
  public String getServerURL() {
    return serverURL;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public ResourceLocator getSettings() {
    if (settings == null) {
      settings = new ResourceLocator(
          "com.stratelia.webactiv.agenda.settings.agendaSettings", "");
    }
    return settings;
  }

  /**
   * Method declaration
   * @see
   */
  public void viewByDay() {
    currentDisplayType = AgendaHtmlView.BYDAY;
  }

  /**
   * Method declaration
   * @see
   */
  public void viewByWeek() {
    currentDisplayType = AgendaHtmlView.BYWEEK;
  }

  /**
   * Method declaration
   * @see
   */
  public void viewByMonth() {
    currentDisplayType = AgendaHtmlView.BYMONTH;
  }

  /**
   * Method declaration
   * @see
   */
  public void viewByYear() {
    currentDisplayType = AgendaHtmlView.BYYEAR;
  }

  /**
   * Method declaration
   * @see
   */
  public void viewChooseDays() {
    currentDisplayType = AgendaHtmlView.CHOOSE_DAYS;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getCurrentDisplayType() {
    return currentDisplayType;
  }

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public AgendaHtmlView getCurrentHtmlView() throws RemoteException {
    AgendaHtmlView agendaView = null;
    Collection schedules = null;

    if (currentDisplayType == AgendaHtmlView.BYDAY) {
      agendaView = new AgendaHtmlView(currentDisplayType, getCurrentDay(),
          this, getSettings());
      agendaView.setCalendarVisible(calendarVisible);
      schedules = getDaySchedulables();
    } else if (currentDisplayType == AgendaHtmlView.BYWEEK) {
      agendaView = new AgendaHtmlView(currentDisplayType,
          getWeekFirstDay(getCurrentDay()), this, getSettings());
      schedules = getWeekSchedulables();
    } else if (currentDisplayType == AgendaHtmlView.BYMONTH) {
      agendaView = new AgendaHtmlView(currentDisplayType,
          getMonthFirstDay(getCurrentDay()), this, getSettings());
      schedules = countMonthSchedulables();
    } else if (currentDisplayType == AgendaHtmlView.BYYEAR) {
      agendaView = new AgendaHtmlView(currentDisplayType,
          getYearFirstDay(getCurrentDay()), this, getSettings());
    } else {
      return null;
    }

    if (schedules != null) {

      for (Object obj : schedules) {
        if (obj instanceof Schedulable) {
          agendaView.add((Schedulable) obj);
        }
        if (obj instanceof SchedulableCount) {
          agendaView.add((SchedulableCount) obj);
        }
      }
    }
    return agendaView;
  }

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<JournalHeader> getDaySchedulables() throws RemoteException {
    String categoryId = null;
    if (getCategory() != null) {
      categoryId = getCategory().getId();
    }

    return calendarBm.
        getDaySchedulablesForUser(DateUtil.date2SQLDate(getCurrentDay()), agendaUserId, categoryId,
        getParticipationStatus().getString());
  }

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<JournalHeader> getMonthSchedulables(Date date) throws RemoteException {
    Date begin = getMonthFirstDay(date);
    Date end = getMonthLastDay(date);

    String categoryId = null;
    if (getCategory() != null) {
      categoryId = getCategory().getId();
    }

    return calendarBm.getPeriodSchedulablesForUser(
        DateUtil.date2SQLDate(begin), DateUtil.date2SQLDate(end), agendaUserId,
        categoryId, getParticipationStatus().getString());
  }

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<JournalHeader> getWeekSchedulables() throws RemoteException {
    Date begin = getWeekFirstDay(getCurrentDay());
    Date end = getWeekLastDay(getCurrentDay());

    String categoryId = null;
    if (getCategory() != null) {
      categoryId = getCategory().getId();
    }

    return calendarBm.getPeriodSchedulablesForUser(
        DateUtil.date2SQLDate(begin), DateUtil.date2SQLDate(end), agendaUserId,
        categoryId, getParticipationStatus().getString());
  }

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<SchedulableCount> countMonthSchedulables() throws RemoteException {
    String month = (DateUtil.date2SQLDate(getCurrentDay())).substring(0, 8);
    String categoryId = null;
    if (getCategory() != null) {
      categoryId = getCategory().getId();
    }

    return calendarBm.countMonthSchedulablesForUser(month, agendaUserId,
        categoryId, getParticipationStatus().getString());
  }

  /**
   * Method declaration
   * @param userId
   * @param day
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getBusyTime(String userId, java.util.Date day)
      throws RemoteException {
    Collection<JournalHeader> result = calendarBm.getDaySchedulablesForUser(DateUtil.date2SQLDate(
        day), userId, null, ParticipationStatus.ACCEPTED);

    if (!userId.equals(getUserId())) {
      Collection subResult = new ArrayList();

      for (JournalHeader aResult : result) {
        Schedulable schedule = aResult;
        boolean toView = false;

        if (!schedule.getClassification().isConfidential()) {
          toView = true;
        } else if (schedule.getDelegatorId().equals(getUserId())) {
          toView = true;
        } else {
          Collection<Attendee> attendees = calendarBm.getJournalAttendees(schedule.getId());

          for (Attendee attendee : attendees) {
            if (attendee.getUserId().equals(getUserId())) {
              toView = true;
            }
          }
        }
        if (toView) {
          subResult.add(schedule);
        }
      }
      result = subResult;
    }
    return result;
  }

  /**
   * Return if day has events for the user or not
   * @param userId
   * @param day
   * @return isDayHasEvents
   * @throws RemoteException
   */
  public boolean isDayHasEvents(String userId, Date day) throws RemoteException {
    boolean isDayHasEvents = false;
    Collection<JournalHeader> result = calendarBm.getDaySchedulablesForUser(DateUtil.date2SQLDate(
        day), userId, null, ParticipationStatus.ACCEPTED);
    if (result != null) {
      if (result.size() > 0) {
        isDayHasEvents = true;
      }
    }
    return isDayHasEvents;
  }

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public boolean hasTentativeSchedulables() throws RemoteException {
    return calendarBm.hasTentativeSchedulablesForUser(getUserId());
  }

  /**
   * Method declaration
   * @return
   * @throws AgendaException
   * @see
   */
  public Collection<JournalHeader> getTentativeSchedulables() throws AgendaException {
    try {
      return calendarBm.getTentativeSchedulablesForUser(getUserId());
    } catch (Exception e) {
      throw new AgendaException(
          "AgendaSessionController.getTentativeSchedulables()",
          SilverpeasException.ERROR,
          "agenda.EX_CANT_GET_TENTATIVE_SCHEDULABLES", "userId=" + getUserId(),
          e);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Date getCurrentDay() {
    if (currentCalendar == null) {
      currentCalendar = Calendar.getInstance();
    }
    return currentCalendar.getTime();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getStartDayInWeek() {
    if (currentCalendar == null) {
      currentCalendar = Calendar.getInstance();
    }
    if (currentDisplayType == AgendaHtmlView.BYDAY) {
      return currentCalendar.get(Calendar.DAY_OF_WEEK);
    }
    return Integer.parseInt(getString("weekFirstDay"));
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getStartDayInMonth() {
    if (currentCalendar == null) {
      currentCalendar = Calendar.getInstance();
    }
    if (currentDisplayType == AgendaHtmlView.BYDAY) {
      return currentCalendar.get(Calendar.DAY_OF_MONTH);
    } else {
      Calendar cal = Calendar.getInstance();

      cal.setTime(getWeekFirstDay(currentCalendar.getTime()));
      return cal.get(Calendar.DAY_OF_MONTH);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getEndDayInMonth() {
    if (currentCalendar == null) {
      currentCalendar = Calendar.getInstance();
    }
    if (currentDisplayType == AgendaHtmlView.BYDAY) {
      return currentCalendar.get(Calendar.DAY_OF_MONTH);
    } else {
      Calendar cal = Calendar.getInstance();

      cal.setTime(getWeekLastDay(currentCalendar.getTime()));
      return cal.get(Calendar.DAY_OF_MONTH);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getStartMonth() {
    if (currentCalendar == null) {
      currentCalendar = Calendar.getInstance();
    }

    if (currentDisplayType == AgendaHtmlView.BYWEEK) {
      Calendar cal = Calendar.getInstance();

      cal.setTime(getWeekFirstDay(currentCalendar.getTime()));
      return cal.get(Calendar.MONTH);
    } else {
      return currentCalendar.get(Calendar.MONTH);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getEndMonth() {
    if (currentCalendar == null) {
      currentCalendar = Calendar.getInstance();
    }

    if (currentDisplayType == AgendaHtmlView.BYWEEK) {
      Calendar cal = Calendar.getInstance();

      cal.setTime(getWeekLastDay(currentCalendar.getTime()));
      return cal.get(Calendar.MONTH);
    } else {
      return currentCalendar.get(Calendar.MONTH);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getStartYear() {
    if (currentCalendar == null) {
      currentCalendar = Calendar.getInstance();
    }
    if (currentDisplayType == AgendaHtmlView.BYWEEK) {
      Calendar result = Calendar.getInstance();

      result.setTime(getWeekFirstDay(currentCalendar.getTime()));
      return result.get(Calendar.YEAR);
    } else {
      return currentCalendar.get(Calendar.YEAR);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getEndYear() {
    if (currentCalendar == null) {
      currentCalendar = Calendar.getInstance();
    }
    Calendar cal = Calendar.getInstance();

    cal.setTime(getWeekLastDay(currentCalendar.getTime()));
    return cal.get(Calendar.YEAR);
  }

  /**
   * Method declaration
   * @param date
   * @see
   */
  public void setCurrentDay(Date date) {
    if (currentCalendar == null) {
      currentCalendar = Calendar.getInstance();
    }
    currentCalendar.setTime(date);
  }

  /**
   * Method declaration
   * @param day
   * @see
   */
  public void selectDay(String day) {
    if (currentCalendar == null) {
      currentCalendar = Calendar.getInstance();
    }
    try {
      Date date = DateUtil.stringToDate(day, getLanguage());
      currentCalendar.setTime(date);
      viewByDay();
    } catch (Exception e) {
      SilverTrace.warn("agenda",
          "AgendaSessionController.selectDay(String day)",
          "agenda.MSG_CANT_CHANGE_DAY", "", e);
    }
  }

  /**
   * Method declaration
   * @see
   */
  public void next() throws RemoteException {
    if (currentDisplayType == AgendaHtmlView.BYDAY) {
      nextDay();
    } else if (currentDisplayType == AgendaHtmlView.BYWEEK) {
      nextWeek();
    } else if (currentDisplayType == AgendaHtmlView.BYMONTH) {
      nextMonth();
    } else if (currentDisplayType == AgendaHtmlView.BYYEAR) {
      nextYear();
    }
  }

  /**
   * Method declaration
   * @see
   */
  public void nextDay() throws RemoteException {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.DATE, 1);
    while (isHolidayDate(currentCalendar.getTime())) {
      currentCalendar.add(Calendar.DATE, 1);
    }
  }

  /**
   * Method declaration
   * @see
   */
  public void nextWeek() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.DATE, 7);
  }

  /**
   * Method declaration
   * @see
   */
  public void nextMonth() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.MONTH, 1);
  }

  /**
   * Method declaration
   * @see
   */
  public void nextYear() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.YEAR, 1);
  }

  /**
   * Method declaration
   * @see
   */
  public void previous() {
    if (currentDisplayType == AgendaHtmlView.BYDAY) {
      previousDay();
    } else if (currentDisplayType == AgendaHtmlView.BYWEEK) {
      previousWeek();
    } else if (currentDisplayType == AgendaHtmlView.BYMONTH) {
      previousMonth();
    } else if (currentDisplayType == AgendaHtmlView.BYYEAR) {
      previousYear();
    }
  }

  /**
   * Method declaration
   * @see
   */
  public void previousDay() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.DATE, -1);
    while (isHolidayDate(currentCalendar.getTime())) {
      currentCalendar.add(Calendar.DATE, -1);
    }
  }

  /**
   * Method declaration
   * @see
   */
  public void previousWeek() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.DATE, -7);
  }

  /**
   * Method declaration
   * @see
   */
  public void previousMonth() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.MONTH, -1);
  }

  /**
   * Method declaration
   * @see
   */
  public void previousYear() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.YEAR, -1);
  }

  /**
   * Method declaration
   * @param date
   * @return
   * @see
   */
  public Date getWeekFirstDay(Date date) {
    int firstDayOfWeek = Integer.parseInt(getString("weekFirstDay"));
    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);
    while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
      calendar.add(Calendar.DATE, -1);
    }
    return calendar.getTime();
  }

  /**
   * Method declaration
   * @param date
   * @return
   * @see
   */
  public Date getWeekLastDay(Date date) {
    int lastDayOfWeek = Integer.parseInt(getString("weekLastDay"));
    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);
    while (calendar.get(Calendar.DAY_OF_WEEK) != lastDayOfWeek) {
      calendar.add(Calendar.DATE, 1);
    }
    return calendar.getTime();
  }

  /**
   * Method declaration
   * @param date
   * @return
   * @see
   */
  static public Date getMonthFirstDay(Date date) {
    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return calendar.getTime();
  }

  /**
   * Get the last day of the month
   * @param date
   * @return date
   * @see
   */
  public Date getMonthLastDay(Date date) {
    Calendar cal = Calendar.getInstance();

    cal.setTime(date);
    int monthLastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    cal.set(Calendar.DAY_OF_MONTH, monthLastDay);
    return cal.getTime();
  }

  /**
   * Method declaration
   * @param date
   * @return
   * @see
   */
  static public Date getYearFirstDay(Date date) {
    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.MONTH, 1);
    return calendar.getTime();
  }

  /**
   * Paramètre le userPannel => tous les users, sélection des users participants.
   * @return 
   */
  public String initSelectionPeas() {
    String m_context = URLManager.getApplicationURL();
    PairObject hostComponentName = new PairObject(getString("agenda"),
        m_context + "/Ragenda/jsp/Main");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("editionListeDiffusion"), m_context
        + "/Ragenda/jsp/Main");
    String hostUrl = m_context + "/Ragenda/jsp/saveMembers";
    String cancelUrl = m_context + "/Ragenda/jsp/saveMembers";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName("");
    sel.setHostPath(hostPath);
    sel.setHostComponentName(hostComponentName);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    // set les users deja selectionnés
    Collection<Attendee> members = getCurrentAttendees();
    if (members != null) {
      String[] usersSelected = new String[members.size()];
      int j = 0;
      for (Attendee attendee : members) {
        usersSelected[j] = attendee.getUserId();
        j++;
      }
      sel.setSelectedElements(usersSelected);
    }

    // Contraintes

    sel.setPopupMode(true);
    sel.setElementSelectable(true);
    sel.setSetSelectable(false);
    sel.setFirstPage(Selection.FIRST_PAGE_DEFAULT);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /**
   * Retourne une Collection de UserDetail des utilisateurs selectionnés via le userPanel
   * @param
   * @return
   * @throws
   * @see
   */
  public Collection<Attendee> getUserSelected() throws AgendaException {
    Selection sel = getSelection();
    List<Attendee> attendees = new ArrayList<Attendee>();
    Collection<Attendee> oldAttendees = null;

    JournalHeader journal = getCurrentJournalHeader();
    if (journal.getId() != null) {
      oldAttendees = getJournalAttendees(journal.getId());
    }

    String[] selectedUsers = sel.getSelectedElements();
    if (selectedUsers != null) {
      for (String selectedUser : selectedUsers) {
        Attendee newAttendee = null;
        if (oldAttendees != null) {
          for (Attendee attendee : oldAttendees) {
            if (attendee.getUserId().equals(selectedUser)) {
              newAttendee = attendee;
            }
          }
        }

        if (newAttendee == null) {
          newAttendee = new Attendee(selectedUser);
        }
        attendees.add(newAttendee);
      }
    }

    return attendees;
  }

  public void close() {
    try {
      if (calendarBm != null) {
        calendarBm.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("agendaSession", "AgendaSessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("agendaSession", "AgendaSessionController.close", "", e);
    }
  }

  /**
   * @return
   */
  public List<Date> getNonSelectableDays() {
    if (nonSelectableDays == null) {
      nonSelectableDays = new ArrayList<Date>();
    }
    return nonSelectableDays;
  }

  /**
   * @param list
   */
  public void setNonSelectableDays(List<Date> list) {
    nonSelectableDays = list;
  }

  /**
   * Get Holidays dates in personal agenda (YYYY/MM/JJ)
   * @return
   */
  public List<String> getHolidaysDates() throws RemoteException {
    if (holidaysDates == null) {
      holidaysDates = getHolidaysDatesInDb();
    }
    return holidaysDates;
  }

  /**
   * Set holidays dates of personal agenda
   * @param list
   */
  public void setHolidaysDates(List<String> list) {
    holidaysDates = list;
  }

  /**
   * Get user by the userPanel (for viewing another agenda)
   * @return
   * @throws
   * @see
   */
  public String initUserPanelOtherAgenda() {
    String m_context = URLManager.getApplicationURL();
    PairObject hostComponentName = new PairObject(getString("agenda"),
        m_context + "/Ragenda/jsp/Main");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("viewOtherAgenda"), m_context
        + URLManager.getURL(URLManager.CMP_AGENDA) + "Main");
    String hostUrl = m_context + URLManager.getURL(URLManager.CMP_AGENDA)
        + "ViewOtherAgenda";
    String cancelUrl = m_context + URLManager.getURL(URLManager.CMP_AGENDA)
        + "Main";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName("");
    sel.setHostPath(hostPath);
    sel.setHostComponentName(hostComponentName);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setPopupMode(true);
    sel.setMultiSelect(false);
    sel.setSetSelectable(false);
    sel.setElementSelectable(true);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /**
   * Get a UserDetail of selected user in UserPanel
   * @param
   * @return UserDetail
   */
  public UserDetail getSelectedUser() {
    Selection sel = getSelection();
    UserDetail selectedUser = null;
    String[] selectedUsers = sel.getSelectedElements();
    if (selectedUsers != null) {
      selectedUser = getUserDetail(selectedUsers[0]);
    }

    return selectedUser;
  }

  /**
   * Set userDetail for viewing his agenda
   * @param userDetail
   */
  public void setAgendaUserDetail(UserDetail userDetail) {
    agendaUserDetail = userDetail;
    agendaUserId = userDetail.getId();
  }

  /**
   * Get userDetail for this agenda
   * @param userDetail
   */
  public UserDetail getAgendaUserDetail() {
    return agendaUserDetail;
  }

  /**
   * Get userId for this agenda
   * @param userDetail
   */
  public String getAgendaUserId() {
    return agendaUserId;
  }

  /**
   * If current agenda is for another user
   * @return true or false
   */
  public boolean isOtherAgendaMode() {
    return !agendaUserId.equals(getUserId());
  }

  /**
   * Get synchronisation user settings
   * @return CalendarImportSettings object containing user settings, null if no settings found
   */
  public CalendarImportSettings getImportSettings() {
    return importSettingsDao.getUserSettings(getUserId());
  }

  /**
   * Save synchronisation user settings
   * @param importSettings CalendarImportSettings object containing user settings
   * @see com.stratelia.webactiv.agenda.model.CalendarImportSettings
   */
  public void saveUserSettings(CalendarImportSettings importSettings)
      throws AgendaException {
    importSettingsDao.saveUserSettings(importSettings);
  }

  /**
   * Update synchronisation user settings
   * @param importSettings CalendarImportSettings object containing user settings
   * @see com.stratelia.webactiv.agenda.model.CalendarImportSettings
   */
  public void updateUserSettings(CalendarImportSettings importSettings)
      throws AgendaException {
    importSettingsDao.updateUserSettings(importSettings);
  }

  /**
   * =============== ICALENDAR MANAGEMENT ====================== *
   */
  /**
   * Export Calendar in Ical format
   * @param startDate
   * @param endDate
   * @return ReturnCode (0=ok, 1=Empty)
   * @throws Exception
   */
  public String exportIcalAgenda(String startDate, String endDate)
      throws Exception {
    SilverTrace.debug("agenda", "AgendaSessionController.exportIcalAgenda()",
        "root.MSG_GEN_ENTER_METHOD");
    String returnCode = new ExportIcalManager(this).exportIcalAgenda(startDate,
        endDate);
    SilverTrace.debug("agenda", "AgendaSessionController.exportIcalAgenda()",
        "root.MSG_GEN_EXIT_METHOD");
    return returnCode;
  }

  public String exportIcalAgenda() throws Exception {
    return exportIcalAgenda(null, null);
  }

  /**
   * Export Calendar in Ical format
   * @param startDate
   * @param endDate
   * @return ReturnCode (See ReturnCodes)
   * @throws Exception
   */
  public String importIcalAgenda(File fileCalendar) throws Exception {
    SilverTrace.debug("agenda", "AgendaSessionController.importIcalAgenda()",
        "root.MSG_GEN_ENTER_METHOD");
    String returnCode = new ImportIcalManager(this).importIcalAgenda(fileCalendar);
    SilverTrace.debug("agenda", "AgendaSessionController.importIcalAgenda()",
        "root.MSG_GEN_EXIT_METHOD");
    return returnCode;
  }

  /**
   * Synchronize localResourceLocator agenda with URLIcalendar
   * @param urlICalendar
   * @param loginIcalendar
   * @param pwdIcalendar
   * @return ReturnCode
   * @throws Exception
   */
  public String synchroIcalAgenda(String urlICalendar, String loginIcalendar,
      String pwdIcalendar) throws Exception {
    SilverTrace.debug("agenda", "AgendaSessionController.synchroIcalAgenda()",
        "root.MSG_GEN_ENTER_METHOD");
    URL iCalendarServerUrl = new URL(urlICalendar);
    String returnCodeSynchro = new SynchroIcalManager(this).synchroIcalAgenda(
        iCalendarServerUrl, getIcalendarFile(), loginIcalendar, pwdIcalendar);
    SilverTrace.debug("agenda", "AgendaSessionController.synchroIcalAgenda()",
        "root.MSG_GEN_EXIT_METHOD");
    return returnCodeSynchro;
  }

  /**
   * Synchronize localResourceLocator agenda with URLIcalendar
   * @param urlICalendar
   * @return ReturnCode
   * @throws Exception
   */
  public String synchroIcalAgenda(String urlICalendar) throws Exception {
    return synchroIcalAgenda(urlICalendar, null, null);
  }

  private File getIcalendarFile() {
    return new File(FileRepositoryManager.getTemporaryPath()
        + AgendaSessionController.AGENDA_FILENAME_PREFIX + getUserId() + ".ics");
  }

  /**
   * Get days off defined for this agenda user
   * @return List of HolidayDetail (yyyy/mm/dd, userId)
   * @throws RemoteException
   */
  public List<String> getHolidaysDatesInDb() throws RemoteException {
    return calendarBm.getHolidayDates(getAgendaUserId());
  }

  public void changeDateStatus(String date, String nextStatus)
      throws RemoteException, ParseException {
    int status = Integer.parseInt(nextStatus);
    HolidayDetail holiday = new HolidayDetail(DateUtil.parse(date), getUserId());
    if (status == WORKING_DAY) {
      // le jour devient un jour travaillé
      calendarBm.removeHolidayDate(holiday);
    } else {
      // le jour devient un jour non travaillé
      calendarBm.addHolidayDate(holiday);
    }
    setHolidaysDates(getHolidaysDatesInDb());
  }

  /**
   * Change worked or non-worked days status
   * @param year
   * @param month
   * @param day
   * @throws RemoteException
   * @throws ParseException
   */
  public void changeDayOfWeekStatus(String year, String month, String day)
      throws RemoteException {
    SilverTrace.info("agenda", "AgendaSessionController.changeDayOfWeekStatus()",
        "root.MSG_GEN_ENTER_METHOD", "year=" + year + ", month=" + month + ", day=" + day);

    int iMonth = Integer.parseInt(month);

    currentCalendar.set(Calendar.YEAR, Integer.parseInt(year));
    currentCalendar.set(Calendar.MONTH, iMonth);
    currentCalendar.set(Calendar.DATE, 1);

    // on se place sur le premier jour du mois
    // correspondant au jour de la semaine passé en paramêtre
    while (currentCalendar.get(Calendar.DAY_OF_WEEK) != Integer.parseInt(day)) {
      currentCalendar.add(Calendar.DATE, 1);
    }

    Date date = currentCalendar.getTime();

    HolidayDetail holidayDate = new HolidayDetail(date, getUserId());
    boolean isHoliday = calendarBm.isHolidayDate(holidayDate);

    List<HolidayDetail> holidayDates = new ArrayList<HolidayDetail>();
    while (currentCalendar.get(Calendar.MONTH) == iMonth) {
      holidayDates.add(new HolidayDetail(currentCalendar.getTime(), getUserId()));
      currentCalendar.add(Calendar.DATE, 7);
    }
    if (isHoliday) {
      calendarBm.removeHolidayDates(holidayDates);
    } else {
      calendarBm.addHolidayDates(holidayDates);
    }
    setHolidaysDates(getHolidaysDatesInDb());
    currentCalendar.set(Calendar.YEAR, Integer.parseInt(year));
    currentCalendar.set(Calendar.MONTH, iMonth);
    currentCalendar.set(Calendar.DATE, 1);
    SilverTrace.info("agenda",
        "AgendaSessionController.changeDayOfWeekStatus()",
        "root.MSG_GEN_EXIT_METHOD", "year=" + year + ", month=" + month
        + ", day=" + day);
  }

  /**
   * Ask if a date is a day off
   * @param date
   * @return
   */
  public boolean isHolidayDate(Date date) {
    boolean isHolidayDate = false;
    try {
      HolidayDetail currentDate = new HolidayDetail(date, getAgendaUserId());
      isHolidayDate = calendarBm.isHolidayDate(currentDate);
    } catch (RemoteException e) {
      throw new AgendaRuntimeException(
          "AgendaSessionController.isHolidayDate()", SilverpeasException.ERROR,
          "agenda.MSG_IS_HOLIDAY_DATE_FAILED", e);
    }
    return isHolidayDate;
  }

  /**
   * Ask if a date is a day off
   * @param date
   * @return
   */
  public boolean isHolidayDate(String agendaUserId, Date date) {
    boolean isHolidayDate = false;
    try {
      HolidayDetail currentDate = new HolidayDetail(date, agendaUserId);
      isHolidayDate = calendarBm.isHolidayDate(currentDate);
    } catch (RemoteException e) {
      throw new AgendaRuntimeException(
          "AgendaSessionController.isHolidayDate()", SilverpeasException.ERROR,
          "agenda.MSG_IS_HOLIDAY_DATE_FAILED", e);
    }
    return isHolidayDate;
  }

  /**
   * Ask if the same days of the month are all holidays
   * @param cal
   * @param month (0=january, etc...)
   * @return true or false
   */
  public boolean isSameDaysAreHolidays(Calendar cal, int currentMonth) {
    Calendar localCalendar = (Calendar) cal.clone();
    boolean isSameDaysAreHolidays = true;
    for (int day = 1; day < 5 && isSameDaysAreHolidays; day++) {
      if (!isHolidayDate(localCalendar.getTime())
          && localCalendar.get(Calendar.MONTH) == currentMonth) {
        isSameDaysAreHolidays = false;
      }
      localCalendar.add(Calendar.DATE, 7);
    }
    return isSameDaysAreHolidays;
  }

  /**
   * Get url access to my diary
   * @return String
   */
  public String getMyAgendaUrl() {
    OrganizationController oc = new OrganizationController();
    String url = "/SubscribeAgenda/" + AGENDA_FILENAME_PREFIX + "?userId="
        + getUserId() + "&amp;login=" + getUserDetail().getLogin() + "&amp;password="
        + oc.getUserFull(getUserId()).getPassword();
    return url;
  }

  /**
   * Get view Type (by Day, Week, Month ou Year)
   * @return
   */
  public String getCurrentViewType() {
    String viewType = "Main";
    switch (getCurrentDisplayType()) {
      case AgendaHtmlView.BYDAY:
        viewType = "Main";
        break;
      case AgendaHtmlView.BYWEEK:
        viewType = "ViewByWeek";
        break;
      case AgendaHtmlView.BYMONTH:
        viewType = "ViewByMonth";
        break;
      case AgendaHtmlView.BYYEAR:
        viewType = "ViewByYear";
        break;
    }
    return viewType;
  }
}
