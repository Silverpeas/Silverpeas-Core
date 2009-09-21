package com.silverpeas.publicationTemplate;

import com.silverpeas.form.Form;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.RecordTemplate;


/**
 * A PublicationTemplate describes a set of publication records
 * built on a same template.
 * 
 * A PublicationTemplate groups :
 * <OL>
 * <LI> a RecordTemplate which describes the built records. 
 * <LI> a RecordSet of records built on this template,
 * <LI> an update Form used to create and update the publication items
 * <LI> a view Form used to show the publications.
 * </OL>
 */
public interface PublicationTemplate
{
  
  /**
   * Returns the RecordTemplate of the publication data item.
   */
  public RecordTemplate getRecordTemplate()
     throws PublicationTemplateException;
  
  /**
   * Returns the RecordSet of all the records built from this template.
   */
  public RecordSet getRecordSet()
     throws PublicationTemplateException;

  /**
   * Returns the Form used to create and update
   * the records built from this template.
   */
  public Form getUpdateForm()
     throws PublicationTemplateException;

  /**
   * Returns the Form used to view
   * the records built from this template.
   */
  public Form getViewForm()
     throws PublicationTemplateException;     
  
  /**
   * Returns the Form used to search 
   * the records built from this template.
   */
  public Form getSearchForm()
     throws PublicationTemplateException;

  
  public void setExternalId(String externalId);
  public Form getEditForm(String name)
	 throws PublicationTemplateException;     
  public String getExternalId();
  
  public String getName();
  public String getDescription();
  public String getThumbnail();
  public String getFileName();
  public boolean isVisible();
  
  public boolean isSearchable();
}
