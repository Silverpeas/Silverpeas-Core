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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.wysiwyg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the getting of the applications a WYSIWYG editor can take images or files from.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class WysiwygBanksTest {

  @TestManagedMock
  private OrganizationController organizationController;

  @TestManagedMock
  private ComponentAccessControl componentAccessControl;

  private final User user = mock(User.class);

  @BeforeEach
  void setUp() {
    final List<ComponentInstLight> imageBanks =
        List.of(app("gallery", 3, "Photos"), app("gallery", 8, "Archives"));
    final List<ComponentInstLight> fileBanks = List.of(app("kmelia", 5, "Documents"));
    when(user.getId()).thenReturn("12");
    when(organizationController.getComponentsWithParameterValue("viewInWysiwyg", "yes"))
        .thenReturn(imageBanks);
    when(organizationController.getComponentsWithParameterValue("publicFiles", "yes"))
        .thenReturn(fileBanks);
  }

  @Test
  void theImageBanksAreTheApplicationsSharingTheirImagesAndAccessibleToTheUser() {
    authorize("gallery8");

    List<WysiwygBanks.Bank> banks = WysiwygBanks.imageBanksFor(user, "fr");

    assertThat(banks, contains(new WysiwygBanks.Bank("gallery", "8", "Archives")));
  }

  @Test
  void theFileBanksAreTheApplicationsSharingTheirFilesAndAccessibleToTheUser() {
    authorize("kmelia5");

    List<WysiwygBanks.Bank> banks = WysiwygBanks.fileBanksFor(user, "fr");

    assertThat(banks, contains(new WysiwygBanks.Bank("kmelia", "5", "Documents")));
  }

  @Test
  void thereIsNoBankWithoutAnyUser() {
    assertThat(WysiwygBanks.imageBanksFor(null, "fr"), empty());
    assertThat(WysiwygBanks.fileBanksFor(null, "fr"), empty());
  }

  @Test
  void theBanksAreGivenToTheEditorAsAJsonArray() {
    assertThat(WysiwygBanks.toJSON(List.of(new WysiwygBanks.Bank("kmelia", "5", "Documents"))),
        is("[{\"name\":\"kmelia\",\"id\":\"5\",\"label\":\"Documents\"}]"));
    assertThat(WysiwygBanks.toJSON(List.of()), is("[]"));
  }

  @SuppressWarnings("unchecked")
  private void authorize(final String... instanceIds) {
    when(componentAccessControl.filterAuthorizedByUser(anyCollection(), eq("12")))
        .thenAnswer(i -> ((Collection<String>) i.getArgument(0)).stream()
            .filter(List.of(instanceIds)::contains));
  }

  private static ComponentInstLight app(final String name, final int localId, final String label) {
    ComponentInstLight app = new ComponentInstLight();
    app.setName(name);
    app.setLocalId(localId);
    app.setLabel(label);
    return app;
  }
}
