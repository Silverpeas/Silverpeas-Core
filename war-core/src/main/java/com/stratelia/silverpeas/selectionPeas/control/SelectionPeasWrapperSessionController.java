/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.selectionPeas.control;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * A simple wrapper to the userpanel.
 * 
 * @author Didier Wenzek
 */
public class SelectionPeasWrapperSessionController extends AbstractComponentSessionController
{

    /**
     * Standard Session Controller Constructeur
     * 
     * @param mainSessionCtrl   The full work session.
     * @param componentContext  The context of this component session.
     */
    public SelectionPeasWrapperSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
    {
        super(mainSessionCtrl, componentContext, "com.stratelia.silverpeas.userPanelPeas.multilang.selectionPeasBundle", "com.stratelia.silverpeas.userPanelPeas.settings.selectionPeasIcons");
    }

    /**
     * Returns the HTML form name whose user element must be set.
     */
    public String getFormName()
    {
        return formName;
    }

    /**
     * Returns the HTML input where the selected user id must be set.
     */
    public String getElementId()
    {
        return elementId;
    }

    /**
     * Returns the HTML input where the selected user name must be set.
     */
    public String getElementName()
    {
        return elementName;
    }

    /**
     * Returns the selected user (if any).
     */
    public UserDetail getSelectedUser()
    {
        return selectedUser;
    }

    /**
     * Set the HTML form name whose user element must be set.
     */
    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    /**
     * Set the HTML input where the selected user id must be set.
     */
    public void setElementId(String elementId)
    {
        this.elementId = elementId;
    }

    /**
     * Set the HTML input where the selected user name must be set.
     */
    public void setElementName(String elementName)
    {
        this.elementName = elementName;
    }

    /**
     * Set the selected user (if any).
     */
    public void setSelectedUserId(String selectedUserId)
    {
        selectedUser = null;
    }

    /**
     * Init the user panel.
     */
    public String initSelectionPeas()
    {
        String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
        String hostUrl = m_context + "/RselectionPeasWrapper/jsp/close";

        Selection sel = getSelection();
        sel.resetAll();
        sel.setHostSpaceName(null);
        sel.setHostComponentName(null);
        sel.setHostPath(null);

        sel.setGoBackURL(hostUrl);
        sel.setCancelURL(hostUrl);

        // Contraintes		
        sel.setMultiSelect(false);
        sel.setPopupMode(false);
        sel.setSetSelectable(false);
        sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
        return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
    }

    /**
     * Reads the selection made with the user panel.
     */
    public void getSelectionPeasSelection()
    {
        Selection sel = getSelection();
        String    id = "";

        id = sel.getFirstSelectedElement();
        if ((id != null) && (id.length() > 0))
        {
            selectedUser = organizationController.getUserDetail(id);
        }
    }

    /**
     * A private OrganizationController.
     */
    static private OrganizationController organizationController = new OrganizationController();

    /**
     * The HTML form name whose user element must be set.
     */
    private String                        formName = null;

    /**
     * The HTML input where the selected user id must be set.
     */
    private String                        elementId = null;

    /**
     * The HTML input where the selected user name must be set.
     */
    private String                        elementName = null;

    /**
     * The selected user (if any).
     */
    private UserDetail                    selectedUser = null;
}
