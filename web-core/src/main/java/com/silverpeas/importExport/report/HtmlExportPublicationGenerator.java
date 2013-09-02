/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.importExport.report;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.b;
import org.apache.ecs.xhtml.body;
import org.apache.ecs.xhtml.br;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.h1;
import org.apache.ecs.xhtml.head;
import org.apache.ecs.xhtml.html;
import org.apache.ecs.xhtml.i;
import org.apache.ecs.xhtml.li;
import org.apache.ecs.xhtml.link;
import org.apache.ecs.xhtml.meta;
import org.apache.ecs.xhtml.p;
import org.apache.ecs.xhtml.ul;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.importExport.model.PublicationType;
import com.silverpeas.publication.importExport.DBModelContentType;
import com.silverpeas.publication.importExport.XMLModelContentType;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import org.silverpeas.importExport.attachment.AttachmentDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * Classe générant le code html d'une publication exportée
 * @author sdevolder
 */
public class HtmlExportPublicationGenerator {

  // Variables
  private final PublicationDetail publicationDetail;
  private DBModelContentType dbModelContent;
  private XMLModelContentType xmlModelContent;
  private ModelDetail modelDetail;
  private String wysiwygText;
  private List<AttachmentDetail> listAttDetail;
  private final String urlPub;
  private final int nbThemes;

  public HtmlExportPublicationGenerator(PublicationType publicationType, ModelDetail modelDetail,
      String wysiwygText, String urlPub, int nbThemes) {
    this.publicationDetail = publicationType.getPublicationDetail();
    if (publicationType.getPublicationContentType() != null) {
      this.dbModelContent = publicationType.getPublicationContentType().getDBModelContentType();
      this.xmlModelContent = publicationType.getPublicationContentType().getXMLModelContentType();
    }
    this.nbThemes = nbThemes + 2;
    if (publicationType.getAttachmentsType() != null) {
      this.listAttDetail = publicationType.getAttachmentsType().getListAttachmentDetail();
    }
    this.modelDetail = modelDetail;
    this.wysiwygText = wysiwygText;
    this.urlPub = StringEscapeUtils.escapeHtml(urlPub).replaceAll("#", "%23");
  }

  /**
   * Display header of publication
   * @return
   */
  public String toHtmlSommairePublication() {
    return toHtmlSommairePublication(null);
  }

  /**
   * Display header of publication
   * @param target : name of iframe destination
   * @return
   */
  public String toHtmlSommairePublication(String target) {
    ElementContainer xhtmlcontainer = new ElementContainer();
    String htmlPubName = HtmlExportGenerator.encode(publicationDetail.getName());
    String htmlPubDescription = HtmlExportGenerator.encode(publicationDetail.getDescription());
    String htmlCreatorName = HtmlExportGenerator.encode(publicationDetail.getCreatorName());
    String dateString = DateUtil.dateToString(publicationDetail.getCreationDate(), "fr");
    li element = new li();
    a link = new a();
    link.setHref(urlPub);
    if (StringUtil.isDefined(target)) {
      link.setTarget(target);
    }
    link.addElement(new b(htmlPubName));
    element.addElement(link);
    if (StringUtil.isDefined(htmlCreatorName)) {
      element.addElement(" - ");
      element.addElement(htmlCreatorName);
    }
    if (StringUtil.isDefined(dateString)) {
      element.addElement(" (");
      element.addElement(dateString);
      element.addElement(")");
    }
    if (StringUtil.isDefined(htmlPubDescription)) {
      element.addElement(new br());
      element.addElement(new i(htmlPubDescription));
    }
    xhtmlcontainer.addElement(element);
    return xhtmlcontainer.toString();
  }

  /**
   * @return
   */
  String toHtmlEnTetePublication() {
    String htmlPubName = HtmlExportGenerator.encode(publicationDetail.getName());
    String htmlCreatorName = HtmlExportGenerator.encode(publicationDetail.getCreatorName());
    String dateString = DateUtil.dateToString(publicationDetail.getCreationDate(), "fr");
    ElementContainer xhtmlcontainer = new ElementContainer();
    h1 title = new h1(htmlPubName);
    xhtmlcontainer.addElement(title);
    div creationDetail;
    if (StringUtil.isDefined(dateString)) {
      creationDetail = new div(htmlCreatorName + " - " + dateString);
    } else {
      creationDetail = new div(htmlCreatorName);
    }
    creationDetail.setClass("creationDetail");
    xhtmlcontainer.addElement(creationDetail);
    return xhtmlcontainer.toString();
  }

  /**
   * @return
   */
  private String toHtmlInfoModel() {
    StringBuilder sb = new StringBuilder();
    String toParse = modelDetail.getHtmlDisplayer();

    List<String> textList = dbModelContent.getListTextParts();
    List<String> imageList = dbModelContent.getListImageParts();

    Iterator<String> textIterator = textList.iterator();
    Iterator<String> imageIterator = imageList.iterator();

    int posit = toParse.indexOf("%WA");
    while (posit != -1) {
      if (posit > 0) {
        sb.append(toParse.substring(0, posit));
        toParse = toParse.substring(posit);
      }
      if (toParse.startsWith("%WATXTDATA%")) {
        if (textIterator.hasNext()) {
          String textPart = textIterator.next();
          sb.append(HtmlExportGenerator.encode(textPart));
        }
        toParse = toParse.substring(11);
      } else if (toParse.startsWith("%WAIMGDATA%")) {
        if (imageIterator.hasNext()) {
          String imagePath = imageIterator.next();
          String imageName = imagePath.substring(imagePath.lastIndexOf(File.separatorChar) + 1,
              imagePath.length());
          if (FileUtil.isImage(imageName)) {
            sb.append("<img border='0' src='").append(imageName).append("' />");
          } else {
            sb.append("<b>FileNotImage</b>");
          }
        }
        toParse = toParse.substring(11);
      }
      posit = toParse.indexOf("%WA");
    }
    sb.append(toParse);
    return sb.toString();
  }

  public String xmlFormToHTML() {
    PublicationTemplateImpl template;
    try {
      template = (PublicationTemplateImpl) PublicationTemplateManager.getInstance().
          getPublicationTemplate(publicationDetail.getPK().getInstanceId()
          + ":" + publicationDetail.getInfoId());
    } catch (Exception e) {
      return "Error getting publication template !";
    }
    try {
      Form formView = template.getViewForm();
      RecordSet recordSet = template.getRecordSet();
      DataRecord dataRecord = recordSet.getRecord(publicationDetail.getPK().getId());
      PagesContext context = new PagesContext();
      context.setComponentId(publicationDetail.getPK().getInstanceId());
      context.setObjectId(publicationDetail.getPK().getId());
      String htmlResult = formView.toString(context, dataRecord);
      htmlResult = replaceImagesPathForExport(htmlResult);
      htmlResult = replaceFilesPathForExport(htmlResult);
      return htmlResult;
    } catch (Exception e) {
      SilverTrace.error("form", "HtmlExportPublicationGenerator.toHtmlXMLModel",
          "root.MSG_GEN_PARAM_VALUE", e);
    }
    return null;
  }

  /**
   * @return
   */
  public String toHtml() {
    String htmlPubDescription = HtmlExportGenerator.encode(publicationDetail.getDescription());
    html html = new html();
    meta meta = new meta();
    meta.setContent("text/html; charset=UTF-8");
    meta.setHttpEquiv("Content-Type");
    head head = new head();
    head.addElement(meta);
    head.addElement(getHtmlStyle());
    html.addElement(head);
    body body = new body();
    body.addElement(toHtmlEnTetePublication());
    div content = new div();
    content.setClass("content");
    p description = new p();
    description.addElement(htmlPubDescription);
    content.addElement(description);
    if (dbModelContent != null) {
      content.addElement(toHtmlInfoModel());
    } else if (wysiwygText != null) {
      content.addElement(wysiwygText);
    } else if (xmlModelContent != null) {
      content.addElement(xmlFormToHTML());
    }
    body.addElement(content);
    if (listAttDetail != null && !listAttDetail.isEmpty()) {
      div attachments = new div();
      attachments.setClass("attachments");
      attachments.addElement(toHtmlAttachments());
      body.addElement(attachments);
    }
    html.addElement(body);
    return html.toString();
  }

  private String getHtmlStyle() {
    ElementContainer xhtmlcontainer = new ElementContainer();
    StringBuilder path = new StringBuilder();
    for (int i = 0; i < nbThemes; i++) {
      path.append("../");
    }
    path.append("treeview/display.css");
    xhtmlcontainer.addElement(new link().setType("text/css").setRel("stylesheet").setHref(path.
        toString()));
    return xhtmlcontainer.toString();
  }

  /**
   * @return
   */
  private String toHtmlAttachments() {
    ul attachments = new ul();
    attachments.setClass("list");
    if (listAttDetail != null && !listAttDetail.isEmpty()) {
      for (AttachmentDetail attDetail : listAttDetail) {
        attachments.addElement(toHtmlAttachmentInfos(attDetail));
      }
    }
    return attachments.toString();
  }

  /**
   * @param attDetail
   * @return
   */
  public static String toHtmlAttachmentInfos(AttachmentDetail attDetail) {
    ElementContainer xhtmlcontainer = new ElementContainer();
    String htmlLogicalName = attDetail.getLogicalName();
    String htmlFormatedFileSize =
        HtmlExportGenerator.encode(FileRepositoryManager.formatFileSize(attDetail.
        getSize()));

    li li = new li();
    a link = new a();
    link.setHref(FileServerUtils.replaceAccentChars(htmlLogicalName));
    link.addElement(FileServerUtils.replaceAccentChars(htmlLogicalName));
    li.addElement(link);
    li.addElement(new br());
    li.addElement(htmlFormatedFileSize);
    if (attDetail.getTitle() != null) {
      i i = new i();
      i.addElement(" ");
      i.addElement(attDetail.getTitle());
      li.addElement(i);
      if (StringUtil.isDefined(attDetail.getInfo())) {
        li.addElement(" - ");
        i info = new i();
        info.addElement(HtmlExportGenerator.encode(attDetail.getInfo()));
        li.addElement(info);
      }
    } else if (attDetail.getInfo() != null) {
      i i = new i();
      li.addElement(" - ");
      i.addElement(HtmlExportGenerator.encode(attDetail.getInfo()));
      li.addElement(i);
    }
    xhtmlcontainer.addElement(li);
    return xhtmlcontainer.toString();
  }

  /**
   * @param htmlText
   * @return
   */
  public static String replaceImagesPathForExport(String htmlText) {
    if (!StringUtil.isDefined(htmlText)) {
      return htmlText;
    }

    String lowerHtml = htmlText.toLowerCase();
    int finPath = 0;
    int debutPath;
    StringBuilder newHtmlText = new StringBuilder();
    String imageSrc;
    if (lowerHtml.indexOf("src=\"", finPath) == -1) {
      // pas d'images dans le fichier
      return htmlText;
    } else {
      while ((debutPath = lowerHtml.indexOf("src=\"", finPath)) != -1) {
        debutPath += 5;
        newHtmlText.append(htmlText.substring(finPath, debutPath));
        finPath = lowerHtml.indexOf('\"', debutPath);
        imageSrc = lowerHtml.substring(debutPath, finPath);
        int d = imageSrc.indexOf("/attached_file/");
        if (d >= 0) {
          int f = imageSrc.lastIndexOf('/');
          imageSrc = imageSrc.substring(f + 1);
          newHtmlText.append(imageSrc);
        } else {
          newHtmlText.append(htmlText.substring(debutPath, finPath));
        }
      }
      newHtmlText.append(htmlText.substring(finPath, htmlText.length()));
    }
    return newHtmlText.toString();
  }

  public static String replaceFilesPathForExport(String htmlText) {
    String lowerHtml = htmlText.toLowerCase();
    int finPath = 0;
    int debutPath;
    StringBuilder newHtmlText = new StringBuilder();
    String imageSrc;
    if (lowerHtml.indexOf("href=\"", finPath) == -1) {
      // pas d'images dans le fichier
      return htmlText;
    } else {
      while ((debutPath = lowerHtml.indexOf("href=\"", finPath)) != -1) {
        debutPath += 6;

        newHtmlText.append(htmlText.substring(finPath, debutPath));
        finPath = lowerHtml.indexOf('\"', debutPath);
        imageSrc = lowerHtml.substring(debutPath, finPath);
        int d = imageSrc.indexOf("/attached_file/");
        if (d >= 0) {
          // C'est une image stockée dans Silverpeas : extraction du nom de l'image
          d += 12;
          int f = imageSrc.lastIndexOf('/');
          imageSrc = imageSrc.substring(f + 1);
          newHtmlText.append(imageSrc);
        } else {
          newHtmlText.append(htmlText.substring(debutPath, finPath));
        }
      }
      newHtmlText.append(htmlText.substring(finPath, htmlText.length()));
    }
    return newHtmlText.toString();
  }
}
