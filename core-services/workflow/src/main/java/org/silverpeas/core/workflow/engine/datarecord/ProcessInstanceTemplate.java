package org.silverpeas.core.workflow.engine.datarecord;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author mmoquillon
 */
public abstract class ProcessInstanceTemplate implements RecordTemplate {

  /**
   * The field names.
   */
  protected String[] fieldNames = null;

  /**
   * The map (fieldName -> IndexedFieldTemplate).
   */
  protected final transient Map<String, IndexedFieldTemplate> fields = new HashMap<>();

  /**
   * The field templates.
   */
  private FieldTemplate[] fieldTemplates = null;

  public abstract FieldTemplate getFieldTemplate(int fieldIndex) throws FormException;

  public abstract Field[] buildFieldsArray();

  public String[] getFieldNames() {
    if (fieldNames == null) {
      fieldNames = new String[fields.size()];
      Iterator<String> names = fields.keySet().iterator();
      String name;
      while (names.hasNext()) {
        name = names.next();
        try {
          fieldNames[getFieldIndex(name)] = name;
        } catch (Exception e) {
          // can't happen : the name is a key
        }
      }
    }
    return fieldNames;
  }

  @Override
  public FieldTemplate[] getFieldTemplates() throws FormException {
    if (fieldTemplates == null) {
      fieldTemplates = new FieldTemplate[fields.size()];
      Iterator<String> names = fields.keySet().iterator();
      String name;
      while (names.hasNext()) {
        name = names.next();
        try {
          fieldTemplates[getFieldIndex(name)] = getFieldTemplate(name);
        } catch (Exception e) {
          // can't happen : the name is a key
        }
      }
    }
    return fieldTemplates;
  }
}
