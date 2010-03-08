package com.stratelia.silverpeas.authentication.password;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ForgottenPasswordException extends SilverpeasException {

  private static final long serialVersionUID = -2521215893839712639L;

  public ForgottenPasswordException(String callingClass, String message, Exception nested) 
    {
        super(callingClass, SilverpeasException.ERROR, message, nested);
    }
  
  public ForgottenPasswordException(String callingClass, String message, String extraParams, Exception nested) 
    {
        super(callingClass, SilverpeasException.ERROR, message, extraParams, nested);
    }

  @Override
  public String getModule() {
    return "forgottenPassword";
  }

}