/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.Form;
import org.silverpeas.core.workflow.api.model.Forms;
import org.silverpeas.core.exception.SilverpeasException;

/**
 * Class implementing the representation of the &lt;forms&gt; element of a Process Model.
 **/
public class FormsImpl implements Serializable, Forms {

  private static final long serialVersionUID = -4621417980509658490L;
  private List<Form> formList;

  /**
   * Constructor
   */
  public FormsImpl() {
    formList = new ArrayList<>();
  }

  /*
   * (non-Javadoc)
   * @see Forms#addForm(com.silverpeas.workflow .api.model.Form)
   */
  @Override
  public void addForm(Form form) {
    formList.add(form);
  }

  /*
   * (non-Javadoc)
   * @see Forms#createForm()
   */
  @Override
  public Form createForm() {
    return new FormImpl();
  }

  /*
   * (non-Javadoc)
   * @see Forms#getForm(java.lang.String)
   */
  @Override
  public Form getForm(String name) {
    for (Form form : formList) {
      if (form.getName().equals(name)) {
        return form;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see Forms#getForm(java.lang.String, java.lang.String)
   */
  @Override
  public Form getForm(String name, String role) {
    Form form2Return = null;
    for (Form form : formList) {
      if (name.equals(form.getName())) {
        if (role != null && role.equalsIgnoreCase(form.getRole())) {
          form2Return = form;
        } else if (form.getRole() == null && form2Return == null) {
          form2Return = form;
        }
      }
    }

    return form2Return;
  }

  /*
   * (non-Javadoc)
   * @see Forms#iterateForm()
   */
  @Override
  public Iterator<Form> iterateForm() {
    return formList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see Forms#removeForm(java.lang.String, java.lang.String)
   */
  @Override
  public void removeForm(String strName, String strRole) throws WorkflowException {
    Iterator<Form> iter = formList.iterator();
    while (iter.hasNext()) {
      Form form = iter.next();
      if (form.getName().equals(strName) && (strRole == null && form.getRole() == null
          || strRole != null && strRole.equals(form.getRole()))) {
        iter.remove();
        return;
      }
    }
    throw new WorkflowException("FormsImpl.removeForm", SilverpeasException.ERROR,
        "workflowEngine.EX_FORM_NOT_FOUND");

  }
}