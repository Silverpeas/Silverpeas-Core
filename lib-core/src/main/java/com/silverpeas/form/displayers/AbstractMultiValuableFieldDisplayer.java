
/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.form.displayers;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;

import com.silverpeas.form.AbstractForm;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.MultiValuableField;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;

/**
 *
 * @author ehugonnet
 * @param <T>
 */
public abstract class AbstractMultiValuableFieldDisplayer<T extends MultiValuableField> implements FieldDisplayer<T> {

  @Override
  public List<String> update(List<FileItem> items, T field, FieldTemplate template,
          PagesContext pageContext) throws FormException {
    String value = FileUploadUtil.getParameter(items, template.getFieldName(), null, pageContext.
            getEncoding());
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES
            && !StringUtil.isDefined(value)) {
      return new ArrayList<String>(0);
    }
    List<String> values = new ArrayList<String>();
    values.add(value);
    if (template.isMultivaluable()) {
      List<String> paramValues =
          FileUploadUtil.getParameterValues(items, template.getFieldName() +
              AbstractForm.REPEATED_FIELD_SEPARATOR, pageContext.getEncoding());
      for (String paramValue : paramValues) {
        if (StringUtil.isDefined(paramValue)) {
          values.add(paramValue);
        }
      }
      // complete list with empty values
      for (int i=values.size(); i<template.getMaximumNumberOfValues(); i++) {
        values.add("");
      }
    }
    return updateValues(values, field, template, pageContext);
  }
  
  @Override
  public List<String> update(String value, T field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    List<String> values = new ArrayList<String>();
    values.add(value);
    return updateValues(values, field, template, pagesContext);
  }

  @Override
  public void index(FullIndexEntry indexEntry, String key, String fieldName, T field,
      String language, boolean stored) {
    if (field != null) {
      String value = field.getStringValue();
      if (value != null) {
        value = value.trim().replaceAll("##", " ");
        indexEntry.addField(key, value, language, stored); // add data in dedicated field
        indexEntry.addTextContent(value, language); // add data in global field
      }
    }
  }
  
  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void display(PrintWriter out, T field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    if (field == null) {
      return;
    }
    String fieldName = template.getFieldName();
    int nbInputsToDisplay = template.getMaximumNumberOfValues();
    
    List<String> values = field.getValues(pageContext.getLanguage());
    
    boolean mandatory = template.isMandatory();
    boolean showMoreFields = false;
    for (int i=0; i<nbInputsToDisplay; i++) {
      String value = "";
      String currentFieldId = fieldName;
      String currentVisibility = AbstractForm.REPEATED_FIELD_CSS_SHOW;
      
      if (i == 0) {
        value = (!field.isNull() ? values.get(0) : getDefaultValue(template, pageContext));
      } else {
        currentFieldId = fieldName+AbstractForm.REPEATED_FIELD_SEPARATOR+i;
        mandatory = false;
        if (values != null && i < values.size()) {
          value = values.get(i);
        } else {
          value = "";
        }
        if (!StringUtil.isDefined(value)) {
          currentVisibility = AbstractForm.REPEATED_FIELD_CSS_HIDE;
          showMoreFields = true;
        }
      }
      if (pageContext.isBlankFieldsUse()) {
        value = "";
      }
            
      out.println("<div id=\"field_"+currentFieldId+"\" class=\"field_"+fieldName+" "+currentVisibility+"\">");
      
      displayInput(currentFieldId, value, mandatory, field, template, pageContext, out);
      
      out.println("</div>");
    }
    if (showMoreFields && !template.isDisabled() && !template.isReadOnly() && !template.isHidden()) {
      Util.printOneMoreInputSnippet(fieldName, pageContext, out);
    }
  }
  
  protected String getDefaultValue(FieldTemplate template, PagesContext pageContext) {
    if (pageContext.isIgnoreDefaultValues()) {
      return "";
    }
    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
    String defaultValue = (parameters.containsKey("default") ? parameters.get("default") : "");
    return defaultValue;
  }
  
  public abstract void displayInput(String inputId, String value, boolean mandatory, T field,
      FieldTemplate template, PagesContext pageContext, PrintWriter out);
}