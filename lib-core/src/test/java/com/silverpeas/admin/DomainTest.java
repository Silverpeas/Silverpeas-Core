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

package com.silverpeas.admin;

import com.silverpeas.components.model.AbstractTestDao;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Domain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-domains.xml", "/spring-jpa-datasource.xml"})
public class DomainTest extends AbstractTestDao {

  private AdminController getAdminController() {
    AdminController ac = new AdminController("1");
    ac.reloadAdminCache();
    return ac;
  }
  
  @Test
  public void testGetDomain() {
    String domainId = "0";
    AdminController ac = getAdminController();
    Domain domain = new Domain();
    domain.setName("Silverpeas");
    domain.setPropFileName("com.stratelia.silverpeas.domains.domainSP");
    domain.setDriverClassName("com.silverpeas.domains.silverpeasdriver.SilverpeasDomainDriver");
    domain.setAuthenticationServer("autDomainSP");
    domain.setId(domainId);
    Domain savedDomain = ac.getDomain(domainId);
    assertThat(savedDomain, is(domain));
  }

  @Test
  public void testAddDomain() {
    AdminController ac = getAdminController();
    Domain domain = new Domain();
    domain.setName("Test new");
    domain.setDriverClassName("com.stratelia.silverpeas.domains.sqldriver.SQLDriver");
    domain.setPropFileName("com.stratelia.silverpeas.domains.domainSQL");
    domain.setAuthenticationServer("autDomainSQL");
    domain.setSilverpeasServerURL("http://localhost:8000");
    String domainId = ac.addDomain(domain);
    assertNotNull(domainId);
    assertEquals("1", domainId);
    domain.setId(domainId);
    Domain savedDomain = ac.getDomain(domainId);
     assertThat(savedDomain, is(domain));
  }
  
  @Override
  protected String getDatasetFileName() {
    return "test-spacesandcomponents-dataset.xml";
  }

}