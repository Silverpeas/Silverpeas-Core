/*
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.form.fieldDisplayer;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload.FileItem;
import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;
import org.apache.ecs.xhtml.script;

/**
 * A displayer of a video.
 * The underlying video player is FlowPlayer (http://flowplayer.org/index.html).
 */
public class VideoFieldDisplayer extends AbstractFieldDisplayer {
  
  /**
   * The default width in pixels of the video display area.
   */
  public static final String DEFAULT_WIDTH = "425";
  /**
   * The default height in pixels of the video display area.
   */
  public static final String DEFAULT_HEIGHT = "300";
  /**
   * Should the video auto starts by default?
   */
  public static final boolean DEFAULT_AUTOPLAY = true;
  /**
   * The video display width parameter name.
   */
  public static final String PARAMETER_WIDTH = "width";
  /**
   * The video display height parameter name.
   */
  public static final String PARAMETER_HEIGHT = "height";
  /**
   * The video autostart parameter name.
   */
  public static final String PARAMETER_AUTOPLAY = "autoplay";

  private static final String CONTEXT_FORM_VIDEO = "XMLFormVideo";
  private static final String SWF_PLAYER_PATH = "/util/flowplayer/flowplayer-3.2.4.swf";
  private static final String VIDEO_PLAYER_ID = "player";
  private static final String OPERATION_KEY = "Operation";
  private static final int DISPLAYED_HTML_OBJECTS = 2;
  
  /**
   * The different kinds of operation that can be applied into an attached video file.
   */
  private enum Operation {
    ADD, UPDATE, DELETION;
  }

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{FileField.TYPE};
  }

  @Override
  public void displayScripts(final PrintWriter out, final FieldTemplate template,
          final PagesContext pagesContext) throws IOException {
    checkFieldType(template.getTypeName(), "VideoFieldDisplayer.displayScripts");
    String language = pagesContext.getLanguage();
    String fieldName = template.getFieldName();
    if (template.isMandatory() && pagesContext.useMandatory()) {
      out.append("	if (isWhitespace(stripInitialWhitespace(field.value))) {\n").append("		var ").
              append(fieldName).append("Value = document.getElementById('").append(fieldName).append(
              Field.FILE_PARAM_NAME_SUFFIX).append("').value;\n").append("		if (").append(fieldName).
              append("Value=='' || ").append(fieldName).append(
              "Value.substring(0,7)==\"remove_\") {\n").append("			errorMsg+=\"  - '").append(EncodeHelper.
              javaStringToJsString(template.getLabel(language))).append("' ").append(Util.getString(
              "GML.MustBeFilled", language)).append("\\n \";\n").append("			errorNb++;\n").append(
              "		}\n").append("	}\n");
    }

    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  @Override
  public void display(PrintWriter out, Field field, FieldTemplate template,
          PagesContext pagesContext) throws FormException {
    checkFieldType(template.getTypeName(), "VideoFieldDisplayer.display");
    String attachmentId = field.getValue();
    if (!StringUtil.isDefined(attachmentId)) {
      attachmentId = "";
    }
    if (!template.isHidden()) {
      ElementContainer xhtmlcontainer = new ElementContainer();
      if (template.isReadOnly()) {
        displayVideo(attachmentId, template, xhtmlcontainer, pagesContext);
      } else if (!template.isDisabled()) {
        displayVideoFormInput(attachmentId, template, xhtmlcontainer, pagesContext);
      }
      
      Map<String, String> parameters = template.getParameters(pagesContext.getLanguage());
      boolean autoplay = (parameters.containsKey(PARAMETER_AUTOPLAY) ? 
        Boolean.valueOf(parameters.get(PARAMETER_AUTOPLAY)): false);
      String playerPath = FileServerUtils.getApplicationContext() + SWF_PLAYER_PATH;
      script js = new script("flowplayer('" + VIDEO_PLAYER_ID + "', '" + playerPath +
              "', {clip: { autoBuffering: " + !autoplay + ", autoPlay: " + autoplay + " } });");
      js.setLanguage("javascript");
      js.setType("text/javascript");
      xhtmlcontainer.addElement(js);
      out.println(xhtmlcontainer.toString());
    }
  }

  @Override
  public List<String> update(String attachmentId, Field field, FieldTemplate template,
          PagesContext PagesContext) throws FormException {
    checkFieldType(field.getTypeName(), "VideoFieldDisplayer.update");
    List<String> attachmentIds = new ArrayList<String>();
    
    if (!StringUtil.isDefined(attachmentId)) {
      field.setNull();
    } else {
      ((FileField) field).setAttachmentId(attachmentId);
      attachmentIds.add(attachmentId);
    }
    return attachmentIds;
  }

  @Override
  public List<String> update(List<FileItem> items, Field field, FieldTemplate template,
          PagesContext pageContext) throws FormException {
    List<String> attachmentIds = new ArrayList<String>();
    
    try {
      String fieldName = template.getFieldName();
      String attachmentId = uploadVideoFile(items, fieldName, pageContext);
      Operation operation = Operation.valueOf(FileUploadUtil.getParameter(items,
              fieldName + OPERATION_KEY));
      String currentAttachmentId = FileUploadUtil.getParameter(items, fieldName
              + Field.FILE_PARAM_NAME_SUFFIX);
      if (isDeletion(operation, currentAttachmentId) || isUpdate(operation, attachmentId)) {
        deleteAttachment(currentAttachmentId, pageContext);
      }
      if (StringUtil.isDefined(attachmentId)) {
        attachmentIds.addAll(update(attachmentId, field, template, pageContext));
      }
    } catch (Exception ex) {
      SilverTrace.error("form", "VideoFieldDisplayer.update", "form.EXP_UNKNOWN_FIELD", null, ex);
    }
    
    return attachmentIds;
  }

  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return DISPLAYED_HTML_OBJECTS;
  }

  /**
   * Checks the type of the field is as expected.
   * The field must be of type file.
   * @param typeName the name of the type.
   * @param contextCall the context of the call: which is the caller of this method. This parameter
   * is used for trace purpose.
   */
  private void checkFieldType(final String typeName, final String contextCall) {
    if (!Field.TYPE_FILE.equals(typeName)) {
      SilverTrace.info("form", contextCall, "form.INFO_NOT_CORRECT_TYPE", Field.TYPE_FILE);
    }
  }

  /**
   * Computes the URL of the video to display from the identifier of the attached video file and by
   * taking into account the displaying context.
   * @param attachmentId the identifier of the attached video file.
   * @param pageContext the displaying page context.
   * @return the URL of the video file as String.
   */
  private String computeVideoURL(final String attachmentId, final PagesContext pageContext) {
    String videoURL = "";
    if (StringUtil.isDefined(attachmentId)) {
      if (attachmentId.startsWith("/")) {
        videoURL = attachmentId;
      } else {
        AttachmentDetail attachment =
                AttachmentController.searchAttachmentByPK(new AttachmentPK(attachmentId,
                pageContext.getComponentId()));
        if (attachment != null) {
          String webContext = FileServerUtils.getApplicationContext();
          videoURL = webContext + attachment.getAttachmentURL();
        }
      }
    }
    return videoURL;
  }

  /**
   * Displays the video refered by the specified URL into the specified XHTML container.
   * @param attachmentId the identifier of the attached file containing the video to display.
   * @param template the template of the field to which is mapped the video.
   * @param xhtmlcontainer the XMLHTML container into which the video is displayed.
   */
  private void displayVideo(final String attachmentId, final FieldTemplate template,
          final ElementContainer xhtmlcontainer, final PagesContext pagesContext) {
    String videoURL = computeVideoURL(attachmentId, pagesContext);
    if (!videoURL.isEmpty()) {
      Map<String, String> parameters = template.getParameters(pagesContext.getLanguage());
      Element videoLink = createVideoElement(videoURL, parameters);
      xhtmlcontainer.addElement(videoLink);
    }
  }

  /**
   * Displays the form part corresponding to the video input.
   * The form input is a way to change or to remove the video file if this one exists.
   * @param attachmentId the identifier of the attached file containing the video to display.
   * @param template the template of the field to which is mapped the video.
   * @param pagesContext the context of the displaying page.
   */
  private void displayVideoFormInput(final String attachmentId, final FieldTemplate template,
          final ElementContainer xhtmlContainer, final PagesContext pagesContext) {
    String fieldName = template.getFieldName();
    String language = pagesContext.getLanguage();
    String deletionIcon = Util.getIcon("delete");
    String deletionLab = Util.getString("removeFile", language);
    String videoURL = computeVideoURL(attachmentId, pagesContext);
    Operation defaultOperation = Operation.ADD;

    if (!videoURL.isEmpty()) {
      defaultOperation = Operation.UPDATE;
      Map<String, String> parameters = template.getParameters(pagesContext.getLanguage());
      parameters.remove(PARAMETER_WIDTH);
      parameters.remove(PARAMETER_HEIGHT);
      // a link to the video
      Element videoLink = createVideoElement(videoURL, parameters);

      // a link to the deletion operation
      img deletionImage = new img();
      deletionImage.setAlt(deletionLab).setSrc(deletionIcon).setWidth(15).setHeight(15).setAlt(
              deletionLab).setTitle(deletionLab);
      a removeLink = new a();
      removeLink.setHref("#")
              .addElement(deletionImage)
              .setOnClick("javascript: document.getElementById('" + fieldName
              + "Video').style.display='none'; document." + pagesContext.getFormName() + "."
              + fieldName + OPERATION_KEY + ".value='" + Operation.DELETION.name() + "';");
      div videoDiv = new div();
      videoDiv.setID(fieldName + "Video");
      videoDiv.setClass("video");
      videoDiv.addElement(videoLink);
      videoDiv.addElement("&nbsp;");
      videoDiv.addElement(removeLink);
      
      xhtmlContainer.addElement(videoDiv);
    }

    // the input from which a video file can be selected
    input fileInput = new input();
    fileInput.setID(fieldName);
    fileInput.setType("file");
    fileInput.setSize(50);
    fileInput.setName(fieldName);
    input attachmentInput = new input();
    attachmentInput.setType("hidden").setName(fieldName + Field.FILE_PARAM_NAME_SUFFIX).setValue(
            attachmentId).setID(fieldName + "Hidden");
    input operationInput = new input();
    operationInput.setType("hidden").setName(fieldName + OPERATION_KEY).setValue(defaultOperation.
            name()).setID(fieldName + OPERATION_KEY);
    div selectionDiv = new div();
    selectionDiv.setID(fieldName + "Selection");
    selectionDiv.addElement(fileInput);
    selectionDiv.addElement(attachmentInput);
    selectionDiv.addElement(operationInput);
    if (template.isMandatory() && pagesContext.useMandatory()) {
      selectionDiv.addElement(Util.getMandatorySnippet());
    }
    xhtmlContainer.addElement(selectionDiv);
  }
  
  /**
   * Creates a XHTML element that refers the video identified by the specified URL.
   * @param videoURL the URL of the video to display.
   * @return the XHTML element that displays the video.
   */
  private Element createVideoElement(final String videoURL, final Map<String, String> parameters) {
    a videoElement = new a();
    String width = (parameters.containsKey(PARAMETER_WIDTH) ? parameters.get(PARAMETER_WIDTH):
      DEFAULT_WIDTH);
    String height = (parameters.containsKey(PARAMETER_HEIGHT) ? parameters.get(PARAMETER_HEIGHT):
      DEFAULT_HEIGHT);
    videoElement.setStyle("display:block;width:" + width + "px;height:" + height + "px;");
    videoElement.setHref(videoURL).setID(VIDEO_PLAYER_ID);
    return videoElement;
  }

  /**
   * Uploads the file containing the video and that is identified by the specified field name.
   * @param items the items of the form. One of them is containg the video file.
   * @param itemKey the key of the item containing the video.
   * @param pageContext the context of the page displaying the form.
   * @throws Exception if an error occurs while uploading the video file.
   * @return the identifier of the uploaded attached video file.
   */
  private String uploadVideoFile(final List<FileItem> items, final String itemKey,
          final PagesContext pageContext) throws Exception {
    String attachmentId = "";

    FileItem item = FileUploadUtil.getFile(items, itemKey);
    if (!item.isFormField()) {
      String componentId = pageContext.getComponentId();
      String userId = pageContext.getUserId();
      String objectId = pageContext.getObjectId();
      String logicalName = item.getName();
      long size = item.getSize();
      if (StringUtil.isDefined(logicalName) && size > 0) {
        if (!FileUtil.isWindows()) {
          logicalName = logicalName.replace('\\', File.separatorChar);
          SilverTrace.info("form", "VideoFieldDisplayer.uploadVideoFile", "root.MSG_GEN_PARAM_VALUE",
                  "fullFileName on Unix = " + logicalName);
        }

        logicalName =
                logicalName.substring(logicalName.lastIndexOf(File.separator) + 1, logicalName.
                length());
        String type = FileRepositoryManager.getFileExtension(logicalName);
        String mimeType = item.getContentType();
        String physicalName = new Long(new Date().getTime()).toString() + "." + type;
        File dir = getVideoPath(componentId, physicalName);
        item.write(dir);
        AttachmentDetail attachmentDetail =
                createAttachmentDetail(objectId, componentId, physicalName, logicalName, mimeType,
                size,
                VideoFieldDisplayer.CONTEXT_FORM_VIDEO, userId);
        attachmentDetail = AttachmentController.createAttachment(attachmentDetail, true);
        attachmentId = attachmentDetail.getPK().getId();
      }
    }
    
    return attachmentId;
  }

  /**
   * Is the specified operation is a deletion?
   * @param operation the operation.
   * @param attachmentId the identifier of the attachment on which the operation is.
   * @return true if the operation is a deletion, false otherwise.
   */
  private boolean isDeletion(final Operation operation, final String attachmentId) {
    return StringUtil.isDefined(attachmentId) && operation == Operation.DELETION;
  }

  /**
   * Is the specified operation is an update?
   * @param operation the operation.
   * @param attachmentId the identifier of the attachment on which the operation is.
   * @return true if the operation is an update, false otherwise.
   */
  private boolean isUpdate(final Operation operation, final String attachmentId) {
    return StringUtil.isDefined(attachmentId) && operation == Operation.UPDATE;
  }
  
  /**
   * Is the specified operation is an add?
   * @param operation the operation.
   * @param attachmentId the identifier of the attachment on which the operation is.
   * @return true if the operation is an add, false otherwise.
   */
  private boolean isAdd(final Operation operation, final String attachmentId) {
    return StringUtil.isDefined(attachmentId) && operation == Operation.ADD;
  }

  /**
   * Deletes the specified attachment, identified by its unique identifier.?
   * @param attachmentId the unique identifier of the attachment to delete.
   * @param pageContext the context of the page.
   */
  private void deleteAttachment(String attachmentId, PagesContext pageContext) {
    SilverTrace.info("form", "VideoFieldDisplayer.deleteAttachment", "root.MSG_GEN_ENTER_METHOD",
        "attachmentId = " + attachmentId + ", componentId = " + pageContext.getComponentId());
    AttachmentPK pk = new AttachmentPK(attachmentId, pageContext.getComponentId());
    AttachmentController.deleteAttachment(pk);
  }

  /**
   * Gets the path of the physical file into which the video will be saved.
   * The path of the file depends on the Silverpeas components for which the video will be
   * uploaded.
   * @param componentId the identifier of the component.
   * @param physicalName the physical name of the video file; the name of the file into which the
   * video will be saved in Silverpeas side.
   * @return the File object that represents the physical video file.
   */
  private File getVideoPath(String componentId, String physicalName) {
    String path = AttachmentController.createPath(componentId, CONTEXT_FORM_VIDEO);
    return new File(path + physicalName);
  }

  /**
   * Creates details about the uploaded attached video file.
   * @param objectId
   * @param componentId the identifier of the component for which the video is uploaded.
   * @param physicalName the name of the physical file in which the video will be stored.
   * @param logicalName the logical name of the video file (name in the upload form).
   * @param mimeType the MIME type of the video (video/flv, ...)
   * @param size the size of the video.
   * @param contextVideo the upload context.
   * @param userId the identifier of the user that is uploading the video.
   * @return an AttachmentDetail object.
   */
  private AttachmentDetail createAttachmentDetail(String objectId, String componentId,
          String physicalName, String logicalName, String mimeType, long size,
          String contextVideo, String userId) {
    // create AttachmentPK with spaceId and componentId
    AttachmentPK atPK = new AttachmentPK(null, "useless", componentId);

    // create foreignKey with spaceId, componentId and id
    // use AttachmentPK to build the foreign key of customer object.
    AttachmentPK foreignKey = new AttachmentPK("-1", "useless", componentId);
    if (objectId != null) {
      foreignKey.setId(objectId);
    }

    // create AttachmentDetail Object
    AttachmentDetail attachmentDetail =
            new AttachmentDetail(atPK, physicalName, logicalName, null, mimeType, size, contextVideo,
            new Date(), foreignKey);
    attachmentDetail.setAuthor(userId);

    return attachmentDetail;
  }
}
