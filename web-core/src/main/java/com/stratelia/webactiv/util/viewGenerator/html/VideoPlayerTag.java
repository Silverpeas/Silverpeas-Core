package com.stratelia.webactiv.util.viewGenerator.html;

import com.silverpeas.form.displayers.VideoPlayer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.ElementContainer;

public class VideoPlayerTag extends TagSupport {
  private static final long serialVersionUID = 1425756234498404463L;

  private static final Integer DEFAULT_WIDTH = 360;
  
  private static final Integer DEFAULT_HEIGHT = 240;
  
  private String url;

  private Integer width = DEFAULT_WIDTH;

  private Integer height = DEFAULT_HEIGHT;

  private boolean autostart = false;

  @Override
  public int doStartTag() throws JspException {

    try {
      ElementContainer xhtmlContainer = new ElementContainer();
      VideoPlayer videoPlayer = new VideoPlayer(getUrl(), isAutostart());
      videoPlayer.setHeight(String.valueOf(getHeight()) + "px");
      videoPlayer.setWidth(String.valueOf(getWidth()) + "px");
      videoPlayer.init(xhtmlContainer);
      videoPlayer.renderIn(xhtmlContainer);
      xhtmlContainer.output(pageContext.getOut());
    } catch (Exception e) {
      throw new JspException("Can't display video player", e);
    }
    return SKIP_BODY;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Integer getWidth() {
    return width;
  }

  public void setWidth(Integer width) {
    this.width = (width == null? DEFAULT_WIDTH:width);
  }

  public Integer getHeight() {
    return height;
  }

  public void setHeight(Integer height) {
    this.height = (height == null? DEFAULT_HEIGHT:height);
  }

  public Boolean isAutostart() {
    return autostart;
  }

  public void setAutostart(Boolean autostart) {
    this.autostart = autostart;
  }

}
