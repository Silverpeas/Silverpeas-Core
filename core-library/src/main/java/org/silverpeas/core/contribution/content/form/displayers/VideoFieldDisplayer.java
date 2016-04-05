/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.contribution.content.form.displayers;

import java.io.PrintWriter;
import java.util.Map;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileServerUtils;

/**
 * A displayer of a video. The underlying video player is FlowPlayer
 * (http://flowplayer.org/index.html).
 */
public class VideoFieldDisplayer extends AbstractFileFieldDisplayer {

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
  private static final int DISPLAYED_HTML_OBJECTS = 2;

  @Override
  public void display(PrintWriter out, FileField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    checkFieldType(template.getTypeName(), "VideoFieldDisplayer.display");
    String attachmentId = field.getValue();
    if (!StringUtil.isDefined(attachmentId)) {
      attachmentId = "";
    }
    if (!template.isHidden()) {
      ElementContainer xhtmlContainer = new ElementContainer();
      VideoPlayer videoPlayer = new VideoPlayer();
      videoPlayer.init(xhtmlContainer);
      if (template.isReadOnly()) {
        displayVideo(videoPlayer, attachmentId, template, xhtmlContainer, pagesContext);
      } else if (!template.isDisabled()) {
        displayVideoFormInput(videoPlayer, attachmentId, template, xhtmlContainer, pagesContext, field);
      }

      out.println(xhtmlContainer.toString());
    }
  }


  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return DISPLAYED_HTML_OBJECTS;
  }

  /**
   * Computes the URL of the video to display from the identifier of the attached video file and by
   * taking into account the displaying context.
   *
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
        SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService()
            .searchDocumentById(new SimpleDocumentPK(attachmentId, pageContext.getComponentId()),
            pageContext.getContentLanguage());
        if (attachment != null) {
          String webContext = FileServerUtils.getApplicationContext();
          videoURL = webContext + attachment.getAttachmentURL();
          if (pageContext.isSharingContext()) {
            videoURL = pageContext.getSharingContext().getSharedUriOf(attachment).toString();
          }
        }
      }
    }
    return videoURL;
  }

  /**
   * Displays the video refered by the specified URL into the specified XHTML container.
   *
   * @param videoPlayer the video player to display.
   * @param attachmentId the identifier of the attached file containing the video to display.
   * @param template the template of the field to which is mapped the video.
   * @param xhtmlContainer the XMLHTML container into which the video is displayed.
   */
  private void displayVideo(final VideoPlayer videoPlayer, final String attachmentId,
      final FieldTemplate template, final ElementContainer xhtmlContainer,
      final PagesContext pagesContext) {
    String videoURL = computeVideoURL(attachmentId, pagesContext);
    if (StringUtil.isDefined(videoURL)) {
      Map<String, String> parameters = template.getParameters(pagesContext.getLanguage());
      initVideoPlayer(videoPlayer, videoURL, parameters);
      videoPlayer.renderIn(xhtmlContainer);
    }
  }

  /**
   * Displays the form part corresponding to the video input. The form input is a way to change or
   * to remove the video file if this one exists.
   *
   * @param videoPlayer the video player to display as a form input.
   * @param attachmentId the identifier of the attached file containing the video to display.
   * @param template the template of the field to which is mapped the video.
   * @param pagesContext the context of the displaying page.
   */
  private void displayVideoFormInput(final VideoPlayer videoPlayer,
      final String attachmentId, final FieldTemplate template,
      final ElementContainer xhtmlContainer, final PagesContext pagesContext, FileField field) {
    String fieldName = Util.getFieldOccurrenceName(template.getFieldName(), field.getOccurrence());
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
      initVideoPlayer(videoPlayer, videoURL, parameters);

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
      videoPlayer.renderIn(videoDiv);
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
        attachmentId).setID(fieldName + FileField.PARAM_ID_SUFFIX);
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
   * Initializes the specified video player with the specified URL and the specified parameters.
   *
   * @param videoPlayer the video player to set up.
   * @param videoURL the URL of the video to play.
   * @param parameters the parameters from which the video player will be initialized (height,
   * width, ...)
   */
  private void initVideoPlayer(final VideoPlayer videoPlayer, String videoURL,
      Map<String, String> parameters) {
    String width = (parameters.containsKey(PARAMETER_WIDTH) ? parameters.get(PARAMETER_WIDTH)
        : DEFAULT_WIDTH) + "px";
    String height = (parameters.containsKey(PARAMETER_HEIGHT) ? parameters.get(PARAMETER_HEIGHT)
        : DEFAULT_HEIGHT) + "px";
    boolean autoplay = (parameters.containsKey(PARAMETER_AUTOPLAY)
        ? Boolean.valueOf(parameters.get(PARAMETER_AUTOPLAY)) : false);
    videoPlayer.setVideoURL(videoURL);
    videoPlayer.setAutoplay(autoplay);
    videoPlayer.setWidth(width);
    videoPlayer.setHeight(height);
  }
}
