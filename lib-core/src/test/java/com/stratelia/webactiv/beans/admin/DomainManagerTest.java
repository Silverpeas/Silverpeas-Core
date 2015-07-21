/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.beans.admin;

import com.silverpeas.components.model.AbstractTestDao;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.silverpeas.admin.user.constant.UserAccessLevel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author ebonnet
 */
public class DomainManagerTest extends AbstractTestDao {

  private Admin instance;

  public DomainManagerTest() {
  }

  @BeforeClass
  public void setUp() throws Exception {
    super.setUp();
    instance = AdminReference.getAdminService();
    instance.reloadCache();
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    instance.reloadCache();
  }

  @Override
  protected String getDatasetFileName() {
    return "test-domain-manager-dataset.xml";
  }

  @Override
  protected String getTableCreationFileName() {
    return "create-database.sql";
  }

  @Test
  public void testGetDomainManagerUser() throws Exception {
    String sUserId = "5";
    String expectedDomainId = "2";
    UserDetail userDetail = instance.getUserDetail(sUserId);
    assertThat(userDetail.getDomainId(), is(expectedDomainId));
    assertThat(userDetail.getAccessLevel(), is(UserAccessLevel.DOMAIN_ADMINISTRATOR));
    assertThat(instance.isDomainManagerUser(sUserId, expectedDomainId), is(true));
    assertThat(instance.isDomainManagerUser("4", expectedDomainId), is(false));
    assertThat(instance.isDomainManagerUser("0", "0"), is(false));
  }
  
}