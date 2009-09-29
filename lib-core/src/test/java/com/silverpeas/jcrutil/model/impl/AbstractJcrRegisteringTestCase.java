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
package com.silverpeas.jcrutil.model.impl;

import java.io.FileNotFoundException;

import javax.jcr.AccessDeniedException;
import javax.jcr.NamespaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
import org.apache.jackrabbit.core.nodetype.compact.ParseException;

import com.silverpeas.jcrutil.model.SilverpeasRegister;

public abstract class AbstractJcrRegisteringTestCase extends
    AbstractJcrTestCase {

  protected static boolean registred = false;

  protected Repository repository;

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public AbstractJcrRegisteringTestCase() {
    super();
  }

  public AbstractJcrRegisteringTestCase(String name) {
    super(name);
  }

  public void registerSilverpeasNodeTypes() throws NamespaceException,
      UnsupportedRepositoryOperationException, AccessDeniedException,
      RepositoryException, ParseException, FileNotFoundException,
      InvalidNodeTypeDefException {
    if (registred) {
      return;
    }
    String cndFileName = this.getClass().getClassLoader().getResource(
        "silverpeas-jcr.txt").getFile().toString().replaceAll("%20", " ");
    SilverpeasRegister.registerNodeTypes(cndFileName);
    registred = true;
  }

}
