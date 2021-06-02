/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.html.SupportedWebPlugins;
import org.silverpeas.core.html.WebPlugin;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.getDynamicSubscriptionJavascriptLoadContent;

/**
 * This TAG can be called into a {@link ButtonTag}.<br>
 * It permits to display a popups in order to ask to the user some validation and confirmations.
 * <p>
 *   For now, it asks:
 *   <ul>
 *     <li>for minor/major modifications</li>
 *     <li>for user notification sending</li>
 *   </ul>
 * </p>
 */
public class ContributionManagementContextTag extends TagSupport {
  private static final long serialVersionUID = 6158988849428896473L;

  private ContributionIdentifier contributionId;
  private SubscriptionResourceType subscriptionResourceType = COMPONENT;
  private String subscriptionResourceId = null;
  private String jsValidationCallbackMethodName;

  private Boolean contributionIndexable = true;

  public SubscriptionResourceType getSubscriptionResourceType() {
    return subscriptionResourceType;
  }

  public ContributionIdentifier getContributionId() {
    return contributionId;
  }

  public void setContributionId(final ContributionIdentifier contributionId) {
    this.contributionId = contributionId;
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
      ElementContainer xhtml = new ElementContainer();
      xhtml.addElement(WebPlugin.get()
          .getHtml(SupportedWebPlugins.CONTRIBUTIONMODICTX, getRequest().getUserLanguage()));
      xhtml.addElement(new script().setType("text/javascript")
          .addElement(getDynamicSubscriptionJavascriptLoadContent(null)));
      xhtml.output(pageContext.getOut());
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
    final ContributionIdentifier cId = ofNullable(getContributionId()).orElseGet(() -> {
      GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession()
          .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      final String componentId = gef.getComponentIdOfCurrentRequest();
      return ContributionIdentifier.from(componentId, componentId, "UNKNOWN");
    });
    final StringBuilder sb = new StringBuilder();
    sb.append("jQuery.contributionModificationContext.validateOnUpdate({");
    sb.append("  contributionId:{");
    sb.append("    componentInstanceId:'").append(cId.getComponentInstanceId()).append("',");
    sb.append("    localId:'").append(cId.getLocalId()).append("',");
    sb.append("    type:'").append(cId.getType()).append("'");
    sb.append("  },status: undefined");
    sb.append("  ,callback: function() {");
    sb.append(renderSubscriptionJs());
    sb.append("  }");
    if (StringUtil.isDefined(getJsValidationCallbackMethodName())) {
      sb.append("  ,validationCallback:").append(getJsValidationCallbackMethodName());
    }
    sb.append("});");
    return sb.toString();
  }

  protected String renderSubscriptionJs() {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    final String componentId = gef.getComponentIdOfCurrentRequest();
    boolean tabComments = StringUtil.getBooleanValue(
        OrganizationController.get().getComponentParameterValue(componentId, "tabComments"));
    boolean comments = StringUtil.getBooleanValue(
        OrganizationController.get().getComponentParameterValue(componentId, "comments"));

    final StringBuilder sb = new StringBuilder();
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
      sb.append("contributionLocalId : '").append(contributionId.getLocalId()).append("',");
      sb.append("contributionType : '").append(contributionId.getType()).append("',");
      sb.append("contributionIndexable : ").append(contributionIndexable);
      sb.append("}");
    }
    sb.append("});");
    return sb.toString();
  }
}
