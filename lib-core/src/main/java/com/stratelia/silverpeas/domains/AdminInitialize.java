/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.domains;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class AdminInitialize implements IInitialize
{

    /**
     * Constructor declaration
     * 
     * 
     * @see
     */
    public AdminInitialize() {}

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @see
     */
    public boolean Initialize()
    {
        // Initialize SilverTrace
        AdminController ac = new AdminController("");
        try
        {
            ac.startServer();
        }
        catch (Exception e)
        {
			SilverTrace.error("admin", "AdminInitialize.Initialize()", "admin.MSG_ERR_GET_DOMAIN", e);
            return false;
        }
        return true;
    }

}
