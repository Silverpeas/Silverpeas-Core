package com.silverpeas.form.fieldDisplayer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import org.apache.ecs.html.Input;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.ArrayList;
import java.util.List;

/**
 * The UniqueIdFieldDisplayer displays a unique id as string in a read-only mode
 * Unique id is the result of the new Date().getTime() operation.
 * A suffix can be added by using the "suffix" parameter (value "userid")
 * 
 * @author Nicolas EYSSERIC
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class UniqueIdFieldDisplayer extends AbstractFieldDisplayer {

  public UniqueIdFieldDisplayer() {
  }

  public String[] getManagedTypes() {
    return new String[]{TextField.TYPE};
  }

  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws IOException {
    if (!template.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "UniqueIdFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);
    }

    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  public void display(PrintWriter out, Field field, FieldTemplate template, PagesContext pageContext)
      throws FormException {
    if (field == null) {
      return;
    }

    if (!field.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "UniqueIdFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);
    }

    String fieldName = template.getFieldName();

    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());

    String defaultValue = Long.toString(new Date().getTime());
    String suffix = parameters.get("suffix");
    if ("userid".equalsIgnoreCase(suffix)) {
      defaultValue += "-" + pageContext.getUserId();
    }

    String value = (!field.isNull() ? field.getValue(pageContext.getLanguage()) : defaultValue);
    if (pageContext.isBlankFieldsUse()) {
      value = "";
    }

    Input input = new Input();
    input.setName(fieldName);
    input.setID(fieldName);
    input.setValue(EncodeHelper.javaStringToHtmlString(value));
    input.setType(template.isHidden() ? Input.hidden : Input.text);
    input.setSize(parameters.containsKey("size") ? parameters.get("size") : "50");
    input.setReadOnly(true);

    out.println(input.toString());
  }

  public List<String> update(String newValue, Field field, FieldTemplate template, PagesContext pagesContext)
      throws FormException {
    if (!field.getTypeName().equals(TextField.TYPE)) {
      throw new FormException("UniqueIdFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE", TextField.TYPE);
    }
    if (field.acceptValue(newValue, pagesContext.getLanguage())) {
      field.setValue(newValue, pagesContext.getLanguage());
    } else {
      throw new FormException("UniqueIdFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE", TextField.TYPE);
    }
    return new ArrayList<String>();
  }

  public boolean isDisplayedMandatory() {
    return true;
  }

  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }
}
