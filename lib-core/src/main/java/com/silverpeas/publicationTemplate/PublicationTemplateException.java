package com.silverpeas.publicationTemplate;

import com.stratelia.webactiv.util.exception.*;

/**
 * Thrown by the form components.
 */
public class PublicationTemplateException extends SilverpeasException
{
  /**
   * Returns the module name (as known by SilverTrace).
   */
  public String getModule()
  {
      return "form";
  }

  /**
   * Set the caller and the error message
   */
  public PublicationTemplateException(String caller,
                       String message)
  {
    super(caller, SilverpeasException.ERROR, message);
  }

  /**
   * Set the caller, the error message and the nested exception.
   */
  public PublicationTemplateException(String caller,
                       String message,
                       Exception nestedException)
  {
    super(caller, SilverpeasException.ERROR, message, nestedException);
  }

  /**
   * Set the caller, infos and the error message
   */
  public PublicationTemplateException(String caller,
                       String message,
                       String infos)
  {
    super(caller, SilverpeasException.ERROR, message, infos);
  }

  /**
   * Set the caller, the error message, infos and the nested exception.
   */
  public PublicationTemplateException(String caller,
                       String message,
                       String infos,
                       Exception nestedException)
  {
    super(caller, SilverpeasException.ERROR, message, infos, nestedException);
  }
}
