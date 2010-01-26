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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/
package com.stratelia.webactiv.agenda.servlets;

import javax.servlet.http.*;

import java.io.File;

import com.silverpeas.ical.ImportIcalManager;
import com.silverpeas.ical.PasswordEncoder;
import com.silverpeas.ical.StringUtils;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.*;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.webactiv.agenda.control.*;
import com.stratelia.webactiv.agenda.model.*;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.calendar.model.Category;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

/**
 * Class declaration
 * @author
 */
public class AgendaRequestRouter extends ComponentRequestRouter {

  /**
   * Constructor declaration
   * @see
   */
  public AgendaRequestRouter() {
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param context
   * @return
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    ComponentSessionController component =
        (ComponentSessionController) new AgendaSessionController(
            mainSessionCtrl, context);

    return component;
  }

  /**
   * This method has to be implemented in the component request router class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "agenda";
  }

  /**
   * This method has to be implemented by the component request router it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Controller, build and initialised.
   * @param request The entering request. The request router need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {

    SilverTrace.info("agenda", "AgendaRequestRouter.getDestination()",
        "root.MSG_GEN_ENTER_METHOD");
    AgendaSessionController scc = (AgendaSessionController) componentSC;
    String destination = "";

    try {
      if (function.startsWith("Main") || function.startsWith(("agenda.jsp"))) {
        scc.viewByDay();
        scc.setAgendaUserDetail(scc.getAgendaUserDetail());

        setCommonAttributes(request, scc);
        destination = "/agenda/jsp/agenda.jsp";
      } else if (function.startsWith("ViewByYear")) {
        scc.viewByYear();
        scc.setAgendaUserDetail(scc.getAgendaUserDetail());

        setCommonAttributes(request, scc);
        destination = "/agenda/jsp/agenda.jsp";
      } else if (function.startsWith("ViewByMonth")) {
        scc.setAgendaUserDetail(scc.getAgendaUserDetail());
        scc.viewByMonth();

        setCommonAttributes(request, scc);
        destination = "/agenda/jsp/agenda.jsp";
      } else if (function.startsWith("ViewByWeek")) {
        scc.setAgendaUserDetail(scc.getAgendaUserDetail());
        scc.viewByWeek();

        setCommonAttributes(request, scc);
        destination = "/agenda/jsp/agenda.jsp";
      } else if (function.startsWith("NextYear")) {
        scc.setAgendaUserDetail(scc.getAgendaUserDetail());
        scc.nextYear();
        destination = getDestination("ToChooseWorkingDays", componentSC,
            request);
      } else if (function.startsWith("PreviousYear")) {
        scc.setAgendaUserDetail(scc.getAgendaUserDetail());
        scc.previousYear();
        destination = getDestination("ToChooseWorkingDays", componentSC,
            request);
      } else if (function.startsWith("ViewByDay")) {
        scc.setAgendaUserDetail(scc.getAgendaUserDetail());
        scc.viewByDay();

        setCommonAttributes(request, scc);
        destination = "/agenda/jsp/agenda.jsp";
      } else if (function.startsWith("SelectDay")) {
        String day = request.getParameter("Day");
        if (StringUtil.isDefined(day)) {
          Date date = DateUtil.stringToDate(day, scc.getLanguage());
          if (!scc.isHolidayDate(date)) {
            scc.selectDay(day);
          }
        }

        setCommonAttributes(request, scc);
        destination = "/agenda/jsp/agenda.jsp";
      } else if (function.startsWith("searchResult")) {
        destination = "/agenda/jsp/journal.jsp?Action=Update&JournalId="
            + request.getParameter("Id");
      } else if (function.startsWith("diffusion")) {
        destination = scc.initSelectionPeas();
      } else if (function.startsWith("saveMembers")) {
        // retour du userPanel
        Collection attendees = scc.getUserSelected();
        scc.setCurrentAttendees(attendees);
        destination = "/agenda/jsp/journal.jsp?Action=DiffusionListOK";
      } else if (function.startsWith("ChooseOtherAgenda")) {
        destination = scc.initUserPanelOtherAgenda();
      } else if (function.startsWith("ViewOtherAgenda")) {
        String id = request.getParameter("Id");
        UserDetail selectedUser;
        if (id != null) {
          // permalink
          selectedUser = scc.getUserDetail(id);
        } else {
          // userPanel return
          selectedUser = scc.getSelectedUser();
        }
        // request.setAttribute("userDetail",selectedUser);
        scc.setAgendaUserDetail(selectedUser);
        SilverTrace.info("agenda", "AgendaRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", selectedUser.getDisplayedName());

        setCommonAttributes(request, scc);
        destination = "/agenda/jsp/agenda.jsp";
      } else if (function.startsWith("ViewCurrentAgenda")) {
        SilverTrace.info("agenda", "AgendaRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", scc.getUserDetail().getDisplayedName());

        UserDetail userDetail = scc.getUserDetail();

        scc.setAgendaUserDetail(userDetail);

        setCommonAttributes(request, scc);
        destination = "/agenda/jsp/agenda.jsp";
      } else if (function.startsWith("importCalendar")) {
        CalendarImportSettings importSettings = scc.getImportSettings();
        if (importSettings != null) {
          request.setAttribute("ImportSettings", importSettings);
        }
        destination = "/agenda/jsp/importCalendar.jsp";
      } else if (function.startsWith("ImportSettings")) {
        // get current imports settings for user
        CalendarImportSettings importSettings = scc.getImportSettings();
        if (importSettings != null) {
          request.setAttribute("ImportSettings", importSettings);
        }
        destination = "/agenda/jsp/importSettings.jsp";
      } else if (function.startsWith("saveImportSettings")
          || function.startsWith("updateImportSettings")) {
        // get updated imports settings for user
        CalendarImportSettings importSettings = new CalendarImportSettings();
        importSettings.setUserId(Integer.parseInt(scc.getUserId()));
        importSettings.setHostName(request.getParameter("hostName"));
        importSettings.setSynchroDelay(Integer.parseInt(request
            .getParameter("synchroDelay")));
        importSettings.setSynchroType(Integer.parseInt(request
            .getParameter("synchroType")));

        // store settings
        if (function.startsWith("saveImportSettings")) {
          scc.saveUserSettings(importSettings);
        } else {
          scc.updateUserSettings(importSettings);
        }

        // reload main frame in order to reload hidden frame with import applet
        //destination = "/agenda/jsp/reloadMainFrame.jsp";
        destination = "/agenda/jsp/agenda.jsp";
      } else if (function.equals("ToExportIcal")) {
        destination = "/agenda/jsp/exportIcal.jsp";
      } else if (function.equals("ToImportIcal")) {
        destination = "/agenda/jsp/importIcal.jsp";
      } else if (function.equals("ExportIcal")) {
        String startDate = request.getParameter("StartDate");
        String endDate = request.getParameter("EndDate");
        String returnCode = scc.exportIcalAgenda(startDate, endDate);
        request.setAttribute("ExportReturnCode", returnCode);
        destination = getDestination("ToExportIcal", componentSC, request);
      } else if (function.equals("ImportIcal")) {
        ImportIcalManager.charset = scc.getSettings().getString(
            "defaultCharset");
        String returnCode = AgendaSessionController.IMPORT_FAILED;
        File fileUploaded = processFormUpload(scc, request);
        if (fileUploaded != null) {
          returnCode = scc.importIcalAgenda(fileUploaded);
          fileUploaded.delete();
        }
        request.setAttribute("ImportReturnCode", returnCode);
        destination = getDestination("ToImportIcal", componentSC, request);
      } else if (function.equals("ToSynchroIcal")) {
        CalendarImportSettings importSettings = scc.getImportSettings();
        String urlIcalendar = null;
        String loginIcalendar = null;
        String pwdIcalendar = null;
        String charset = null;
        if (importSettings != null) {
          urlIcalendar = importSettings.getUrlIcalendar();
          loginIcalendar = importSettings.getLoginIcalendar();
          if (StringUtil.isDefined(importSettings.getPwdIcalendar())) {
            pwdIcalendar = StringUtils.decodePassword(importSettings
                .getPwdIcalendar());
          }
          charset = importSettings.getCharset();
        }
        request.setAttribute("UrlIcalendar", urlIcalendar);
        request.setAttribute("LoginIcalendar", loginIcalendar);
        request.setAttribute("PwdIcalendar", pwdIcalendar);
        request.setAttribute("Charset", charset);
        destination = "/agenda/jsp/synchroIcal.jsp";
      } else if (function.equals("SynchroIcal")) {
        ImportIcalManager.charset = scc.getSettings().getString(
            "defaultCharset");
        // get updated imports settings for user
        boolean newSettings = false;
        boolean authNeeded = false;
        CalendarImportSettings importSettings = scc.getImportSettings();
        if (importSettings == null) {
          importSettings = new CalendarImportSettings();
          newSettings = true;
        }
        String remoteUrlIcalendar = request.getParameter("UrlIcalendar");
        String remoteLoginIcalendar = request.getParameter("LoginIcalendar");
        String remotePwdIcalendar = request.getParameter("PwdIcalendar");
        String charset = request.getParameter("Charset");
        importSettings.setLoginIcalendar(remoteLoginIcalendar);
        remotePwdIcalendar = PasswordEncoder.encodePassword(remotePwdIcalendar);
        importSettings.setPwdIcalendar(remotePwdIcalendar);
        if (StringUtil.isDefined(remoteLoginIcalendar)) {
          authNeeded = true;
        }
        importSettings.setUrlIcalendar(remoteUrlIcalendar);
        importSettings.setUserId(Integer.parseInt(scc.getUserId()));
        importSettings.setHostName("");
        if (StringUtil.isDefined(charset)) {
          importSettings.setCharset(charset);
          ImportIcalManager.charset = charset;
        }
        if (newSettings) {
          scc.saveUserSettings(importSettings);
        } else {
          scc.updateUserSettings(importSettings);
        }
        String returnCode;
        if (authNeeded) {
          returnCode = scc.synchroIcalAgenda(remoteUrlIcalendar,
              remoteLoginIcalendar, remotePwdIcalendar);
        } else {
          returnCode = scc.synchroIcalAgenda(remoteUrlIcalendar);
        }

        request.setAttribute("SynchroReturnCode", returnCode);
        destination = getDestination("ToSynchroIcal", componentSC, request);
      } else if (function.equals("ToChooseWorkingDays")) {
        scc.viewChooseDays();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(scc.getCurrentDay());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, 0);
        Date beginDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.add(Calendar.MONTH, 11);
        Date endDate = calendar.getTime();

        request.setAttribute("BeginDate", beginDate);
        request.setAttribute("EndDate", endDate);
        request.setAttribute("HolidayDates", scc.getHolidaysDates());
        destination = "/agenda/jsp/chooseWorkingDays.jsp";
      } else if (function.equals("ChangeDateStatus")) {
        String date = request.getParameter("Date");
        String status = request.getParameter("Status");
        scc.changeDateStatus(date, status);
        destination = getDestination("ToChooseWorkingDays", componentSC,
            request);
      } else if (function.equals("ChangeDayOfWeekStatus")) {
        String year = request.getParameter("Year");
        String month = request.getParameter("Month");
        String day = request.getParameter("DayOfWeek");
        scc.changeDayOfWeekStatus(year, month, day);
        destination = getDestination("ToChooseWorkingDays", componentSC,
            request);
      } else if (function.equals("UpdateEvent")) {
        String journalId = request.getParameter("JournalId");
        request.setAttribute("JournalId", journalId);
        destination = "/agenda/jsp/journal.jsp";
      } else if (function.equals("ReallyEditCategories")) {
        String selectedCategories = request.getParameter("selectedCategories");
        StringTokenizer st = new StringTokenizer(selectedCategories, ",");
        String[] categoryIds = new String[st.countTokens()];
        Collection selectedCategoryIds = new ArrayList();
        Collection categories = new ArrayList();
        int i = 0;
        while (st.hasMoreTokens()) {
          String categIcal = st.nextToken();
          categoryIds[i] = categIcal;
          selectedCategoryIds.add(new Integer(categoryIds[i]));
          Category categ = scc.getCategory(categoryIds[i]);
          categories.add(categ);
          i++;
        }
        String journalId = request.getParameter("JournalId");
        /*
         * if (StringUtil.isDefined(journalId) && !"-1".equals(journalId))
         * scc.setJournalCategories(journalId, categoryIds); else {
         * scc.setCurrentCategories(categories); journalId = "-1"; }
         */
        scc.setCurrentCategories(categories);

        request.setAttribute("FromCategories", "1");
        destination = "/agenda/jsp/journal.jsp";
      } else {
        destination = "/agenda/jsp/" + function;
      }
    } catch (Exception exce_all) {
      request.setAttribute("javax.servlet.jsp.jspException", exce_all);
      return "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }

  private void setCommonAttributes(HttpServletRequest request,
      AgendaSessionController controller) {
    request.setAttribute("MyAgendaUrl", controller.getMyAgendaUrl());
    request.setAttribute("RSSUrl", controller.getRSSUrl());
  }

  private List getRequestItems(HttpServletRequest request)
      throws FileUploadException {
    DiskFileUpload dfu = new DiskFileUpload();
    List items = dfu.parseRequest(request);
    return items;
  }

  private FileItem getUploadedFile(List items, String parameterName) {
    Iterator iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = (FileItem) iter.next();
      if (!item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item;
      }
    }
    return null;
  }

  private File processFormUpload(AgendaSessionController agendaSc,
      HttpServletRequest request) throws FileUploadException {
    String logicalName = "";
    String tempFolderName = "";
    String tempFolderPath = "";
    String fileType = "";
    long fileSize = 0;
    FileItem fileItem = null;
    File fileUploaded = null;
    try {
      List items = getRequestItems(request);
      fileItem = getUploadedFile(items, "fileCalendar");
      if (fileItem != null) {
        logicalName = fileItem.getName();
        if (logicalName != null) {
          logicalName = logicalName.substring(logicalName
              .lastIndexOf(File.separator) + 1, logicalName.length());

          // Name of temp folder: timestamp and userId
          tempFolderName = new Long(new Date().getTime()).toString() + "_"
              + agendaSc.getUserId();

          // Mime type of the file
          fileType = fileItem.getContentType();
          fileSize = fileItem.getSize();

          // Directory Temp for the uploaded file
          tempFolderPath = FileRepositoryManager.getAbsolutePath(agendaSc
              .getComponentId())
              + GeneralPropertiesManager.getGeneralResourceLocator().getString(
              "RepositoryTypeTemp") + File.separator + tempFolderName;
          if (!new File(tempFolderPath).exists()) {
            FileRepositoryManager.createAbsolutePath(agendaSc.getComponentId(),
                GeneralPropertiesManager.getGeneralResourceLocator().getString(
                "RepositoryTypeTemp")
                + File.separator + tempFolderName);
          }

          // Creation of the file in the temp folder
          fileUploaded = new File(FileRepositoryManager
              .getAbsolutePath(agendaSc.getComponentId())
              + GeneralPropertiesManager.getGeneralResourceLocator().getString(
              "RepositoryTypeTemp")
              + File.separator
              + tempFolderName
              + File.separator + logicalName);
          fileItem.write(fileUploaded);

          // Is a real file ?
          if (fileSize > 0) {
            SilverTrace.debug("agenda",
                "AgendaRequestRouter.processFormUpload()",
                "root.MSG_GEN_PARAM_VALUE", "fileUploaded = " + fileUploaded
                + " fileSize=" + fileSize + " fileType=" + fileType);
          }
        }
      }
    } catch (Exception e) {
      // Other exception
      SilverTrace.warn("agenda", "AgendaRequestRouter.processFormUpload()",
          "root.EX_LOAD_ATTACHMENT_FAILED", e);
    }
    return fileUploaded;
  }
}
