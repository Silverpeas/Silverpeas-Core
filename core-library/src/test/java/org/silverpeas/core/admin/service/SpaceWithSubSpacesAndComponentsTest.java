/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.kernel.test.UnitTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.util.CollectionUtil.asSet;

/**
 * @author silveryocha
 */
@UnitTest
class SpaceWithSubSpacesAndComponentsTest {

  private SpaceWithSubSpacesAndComponents rootView;

  @BeforeEach
  void setup() {
    rootView = new SpaceWithSubSpacesAndComponents(new SpaceInstLight());
    final SpaceWithSubSpacesAndComponents space1Level1 = createSpaceView(1, 0, "kmelia1", "blog2");
    final SpaceWithSubSpacesAndComponents space2Level1 = createSpaceView(2, 0, "kmelia3", "blog4");
    final SpaceWithSubSpacesAndComponents space21Level2 = createSpaceView(21, 2, "almanach5");
    final SpaceWithSubSpacesAndComponents space211Level3 = createSpaceView(211, 21, "forums6", "gallery7");
    final SpaceWithSubSpacesAndComponents space22Level2 = createSpaceView(22, 2, "forums8", "gallery9");
    final SpaceWithSubSpacesAndComponents space3Level1 = createSpaceView(3, 0, "kmelia10", "blog11");
    final SpaceWithSubSpacesAndComponents space31Level2 = createSpaceView(31, 3, "blog12", "kmelia13");
    rootView.setSubSpaces(asList(space1Level1, space2Level1, space3Level1));
    space2Level1.setSubSpaces(asList(space21Level2, space22Level2));
    space21Level2.setSubSpaces(singletonList(space211Level3));
    space3Level1.setSubSpaces(singletonList(space31Level2));
  }

  @Test
  void componentInstanceSelectorSelectingWithoutParametrizationShouldNotWork() {
    Assertions.assertThrows(IllegalStateException.class, () -> {
      rootView.componentInstanceSelector().select();
    });
  }

  @Test
  void componentInstanceSelectorSelectingFromAllSpacesShouldWork() {
    final List<String> componentIds = rootView.componentInstanceSelector()
        .fromAllSpaces()
        .select().stream()
        .map(SilverpeasComponentInstance::getId).collect(Collectors.toList());
    assertThat(componentIds,
        contains("kmelia1", "blog2", "kmelia3", "blog4", "almanach5", "forums6", "gallery7",
            "forums8", "gallery9", "kmelia10", "blog11", "blog12", "kmelia13"));
  }

  @Test
  void componentInstanceSelectorSelectingFromGivenSpacesShouldWork() {
    final List<String> componentIds = rootView.componentInstanceSelector()
        .fromSpaces(asSet("3", "21"))
        .select().stream()
        .map(SilverpeasComponentInstance::getId).collect(Collectors.toList());
    assertThat(componentIds,
        contains("almanach5", "forums6", "gallery7", "kmelia10", "blog11", "blog12", "kmelia13"));
  }

  @Test
  void componentInstanceSelectorSelectingFromGivenSpacesExcludingInstancesShouldWork() {
    final List<String> componentIds = rootView.componentInstanceSelector()
        .fromSpaces(asSet("3", "21"))
        .excludingComponentInstances(asSet("forums6", "blog12"))
        .select().stream()
        .map(SilverpeasComponentInstance::getId).collect(Collectors.toList());
    assertThat(componentIds,
        contains("almanach5", "gallery7", "kmelia10", "blog11", "kmelia13"));
  }

  @Test
  void componentInstanceSelectorSelectingFromSubSpacesOfGivenSpacesShouldWork() {
    final List<String> componentIds = rootView.componentInstanceSelector()
        .fromSubSpacesOfSpaces(asSet("3", "21"))
        .select().stream()
        .map(SilverpeasComponentInstance::getId).collect(Collectors.toList());
    assertThat(componentIds, contains("forums6", "gallery7", "blog12", "kmelia13"));
  }

  private SpaceWithSubSpacesAndComponents createSpaceView(int spaceId, int fatherId, String ... componentIds) {
    final SpaceInstLight space = new SpaceInstLight();
    space.setLocalId(spaceId);
    space.setFatherId(fatherId);
    final SpaceWithSubSpacesAndComponents spaceView = new SpaceWithSubSpacesAndComponents(space);
    spaceView.setComponents(Stream.of(componentIds).map(i -> {
      final SilverpeasComponentInstance instance = mock(SilverpeasComponentInstance.class);
      when(instance.getId()).then(new Returns(i));
      return instance;
    }).collect(Collectors.toList()));
    return spaceView;
  }
}