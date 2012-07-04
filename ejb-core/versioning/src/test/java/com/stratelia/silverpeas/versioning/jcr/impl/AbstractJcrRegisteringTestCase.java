/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.versioning.jcr.impl;

import com.silverpeas.jcrutil.model.SilverpeasRegister;
import org.apache.jackrabbit.commons.cnd.ParseException;

import javax.annotation.Resource;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import java.io.IOException;

public abstract class AbstractJcrRegisteringTestCase extends AbstractJcrTestCase {

  private static boolean registred = false;

  public static boolean isRegistred() {
    return registred;
  }

  public static void setRegistred(boolean registred) {
    AbstractJcrRegisteringTestCase.registred = registred;
  }
  
  @Resource
  private Repository repository;

  public AbstractJcrRegisteringTestCase() {
    super();
  }

  public Repository getRepository() {
    return repository;
  }

  public void registerSilverpeasNodeTypes() throws RepositoryException, ParseException,
        IOException {
    if (registred) {
      return;
    }
    String cndFileName = this.getClass().getClassLoader().getResource(
        "silverpeas-jcr.txt").getFile().toString().replaceAll("%20", " ");
    SilverpeasRegister.registerNodeTypes(cndFileName);
    registred = true;
  }

}
