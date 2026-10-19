/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.persistence.ComponentInstanceI18NTable;
import org.silverpeas.core.admin.persistence.ComponentInstanceRow;
import org.silverpeas.core.admin.persistence.ComponentInstanceTable;
import org.silverpeas.core.admin.persistence.InstanceDataTable;
import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.persistence.UserRoleTable;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the {@link ComponentInstManager}.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ComponentInstManagerTest {

  @TestManagedMock
  private OrganizationSchema organizationSchema;

  @TestedBean
  private ComponentInstManager manager;

  private ComponentInstanceTable instanceTable;

  @BeforeEach
  void mockTheDataSource() {
    instanceTable = mock(ComponentInstanceTable.class);
    when(organizationSchema.instance()).thenReturn(instanceTable);
    when(organizationSchema.instanceData()).thenReturn(mock(InstanceDataTable.class));
    when(organizationSchema.userRole()).thenReturn(mock(UserRoleTable.class));
    when(organizationSchema.instanceI18N()).thenReturn(mock(ComponentInstanceI18NTable.class));
  }

  @Test
  void anExistingComponentInstanceIsFound() throws AdminException, SQLException {
    ComponentInstanceRow row = new ComponentInstanceRow();
    row.id = 12;
    row.spaceId = 3;
    row.componentName = "kmelia";
    row.name = "Documentation";
    row.lang = "fr";
    when(instanceTable.getComponentInstance(12)).thenReturn(row);

    Optional<ComponentInst> instance = manager.getComponentInst(12);
    assertThat(instance.isPresent(), is(true));
    assertThat(instance.get().getId(), is("kmelia12"));
    assertThat(instance.get().getLabel(), is("Documentation"));
  }

  @Test
  void noComponentInstanceIsFoundForAnUnknownIdentifier() throws AdminException, SQLException {
    Optional<ComponentInst> instance = manager.getComponentInst(12);
    assertThat(instance.isEmpty(), is(true));
    verify(instanceTable).getComponentInstance(12);
  }

  @Test
  void anUndefinedComponentInstanceIsNotSearchedInTheDataSource() throws AdminException {
    assertThat(manager.getComponentInst(-1).isEmpty(), is(true));
    assertThat(manager.getComponentInst(0).isEmpty(), is(true));
    verifyNoInteractions(instanceTable);
  }
}
