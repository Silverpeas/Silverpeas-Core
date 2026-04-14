/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.upload;

import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.MultiPartElement;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.fieldset;
import org.apache.ecs.xhtml.legend;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.jstl.core.Config;
import jakarta.servlet.jsp.tagext.TagSupport;
import java.util.Locale;

import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.script;

public class FileUploadTag extends TagSupport {
  private static final long serialVersionUID = -7065381733362836565L;

  public static final String FILE_UPLOAD_ATT = "@FileUploadTag@";
  public static final String FILE_UPLOAD_CONTEXT = FILE_UPLOAD_ATT + "@FileUploadContext@";

  public static final String DEFAULT_FILE_UPLOAD_ID = "fileUpload";
  private static final String NUMBER = "@number@";

  private boolean fieldset = false;
  private String title = "";
  private boolean multiple = true;
  private boolean infoInputs = true;
  private boolean dragAndDropDisplay = true;
  private String jqueryFormSelector = "";
  private Integer nbFileLimit = 0;
  private transient FileUploadContext fileUploadContext = null;

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

  public boolean isInfoInputs() {
    return infoInputs;
  }

  public void setInfoInputs(final boolean infoInputs) {
    this.infoInputs = infoInputs;
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

  /**
   * Create bloc
   */
  private void performBloc(ElementContainer xhtmlcontainer) {
    MultiPartElement container = (isFieldset()) ? new fieldset() : new div();
    container.setID(getId());
    if (isFieldset()) {
      container.setClass("fileUpload skinFieldset fileUpload-tag");
      container.addElementToRegistry(new legend().addElement(getTitle()));
    } else {
      container.setClass("fileUpload fileUpload-tag");
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
      final Element jsPlugin = script(
          URLUtil.getApplicationURL() + "/util/javaScript/silverpeas-fileUpload.js");
      xhtmlcontainer.addElement(jsPlugin);
      String jQueryStart = "jQuery(document).ready(function(){jQuery('.fileUpload-tag').fileUpload({" +
                           "multiple:" + isMultiple() +
                           ",infoInputs:" + isInfoInputs() +
                           ",dragAndDropDisplay:" + isDragAndDropDisplay() +
                           ",jqueryFormSelector:\"" + getJqueryFormSelector() +
                           "\",nbFileLimit:" + getNbFileLimit() +
                           ",labels:{" +
                           "browse:\"" +
                           fileUploadContext.generalBundle.getString("GML.upload.choose.browse") +
                           "\",chooseFile:\"" +
                           fileUploadContext.generalBundle.getString("GML.upload.choose.file") +
                           "\",chooseFiles:\"" +
                           fileUploadContext.generalBundle.getString("GML.upload.choose.files") +
                           "\",dragAndDropFile:\"" +
                           fileUploadContext.generalBundle.getString("GML.upload.dragAndDrop.file") +
                           "\",dragAndDropFiles:\"" +
                           fileUploadContext.generalBundle.getString("GML.upload.dragAndDrop.files") +
                           "\",sendingFile:\"" +
                           fileUploadContext.generalBundle.getStringWithParams("GML.upload.sending.file", "@name@") +
                           "\",sendingFiles:\"" +
                           fileUploadContext.generalBundle
              .getStringWithParams("GML.upload.sending.files", NUMBER) +
                           "\",sendingWaitingWarning:\"" +
                           fileUploadContext.generalBundle.getString("GML.upload.warning") +
                           "\",limitFileWarning:\"" +
                           fileUploadContext.generalBundle.getString("GML.upload.warning.file.limit") +
                           "\",limitFilesWarning:\"" +
                           fileUploadContext.generalBundle
              .getStringWithParams("GML.upload.warning.files.limit", NUMBER) +
                           "\",limitFileReached:\"" +
                           fileUploadContext.generalBundle.getString("GML.upload.warning.file.limit.reached") +
                           "\",limitFilesReached:\"" +
                           fileUploadContext.generalBundle
              .getStringWithParams("GML.upload.warning.files.limit.reached", NUMBER) +
                           "\",title:\"" +
                           fileUploadContext.generalBundle.getString("GML.title") +
                           "\",description:\"" +
                           fileUploadContext.generalBundle.getString("GML.description") +
                           "\",deleteFile:\"" +
                           fileUploadContext.generalBundle.getString("GML.delete") +
                           "\"}" +
                           "});});";
      script startJsPlugin =
          new script().setType("text/javascript").addElement(jQueryStart);
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
          StringUtil.isDefined(language) ? language : I18NHelper.getDefaultLanguage();
      fileUploadContext.generalBundle =
          ResourceLocator.getGeneralLocalizationBundle(fileUploadContext.language);
    }
    pageContext.setAttribute(FILE_UPLOAD_ATT, this);
  }

  /**
   * File upload context.
   */
  private static class FileUploadContext {
    public boolean jsPluginLoaded = false;
    public String language = null;
    public LocalizationBundle generalBundle = null;
  }
}