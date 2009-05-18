package com.stratelia.webactiv.util.indexEngine.model;

import java.io.Serializable;

import com.silverpeas.util.i18n.I18NHelper;

/**
 * A ContentDescription pack all the needed information
 * to parse and index a content.
 *
 * We need :
 * <UL>
 *   <LI>the content itself</LI>
 *   <LI>the language of the file</LI>
 * </UL>
 */
public class TextDescription implements Serializable
{
  public TextDescription(String content, String lang)
  {
    this.content = content;
    
    this.lang = I18NHelper.checkLanguage(lang);; 
  }

  /**
   * Return the content itself
   */
  public String getContent()
  {
    return content;
  }

  /**
   * Return the content language
   */
  public String getLang()
  {
    return lang;
  }

  /**
   * All the attributes are private and final.
   */
  private final String content;
  private final String lang;
}
