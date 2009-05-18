/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.selectionPeas.servlets;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.selectionPeas.control.SelectionPeasWrapperSessionController;

/**
 * A simple wrapper for the userpanel.
 */
public class SelectionPeasWrapper extends ComponentRequestRouter
{

    /**
     * Returns a new session controller
     */
    public ComponentSessionController createComponentSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
    {
        return new SelectionPeasWrapperSessionController(mainSessionCtrl, componentContext);
    }

    /**
     * Returns the base name for the session controller of this router.
     */
    public String getSessionControlBeanName()
    {
        return "selectionPeasWrapper";
    }

    /**
     * Do the requested function and return the destination url.
     */
    public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request)
    {
        SelectionPeasWrapperSessionController session = (SelectionPeasWrapperSessionController) componentSC;

        try
        {
            if (function.equals("open"))
            {
                session.setFormName(request.getParameter("formName"));
                session.setElementId(request.getParameter("elementId"));
                session.setElementName(request.getParameter("elementName"));
                session.setSelectedUserId(request.getParameter("selectedUser"));
                return session.initSelectionPeas();
            }
            else if (function.equals("close"))
            {
                session.getSelectionPeasSelection();
                request.setAttribute("formName", session.getFormName());
                request.setAttribute("elementId", session.getElementId());
                request.setAttribute("elementName", session.getElementName());

                request.setAttribute("user", session.getSelectedUser());

                return "/selectionPeas/jsp/closeWrapper.jsp";
            }
            else
            {
                return "/admin/jsp/errorpageMain.jsp";
            }
        }
        catch (Exception e)
        {
            request.setAttribute("javax.servlet.jsp.jspException", e);
            return "/admin/jsp/errorpageMain.jsp";
        }
    }

}