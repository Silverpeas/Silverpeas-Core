package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.FieldSet;
import org.apache.ecs.html.Legend;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

import static org.silverpeas.core.web.mvc.controller.MainSessionController
    .MAIN_SESSION_CONTROLLER_ATT;

/**
 * Created by Nicolas on 14/09/2017.
 */
public class DirectoryExtraFormTag extends SimpleTagSupport {

  private String userId;
  private boolean edition = false;

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public void setEdition(final boolean edition) {
    this.edition = edition;
  }

  @Override
  public void doTag() throws JspException, IOException {
    Form form = getForm();
    if (form != null) {

      PagesContext formContext = getFormContext();
      if (edition) {
        form.displayScripts(getOut(), formContext);
      }

      ElementContainer container = new ElementContainer();

      FieldSet fieldSet = new FieldSet();
      fieldSet.setID("identity-template");
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

  private Form getForm() {
    PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
    return templateManager.getDirectoryForm(getFormContext(), !edition);
  }

  private PagesContext getFormContext() {
    MainSessionController session = (MainSessionController) getJspContext().getAttribute(
        MAIN_SESSION_CONTROLLER_ATT, PageContext.SESSION_SCOPE);
    if (session != null) {
      return PagesContext.getDirectoryContext(userId, session.getUserId(),
          session.getFavoriteLanguage());
    }
    return null;
  }

}