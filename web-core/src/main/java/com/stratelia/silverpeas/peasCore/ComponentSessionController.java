/**
 * @author nicolas eysseric et didier wenzek
 * @version 1.0
*/

package com.stratelia.silverpeas.peasCore;

import java.util.List;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.clipboard.control.ejb.ClipboardBm;
import com.stratelia.webactiv.personalization.control.ejb.PersonalizationBm;

/**
 * The interface for all component session controllers.
 */

public interface ComponentSessionController
{
    /** Return the organizationController */
    public OrganizationController getOrganizationController();

    /** Return the user language */
    public String getLanguage() ;

    /** Return the UserDetail of the current user */
    public UserDetail getUserDetail();

    /** Return the UserId of the current user */
    public String getUserId();

    /** Return the space label (as known by the user)*/
    public String getSpaceLabel();

    /** Return the space id */
    public String getSpaceId();

    /** Return the name of the component (as specified in the xmlComponent)*/
    public String getComponentName();
    
    /** Return the component label (as known by the user)*/
    public String getComponentLabel();

    /** Return the component id */
    public String getComponentId();

    /** Return the parameters for this component instance */
    public List getComponentParameters();
    
     /** Return the parameter value for this component instance and the given parameter name */
    public String getComponentParameterValue(String parameterName);

    public String[] getUserAvailComponentIds();

    public String[] getUserAvailSpaceIds();

    public String getComponentUrl();

    /** Return the name of the user's roles */
    public String[] getUserRoles();

    /** Return the higher user's role (admin, publisher or user) */
    public String getUserRoleLevel();

    public ClipboardBm getClipboard();

    public PersonalizationBm getPersonalization();

    public ResourceLocator getMultilang();

    public ResourceLocator getIcon();
    
    public ResourceLocator getSettings();

    // Maintenance Mode
    public boolean isAppInMaintenance();

    public void setAppModeMaintenance(boolean mode);

    public boolean isSpaceInMaintenance(String spaceId);

    public void setSpaceModeMaintenance(String spaceId, boolean mode); 

}
