/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.silverpeas.form.fieldDisplayer;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author ehugonnet
 */
public abstract class AbstractFieldDisplayer implements FieldDisplayer{

 
  @Override
  public List<String> update(List<FileItem> items, Field field, FieldTemplate template, PagesContext pageContext) throws FormException {
    String value = FileUploadUtil.getParameter(items,template.getFieldName());
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES && !StringUtil.isDefined(value)) {
			return new ArrayList<String>();
    }
    return update(value, field, template, pageContext);
  }

  @Override
  public void index(FullIndexEntry indexEntry, String key, String fieldName, Field field, String language) {
    String value = field.getStringValue().trim().replaceAll("##", " ");
    indexEntry.addField(key, value, language);
  }


}
