package com.stratelia.webactiv.util.viewGenerator.html;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Random;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.silverpeas.peasCore.URLManager;

public class VideoPlayerTag extends TagSupport {

  private String url;
  
  private Integer width = 360;
  
  private Integer height = 240;
  
  private Boolean autostart = false;
  
  private static final String playerUrl = URLManager.getApplicationURL() + "/util/flowplayer/flowplayer-3.2.4.swf";
  
  private static final Random randomGenerator = new Random();
  
  private static final String template = "<a id=''{0}'' href=''{1}'' " +
  		"style=''display:block;width:{2,number,integer}px;height:{3,number,integer}px;''>" +
  		"</a><script type=''text/javascript'' language=''javascript''>" +
  		"flowplayer(''{0}'', ''{4}'', '{'wmode: ''opaque'', clip: '{' " +
  		"autoBuffering: false, autoPlay: {5} '}' '}');</script>";
  
  @Override
  public int doStartTag() throws JspException {  
    
    try {
      pageContext.getOut().print(MessageFormat.format(template, getGeneratedId(), 
          getUrl(), getWidth(), getHeight(), playerUrl, isAutostart()));
    } catch (IOException e) {
      throw new JspException("Can't display video player", e);
    }
    return SKIP_BODY;
  }
  
  
  private String getGeneratedId() {
    return "player" + randomGenerator.nextInt();
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
    this.width = width;
  }

  public Integer getHeight() {
    return height;
  }

  public void setHeight(Integer height) {
    this.height = height;
  }

  public Boolean isAutostart() {
    return autostart;
  }

  public void setAutostart(Boolean autostart) {
    this.autostart = autostart;
  }
  
  

}
