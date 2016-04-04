/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.node.coordinates.persistence;

import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.coordinates.model.CoordinatePK;
import org.silverpeas.core.node.coordinates.model.CoordinatePoint;
import org.dbunit.Assertion;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;
import static org.silverpeas.core.test.rule.DbSetupRule.getActualDataSet;
import static org.silverpeas.core.test.rule.DbSetupRule.getSafeConnection;

/**
 *
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class CoordinatesDAOIntegrationTest {

  private static final String TABLE_CREATION_SCRIPT = "/node-create-database.sql";
  private static final String DATASET_XML_SCRIPT = "coordinates-test-dataset.xml";

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule(TABLE_CREATION_SCRIPT, DATASET_XML_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(CoordinatesDAOIntegrationTest.class)
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addIndexEngineFeatures()
        .addWysiwygFeatures()
        .testFocusedOn(
            war -> war.addPackages(true, "org.silverpeas.core.node")
                .addAsResource("node-create-database.sql")
                .addAsResource("org/silverpeas/node")
                .addAsResource("org/silverpeas/core/node"))
        .build();
  }

  /**
   * Test of selectByFatherIds method, of class CoordinatesDAO.
   * @throws Exception
   */
  @Test
  public void testSelectByFatherIds() throws Exception {
    try (Connection con = getSafeConnection()) {
      List<Integer> fatherIds = new ArrayList<>();
      fatherIds.add(1060);
      String instanceId = "kmax888";
      CoordinatePK pk = new CoordinatePK(null, null, instanceId);
      Collection<String> expResult = new ArrayList<>(2);
      expResult.add("1");
      expResult.add("2");
      @SuppressWarnings("unchecked") Collection<String> result =
          CoordinatesDAO.selectByFatherIds(con, fatherIds, pk);
      assertEquals(expResult, result);
    }
  }

  /**
   * Test of selectByFatherPaths method, of class CoordinatesDAO.
   * @throws Exception
   */
  @Test
  public void testSelectByFatherPaths() throws Exception {
    try (Connection con = getSafeConnection()) {
      List<String> fatherPaths = new ArrayList<>();
      fatherPaths.add("/0/1/1060");
      String instanceId = "kmax888";
      CoordinatePK pk = new CoordinatePK(null, null, instanceId);
      Collection<String> expResult = new HashSet<>(2);
      expResult.add("1");
      expResult.add("2");
      @SuppressWarnings("unchecked") HashSet<String> result =
          new HashSet<>(CoordinatesDAO.selectByFatherPaths(con, fatherPaths, pk));
      assertEquals(expResult, result);
    }
  }

  /**
   * Test of addCoordinate method, of class CoordinatesDAO.
   * @throws Exception
   */
  @Test
  public void testAddCoordinate() throws Exception {
    try (Connection con = getSafeConnection()) {
      String instanceId = "kmax888";
      CoordinatePK pk = new CoordinatePK(null, null, instanceId);
      List<CoordinatePoint> points = new ArrayList<>(2);
      CoordinatePoint point1 = new CoordinatePoint(1, 1000, false);
      point1.setOrder(1);
      CoordinatePoint point2 = new CoordinatePoint(2, 2000, true);
      point2.setOrder(2);
      points.add(point1);
      points.add(point2);
      CoordinatesDAO.addCoordinate(con, pk, points);
      IDataSet databaseDataSet = getActualDataSet(con);
      ITable actualTable = databaseDataSet.getTable("sb_coordinates_coordinates");

      // Load expected data from an XML dataset
      DefaultTable expectedTable = new DefaultTable("sb_coordinates_coordinates",
          new Column[]{new Column("coordinatesid", DataType.INTEGER),
              new Column("nodeid", DataType.INTEGER),
              new Column("coordinatesleaf", DataType.VARCHAR),
              new Column("coordinatesdisplayorder", DataType.INTEGER),
              new Column("instanceid", DataType.VARCHAR)});
      expectedTable.addRow(new Object[]{1, 1057, "0", 1, "kmax888"});
      expectedTable.addRow(new Object[]{1, 1058, "0", 2, "kmax888"});
      expectedTable.addRow(new Object[]{1, 1060, "1", 1, "kmax888"});
      expectedTable.addRow(new Object[]{1, 1064, "1", 2, "kmax888"});
      expectedTable.addRow(new Object[]{1, 1603, "0", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{1, 1604, "1", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{1, 1612, "1", 2, "kmax1144"});

      expectedTable.addRow(new Object[]{2, 1057, "0", 1, "kmax888"});
      expectedTable.addRow(new Object[]{2, 1058, "1", 2, "kmax888"});
      expectedTable.addRow(new Object[]{2, 1060, "1", 1, "kmax888"});
      expectedTable.addRow(new Object[]{2, 1603, "0", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1604, "1", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1612, "0", 2, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1613, "1", 2, "kmax1144"});

      expectedTable.addRow(new Object[]{3, 1057, "0", 1, "kmax888"});
      expectedTable.addRow(new Object[]{3, 1058, "1", 2, "kmax888"});
      expectedTable.addRow(new Object[]{3, 1061, "1", 1, "kmax888"});

      expectedTable.addRow(new Object[]{4, 1000, "0", 1, "kmax888"});
      expectedTable.addRow(new Object[]{4, 2000, "1", 2, "kmax888"});

      Assertion.assertEquals(expectedTable, actualTable);
    }
  }

  /**
   * Test of removeCoordinates method, of class CoordinatesDAO.
   * @throws Exception
   */
  @Test()
  @SuppressWarnings("unchecked")
  public void testRemoveCoordinates() throws Exception {
    try (Connection con = getSafeConnection()) {
      String instanceId = "kmax888";
      CoordinatePK pk = new CoordinatePK(null, null, instanceId);
      List<String> coordinateIds = new ArrayList<>(2);
      coordinateIds.add("1");
      coordinateIds.add("2");
      @SuppressWarnings("unchecked") Collection<Coordinate> result =
          CoordinatesDAO.selectCoordinatesByCoordinateIds(con, coordinateIds, pk);
      assertNotNull(result);
      assertEquals("We should have 2 coordinates", 2, result.size());
      Coordinate coordinate1 = new Coordinate();
      coordinate1.setCoordinateId(1);
      List<CoordinatePoint> points1 = new ArrayList<>();
      points1.add(new CoordinatePoint(1, 1060, true));
      points1.add(new CoordinatePoint(1, 1064, true));
      coordinate1.setCoordinatePoints(points1);
      assertThat(result, hasItem(coordinate1));

      Coordinate coordinate2 = new Coordinate();
      coordinate2.setCoordinateId(2);
      List<CoordinatePoint> points2 = new ArrayList<>();
      points2.add(new CoordinatePoint(2, 1060, true));
      points2.add(new CoordinatePoint(2, 1058, true));
      coordinate2.setCoordinatePoints(points2);
      assertThat(result, hasItem(coordinate2));
      CoordinatesDAO.removeCoordinates(con, pk, coordinateIds);
      result = CoordinatesDAO.selectCoordinatesByCoordinateIds(con, coordinateIds, pk);
      assertNotNull(result);
      assertEquals("We should have 2 coordinate", 2, result.size());
      coordinate1 = new Coordinate();
      coordinate1.setCoordinateId(1);
      coordinate1.setCoordinatePoints(new ArrayList<CoordinatePoint>());
      coordinate2 = new Coordinate();
      coordinate2.setCoordinateId(2);
      coordinate2.setCoordinatePoints(new ArrayList<CoordinatePoint>());
      assertThat(result, hasItem(coordinate1));
      assertThat(result, hasItem(coordinate2));
      IDataSet databaseDataSet = getActualDataSet(con);
      ITable actualTable = databaseDataSet.getTable("sb_coordinates_coordinates");

      // Load expected data from an XML dataset
      DefaultTable expectedTable = new DefaultTable("sb_coordinates_coordinates",
          new Column[]{new Column("coordinatesid", DataType.INTEGER),
              new Column("nodeid", DataType.INTEGER),
              new Column("coordinatesleaf", DataType.VARCHAR),
              new Column("coordinatesdisplayorder", DataType.INTEGER),
              new Column("instanceid", DataType.VARCHAR)});

      expectedTable.addRow(new Object[]{1, 1603, "0", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{1, 1604, "1", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{1, 1612, "1", 2, "kmax1144"});

      expectedTable.addRow(new Object[]{2, 1603, "0", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1604, "1", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1612, "0", 2, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1613, "1", 2, "kmax1144"});

      expectedTable.addRow(new Object[]{3, 1057, "0", 1, "kmax888"});
      expectedTable.addRow(new Object[]{3, 1058, "1", 2, "kmax888"});
      expectedTable.addRow(new Object[]{3, 1061, "1", 1, "kmax888"});

      Assertion.assertEquals(expectedTable, actualTable);
    }
  }

  /**
   * Test of removeCoordinatesByPoints method, of class CoordinatesDAO.
   * @throws Exception
   */
  @Test
  public void testRemoveCoordinatesByPoints() throws Exception {
    try (Connection con = getSafeConnection()) {
      String instanceId = "kmax888";
      CoordinatePK pk = new CoordinatePK(null, null, instanceId);
      List<String> coordinateIds = new ArrayList<>(2);
      coordinateIds.add("1");
      coordinateIds.add("2");
      @SuppressWarnings("unchecked") Collection<Coordinate> result =
          CoordinatesDAO.selectCoordinatesByCoordinateIds(con, coordinateIds, pk);
      assertNotNull(result);
      assertEquals("We should have 2 coordinates", 2, result.size());
      Coordinate coordinate1 = new Coordinate();
      coordinate1.setCoordinateId(1);
      List<CoordinatePoint> points1 = new ArrayList<>();
      points1.add(new CoordinatePoint(1, 1060, true));
      points1.add(new CoordinatePoint(1, 1064, true));
      coordinate1.setCoordinatePoints(points1);
      assertThat(result, hasItem(coordinate1));

      Coordinate coordinate2 = new Coordinate();
      coordinate2.setCoordinateId(2);
      List<CoordinatePoint> points2 = new ArrayList<>();
      points2.add(new CoordinatePoint(2, 1060, true));
      points2.add(new CoordinatePoint(2, 1058, true));
      coordinate2.setCoordinatePoints(points2);
      assertThat(result, hasItem(coordinate2));
      List<String> removedPoints = new ArrayList<>();
      removedPoints.add("1064");
      removedPoints.add("1060");
      removedPoints.add("1058");
      CoordinatesDAO.removeCoordinatesByPoints(con, pk, removedPoints);

      IDataSet databaseDataSet = getActualDataSet(con);
      ITable actualTable = databaseDataSet.getTable("sb_coordinates_coordinates");

      // Load expected data from an XML dataset
      DefaultTable expectedTable = new DefaultTable("sb_coordinates_coordinates",
          new Column[]{new Column("coordinatesid", DataType.INTEGER),
              new Column("nodeid", DataType.INTEGER),
              new Column("coordinatesleaf", DataType.VARCHAR),
              new Column("coordinatesdisplayorder", DataType.INTEGER),
              new Column("instanceid", DataType.VARCHAR)});
      expectedTable.addRow(new Object[]{1, 1057, "0", 1, "kmax888"});
      expectedTable.addRow(new Object[]{1, 1603, "0", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{1, 1604, "1", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{1, 1612, "1", 2, "kmax1144"});

      expectedTable.addRow(new Object[]{2, 1057, "0", 1, "kmax888"});
      expectedTable.addRow(new Object[]{2, 1603, "0", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1604, "1", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1612, "0", 2, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1613, "1", 2, "kmax1144"});

      expectedTable.addRow(new Object[]{3, 1057, "0", 1, "kmax888"});
      expectedTable.addRow(new Object[]{3, 1061, "1", 1, "kmax888"});

      Assertion.assertEquals(expectedTable, actualTable);
    }
  }

  /**
   * Test of selectCoordinatesByCoordinateIds method, of class CoordinatesDAO.
   * @throws Exception
   */
  @Test
  public void testSelectCoordinatesByCoordinateIds() throws Exception {
    try (Connection con = getSafeConnection()) {
      String instanceId = "kmax888";
      List<String> coordinateIds = new ArrayList<>(2);
      coordinateIds.add("1");
      coordinateIds.add("2");
      @SuppressWarnings("unchecked") Collection<Coordinate> result = CoordinatesDAO
          .selectCoordinatesByCoordinateIds(con, coordinateIds,
              new CoordinatePK(null, null, instanceId));
      assertNotNull(result);
      assertEquals("We should have 2 coordinates", 2, result.size());
      Coordinate coordinate1 = new Coordinate();
      coordinate1.setCoordinateId(1);
      List<CoordinatePoint> points1 = new ArrayList<>();
      points1.add(new CoordinatePoint(1, 1060, true));
      points1.add(new CoordinatePoint(1, 1064, true));
      coordinate1.setCoordinatePoints(points1);
      assertThat(result, hasItem(coordinate1));

      Coordinate coordinate2 = new Coordinate();
      coordinate2.setCoordinateId(2);
      List<CoordinatePoint> points2 = new ArrayList<>();
      points2.add(new CoordinatePoint(2, 1060, true));
      points2.add(new CoordinatePoint(2, 1058, true));
      coordinate2.setCoordinatePoints(points2);
      assertThat(result, hasItem(coordinate2));
    }
  }

  /**
   * Test of addPointToAllCoordinates method, of class CoordinatesDAO.
   * @throws Exception
   */
  @Test
  public void testAddPointToAllCoordinates() throws Exception {
    try (Connection con = getSafeConnection()) {
      String instanceId = "kmax888";
      CoordinatePK pk = new CoordinatePK(null, null, instanceId);
      CoordinatePoint point = new CoordinatePoint(-1, 1000, false);
      CoordinatesDAO.addPointToAllCoordinates(con, pk, point);
      IDataSet databaseDataSet = getActualDataSet(con);
      ITable actualTable = databaseDataSet.getTable("sb_coordinates_coordinates");

      // Load expected data from an XML dataset
      DefaultTable expectedTable = new DefaultTable("sb_coordinates_coordinates",
          new Column[]{new Column("coordinatesid", DataType.INTEGER),
              new Column("nodeid", DataType.INTEGER),
              new Column("coordinatesleaf", DataType.VARCHAR),
              new Column("coordinatesdisplayorder", DataType.INTEGER),
              new Column("instanceid", DataType.VARCHAR)});
      expectedTable.addRow(new Object[]{1, 1000, "0", 3, "kmax888"});
      expectedTable.addRow(new Object[]{1, 1057, "0", 1, "kmax888"});
      expectedTable.addRow(new Object[]{1, 1058, "0", 2, "kmax888"});
      expectedTable.addRow(new Object[]{1, 1060, "1", 1, "kmax888"});
      expectedTable.addRow(new Object[]{1, 1064, "1", 2, "kmax888"});
      expectedTable.addRow(new Object[]{1, 1603, "0", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{1, 1604, "1", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{1, 1612, "1", 2, "kmax1144"});

      expectedTable.addRow(new Object[]{2, 1000, "0", 3, "kmax888"});
      expectedTable.addRow(new Object[]{2, 1057, "0", 1, "kmax888"});
      expectedTable.addRow(new Object[]{2, 1058, "1", 2, "kmax888"});
      expectedTable.addRow(new Object[]{2, 1060, "1", 1, "kmax888"});
      expectedTable.addRow(new Object[]{2, 1603, "0", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1604, "1", 1, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1612, "0", 2, "kmax1144"});
      expectedTable.addRow(new Object[]{2, 1613, "1", 2, "kmax1144"});

      expectedTable.addRow(new Object[]{3, 1000, "0", 3, "kmax888"});
      expectedTable.addRow(new Object[]{3, 1057, "0", 1, "kmax888"});
      expectedTable.addRow(new Object[]{3, 1058, "1", 2, "kmax888"});
      expectedTable.addRow(new Object[]{3, 1061, "1", 1, "kmax888"});

      Assertion.assertEquals(expectedTable, actualTable);
    }
  }

  /**
   * Test of getCoordinateIdsByNodeId method, of class CoordinatesDAO.
   * @throws Exception
   */
  @Test
  public void testGetCoordinateIdsByNodeId() throws Exception {
    try (Connection con = getSafeConnection()) {
      String instanceId = "kmax888";
      CoordinatePK pk = new CoordinatePK(null, null, instanceId);
      String nodeId = "1060";
      @SuppressWarnings("unchecked") Collection<String> result =
          CoordinatesDAO.getCoordinateIdsByNodeId(con, pk, nodeId);
      assertNotNull(result);
      assertEquals("We should have 2 coordinates", 2, result.size());
      assertThat(result, hasItem("1"));
      assertThat(result, hasItem("2"));
    }
  }

  /**
   * Test of getCoordinateIdsByNodeId method, of class CoordinatesDAO.
   * @throws Exception
   */
  @Test
  public void testGetCoordinateIds() throws Exception {
    try (Connection con = getSafeConnection()) {
      String instanceId = "kmax888";
      CoordinatePK pk = new CoordinatePK(null, null, instanceId);
      @SuppressWarnings("unchecked") Collection<String> result =
          CoordinatesDAO.getCoordinateIds(con, pk);
      assertNotNull(result);
      assertEquals("We should have 3 coordinates", 3, result.size());
      assertThat(result, hasItem("1"));
      assertThat(result, hasItem("2"));
      assertThat(result, hasItem("3"));
    }
  }
}
