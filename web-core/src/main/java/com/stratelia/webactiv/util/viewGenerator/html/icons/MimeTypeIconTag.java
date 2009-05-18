package com.stratelia.webactiv.util.viewGenerator.html.icons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.webactiv.util.FileRepositoryManager;

public class MimeTypeIconTag extends TagSupport {
  private static final String DEFAULT_STYLE = "unknown";
  private static final Properties STYLES = new Properties();
  static {
    try {
      ClassLoader loader = TagSupport.class.getClassLoader();
      InputStream in = loader.getResourceAsStream(
      "com/silverpeas/view/generator/mime_types_styles.properties");
      STYLES.load(in);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  private String fileExtension = "";
  private String divId = "";
  
  public int doEndTag() throws JspException {
    try {
      pageContext.getOut().println("</div>");
    } catch (IOException ioex) {
      throw new JspException(ioex);
    }
    return EVAL_PAGE;
  }

  public int doStartTag() throws JspException {
    try {
      boolean styleFound = true;
      String style = STYLES.getProperty(this.fileExtension);
      if(style == null) {
        styleFound = false;
        style = STYLES.getProperty(DEFAULT_STYLE);
      }      
      pageContext.getOut().print("<div class=\"");
      pageContext.getOut().print(style);
      if(this.divId != null) {
        styleFound = true;
        pageContext.getOut().print("\" id=\"");
        pageContext.getOut().print(divId);
      }
      pageContext.getOut().print("\">");
      if (styleFound) {
        pageContext.getOut().print("&nbsp;");
        return SKIP_BODY;
      }      
      return EVAL_BODY_INCLUDE;
    } catch (IOException ioex) {
      throw new JspException(ioex);
    }
  }

  /**
   * @param fileName
   *          the fileName to set for the wanted icon.
   */
  public void setFileName(String fileName) {
    this.fileExtension = FileRepositoryManager.getFileExtension(fileName)
        .toLowerCase();
  }

  /**
   * @param fileExtension
   *          the file extension to set
   */
  public void setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
  }
  
  /**
   * @param divId
   *          the id attribute for the cerated div.
   */
  public void setDivId(String divId) {
    this.divId = divId;
  }
}
