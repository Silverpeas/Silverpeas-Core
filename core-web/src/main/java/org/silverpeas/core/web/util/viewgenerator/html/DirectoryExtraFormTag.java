package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.FieldSet;
import org.apache.ecs.html.Legend;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.silverpeas.core.web.mvc.controller.MainSessionController
    .MAIN_SESSION_CONTROLLER_ATT;

/**
 * Created by Nicolas on 14/09/2017.
 */
public class DirectoryExtraFormTag extends SimpleTagSupport {

  private String userId;
  private boolean edition = false;
  private boolean skippable = false;
  private String domainId;

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public void setEdition(final boolean edition) {
    this.edition = edition;
  }

  public void setSkippable(final boolean skippable) { this.skippable = skippable; }

  public void setDomainId(final String domainId) { this.domainId = domainId; }

  @Override
  public void doTag() {
    PagesContext formContext = getFormContext();
    Form form = getForm(formContext);
    if (form != null) {

      ElementContainer container = new ElementContainer();

      String fieldSetId = "identity-template";
      if (edition) {
        formContext.setFormSkippable(skippable);
        formContext.setElementToHideWhenSkipping(fieldSetId);
        form.displayScripts(getOut(), formContext);
      }

      FieldSet fieldSet = new FieldSet();
      fieldSet.setID(fieldSetId);
      fieldSet.setClass("skinFieldset");

      Legend legend = new Legend(form.getTitle());
      legend.setClass("without-img");
      fieldSet.addElement(legend);

      String strForm = form.toString(formContext);
      fieldSet.addElement(strForm);

      container.addElement(fieldSet);

      container.output(getOut());
    }
  }

  protected JspWriter getOut() {
    return getJspContext().getOut();
  }

  private Form getForm(PagesContext pageContext) {
    PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
    return templateManager.getDirectoryForm(pageContext, !edition);
  }

  private PagesContext getFormContext() {
    MainSessionController session = (MainSessionController) getJspContext().getAttribute(
        MAIN_SESSION_CONTROLLER_ATT, PageContext.SESSION_SCOPE);
    if (session != null) {
      PagesContext pageContext = PagesContext.getDirectoryContext(userId, session.getUserId(),
          session.getFavoriteLanguage());
      if (StringUtil.isNotDefined(userId)) {
        // creation case
        pageContext.setDomainId(domainId);
      }
      return pageContext;
    }
    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
  }

}