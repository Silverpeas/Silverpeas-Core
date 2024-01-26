/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html.buttons;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.html.WebPlugin;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.html.SupportedWebPlugin.Constants.CONTRIBUTIONMODICTX;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.getDynamicSubscriptionJavascriptLoadContent;


abstract class AbstractContributionManagementContextTag extends TagSupport {
  private static final long serialVersionUID = 317414465687842654L;

  public abstract String getJsValidationCallbackMethodName();
  abstract List<Item> getItems();

  public AbstractContributionManagementContextTag() {
    this.init();
  }

  @Override
  public void release() {
    super.release();
    this.init();
  }

  abstract void init();

  @Override
  public int doEndTag() throws JspException {
    ButtonTag buttonTag = (ButtonTag) findAncestorWithClass(this, ButtonTag.class);
    if (buttonTag != null) {
      buttonTag.setActionPreProcessing("");
      ElementContainer xhtml = new ElementContainer();
      xhtml.addElement(
          WebPlugin.get().getHtml(CONTRIBUTIONMODICTX, getRequest().getUserLanguage()));
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
    final List<Item> items = getItems();
    final StringBuilder sb = new StringBuilder();
    sb.append("jQuery.contributionModificationContext.validateOnUpdate({");
    sb.append("  items:[");
    sb.append(items.stream()
        .map(this::renderContributionObject)
        .collect(Collectors.joining(",")));
    sb.append("  ]");
    sb.append("  ,callback: function() {");
    sb.append(renderSubscriptionJs(items));
    sb.append("  }");
    if (StringUtil.isDefined(getJsValidationCallbackMethodName())) {
      sb.append("  ,validationCallback:").append(getJsValidationCallbackMethodName());
    }
    sb.append("});");
    return sb.toString();
  }

  protected String renderContributionObject(final Item item) {
    final ContributionIdentifier cId = ofNullable(item.contributionId).orElseGet(() -> {
      GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession()
          .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      final String componentId = gef.getComponentIdOfCurrentRequest();
      return ContributionIdentifier.from(componentId, componentId, "UNKNOWN");
    });
    final String status = item.contributionStatus != null ?
        String.format("'%s'", item.contributionStatus) :
        "undefined";
    final String locationId = item.location != null &&
        item.location.getComponentInstanceId().equals(item.contributionId.getComponentInstanceId()) ?
        String.format("'%s'", item.location.getLocalId()) :
        "undefined";
    final String indexable = item.contributionIndexable != null ?
        item.contributionIndexable.toString().toLowerCase() :
        "true";
    return String.format(
        "{contributionId:{componentInstanceId:'%s',localId:'%s',type:'%s'},status:%s,locationId:%s,indexable:%s}",
        cId.getComponentInstanceId(), cId.getLocalId(), cId.getType(), status, locationId, indexable);
  }

  protected String renderSubscriptionJs(final List<Item> items) {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    final String componentId = gef.getComponentIdOfCurrentRequest();
    boolean tabComments = StringUtil.getBooleanValue(
        OrganizationController.get().getComponentParameterValue(componentId, "tabComments"));
    boolean comments = StringUtil.getBooleanValue(
        OrganizationController.get().getComponentParameterValue(componentId, "comments"));

    final StringBuilder sb = new StringBuilder();
    sb.append("jQuery.subscription.confirmNotificationSendingOnUpdate({items:[");
    sb.append(items.stream()
        .map(i -> renderContributionSubscriptionItem(componentId, i, tabComments || comments))
        .collect(Collectors.joining(",")));
    sb.append("  ],callback: function() {");
    sb.append("    {action};");
    sb.append("  }");
    if (StringUtil.isDefined(getJsValidationCallbackMethodName())) {
      sb.append("  ,validationCallback:").append(getJsValidationCallbackMethodName());
    }
    sb.append("});");
    return sb.toString();
  }

  protected String renderContributionSubscriptionItem(final String componentId, final Item item,
      final boolean isComment) {
    final StringBuilder sb = new StringBuilder();
    sb.append("{contribution:").append(renderContributionObject(item));
    sb.append(",subscription:{");
    sb.append("  componentInstanceId:'").append(componentId).append("'");
    final SubscriptionResourceType type = item.subscriptionResourceType;
    sb.append("  ,type:").append("$.subscription.subscriptionType.").append(type.getName());
    if (COMPONENT != type) {
      sb.append("  ,resourceId:'").append(item.subscriptionResourceId).append("'");
    }
    sb.append("  }");
    if (isComment) {
      sb.append("  ,comment:{");
      sb.append("saveNote : true");
      sb.append("}");
    }
    return sb.append("}").toString();
  }

  static class Item {
    ContributionIdentifier contributionId;
    ContributionStatus contributionStatus;
    Location location;
    SubscriptionResourceType subscriptionResourceType = COMPONENT;
    String subscriptionResourceId = null;
    Boolean contributionIndexable = true;
  }
}
