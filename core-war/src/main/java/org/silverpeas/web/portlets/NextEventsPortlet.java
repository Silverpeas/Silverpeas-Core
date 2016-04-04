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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.portlets;

import org.silverpeas.core.web.portlets.FormNames;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.web.tools.agenda.control.AgendaAccess;
import org.silverpeas.core.web.tools.agenda.control.AgendaException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.calendar.model.JournalHeader;
import org.silverpeas.core.util.StringUtil;

import javax.portlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NextEventsPortlet extends GenericPortlet implements FormNames {

  @Override
  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    PortletPreferences pref = request.getPreferences();
    int nbEvents = Integer.parseInt(pref.getValue("nbEvents", "5"));

    List<JournalHeader> events = new ArrayList<JournalHeader>();
    try {
      events = (List<JournalHeader>) AgendaAccess.getNextDaySchedulables(
          UserDetail.getCurrentRequester().getId());

      if (events.size() > nbEvents) {
        events = events.subList(0, nbEvents);
      }
    } catch (AgendaException e) {
      SilverTrace.error("portlet", "NextEvents", "portlet.ERROR", e);
    }

    request.setAttribute("Events", events.iterator());

    include(request, response, "portlet.jsp");
  }

  @Override
  public void doEdit(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "edit.jsp");
  }

  /** Include "help" JSP. */
  @Override
  public void doHelp(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "help.jsp");
  }

  /** Include a page. */
  private void include(RenderRequest request, RenderResponse response,
      String pageName) throws PortletException {
    response.setContentType(request.getResponseContentType());
    if (!StringUtil.isDefined(pageName)) {
      // assert
      throw new NullPointerException("null or empty page name");
    }
    try {
      PortletRequestDispatcher dispatcher = getPortletContext()
          .getRequestDispatcher("/portlets/jsp/nextEvents/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }

  /*
   * Process Action.
   */
  @Override
  public void processAction(ActionRequest request, ActionResponse response)
      throws PortletException {
    if (request.getParameter(SUBMIT_FINISHED) != null) {
      //
      // handle "finished" button on edit page
      // return to view mode
      //
      processEditFinishedAction(request, response);
    } else if (request.getParameter(SUBMIT_CANCEL) != null) {
      //
      // handle "cancel" button on edit page
      // return to view mode
      //
      processEditCancelAction(request, response);
    }
  }

  /*
   * Process the "cancel" action for the edit page.
   */
  private void processEditCancelAction(ActionRequest request,
      ActionResponse response) throws PortletException {
    response.setPortletMode(PortletMode.VIEW);
  }

  /*
   * Process the "finished" action for the edit page. Set the "url" to the value specified in the
   * edit page.
   */
  private void processEditFinishedAction(ActionRequest request,
      ActionResponse response) throws PortletException {
    String nbItems = request.getParameter(TEXTBOX_NB_ITEMS);

    // Check if it is a number
    try {
      int nb = Integer.parseInt(nbItems);

      if (nb < 0 || nb > 30)
        throw new NumberFormatException();

      // store preference
      PortletPreferences pref = request.getPreferences();
      try {
        pref.setValue("nbEvents", nbItems);
        pref.store();
      } catch (ValidatorException ve) {
        getPortletContext().log("could not set nbEvents", ve);
        throw new PortletException("IFramePortlet.processEditFinishedAction",
            ve);
      } catch (IOException ioe) {
        getPortletContext().log("could not set nbEvents", ioe);
        throw new PortletException("IFramePortlet.prcoessEditFinishedAction",
            ioe);
      }
      response.setPortletMode(PortletMode.VIEW);

    } catch (NumberFormatException e) {
      response.setRenderParameter(ERROR_BAD_VALUE, "true");
      response.setPortletMode(PortletMode.EDIT);
    }
  }
}
