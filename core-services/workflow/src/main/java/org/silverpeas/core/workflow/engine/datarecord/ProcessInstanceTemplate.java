package org.silverpeas.core.workflow.engine.datarecord;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;

/**
 * @author mmoquillon
 */
public interface ProcessInstanceTemplate extends RecordTemplate {

  FieldTemplate getFieldTemplate(int fieldIndex) throws FormException;

  Field[] buildFieldsArray();
}
