
/*
 * Aliaksei_Budnikau
 * Date: Oct 14, 2002
 */
package com.silverpeas.interestCenter.ejb;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class DAOException extends SilverpeasException {

    public DAOException(String callingClass, String message) {
        super(callingClass, ERROR, message);
    }

    public DAOException(String callingClass, String message, String extraParams) {
        super(callingClass, ERROR, message, extraParams);
    }

    public DAOException(String callingClass, String message, Exception nested) {
        super(callingClass, ERROR, message, nested);
    }

    public DAOException(String callingClass, String message, String extraParams, Exception nested) {
        super(callingClass, ERROR, message, extraParams, nested);
    }

    public String getModule() {
        return "InterestCenter";
    }

}


