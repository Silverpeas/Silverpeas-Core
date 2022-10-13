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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.tools.agenda.control;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.personalorganizer.model.Category;
import org.silverpeas.core.personalorganizer.model.HolidayDetail;
import org.silverpeas.core.personalorganizer.model.JournalHeader;
import org.silverpeas.core.personalorganizer.model.ParticipationStatus;
import org.silverpeas.core.personalorganizer.model.Schedulable;
import org.silverpeas.core.personalorganizer.model.SchedulableCount;
import org.silverpeas.core.personalorganizer.service.CalendarException;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.URLEncoder;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.calendar.ical.ExportIcalManager;
import org.silverpeas.core.web.calendar.ical.ImportIcalManager;
import org.silverpeas.core.web.calendar.ical.SynchroIcalManager;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.tools.agenda.model.CalendarImportSettings;
import org.silverpeas.core.web.tools.agenda.model.CalendarImportSettingsDao;
import org.silverpeas.core.web.tools.agenda.notification.AgendaUserNotification;
import org.silverpeas.core.web.tools.agenda.view.AgendaHtmlView;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class AgendaSessionController extends AbstractComponentSessionController {

  private static final String RAGENDA_JSP_MAIN = "/Ragenda/jsp/Main";
  private int currentDisplayType = AgendaHtmlView.BYDAY;
  private boolean calendarVisible = true;
  private Calendar currentCalendar;
  private SilverpeasCalendar calendarBm;
  private Category category = null;
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
  private final CalendarImportSettingsDao importSettingsDao =
      CalendarImportSettingsDao.getInstance();
  public static final String ICALENDAR_MIME_TYPE = "text/calendar";
  public static final String EXPORT_SUCCEEDED = "0";
  public static final String EXPORT_FAILED = "1";
  public static final String EXPORT_EMPTY = "2";
  public static final String IMPORT_SUCCEEDED = "0";
  public static final String IMPORT_FAILED = "1";
  public static final String SYNCHRO_SUCCEEDED = "0";
  public static final String SYNCHRO_FAILED = "1";
  public static final String AGENDA_FILENAME_PREFIX = "agenda";
  public static final int WORKING_DAY = 0;
  public static final int HOLIDAY_DAY = 1;

  public AgendaSessionController(MainSessionController mainSessionCtrl, ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.agenda.multilang.agenda");
    setComponentRootName(URLUtil.CMP_AGENDA);
    initEJB();
    AdminController admin = ServiceProvider.getService(AdminController.class);
    Domain defaultDomain = admin.getDomain(getUserDetail().getDomainId());
    serverURL = defaultDomain.getSilverpeasServerURL();
  }

  private boolean isUseRss() {
    return "yes".equals(getSettings().getString("calendarRss"));
  }

  @Override
  public String getRSSUrl() {
    if (isUseRss()) {
      return "/rssAgenda/" + getAgendaUserId() + "?userId=" + getUserId() + "&amp;login=" +
          URLEncoder.encodePathParamValue(getUserDetail().getLogin()) + "&amp;password=" +
          URLEncoder.encodePathParamValue(getOrganisationController().getUserFull(getUserId()).
              getPassword());
    }
    return null;
  }

  /**
   * Method declaration
   *
   */
  private void initEJB() {
    // 1 - Remove all data stored by this SessionController (includes ref to EJB)
    calendarBm = null;
    // 2 - Init EJB used by this SessionController
    setCalendarBm();
  }

  /**
   * Method declaration
   *
   */
  private void setCalendarBm() {
    if (calendarBm == null) {
      try {
        calendarBm = ServiceProvider.getService(SilverpeasCalendar.class);
      } catch (Exception e) {
        throw new AgendaRuntimeException(e);
      }
    }
  }

  protected String getComponentInstName() {
    return URLUtil.CMP_AGENDA;
  }

  /**
   * @param name
   * @param description
   * @param priority
   * @param classification
   * @param startDay
   * @param startHour
   * @param endDay
   * @param endHour
   * @return
   * @throws RemoteException
   */
  public String addJournal(String name, String description, String priority, String classification,
      Date startDay, String startHour, Date endDay, String endHour) {
    JournalHeader journal = new JournalHeader(name, getUserId());

    journal.setDescription(description);
    try {
      journal.getPriority().setValue(Integer.parseInt(priority));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);

    }
    try {
      journal.getClassification().setString(classification);
      journal.setStartDay(DateUtil.date2SQLDate(startDay));
      journal.setStartHour(startHour);
      journal.setEndDay(DateUtil.date2SQLDate(endDay));
      journal.setEndHour(endHour);
    } catch (java.text.ParseException pe) {
      throw new AgendaRuntimeException(pe);
    }
    return calendarBm.addJournal(journal);
  }

  /**
   * Notification from delagator to attendees.
   * @param id
   * @param action
   */
  protected void notifyAttendees(final String id, final NotifAction action) {
    try {
      UserNotificationHelper
          .buildAndSend(new AgendaUserNotification(action, getUserDetail(), getJournalHeader(id)));

    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  /**
   * Notification from attendee to delegator.
   * @param id
   * @param attend
   */
  protected void notifyFromAttendee(final String id, final String attend) {
    try {

      UserNotificationHelper
          .buildAndSend(new AgendaUserNotification(getUserDetail(), getJournalHeader(id), attend));

    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
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
   *
   */
  public void updateJournal(String id, String name, String description, String priority,
      String classification, Date startDay, String startHour, Date endDay, String endHour) {
    boolean changed = false;
    JournalHeader journal = getJournalHeader(id);

    journal.setName(name);
    journal.setDescription(description);
    try {
      journal.getPriority().setValue(Integer.parseInt(priority));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    try {
      changed = eventHasChanged(journal, startDay, startHour, endDay, endHour);
      journal.getClassification().setString(classification);
      journal.setStartDay(DateUtil.date2SQLDate(startDay));
      journal.setStartHour(startHour);
      journal.setEndDay(DateUtil.date2SQLDate(endDay));
      journal.setEndHour(endHour);
    } catch (java.text.ParseException pe) {
      throw new AgendaRuntimeException(pe);
    }

    calendarBm.updateJournal(journal);
    if (changed) {
      notifyAttendees(id, NotifAction.UPDATE);
    }
  }

  private boolean eventHasChanged(JournalHeader journal, Date startDay, String startHour,
      Date endDay, String endHour) {
    return DateUtil.datesAreEqual(journal.getStartDate(), startDay) &&
        DateUtil.datesAreEqual(journal.getEndDate(), endDay) &&
        (journal.getStartHour() != null && journal.getStartHour().equals(startHour)) &&
        (journal.getEndHour() != null && journal.getEndHour().equals(endHour));
  }

  /**
   * Method declaration
   * @param id
   * @throws CalendarException
   * @throws RemoteException
   */
  public void removeJournal(String id) {
    notifyAttendees(id, NotifAction.DELETE);
    calendarBm.removeJournal(id);
  }

  /**
   * Method declaration
   * @param journalId
   * @return
   * @throws RemoteException
   *
   */
  public JournalHeader getJournalHeader(String journalId) {
    return calendarBm.getJournalHeader(journalId);
  }

  /**
   * methods for attendees
   */
  public Collection<Attendee> getJournalAttendees(String journalId) throws AgendaException {
    try {
      return calendarBm.getJournalAttendees(journalId);
    } catch (Exception e) {
      throw new AgendaException(e);
    }

  }

  /**
   * Method declaration
   * @param journalId
   * @param userIds
   * @throws AgendaException
   *
   */
  public void setJournalAttendees(String journalId, String[] userIds) throws AgendaException {
    try {
      calendarBm.setJournalAttendees(journalId, userIds);
      notifyAttendees(journalId, NotifAction.CREATE);
    } catch (Exception e) {
      throw new AgendaException(e);
    }
  }

  /**
   * Method declaration
   * @param journalId
   * @param userId
   * @param status
   * @throws AgendaException
   *
   */
  public void setJournalParticipationStatus(String journalId, String userId, String status)
      throws AgendaException {
    try {
      calendarBm.setJournalParticipationStatus(journalId, userId, status);
      notifyFromAttendee(journalId, status);
    } catch (Exception e) {
      throw new AgendaException(e);
    }
  }

  /**
   * methods for categories
   */
  public Collection<Category> getAllCategories() throws AgendaException {
    try {
      return calendarBm.getAllCategories();
    } catch (Exception e) {
      throw new AgendaException(e);
    }
  }

  /**
   * Method declaration
   * @param categoryId
   * @return
   * @throws AgendaException
   *
   */
  public Category getCategory(String categoryId) throws AgendaException {
    try {
      return calendarBm.getCategory(categoryId);
    } catch (Exception e) {
      throw new AgendaException(e);
    }
  }

  /**
   * Method declaration
   * @param journalId
   * @return
   * @throws AgendaException
   *
   */
  public Collection<Category> getJournalCategories(String journalId) throws AgendaException {
    try {
      return calendarBm.getJournalCategories(journalId);
    } catch (Exception e) {
      throw new AgendaException(e);
    }
  }

  /**
   * Method declaration
   * @param journalId
   * @param categoryIds
   * @throws AgendaException
   *
   */
  public void setJournalCategories(String journalId, String[] categoryIds) throws AgendaException {
    try {
      calendarBm.setJournalCategories(journalId, categoryIds);
    } catch (Exception e) {
      throw new AgendaException(e);
    }

  }

  /**
   * Method declaration
   * @return
   *
   */
  public UserDetail[] getUserList() {
    return getOrganisationController().getAllUsers();
  }

  /**
   * Method declaration
   * @param userId
   * @return
   *
   */
  public UserDetail getUserDetail(String userId) {
    return getOrganisationController().getUserDetail(userId);
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
   *
   */
  public void setCategory(Category category) {
    this.category = category;
  }

  /**
   * Method declaration
   * @return
   *
   */
  public JournalHeader getCurrentJournalHeader() {
    return currentJournalHeader;
  }

  /**
   * Method declaration
   * @param journalHeader
   *
   */
  public void setCurrentJournalHeader(JournalHeader journalHeader) {
    currentJournalHeader = journalHeader;
  }

  /**
   * Method declaration
   * @return
   *
   */
  public Collection<Attendee> getCurrentAttendees() {
    return currentAttendees;
  }

  /**
   * Method declaration
   * @param attendees
   *
   */
  public void setCurrentAttendees(Collection<Attendee> attendees) {
    currentAttendees = attendees;
  }

  /**
   * Method declaration
   * @return
   *
   */
  public Collection<Category> getCurrentCategories() {
    return currentCategories;
  }

  /**
   * Method declaration
   * @param categories
   *
   */
  public void setCurrentCategories(Collection<Category> categories) {
    currentCategories = categories;
  }

  /**
   * Method declaration
   * @param visible
   *
   */
  public void setCalendarVisible(boolean visible) {
    this.calendarVisible = visible;
  }

  /**
   * Method declaration
   * @return
   *
   */
  public ParticipationStatus getParticipationStatus() {
    if (participationStatus == null) {
      participationStatus = new ParticipationStatus(ParticipationStatus.ACCEPTED);
    }
    return participationStatus;
  }

  /**
   * Method declaration
   * @return
   *
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
   *
   */
  public SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle("org.silverpeas.agenda.settings.agendaSettings");
  }

  /**
   * Method declaration
   *
   */
  public void viewByDay() {
    currentDisplayType = AgendaHtmlView.BYDAY;
  }

  /**
   * Method declaration
   *
   */
  public void viewByWeek() {
    currentDisplayType = AgendaHtmlView.BYWEEK;
  }

  /**
   * Method declaration
   *
   */
  public void viewByMonth() {
    currentDisplayType = AgendaHtmlView.BYMONTH;
  }

  /**
   * Method declaration
   *
   */
  public void viewByYear() {
    currentDisplayType = AgendaHtmlView.BYYEAR;
  }

  /**
   * Method declaration
   *
   */
  public void viewChooseDays() {
    currentDisplayType = AgendaHtmlView.CHOOSE_DAYS;
  }

  /**
   * Method declaration
   * @return
   *
   */
  public int getCurrentDisplayType() {
    return currentDisplayType;
  }

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   *
   */
  public AgendaHtmlView getCurrentHtmlView() throws RemoteException {
    AgendaHtmlView agendaView = null;
    Collection<?> schedules = null;

    if (currentDisplayType == AgendaHtmlView.BYDAY) {
      agendaView = new AgendaHtmlView(currentDisplayType, getCurrentDay(), this, getSettings());
      agendaView.setCalendarVisible(calendarVisible);
      schedules = getDaySchedulables();
    } else if (currentDisplayType == AgendaHtmlView.BYWEEK) {
      agendaView = new AgendaHtmlView(currentDisplayType, getWeekFirstDay(getCurrentDay()), this,
          getSettings());
      schedules = getWeekSchedulables();
    } else if (currentDisplayType == AgendaHtmlView.BYMONTH) {
      agendaView = new AgendaHtmlView(currentDisplayType, getMonthFirstDay(getCurrentDay()), this,
          getSettings());
      schedules = countMonthSchedulables();
    } else if (currentDisplayType == AgendaHtmlView.BYYEAR) {
      agendaView = new AgendaHtmlView(currentDisplayType, getYearFirstDay(getCurrentDay()), this,
          getSettings());
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
   *
   */
  public Collection<JournalHeader> getDaySchedulables() {
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
   *
   */
  public Collection<JournalHeader> getMonthSchedulables(Date date) {
    Date begin = getMonthFirstDay(date);
    Date end = getMonthLastDay(date);

    String categoryId = null;
    if (getCategory() != null) {
      categoryId = getCategory().getId();
    }

    return calendarBm.getPeriodSchedulablesForUser(DateUtil.date2SQLDate(begin),
        DateUtil.date2SQLDate(end), agendaUserId, categoryId, getParticipationStatus().getString());
  }

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   *
   */
  public Collection<JournalHeader> getWeekSchedulables() {
    Date begin = getWeekFirstDay(getCurrentDay());
    Date end = getWeekLastDay(getCurrentDay());

    String categoryId = null;
    if (getCategory() != null) {
      categoryId = getCategory().getId();
    }

    return calendarBm.getPeriodSchedulablesForUser(DateUtil.date2SQLDate(begin),
        DateUtil.date2SQLDate(end), agendaUserId, categoryId, getParticipationStatus().getString());
  }

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   *
   */
  public Collection<SchedulableCount> countMonthSchedulables() {
    String month = (DateUtil.date2SQLDate(getCurrentDay())).substring(0, 8);
    String categoryId = null;
    if (getCategory() != null) {
      categoryId = getCategory().getId();
    }

    return calendarBm.countMonthSchedulablesForUser(month, agendaUserId, categoryId,
        getParticipationStatus().getString());
  }

  /**
   * Method declaration
   * @param userId
   * @param day
   * @return
   * @throws RemoteException
   *
   */
  public Collection<JournalHeader> getBusyTime(String userId, java.util.Date day)
      throws RemoteException {
    Collection<JournalHeader> result = calendarBm
        .getDaySchedulablesForUser(DateUtil.date2SQLDate(day), userId, null,
            ParticipationStatus.ACCEPTED);

    if (!userId.equals(getUserId())) {
      Collection<JournalHeader> subResult = new ArrayList<>();

      for (JournalHeader schedule : result) {
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
  public boolean isDayHasEvents(String userId, Date day) {
    Collection<JournalHeader> result =
        calendarBm.getDaySchedulablesForUser(DateUtil.date2SQLDate(day), userId, null,
            ParticipationStatus.ACCEPTED);
    return result != null && !result.isEmpty();
  }

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   *
   */
  public boolean hasTentativeSchedulables() throws RemoteException {
    return calendarBm.hasTentativeSchedulablesForUser(getUserId());
  }

  /**
   * Method declaration
   * @return
   * @throws AgendaException
   *
   */
  public Collection<JournalHeader> getTentativeSchedulables() throws AgendaException {
    try {
      return calendarBm.getTentativeSchedulablesForUser(getUserId());
    } catch (Exception e) {
      throw new AgendaException(e);
    }
  }

  /**
   * Method declaration
   * @return
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
      SilverLogger.getLogger(this).error(e);
    }
  }

  /**
   * Method declaration
   *
   */
  public void next() {
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
   *
   */
  public void nextDay() {
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
   *
   */
  public void nextWeek() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.DATE, 7);
  }

  /**
   * Method declaration
   *
   */
  public void nextMonth() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.MONTH, 1);
  }

  /**
   * Method declaration
   *
   */
  public void nextYear() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.YEAR, 1);
  }

  /**
   * Method declaration
   *
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
   *
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
   *
   */
  public void previousWeek() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.DATE, -7);
  }

  /**
   * Method declaration
   *
   */
  public void previousMonth() {
    if (currentCalendar == null) {
      return;
    }
    currentCalendar.add(Calendar.MONTH, -1);
  }

  /**
   * Method declaration
   *
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
   *
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
   *
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
   *
   */
  public static Date getMonthFirstDay(Date date) {
    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return calendar.getTime();
  }

  /**
   * Get the last day of the month
   * @param date
   * @return date
   *
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
   *
   */
  public static Date getYearFirstDay(Date date) {
    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.MONTH, 1);
    return calendar.getTime();
  }

  /**
   * Parametre le userPannel => tous les users, sélection des users participants.
   * @return
   */
  public String initSelectionPeas() {
    String ctx = URLUtil.getApplicationURL();
    Pair<String, String> hostComponentName =
        new Pair<>(getString(AGENDA_FILENAME_PREFIX), ctx + RAGENDA_JSP_MAIN);
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("editionListeDiffusion"), ctx + RAGENDA_JSP_MAIN);
    String hostUrl = ctx + "/Ragenda/jsp/saveMembers";
    String cancelUrl = ctx + "/Ragenda/jsp/saveMembers";

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
    return Selection.getSelectionURL();
  }

  /**
   * Retourne une Collection de UserDetail des utilisateurs selectionnés via le userPanel
   * @param
   * @return
   * @throws
   *
   */
  public Collection<Attendee> getUserSelected() throws AgendaException {
    Selection sel = getSelection();
    List<Attendee> attendees = new ArrayList<>();
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
    calendarBm = null;
  }

  /**
   * @return
   */
  public List<Date> getNonSelectableDays() {
    if (nonSelectableDays == null) {
      nonSelectableDays = new ArrayList<>();
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
  public List<String> getHolidaysDates() {
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
   *
   */
  public String initUserPanelOtherAgenda() {
    String ctx = URLUtil.getApplicationURL();
    Pair<String, String> hostComponentName =
        new Pair<>(getString(AGENDA_FILENAME_PREFIX), ctx + RAGENDA_JSP_MAIN);
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("viewOtherAgenda"),
        ctx + URLUtil.getURL(URLUtil.CMP_AGENDA, null, null) + "Main");
    String hostUrl = ctx + URLUtil.getURL(URLUtil.CMP_AGENDA, null, null) + "ViewOtherAgenda";
    String cancelUrl = ctx + URLUtil.getURL(URLUtil.CMP_AGENDA, null, null) + "Main";

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
    return Selection.getSelectionURL();
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
   */
  public UserDetail getAgendaUserDetail() {
    return agendaUserDetail;
  }

  /**
   * Get userId for this agenda
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
   * @see org.silverpeas.core.web.tools.agenda.model.CalendarImportSettings
   */
  public void saveUserSettings(CalendarImportSettings importSettings) throws AgendaException {
    importSettingsDao.saveUserSettings(importSettings);
  }

  /**
   * Update synchronisation user settings
   * @param importSettings CalendarImportSettings object containing user settings
   * @see org.silverpeas.core.web.tools.agenda.model.CalendarImportSettings
   */
  public void updateUserSettings(CalendarImportSettings importSettings) throws AgendaException {
    importSettingsDao.updateUserSettings(importSettings);
  }

  /**
   * =============== ICALENDAR MANAGEMENT ====================== *
   */
  /**
   * Export SilverpeasCalendar in Ical format
   * @param startDate
   * @param endDate
   * @return ReturnCode (0=ok, 1=Empty)
   * @throws Exception
   */
  public String exportIcalAgenda(String startDate, String endDate) throws AgendaException {
    return new ExportIcalManager(this).exportIcalAgenda(startDate, endDate);
  }

  public String exportIcalAgenda() throws AgendaException {
    return exportIcalAgenda(null, null);
  }

  public String importIcalAgenda(File fileCalendar) {
    return new ImportIcalManager(this).importIcalAgenda(fileCalendar);
  }

  /**
   * Synchronize localResourceLocator agenda with URLIcalendar
   * @param urlICalendar
   * @param loginIcalendar
   * @param pwdIcalendar
   * @return ReturnCode
   * @throws Exception
   */
  public String synchroIcalAgenda(String urlICalendar, String loginIcalendar, String pwdIcalendar)
      throws MalformedURLException {
    URL iCalendarServerUrl = new URL(urlICalendar);
    return new SynchroIcalManager(this).synchroIcalAgenda(iCalendarServerUrl, getIcalendarFile(),
        loginIcalendar, pwdIcalendar);
  }

  /**
   * Synchronize localResourceLocator agenda with URLIcalendar
   * @param urlICalendar
   * @return ReturnCode
   * @throws Exception
   */
  public String synchroIcalAgenda(String urlICalendar) throws MalformedURLException {
    return synchroIcalAgenda(urlICalendar, null, null);
  }

  private File getIcalendarFile() {
    return new File(
        FileRepositoryManager.getTemporaryPath() + AgendaSessionController.AGENDA_FILENAME_PREFIX +
            getUserId() + ".ics");
  }

  /**
   * Get days off defined for this agenda user
   * @return List of HolidayDetail (yyyy/mm/dd, userId)
   * @throws RemoteException
   */
  public List<String> getHolidaysDatesInDb() {
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
  public void changeDayOfWeekStatus(String year, String month, String day) throws RemoteException {


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

    List<HolidayDetail> holidayDates = new ArrayList<>();
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

  }

  /**
   * Ask if a date is a day off
   * @param date
   * @return
   */
  public boolean isHolidayDate(Date date) {
    HolidayDetail currentDate = new HolidayDetail(date, getAgendaUserId());
    return calendarBm.isHolidayDate(currentDate);
  }

  /**
   * Ask if a date is a day off
   * @param date
   * @return
   */
  public boolean isHolidayDate(String agendaUserId, Date date) {
    HolidayDetail currentDate = new HolidayDetail(date, agendaUserId);
    return calendarBm.isHolidayDate(currentDate);
  }

  /**
   * Ask if the same days of the month are all holidays
   * @param cal
   * @param currentMonth (0=january, etc...)
   * @return true or false
   */
  public boolean isSameDaysAreHolidays(Calendar cal, int currentMonth) {
    Calendar localCalendar = (Calendar) cal.clone();
    boolean isSameDaysAreHolidays = true;
    for (int day = 1; day < 5 && isSameDaysAreHolidays; day++) {
      if (!isHolidayDate(localCalendar.getTime()) &&
          localCalendar.get(Calendar.MONTH) == currentMonth) {
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
    return "/SubscribeAgenda/" + AGENDA_FILENAME_PREFIX + "?userId=" + getUserId() + "&amp;login=" +
        URLEncoder.encodePathParamValue(getUserDetail().getLogin()) + "&amp;password=" +
        URLEncoder.encodePathParamValue(OrganizationControllerProvider.
            getOrganisationController().getUserFull(getUserId()).getPassword());
  }

  /**
   * Get view Type (by Day, Week, Month ou Year)
   * @return
   */
  public String getCurrentViewType() {
    String viewType;
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
      default:
        viewType = "Main";
        break;
    }
    return viewType;
  }
}
