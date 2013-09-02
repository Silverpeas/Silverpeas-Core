package com.stratelia.webactiv.util.viewGenerator.html.template;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.tagext.TagSupport;

import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;

public class StringTemplateTag extends TagSupport {
  private static final long serialVersionUID = -4748892203365639129L;

  public static final String STRING_TEMPLATE_ATT = "@StringTemplateTag@";
  private static final String CORE_NAMESPACE = "core";

  private String nameSpace = "";
  private String locationBase = "";
  private String name = "";
  private SilverpeasTemplate template;

  /**
   * @return the locationBase
   */
  public String getLocationBase() {
    return locationBase;
  }

  /**
   * @param locationBase the locationBase to set
   */
  public void setLocationBase(final String locationBase) {
    this.locationBase = locationBase;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public int doStartTag() throws JspException {
    try {

      // Component or Core ?
      final String[] locationNsAndBase = locationBase.split(":");
      if (locationNsAndBase == null || locationNsAndBase.length != 2) {
        throw new IllegalArgumentException(
            "locationBase parameter is not correct. Please check if the namespace is defined.");
      }
      nameSpace = locationNsAndBase[0];
      locationBase = locationNsAndBase[1];

      // Template location
      if (CORE_NAMESPACE.equals(nameSpace)) {
        template = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore(locationBase);
      } else {
        template = SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents(locationBase);
      }

      // Register in context
      pageContext.setAttribute(STRING_TEMPLATE_ATT, template);

      // Evaluate body
      return EVAL_BODY_INCLUDE;
    } catch (final Exception e) {
      throw new JspException("StringTemplate tag", e);
    }
  }

  @Override
  public int doEndTag() throws JspException {

    // Language
    String language = null;
    final Locale locale = (Locale) Config.find(pageContext, Config.FMT_LOCALE);
    if (locale != null) {
      language = locale.getLanguage();
    }
    language = I18NHelper.checkLanguage(language);

    // Template file
    try {
      String templateName = name + "_" + language;
      if (CORE_NAMESPACE.equals(nameSpace)) {
        pageContext.getOut().print(template.applyFileTemplate(templateName));
      } else {
        pageContext.getOut().print(template.applyFileTemplateOnComponent(nameSpace, templateName));
      }
    } catch (final IOException e) {
      throw new JspException("StringTemplate tag", e);
    }
    return EVAL_PAGE;
  }
}