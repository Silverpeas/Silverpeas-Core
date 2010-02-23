/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.Form;
import com.silverpeas.workflow.api.model.Forms;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Class implementing the representation of the &lt;forms&gt; element of a Process Model.
 **/
public class FormsImpl implements Serializable, Forms {
  private List formList;

  /**
   * Constructor
   */
  public FormsImpl() {
    formList = new ArrayList();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Forms#addForm(com.silverpeas.workflow .api.model.Form)
   */
  public void addForm(Form form) {
    formList.add(form);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Forms#createForm()
   */
  public Form createForm() {
    return new FormImpl();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Forms#getForm(java.lang.String)
   */
  public Form getForm(String name) {
    Form form = null;

    for (int f = 0; f < formList.size(); f++) {
      form = (Form) formList.get(f);
      if (form.getName().equals(name)) {
        return form;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Forms#getForm(java.lang.String, java.lang.String)
   */
  public Form getForm(String name, String role) {
    Form form = null;
    Form form2Return = null;

    for (int f = 0; f < formList.size(); f++) {
      form = (Form) formList.get(f);

      if (name.equals(form.getName())) {
        if (role != null && role.equalsIgnoreCase(form.getRole())) {
          form2Return = form;
        } else if (form.getRole() == null && form2Return == null)
          form2Return = form;
      }
    }

    return form2Return;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Forms#iterateForm()
   */
  public Iterator iterateForm() {
    return formList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Forms#removeForm(java.lang.String, java.lang.String)
   */
  public void removeForm(String strName, String strRole)
      throws WorkflowException {
    Form form;

    for (int i = 0; i < formList.size(); i++) {
      form = (Form) formList.get(i);

      if (form.getName().equals(strName)
          && (strRole == null && form.getRole() == null || strRole != null
          && strRole.equals(form.getRole()))) {
        formList.remove(i);
        return;
      }
    }

    throw new WorkflowException("FormsImpl.removeForm", //$NON-NLS-1$
        SilverpeasException.ERROR, "workflowEngine.EX_FORM_NOT_FOUND"); //$NON-NLS-1$

  }
}