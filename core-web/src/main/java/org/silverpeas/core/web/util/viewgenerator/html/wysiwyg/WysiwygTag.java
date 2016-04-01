package org.silverpeas.core.web.util.viewgenerator.html.wysiwyg;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.net.URLDecoder;

public class WysiwygTag extends TagSupport {

  private static final long serialVersionUID = -7606894410387540973L;

  private String replace;
  private String height = "500";
  private String width = "100%";
  private String language = "en";
  private String toolbar = "Default";
  private String spaceId;
  private String spaceName;
  private String componentId;
  private String componentName;
  private String browseInfo;
  private String objectId;
  private boolean displayFileBrowser = true;

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getToolbar() {
    return toolbar;
  }

  public void setToolbar(String toolbar) {
    this.toolbar = toolbar;
  }

  public String getReplace() {
    return replace;
  }

  public void setReplace(String replace) {
    this.replace = replace;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public String getSpaceName() {
    return spaceName;
  }

  public void setSpaceName(String spaceName) {
    this.spaceName = spaceName;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public String getBrowseInfo() {
    return browseInfo;
  }

  public void setBrowseInfo(String browseInfo) {
    this.browseInfo = browseInfo;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public boolean getDisplayFileBrowser() {
    return displayFileBrowser;
  }

  public void setDisplayFileBrowser(boolean displayFileBrowser) {
    this.displayFileBrowser = displayFileBrowser;
  }

  @Override
  public int doEndTag() throws JspException {
    Wysiwyg wysiwyg = new Wysiwyg();
    wysiwyg.setReplace(getReplace());
    wysiwyg.setWidth(getWidth());
    wysiwyg.setHeight(getHeight());
    wysiwyg.setLanguage(getLanguage());
    wysiwyg.setToolbar(getToolbar());
    wysiwyg.setServerURL(URLUtil.getServerURL((HttpServletRequest) pageContext.getRequest()));

    HttpSession session = pageContext.getSession();
    GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    if(getSpaceId() != null) {
      session.setAttribute("WYSIWYG_SpaceId", getSpaceId());
    }
    if(getSpaceName() != null) {
      session.setAttribute("WYSIWYG_SpaceName", URLDecoder.decode(getSpaceName()));
    }
    if(getComponentId() != null) {
      session.setAttribute("WYSIWYG_ComponentId", getComponentId());
    }
    if(getComponentName() != null) {
      session.setAttribute("WYSIWYG_ComponentName", URLDecoder.decode(getComponentName()));
    }
    if(getBrowseInfo() != null) {
      session.setAttribute("WYSIWYG_BrowseInfo", URLDecoder.decode(getBrowseInfo()));
    }
    if(getObjectId() != null) {
      session.setAttribute("WYSIWYG_ObjectId", getObjectId());
    }
    session.setAttribute("WYSIWYG_Language", getLanguage());

    SettingBundle settings = gef.getFavoriteLookSettings();
    if (settings != null) {
      wysiwyg.setCustomCSS(settings.getString("StyleSheet", ""));
    }

    wysiwyg.setDisplayFileBrowser(getDisplayFileBrowser());

    try {
      pageContext.getOut().println(wysiwyg.print());
    } catch (IOException e) {
      throw new JspException("WysiwygTag tag", e);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    return EVAL_BODY_INCLUDE;
  }


}