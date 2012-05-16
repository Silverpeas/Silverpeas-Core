/**
 * Copyright (C) 2000 - 2011 Silverpeas
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


import com.silverpeas.jcrutil.model.SilverpeasRegister;
import javax.annotation.Resource;
import javax.jcr.Repository;
import org.junit.AfterClass;
import org.junit.Before;

public abstract class AbstractJcrRegisteringTestCase extends AbstractJcrTestCase {

  private static boolean registred = false;
  @Resource
  private Repository repository;

  public static boolean isRegistred() {
    return registred;
  }

  public Repository getRepository() {
    return this.repository;
  }

  public AbstractJcrRegisteringTestCase() {
    super();
  }

  @Before
  public void registerSilverpeasNodeTypes() throws Exception {
    System.out.print("Register Silverpeas Node Types");
    if (!registred) {
      String cndFileName = AbstractJcrRegisteringTestCase.class.getClassLoader().getResource(
          "silverpeas-jcr.txt").getFile().toString().replaceAll("%20", " ");
      SilverpeasRegister.registerNodeTypes(cndFileName);
      registred = true;
      System.out.println(" -> node types registered");
    } else {
      System.out.println(" -> node types already registered!");
    }
  }

  @AfterClass
  public static void unregisterSilverpeasNodeTypes() throws Exception {
    System.out.println("Unregister Silverpeas Node Types");
    registred = false;
  }
}
