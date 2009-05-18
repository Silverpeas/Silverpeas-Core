package com.stratelia.webactiv.util.indexEngine.model;

import java.io.Serializable;
import java.util.Date;

import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.indexEngine.DateFormatter;

/**
 * A FieldDescription pack all the needed information
 * to parse and index a generic field (xml field, iptc field)
 *
 * We need :
 * <UL>
 * 	 <LI>the name of the field</LI>
 *   <LI>its content</LI>
 *   <LI>its language</LI>
 * </UL>
 */
public class FieldDescription implements Serializable
{
  public FieldDescription(String fieldName, String content, String lang)
  {
	  this.content = content;
	  this.lang = I18NHelper.checkLanguage(lang);
	  this.fieldName = fieldName;
  }
  
  public FieldDescription(String fieldName, Date begin, Date end, String lang)
  {
	  String content = "";
	  if (begin != null && end != null)
		  content = "["+DateFormatter.date2IndexFormat(begin)+" TO "+DateFormatter.date2IndexFormat(end)+"]";
	  else if (begin != null && end == null)
		  content = "["+DateFormatter.date2IndexFormat(begin)+" TO "+DateFormatter.nullEndDate+"]";
	  else if (begin == null && end != null)
		  content = "["+DateFormatter.nullBeginDate+" TO "+DateFormatter.date2IndexFormat(end)+"]";
	  
	  this.content = content;
	  this.lang = I18NHelper.checkLanguage(lang);
	  this.fieldName = fieldName;
  }

  /**
   * Return the fieldName
   */
  public String getFieldName()
  {
    return fieldName;
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
  private final String fieldName;
}
