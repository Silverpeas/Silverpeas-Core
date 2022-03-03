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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.buttons;

import org.apache.ecs.xhtml.script;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;

/**
 * This TAG can be called into a {@link ButtonTag}.<br>
 * It permits to display a popup in order to ask to the user to confirm the subscription
 * notification sending linked to its modifications.
 */
public class ConfirmResourceSubscriptionSendingTag extends TagSupport {
  private static final long serialVersionUID = 6158988849428896473L;

  private SubscriptionResourceType subscriptionResourceType = COMPONENT;
  private String subscriptionResourceId = null;
  private String jsValidationCallbackMethodName;

  private String contributionLocalId;
  private String contributionType;
  private Boolean contributionIndexable = true;

  public SubscriptionResourceType getSubscriptionResourceType() {
    return subscriptionResourceType;
  }

  public void setSubscriptionResourceType(final SubscriptionResourceType subscriptionResourceType) {
    this.subscriptionResourceType = subscriptionResourceType;
  }

  public String getSubscriptionResourceId() {
    return subscriptionResourceId;
  }

  public void setSubscriptionResourceId(final String subscriptionResourceId) {
    this.subscriptionResourceId = subscriptionResourceId;
  }

  public String getJsValidationCallbackMethodName() {
    return jsValidationCallbackMethodName;
  }

  public void setJsValidationCallbackMethodName(final String jsValidationCallbackMethodName) {
    this.jsValidationCallbackMethodName = jsValidationCallbackMethodName;
  }

  public String getContributionLocalId() {
    return contributionLocalId;
  }

  public void setContributionLocalId(final String contributionLocalId) {
    this.contributionLocalId = contributionLocalId;
  }

  public String getContributionType() {
    return contributionType;
  }

  public void setContributionType(final String contributionType) {
    this.contributionType = contributionType;
  }

  public Boolean getContributionIndexable() {
    return contributionIndexable;
  }

  public void setContributionIndexable(final Boolean contributionIndexable) {
    this.contributionIndexable = contributionIndexable;
  }

  @Override
  public int doEndTag() throws JspException {
    ButtonTag buttonTag = (ButtonTag) findAncestorWithClass(this, ButtonTag.class);
    if (buttonTag != null) {
      buttonTag.setActionPreProcessing("");
      try {
        pageContext.getOut().println(new script().setType("text/javascript")
            .addElement(JavascriptPluginInclusion.getDynamicSubscriptionJavascriptLoadContent(null))
            .toString());
      } catch (IOException e) {
        throw new JspException("ConfirmResourceSubscriptionSendingTag Tag", e);
      }
      buttonTag.setActionPreProcessing(renderJs());
      return EVAL_PAGE;
    } else {
      throw new JspException(this.getClass().getSimpleName() + " must be wrapped by " +
          ButtonTag.class.getSimpleName());
    }
  }

  @Override
  public int doStartTag() throws JspException {
    return EVAL_BODY_INCLUDE;
  }

  protected HttpRequest getRequest() {
    return (HttpRequest) pageContext.getRequest();
  }

  protected String renderJs() {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    final String componentId = gef.getComponentIdOfCurrentRequest();
    boolean tabComments = StringUtil.getBooleanValue(
        OrganizationController.get().getComponentParameterValue(componentId, "tabComments"));
    boolean comments = StringUtil.getBooleanValue(
        OrganizationController.get().getComponentParameterValue(componentId, "comments"));

    StringBuilder sb = new StringBuilder();
    sb.append("jQuery.subscription.confirmNotificationSendingOnUpdate({subscription:{");
    sb.append("  componentInstanceId:'").append(componentId).append("'");
    final SubscriptionResourceType type = getSubscriptionResourceType();
    if (COMPONENT != type) {
      sb.append("  ,type:").append("$.subscription.subscriptionType.").append(type.getName());
      sb.append("  ,resourceId:'").append(getSubscriptionResourceId()).append("'");
    }
    sb.append("  },callback: function() {");
    sb.append("    {action};");
    sb.append("  }");
    if (StringUtil.isDefined(getJsValidationCallbackMethodName())) {
      sb.append("  ,validationCallback:").append(getJsValidationCallbackMethodName());
    }
    if (tabComments || comments) {
      sb.append("  ,comment:{");
      sb.append("saveNote : true,");
      sb.append("contributionLocalId : '").append(contributionLocalId).append("',");
      sb.append("contributionType : '").append(contributionType).append("',");
      sb.append("contributionIndexable : ").append(contributionIndexable);
      sb.append("}");
    }
    sb.append("});");
    return sb.toString();
  }
}
