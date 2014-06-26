package com.stratelia.webactiv.util.viewGenerator.html;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.file.ImageResizingProcessor;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * @author mmoquillon
 */
public class ImageTag extends SimpleTagSupport {

  private static final ResourceLocator settings =
      new ResourceLocator("org.silverpeas.lookAndFeel.generalLook", "");
  private static final String IMAGE_SIZE_KEY_PREFIX = "image.size.";

  private String src;
  private String alt;
  private String type;
  private String size;
  private String css;

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

  @Override
  public void doTag() throws JspException, IOException {
    println(generateHtml());
  }

  public String generateHtml() {
    String imageSrc = getSrc().trim();
    String cssClass = getCss();
    if (!imageSrc.contains("/jsp/") && !imageSrc.contains("/icons/")) {
      String imageSize = getSize();
      if (!StringUtil.isDefined(imageSize)) {
        String type = getType();
        if (StringUtil.isDefined(type)) {
          imageSize = settings.getString(IMAGE_SIZE_KEY_PREFIX + type.trim());
        }
      }
      imageSrc = FileServerUtils.getImageURL(imageSrc, imageSize);
    }
    if (!imageSrc.startsWith(getWebContext())) {
      imageSrc = getWebContext() + imageSrc;
    }

    if (!StringUtil.isDefined(cssClass)) {
      cssClass = getType();
    }
    if (StringUtil.isDefined(cssClass)) {
      cssClass = "class='" + cssClass + "'";
    } else {
      cssClass = "";
    }
    return "<img src='" + imageSrc + "' alt='" + getAlt() + "'" + cssClass + "/>";
  }

  protected void println(String txt) throws IOException {
    getJspContext().getOut().println(txt);
  }

  protected String getWebContext() {
    return URLManager.getApplicationURL();
  }
}
