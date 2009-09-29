package com.silverpeas.portlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.todo.control.ToDoAccess;
import com.stratelia.webactiv.todo.control.TodoException;

public class MyTodosPortlet extends GenericPortlet implements FormNames {

  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    PortletSession session = request.getPortletSession();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session
        .getAttribute("SilverSessionController",
            PortletSession.APPLICATION_SCOPE);

    List todos = new ArrayList();
    try {
      todos = (List) ToDoAccess.getNotCompletedToDos(m_MainSessionCtrl
          .getUserId());
    } catch (TodoException e) {
      SilverTrace.error("portlet", "MyTodosPortlet", "portlet.ERROR", e);
    }

    request.setAttribute("Todos", todos.iterator());

    include(request, response, "portlet.jsp");
  }

  public void doEdit(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "edit.jsp");
  }

  /** Include "help" JSP. */
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
          .getRequestDispatcher("/portlets/jsp/myTodos/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }
}
