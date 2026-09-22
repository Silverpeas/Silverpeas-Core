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
package org.silverpeas.core.node.dao;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.silverpeas.core.test.integration.rule.DbSetupRule.getSafeConnection;

/**
 * Verifies the sorting of the nodes applies to the component instance they belong to, and to it
 * only. The identifier of a node isn't unique by itself: the root node of every component instance
 * is numbered 0, and the ones below it can bear the same numbers from an instance to another. So
 * sorting the nodes of an instance must leave the ones of the other instances untouched.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class NodeSortingIT {

  private static final String TABLE_CREATION_SCRIPT = "/node-create-database.sql";
  private static final String DATASET_SCRIPT =
      "/org/silverpeas/core/node/dao/nodes-sorting-dataset.sql";

  private static final String THE_INSTANCE = "kmelia1";
  private static final String ANOTHER_INSTANCE = "kmelia2";

  @Inject
  private NodeDAO nodeDAO;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return LibCoreWarBuilder.onFullWarForTestClass(NodeSortingIT.class)
        .addAsResource("node-create-database.sql")
        .addAsResource("org/silverpeas/node")
        .addAsResource("org/silverpeas/core/node")
        .build();
  }

  @Test
  public void sortingTheNodesOfAnInstanceOrdersThem() throws Exception {
    try (final Connection connection = getSafeConnection()) {
      nodeDAO.sortNodes(connection, nodesOf(THE_INSTANCE, "2", "1"));
      assertEquals("the nodes should have been ordered as submitted",
          Arrays.asList(0, 1), orderNumbersIn(connection, THE_INSTANCE, "2", "1"));
    }
  }

  @Test
  public void sortingTheNodesOfAnInstanceLeavesTheOtherInstancesUntouched() throws Exception {
    try (final Connection connection = getSafeConnection()) {
      nodeDAO.sortNodes(connection, nodesOf(THE_INSTANCE, "2", "1"));
      assertEquals("the nodes of the other instance bear the same identifiers but shouldn't have " +
              "been ordered", Arrays.asList(7, 8),
          orderNumbersIn(connection, ANOTHER_INSTANCE, "1", "2"));
    }
  }

  /**
   * Sorting nodes on behalf of another instance changes nothing: they aren't the nodes of the
   * instance the sorting applies to.
   */
  @Test
  public void sortingTheNodesOfAnInstanceFromAnotherOneChangesNothing() throws Exception {
    try (final Connection connection = getSafeConnection()) {
      nodeDAO.sortNodes(connection, nodesOf(ANOTHER_INSTANCE, "2", "1"));
      assertEquals("the nodes of the instance shouldn't have been ordered",
          Arrays.asList(0, 1), orderNumbersIn(connection, THE_INSTANCE, "1", "2"));
    }
  }

  private List<NodePK> nodesOf(final String instanceId, final String... nodeIds) {
    return Arrays.stream(nodeIds).map(id -> new NodePK(id, instanceId)).collect(toList());
  }

  private List<Integer> orderNumbersIn(final Connection connection, final String instanceId,
      final String... nodeIds) throws Exception {
    final List<Integer> orderNumbers = new java.util.ArrayList<>();
    for (final String nodeId : nodeIds) {
      try (final PreparedStatement statement = connection.prepareStatement(
          "SELECT orderNumber FROM sb_node_node WHERE nodeId = ? AND instanceId = ?")) {
        statement.setInt(1, Integer.parseInt(nodeId));
        statement.setString(2, instanceId);
        try (final ResultSet rs = statement.executeQuery()) {
          rs.next();
          orderNumbers.add(rs.getInt(1));
        }
      }
    }
    return orderNumbers;
  }
}
