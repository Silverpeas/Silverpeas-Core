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
package org.silverpeas.core.pdc.pdc.service;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.silverpeas.core.pdc.pdc.model.AxisHeaderPersistence;
import org.silverpeas.core.pdc.pdc.model.AxisPK;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.subscription.service.PdcSubscriptionService;
import org.silverpeas.core.pdc.tree.model.TreeNode;
import org.silverpeas.core.pdc.tree.model.TreeNodePK;
import org.silverpeas.core.pdc.tree.service.TreeService;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the checking of the subscriptions on the PdC when an axis or a value of an axis
 * is deleted: the subscriptions concerned by the deletion have to be updated or removed, and
 * their subscribers warned about it. This is the responsibility of the service on the
 * subscriptions on the PdC, to which the deleted axis or value has to be passed.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PdcSubscriptionsCheckOnDeletionTest {

  private static final String AXIS_ID = "3";
  private static final String AXIS_NAME = "Topics";
  private static final String TREE_ID = "1";
  private static final String VALUE_ID = "45";
  private static final String MOTHER_VALUE_ID = "12";
  private static final String PATH_OF_THE_MOTHER_VALUE = "/0/";
  private static final String PATH_OF_THE_VALUE = "/0/12/";

  private final Connection connection = mock(Connection.class);
  private final TreeService treeService = mock(TreeService.class);
  private final PdcSubscriptionService pdcSubscriptionService =
      mock(PdcSubscriptionService.class);
  // the subtree of the PdC whose the root is the value to delete
  private final List<TreeNode> subtree = new ArrayList<>();
  private final List<Value> pathOfTheValue = List.of(mock(Value.class), mock(Value.class));
  private MockedStatic<PdcRightsDAO> pdcRightsDAO;
  private GlobalPdcManager pdcManager;

  @BeforeEach
  void setUpThePdcManager(@TestManagedMock PdcManager aPdcManager) throws Exception {
    // the values of the PdC are got from the data source through a connection opened by the PdC
    // manager itself: it is spied to do without it
    pdcManager = spy(new GlobalPdcManager());
    FieldUtils.writeField(pdcManager, "treeService", treeService, true);
    FieldUtils.writeField(pdcManager, "pdcSubscriptionService", pdcSubscriptionService, true);
    FieldUtils.writeField(pdcManager, "pdcClassificationService",
        mock(PdcClassificationService.class), true);
    FieldUtils.writeField(pdcManager, "pdcClassifyManager", mock(PdcClassifyManager.class), true);
    FieldUtils.writeField(pdcManager, "pdcUtilizationService", mock(PdcUtilizationService.class),
        true);
    FieldUtils.writeField(pdcManager, "axisHeaderI18NDAO", mock(AxisHeaderI18NDAO.class), true);
    pdcRightsDAO = mockStatic(PdcRightsDAO.class);

    when(aPdcManager.getDaughterValues(anyString(), anyString())).thenReturn(List.of());
    setUpTheAxisOfThePdc();
    setUpTheValueToDelete();
  }

  @SuppressWarnings("unchecked")
  private void setUpTheAxisOfThePdc() throws Exception {
    final AxisHeaderPersistence axis = mock(AxisHeaderPersistence.class);
    when(axis.getPK()).thenReturn(new AxisPK(AXIS_ID));
    when(axis.getName()).thenReturn(AXIS_NAME);
    final SilverpeasBeanDAO<AxisHeaderPersistence> axisDao = mock(SilverpeasBeanDAO.class);
    when(axisDao.findByPrimaryKey(any(Connection.class), any(AxisPK.class))).thenReturn(axis);
    FieldUtils.writeField(pdcManager, "dao", axisDao, true);
  }

  private void setUpTheValueToDelete() throws Exception {
    final TreeNode value = aTreeNode(VALUE_ID, MOTHER_VALUE_ID, PATH_OF_THE_VALUE);
    final TreeNode motherValue = aTreeNode(MOTHER_VALUE_ID, "0", PATH_OF_THE_MOTHER_VALUE);
    subtree.add(value);
    when(treeService.getSubTree(any(), any(TreeNodePK.class), eq(TREE_ID))).thenAnswer(
        invocation -> new ArrayList<>(subtree));
    when(treeService.getNode(any(), any(TreeNodePK.class), eq(TREE_ID))).thenAnswer(
        invocation -> {
          final TreeNodePK pk = invocation.getArgument(1);
          return VALUE_ID.equals(pk.getId()) ? value : motherValue;
        });
    doAnswer(invocation -> {
      subtree.clear();
      return null;
    }).when(treeService).deleteNode(any(), any(TreeNodePK.class), eq(TREE_ID));

    final Value valueToDelete = new Value(value);
    doReturn(valueToDelete).when(pdcManager).getAxisValue(VALUE_ID, TREE_ID);
    doReturn(pathOfTheValue).when(pdcManager).getFullPath(VALUE_ID, TREE_ID);
  }

  @AfterEach
  void releaseTheStaticMocks() {
    pdcRightsDAO.close();
  }

  @Test
  void theSubscriptionsAreCheckedWhenAnAxisIsDeleted() throws Exception {
    pdcManager.deleteAxis(connection, AXIS_ID);

    verify(pdcSubscriptionService).checkAxisOnDelete(Integer.parseInt(AXIS_ID), AXIS_NAME);
  }

  /**
   * The contributions classified on the deleted value or on one of the values of its subtree are
   * then classified on the mother of the deleted value.
   */
  @Test
  void theSubscriptionsAreCheckedWhenAValueIsDeletedWithItsSubtree() throws Exception {
    pdcManager.deleteValueAndSubtree(connection, VALUE_ID, AXIS_ID, TREE_ID);

    verify(pdcSubscriptionService).checkValueOnDelete(Integer.parseInt(AXIS_ID), AXIS_NAME,
        List.of("/0/12/45/"), List.of("/0/12/"), pathOfTheValue);
  }

  /**
   * The deleted value is removed from the path of its daughters: the contributions classified on
   * it are then classified on its mother.
   */
  @Test
  void theSubscriptionsAreCheckedWhenAValueIsDeleted() throws Exception {
    pdcManager.deleteValue(connection, VALUE_ID, AXIS_ID, TREE_ID);

    verify(pdcSubscriptionService).checkValueOnDelete(Integer.parseInt(AXIS_ID), AXIS_NAME,
        List.of("/0/12/45/"), List.of("/0/12/"), pathOfTheValue);
  }

  @Test
  void noSubscriptionIsCheckedWhenTheValueToDeleteIsNotFound() throws Exception {
    subtree.clear();

    pdcManager.deleteValue(connection, VALUE_ID, AXIS_ID, TREE_ID);

    verify(pdcSubscriptionService, never()).checkValueOnDelete(anyInt(), anyString(), anyList(),
        anyList(), anyList());
  }

  private static TreeNode aTreeNode(final String id, final String fatherId, final String path) {
    final TreeNode node = new TreeNode(id, TREE_ID);
    node.setFatherId(fatherId);
    node.setPath(path);
    return node;
  }
}
