package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.FieldTemplate;

public final class IndexedFieldTemplate
{
   public final int index;
   public final FieldTemplate fieldTemplate;

   public IndexedFieldTemplate(int index, FieldTemplate fieldTemplate)
   {
      this.index = index;
      this.fieldTemplate = fieldTemplate;
   }
}
