/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.test.extention.TestManagedMock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@UnitTest
@EnableSilverTestEnv
class RemovedSpaceAndComponentInstanceCheckerTest {

  private static final String INSTANCE_ID = "kmelia3";

  @TestManagedMock
  private OrganizationController controller;

  @TestManagedBean
  private RemovedSpaceAndComponentInstanceChecker checker;

  private CheckerView checkerView;

  @BeforeEach
  void setup() {
    checkerView = new CheckerView(checker);
    when(controller.getPathToComponent(anyString())).then(a -> {
      final String componentId = a.getArgument(0, String.class);
      Optional<SilverpeasComponentInstance> componentInstance =
          controller.getComponentInstance(
          componentId);
      if (componentInstance.isPresent() && !componentInstance.get().isPersonal()) {
        return controller.getPathToSpace(componentInstance.get().getSpaceId());
      }
      return new ArrayList<>();
    });
    when(controller.getPathToSpace(anyString())).then(a -> {
      final String spaceId = a.getArgument(0, String.class);
      List<SpaceInstLight> path = new ArrayList<>();
      SpaceInstLight spaceInst = controller.getSpaceInstLightById(spaceId);
      if (spaceInst != null) {
        if (!spaceInst.isRoot()) {
          path.addAll(controller.getPathToSpace(spaceInst.getFatherId()));
        }
        path.add(0, spaceInst);
      }
      return path;
    });
  }

  @Test
  void componentInstanceIsNull() {
    assertThat(checker.isRemovedComponentInstanceById(null), is(true));
    assertThat(checkerView.getInstanceIdCache().size(), is(0));
    assertThat(checkerView.getSpaceIdCache().size(), is(0));
  }

  @Test
  void componentInstanceDoesNotExist() {
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(true));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(true));
    assertThat(checkerView.getSpaceIdCache().size(), is(0));
  }

  @Test
  void componentInstanceExistsWithoutParentSpace() {
    mockInstance();
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(true));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(true));
    assertThat(checkerView.getSpaceIdCache().size(), is(0));
  }

  @Test
  void componentInstanceExists() {
    final String spaceId = "spaceId";
    mockSpace(spaceId, mockInstance());
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(false));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(false));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(1));
    assertThat(spaceIdCache.get(spaceId), is(false));
  }

  @Test
  void componentInstanceRemoved() {
    final String spaceId = "spaceId";
    mockSpace(spaceId, mockRemovedInstance());
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(true));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(true));
    // when removed data is on component instance, no need to check space data
    assertThat(checkerView.getSpaceIdCache().size(), is(0));
  }

  @Test
  void componentInstanceIsPersonalOne() {
    final String spaceId = "spaceId";
    // creating a removed instance to demonstrate that only personal value will be taken into
    // account in this test case
    // It is same test as the previous one, but with the personal difference.
    final SilverpeasComponentInstance personalComponent = mockRemovedInstance();
    when(personalComponent.isPersonal()).thenReturn(true);
    mockSpace(spaceId, personalComponent);
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(false));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(false));
    // when removed data is on component instance, no need to check space data
    assertThat(checkerView.getSpaceIdCache().size(), is(0));
  }

  @Test
  void componentInstanceRemovedAndParentSpaceAlsoRemoved() {
    final String removedSpaceId = "removedSpaceId1";
    mockRemovedSpace(removedSpaceId, mockRemovedInstance());
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(true));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(true));
    // when removed data is on component instance, no need to check space data
    assertThat(checkerView.getSpaceIdCache().size(), is(0));
  }

  @Test
  void componentInstanceExistsButParentSpaceRemoved() {
    final String removedSpaceId = "removedSpaceId2";
    mockRemovedSpace(removedSpaceId, mockInstance());
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(true));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    // even if the removed data is on space, the removed check is cached at component instance level
    assertThat(instanceIdCache.get(INSTANCE_ID), is(true));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(1));
    assertThat(spaceIdCache.get(removedSpaceId), is(true));
  }

  @Test
  void componentInstanceExistsWithSeveralParentSpace() {
    final String spaceId1 = "spaceId1";
    final String spaceId12 = "spaceId12";
    final String spaceId123 = "spaceId123";
    mockSpace(spaceId1, mockSpace(spaceId12, mockSpace(spaceId123, mockInstance())));
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(false));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(false));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(3));
    assertThat(spaceIdCache.get(spaceId1), is(false));
    assertThat(spaceIdCache.get(spaceId12), is(false));
    assertThat(spaceIdCache.get(spaceId123), is(false));
  }

  @Test
  void componentInstanceRemovedWithSeveralParentSpace() {
    final String spaceId1 = "spaceId1";
    final String spaceId12 = "spaceId12";
    final String spaceId123 = "spaceId123";
    mockSpace(spaceId1, mockSpace(spaceId12, mockSpace(spaceId123, mockRemovedInstance())));
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(true));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(true));
    // when removed data is on component instance, no need to check space data
    assertThat(checkerView.getSpaceIdCache().size(), is(0));
  }

  @Test
  void componentInstanceRemovedWithSeveralParentSpaceAndDirectParentRemoved() {
    final String spaceId1 = "spaceId1";
    final String spaceId12 = "spaceId12";
    final String spaceId123 = "spaceId123";
    mockSpace(spaceId1, mockSpace(spaceId12, mockRemovedSpace(spaceId123, mockRemovedInstance())));
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(true));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(true));
    // when removed data is on component instance, no need to check space data
    assertThat(checkerView.getSpaceIdCache().size(), is(0));
  }

  @Test
  void componentInstanceExistsWithSeveralParentSpaceAndDirectParentRemoved() {
    final String spaceId1 = "spaceId1";
    final String spaceId12 = "spaceId12";
    final String spaceId123 = "spaceId123";
    mockSpace(spaceId1, mockSpace(spaceId12, mockRemovedSpace(spaceId123, mockInstance())));
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(true));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(true));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(1));
    assertThat(spaceIdCache.get(spaceId123), is(true));
  }

  @Test
  void componentInstanceExistsWithSeveralParentSpaceAndSecondParentRemoved() {
    final String spaceId1 = "spaceId1";
    final String spaceId12 = "spaceId12";
    final String spaceId123 = "spaceId123";
    mockSpace(spaceId1, mockRemovedSpace(spaceId12, mockSpace(spaceId123, mockInstance())));
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(true));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(true));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(2));
    assertThat(spaceIdCache.get(spaceId12), is(true));
    assertThat(spaceIdCache.get(spaceId123), is(false));
  }

  @Test
  void componentInstanceExistsWithSeveralParentSpaceAndAllParentsRemoved() {
    final String spaceId1 = "spaceId1";
    final String spaceId12 = "spaceId12";
    final String spaceId123 = "spaceId123";
    mockRemovedSpace(spaceId1, mockRemovedSpace(spaceId12, mockRemovedSpace(spaceId123, mockInstance())));
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(true));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(true));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(1));
    assertThat(spaceIdCache.get(spaceId123), is(true));
  }

  @Test
  void spaceIsNull() {
    assertThat(checker.isRemovedSpaceById(null), is(true));
    assertThat(checkerView.getInstanceIdCache().size(), is(0));
    assertThat(checkerView.getSpaceIdCache().size(), is(0));
  }

  @Test
  void spaceDoesNotExist() {
    final String spaceId = "spaceId";
    assertThat(checker.isRemovedSpaceById(spaceId), is(true));
    assertThat(checkerView.getInstanceIdCache().size(), is(0));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(1));
    assertThat(spaceIdCache.get(spaceId), is(true));
  }

  @Test
  void spaceExistsWithoutParentSpace() {
    final String spaceId = "spaceId";
    mockSpace(spaceId);
    assertThat(checker.isRemovedSpaceById(spaceId), is(false));
    assertThat(checkerView.getInstanceIdCache().size(), is(0));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(1));
    assertThat(spaceIdCache.get(spaceId), is(false));
  }

  @Test
  void spaceRemovedWithoutParentSpace() {
    final String spaceId = "spaceId";
    mockRemovedSpace(spaceId);
    assertThat(checker.isRemovedComponentInstanceById(INSTANCE_ID), is(true));
    final Map<String, Boolean> instanceIdCache = checkerView.getInstanceIdCache();
    assertThat(instanceIdCache.size(), is(1));
    assertThat(instanceIdCache.get(INSTANCE_ID), is(true));
    // when removed data is on component instance, no need to check space data
    assertThat(checkerView.getSpaceIdCache().size(), is(0));
  }

  @Test
  void spaceExistsWithSeveralParentSpaces() {
    final String spaceId1 = "spaceId1";
    final String spaceId12 = "spaceId12";
    final String spaceId123 = "spaceId123";
    mockSpace(spaceId1, mockSpace(spaceId12, mockSpace(spaceId123)));
    assertThat(checker.isRemovedSpaceById(spaceId123), is(false));
    assertThat(checkerView.getInstanceIdCache().size(), is(0));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(3));
    assertThat(spaceIdCache.get(spaceId1), is(false));
    assertThat(spaceIdCache.get(spaceId12), is(false));
    assertThat(spaceIdCache.get(spaceId123), is(false));
  }

  @Test
  void spaceRemovedWithSeveralParentSpaces() {
    final String spaceId1 = "spaceId1";
    final String spaceId12 = "spaceId12";
    final String spaceId123 = "spaceId123";
    mockSpace(spaceId1, mockSpace(spaceId12, mockRemovedSpace(spaceId123)));
    assertThat(checker.isRemovedSpaceById(spaceId123), is(true));
    assertThat(checkerView.getInstanceIdCache().size(), is(0));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(1));
    assertThat(spaceIdCache.get(spaceId123), is(true));
  }

  @Test
  void spaceExistsWithSeveralParentSpacesAndDirectParentRemoved() {
    final String spaceId1 = "spaceId1";
    final String spaceId12 = "spaceId12";
    final String spaceId123 = "spaceId123";
    mockSpace(spaceId1, mockRemovedSpace(spaceId12, mockSpace(spaceId123)));
    assertThat(checker.isRemovedSpaceById(spaceId123), is(true));
    assertThat(checkerView.getInstanceIdCache().size(), is(0));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(2));
    assertThat(spaceIdCache.get(spaceId12), is(true));
    assertThat(spaceIdCache.get(spaceId123), is(false));
  }

  @Test
  void spaceExistsWithSeveralParentSpacesAndAllParentsRemoved() {
    final String spaceId1 = "spaceId1";
    final String spaceId12 = "spaceId12";
    final String spaceId123 = "spaceId123";
    mockRemovedSpace(spaceId1, mockRemovedSpace(spaceId12, mockSpace(spaceId123)));
    assertThat(checker.isRemovedSpaceById(spaceId123), is(true));
    assertThat(checkerView.getInstanceIdCache().size(), is(0));
    final Map<String, Boolean> spaceIdCache = checkerView.getSpaceIdCache();
    assertThat(spaceIdCache.size(), is(2));
    assertThat(spaceIdCache.get(spaceId12), is(true));
    assertThat(spaceIdCache.get(spaceId123), is(false));
  }

  private SilverpeasComponentInstance mockRemovedInstance() {
    final SilverpeasComponentInstance instance = mockInstance();
    when(instance.isRemoved()).thenReturn(true);
    return instance;
  }

  private SilverpeasComponentInstance mockInstance() {
    final SilverpeasComponentInstance instance = mock(SilverpeasComponentInstance.class);
    when(instance.getId()).thenReturn(INSTANCE_ID);
    when(controller.getComponentInstance(INSTANCE_ID)).thenReturn(Optional.of(instance));
    return instance;
  }

  private SpaceInstLight mockRemovedSpace(final String id, SilverpeasComponentInstance child) {
    final SpaceInstLight instance = mockSpace(id, child);
    when(instance.isRemoved()).thenReturn(true);
    return instance;
  }

  private SpaceInstLight mockRemovedSpace(final String id, SpaceInstLight child) {
    final SpaceInstLight instance = mockSpace(id, child);
    when(instance.isRemoved()).thenReturn(true);
    return instance;
  }

  private SpaceInstLight mockRemovedSpace(final String id) {
    final SpaceInstLight instance = mockSpace(id);
    when(instance.isRemoved()).thenReturn(true);
    return instance;
  }

  private SpaceInstLight mockSpace(final String id, SilverpeasComponentInstance child) {
    final SpaceInstLight instance = mock(SpaceInstLight.class);
    when(instance.getId()).thenReturn(id);
    when(child.getSpaceId()).thenReturn(id);
    when(controller.getSpaceInstLightById(id)).thenReturn(instance);
    return instance;
  }

  private SpaceInstLight mockSpace(final String id, SpaceInstLight child) {
    final SpaceInstLight instance = mockSpace(id);
    when(child.getFatherId()).thenReturn(id);
    return instance;
  }

  private SpaceInstLight mockSpace(final String id) {
    final SpaceInstLight instance = mock(SpaceInstLight.class);
    when(instance.getId()).thenReturn(id);
    when(controller.getSpaceInstLightById(id)).thenReturn(instance);
    return instance;
  }
  
  private static class CheckerView {
    private final RemovedSpaceAndComponentInstanceChecker checker;

    private CheckerView(final RemovedSpaceAndComponentInstanceChecker checker) {
      this.checker = checker;
    }

    @SuppressWarnings("unchecked")
    Map<String, Boolean> getInstanceIdCache() {
      try {
        return (Map<String, Boolean>) readDeclaredField(checker, "instanceIdCache", true);
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException(e);
      }
    }

    @SuppressWarnings("unchecked")
    Map<String, Boolean> getSpaceIdCache() {
      try {
        return (Map<String, Boolean>) readDeclaredField(checker, "spaceIdCache", true);
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }
}