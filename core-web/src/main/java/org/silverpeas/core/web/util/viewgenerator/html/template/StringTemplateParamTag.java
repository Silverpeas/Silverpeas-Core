package org.silverpeas.core.web.util.viewgenerator.html.template;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.silverpeas.core.template.SilverpeasTemplate;

public class StringTemplateParamTag extends TagSupport {
  private static final long serialVersionUID = -8246867491413179780L;

  private String name;
  private Object value;

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

  /**
   * @return the value
   */
  public Object getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final Object value) {
    this.value = value;
  }

  @Override
  public int doStartTag() throws JspException {
    final SilverpeasTemplate template =
        (SilverpeasTemplate) pageContext.getAttribute(StringTemplateTag.STRING_TEMPLATE_ATT);
    template.setAttribute(name, value);
    return SKIP_BODY;
  }
}