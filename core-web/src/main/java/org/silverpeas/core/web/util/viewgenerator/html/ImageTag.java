/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.ResourceLocator;
import org.apache.ecs.xhtml.img;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * @author mmoquillon
 */
public class ImageTag extends SimpleTagSupport {

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
  private static final String IMAGE_SIZE_KEY_PREFIX = "image.size.";

  private String src;
  private String alt;
  private String type;
  private String size;
  private String css;
  private String id;

  public String getSrc() {
    return src;
  }

  public void setSrc(final String src) {
    this.src = src;
  }

  public String getAlt() {
    return (StringUtil.isDefined(alt) ? alt : "");
  }

  public void setAlt(final String alt) {
    this.alt = alt;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getSize() {
    return size;
  }

  public void setSize(final String size) {
    this.size = size;
  }

  public String getCss() {
    return css;
  }

  public void setCss(final String css) {
    this.css = css;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  @Override
  public void doTag() throws JspException, IOException {
    println(generateHtml());
  }

  public String generateHtml() {
    String imageSrc = "";
    if (StringUtil.isDefined(getSrc())) {
      imageSrc = getSrc().trim();
    }
    String cssClass = getCss();
    String id = getId();
    String type = getType();
    if (!imageSrc.contains("/jsp/") && !imageSrc.contains("/icons/")) {
      String imageSize = getSize();
      if (!StringUtil.isDefined(imageSize) && StringUtil.isDefined(type)) {
        imageSize = settings.getString(IMAGE_SIZE_KEY_PREFIX + type.trim());
      }
      imageSrc = FileServerUtils.getImageURL(imageSrc, imageSize);
    }
    if (!imageSrc.startsWith(getWebContext())) {
      imageSrc = getWebContext() + imageSrc;
    }
    img img = new img(imageSrc);
    img.setAlt(getAlt());

    if (!StringUtil.isDefined(cssClass)) {
      if (StringUtil.isDefined(type)) {
        img.setClass(type);
      }
    } else {
      img.setClass(cssClass);
    }
    if (StringUtil.isDefined(id)) {
      img.setID(id);
    }
    return img.toString();
  }

  @Override
  public String toString() {
    return generateHtml();
  }

  protected void println(String txt) throws IOException {
    getJspContext().getOut().println(txt);
  }

  protected String getWebContext() {
    return URLUtil.getApplicationURL();
  }
}
