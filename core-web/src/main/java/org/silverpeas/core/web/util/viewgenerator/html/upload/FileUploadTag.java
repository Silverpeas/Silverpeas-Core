/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.upload;

import org.silverpeas.core.util.URLUtil;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.MultiPartElement;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.fieldset;
import org.apache.ecs.xhtml.legend;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Locale;

public class FileUploadTag extends TagSupport {
  private static final long serialVersionUID = -7065381733362836565L;

  public static final String FILE_UPLOAD_ATT = "@FileUploadTag@";
  public static final String FILE_UPLOAD_CONTEXT = FILE_UPLOAD_ATT + "@FileUploadContext@";

  public static final String DEFAULT_FILE_UPLOAD_ID = "fileUpload";

  private boolean fieldset = false;
  private String title = "";
  private boolean multiple = true;
  private boolean dragAndDropDisplay = true;
  private String jqueryFormSelector = "";
  private Integer nbFileLimit = 0;
  private FileUploadContext fileUploadContext = null;

  public boolean isFieldset() {
    return fieldset;
  }

  public void setFieldset(final boolean fieldset) {
    this.fieldset = fieldset;
  }

  public String getTitle() {
    return (StringUtil.isDefined(title) ? title :
        fileUploadContext.generalBundle.getString("GML.attachments"));
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public boolean isMultiple() {
    return multiple;
  }

  public void setMultiple(final boolean multiple) {
    this.multiple = multiple;
  }

  public boolean isDragAndDropDisplay() {
    return dragAndDropDisplay;
  }

  public void setDragAndDropDisplay(final boolean dragAndDropDisplay) {
    this.dragAndDropDisplay = dragAndDropDisplay;
  }

  public String getJqueryFormSelector() {
    return jqueryFormSelector;
  }

  public void setJqueryFormSelector(final String jqueryFormSelector) {
    this.jqueryFormSelector = jqueryFormSelector;
  }

  public Integer getNbFileLimit() {
    return nbFileLimit;
  }

  public void setNbFileLimit(final Integer nbFileLimit) {
    this.nbFileLimit = nbFileLimit;
  }

  @Override
  public String getId() {
    String id = super.getId();
    return (StringUtil.isDefined(id) ? id : DEFAULT_FILE_UPLOAD_ID);
  }

  @Override
  public int doStartTag() throws JspException {
    try {
      performContext();
      ElementContainer xhtmlcontainer = new ElementContainer();
      performJsPlugin(xhtmlcontainer);
      performBloc(xhtmlcontainer);
      xhtmlcontainer.output(pageContext.getOut());
      // Evaluate body
      return EVAL_BODY_INCLUDE;
    } catch (final Exception e) {
      throw new JspException("FileUpload tag", e);
    }
  }

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  /**
   * Create bloc
   */
  private void performBloc(ElementContainer xhtmlcontainer) {
    MultiPartElement container = (isFieldset()) ? new fieldset() : new div();
    container.setID(getId());
    if (isFieldset()) {
      container.setClass("fileUpload skinFieldset");
      container.addElementToRegistry(new legend().addElement(getTitle()));
    } else {
      container.setClass("fileUpload");
    }
    xhtmlcontainer.addElement(container);
  }

  /**
   * Add JavascriptFiles
   */
  private void performJsPlugin(ElementContainer xhtmlcontainer) {
    if (!fileUploadContext.jsPluginLoaded) {
      fileUploadContext.jsPluginLoaded = true;
      JavascriptPluginInclusion.includeIFrameAjaxTransport(xhtmlcontainer);
      script jsPlugin = new script().setType("text/javascript").
          setSrc(URLUtil.getApplicationURL() + "/util/javaScript/silverpeas-fileUpload.js");
      xhtmlcontainer.addElement(jsPlugin);
      StringBuilder jQueryStart = new StringBuilder();
      jQueryStart.append("jQuery(document).ready(function(){jQuery('.fileUpload').fileUpload({");
      jQueryStart.append("multiple:");
      jQueryStart.append(isMultiple());
      jQueryStart.append(",dragAndDropDisplay:");
      jQueryStart.append(isDragAndDropDisplay());
      jQueryStart.append(",jqueryFormSelector:\"");
      jQueryStart.append(getJqueryFormSelector());
      jQueryStart.append("\",nbFileLimit:");
      jQueryStart.append(getNbFileLimit());
      jQueryStart.append(",labels:{");
      jQueryStart.append("browse:\"");
      jQueryStart.append(fileUploadContext.generalBundle.getString("GML.upload.choose.browse"));
      jQueryStart.append("\",chooseFile:\"");
      jQueryStart.append(fileUploadContext.generalBundle.getString("GML.upload.choose.file"));
      jQueryStart.append("\",chooseFiles:\"");
      jQueryStart.append(fileUploadContext.generalBundle.getString("GML.upload.choose.files"));
      jQueryStart.append("\",dragAndDropFile:\"");
      jQueryStart.append(fileUploadContext.generalBundle.getString("GML.upload.dragAndDrop.file"));
      jQueryStart.append("\",dragAndDropFiles:\"");
      jQueryStart.append(fileUploadContext.generalBundle.getString("GML.upload.dragAndDrop.files"));
      jQueryStart.append("\",sendingFile:\"");
      jQueryStart.append(
          fileUploadContext.generalBundle.getStringWithParams("GML.upload.sending.file", "@name@"));
      jQueryStart.append("\",sendingFiles:\"");
      jQueryStart.append(fileUploadContext.generalBundle
          .getStringWithParams("GML.upload.sending.files", "@number@"));
      jQueryStart.append("\",sendingWaitingWarning:\"");
      jQueryStart.append(fileUploadContext.generalBundle.getString("GML.upload.warning"));
      jQueryStart.append("\",limitFileWarning:\"");
      jQueryStart
          .append(fileUploadContext.generalBundle.getString("GML.upload.warning.file.limit"));
      jQueryStart.append("\",limitFilesWarning:\"");
      jQueryStart.append(fileUploadContext.generalBundle
          .getStringWithParams("GML.upload.warning.files.limit", "@number@"));
      jQueryStart.append("\",limitFileReached:\"");
      jQueryStart.append(
          fileUploadContext.generalBundle.getString("GML.upload.warning.file.limit.reached"));
      jQueryStart.append("\",limitFilesReached:\"");
      jQueryStart.append(fileUploadContext.generalBundle
          .getStringWithParams("GML.upload.warning.files.limit.reached", "@number@"));
      jQueryStart.append("\",title:\"");
      jQueryStart.append(fileUploadContext.generalBundle.getString("GML.title"));
      jQueryStart.append("\",description:\"");
      jQueryStart.append(fileUploadContext.generalBundle.getString("GML.description"));
      jQueryStart.append("\",deleteFile:\"");
      jQueryStart.append(fileUploadContext.generalBundle.getString("GML.delete"));
      jQueryStart.append("\"}");
      jQueryStart.append("});});");
      script startJsPlugin =
          new script().setType("text/javascript").addElement(jQueryStart.toString());
      xhtmlcontainer.addElement(startJsPlugin);
    }
  }

  /**
   * Initialize or retrieve file upload context.
   */
  private void performContext() {
    fileUploadContext = (FileUploadContext) pageContext.getAttribute(FILE_UPLOAD_CONTEXT);
    if (fileUploadContext == null) {
      fileUploadContext = new FileUploadContext();
      pageContext.setAttribute(FILE_UPLOAD_CONTEXT, fileUploadContext);

      // Language
      String language = null;
      final Locale locale = (Locale) Config.find(pageContext, Config.FMT_LOCALE);
      if (locale != null) {
        language = locale.getLanguage();
      }
      fileUploadContext.language =
          StringUtil.isDefined(language) ? language : I18NHelper.defaultLanguage;
      fileUploadContext.generalBundle =
          ResourceLocator.getGeneralLocalizationBundle(fileUploadContext.language);
    }
    pageContext.setAttribute(FILE_UPLOAD_ATT, this);
  }

  /**
   * File upload context.
   */
  private class FileUploadContext {
    public boolean jsPluginLoaded = false;
    public String language = null;
    public LocalizationBundle generalBundle = null;
  }
}