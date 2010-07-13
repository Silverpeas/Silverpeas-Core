/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.stratelia.webactiv.util.coordinates.ejb;

import com.silverpeas.components.model.AbstractTestDao;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePK;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePoint;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

/**
 *
 * @author ehugonnet
 */
public class CoordinatesDAOTest extends AbstractTestDao {

  public CoordinatesDAOTest() {
  }

  /**
   * Test of selectByFatherIds method, of class CoordinatesDAO.
   * @throws Exception 
   */
  @Test
  public void testSelectByFatherIds() throws Exception {
    Connection con = getConnection().getConnection();
    List<Integer> fatherIds = new ArrayList<Integer>();
    fatherIds.add(Integer.valueOf(1060));
    String instanceId = "kmax888";
    CoordinatePK pk = new CoordinatePK(null, null, instanceId);
    Collection<String> expResult = new ArrayList<String>(2);
    expResult.add("1");
    expResult.add("2");
    @SuppressWarnings("unchecked")
    Collection<String> result = CoordinatesDAO.selectByFatherIds(con, fatherIds, pk);
    assertEquals(expResult, result);
  }

  /**
   * Test of selectByFatherPaths method, of class CoordinatesDAO.
   * @throws Exception 
   */
  @Test
  public void testSelectByFatherPaths() throws Exception {
    Connection con = getConnection().getConnection();
    List<String> fatherPaths = new ArrayList<String>();
    fatherPaths.add("/0/1/1060");
    String instanceId = "kmax888";
    CoordinatePK pk = new CoordinatePK(null, null, instanceId);
    Collection<String> expResult = new ArrayList<String>(2);
    expResult.add("1");
    expResult.add("2");
    @SuppressWarnings("unchecked")
    Collection<String> result = CoordinatesDAO.selectByFatherPaths(con, fatherPaths, pk);
    assertEquals(expResult, result);
  }

  /**
   * Test of addCoordinate method, of class CoordinatesDAO.
   * @throws Exception 
   */
  @Test
  public void testAddCoordinate() throws Exception {
    Connection con = getConnection().getConnection();
    String instanceId = "kmax888";
    CoordinatePK pk = new CoordinatePK(null, null, instanceId);
    List<CoordinatePoint> points = new ArrayList<CoordinatePoint>(2);
    CoordinatePoint point1 = new CoordinatePoint(1, 1000, false);
    point1.setOrder(1);
    CoordinatePoint point2 = new CoordinatePoint(2, 2000, true);
    point2.setOrder(2);
    points.add(point1);
    points.add(point2);
    CoordinatesDAO.addCoordinate(con, pk, points);
    IDatabaseConnection dataSetConnection = getConnection();
    IDataSet databaseDataSet = dataSetConnection.createDataSet();
    ITable actualTable = databaseDataSet.getTable("sb_coordinates_coordinates");

    // Load expected data from an XML dataset
    DefaultTable expectedTable = new DefaultTable("sb_coordinates_coordinates", new Column[]{
          new Column("coordinatesid", DataType.INTEGER),
          new Column("nodeid", DataType.INTEGER),
          new Column("coordinatesleaf", DataType.VARCHAR),
          new Column("coordinatesdisplayorder", DataType.INTEGER),
          new Column("instanceid", DataType.VARCHAR)});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1057), "0", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1058), "0", Integer.
          valueOf(2), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1060), "1", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1064), "1", Integer.
          valueOf(2), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1603), "0", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1604), "1", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1612), "1", Integer.
          valueOf(2), "kmax1144"});

    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1057), "0", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1058), "1", Integer.
          valueOf(2), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1060), "1", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1603), "0", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1604), "1", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1612), "0", Integer.
          valueOf(2), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1613), "1", Integer.
          valueOf(2), "kmax1144"});

    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1057), "0", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1058), "1", Integer.
          valueOf(2), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1061), "1", Integer.
          valueOf(1), "kmax888"});

    expectedTable.addRow(new Object[]{Integer.valueOf(4), Integer.valueOf(1000), "0", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(4), Integer.valueOf(2000), "1", Integer.
          valueOf(2), "kmax888"});

    Assertion.assertEquals(expectedTable, actualTable);
    dataSetConnection.close();
  }

  /**
   * Test of removeCoordinates method, of class CoordinatesDAO.
   * @throws Exception 
   */
  @Test()
  @SuppressWarnings("unchecked")
  public void testRemoveCoordinates() throws Exception {
    Connection con = getConnection().getConnection();
    String instanceId = "kmax888";
    CoordinatePK pk = new CoordinatePK(null, null, instanceId);
    List<String> coordinateIds = new ArrayList<String>(2);
    coordinateIds.add("1");
    coordinateIds.add("2");
    @SuppressWarnings("unchecked")
    Collection<Coordinate> result = CoordinatesDAO.selectCoordinatesByCoordinateIds(con,
        coordinateIds, pk);
    assertNotNull(result);
    assertEquals("We should have 2 coordinates", 2, result.size());
    Coordinate coordinate1 = new Coordinate();
    coordinate1.setCoordinateId(1);
    List<CoordinatePoint> points1 = new ArrayList<CoordinatePoint>();
    points1.add(new CoordinatePoint(1, 1060, true));
    points1.add(new CoordinatePoint(1, 1064, true));
    coordinate1.setCoordinatePoints(points1);
    Assert.assertThat(result, JUnitMatchers.hasItem(coordinate1));

    Coordinate coordinate2 = new Coordinate();
    coordinate2.setCoordinateId(2);
    List<CoordinatePoint> points2 = new ArrayList<CoordinatePoint>();
    points2.add(new CoordinatePoint(2, 1060, true));
    points2.add(new CoordinatePoint(2, 1058, true));
    coordinate2.setCoordinatePoints(points2);
    Assert.assertThat(result, JUnitMatchers.hasItem(coordinate2));
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
    Assert.assertThat(result, JUnitMatchers.hasItem(coordinate1));
    Assert.assertThat(result, JUnitMatchers.hasItem(coordinate2));

    IDatabaseConnection dataSetConnection = getConnection();
    IDataSet databaseDataSet = dataSetConnection.createDataSet();
    ITable actualTable = databaseDataSet.getTable("sb_coordinates_coordinates");

    // Load expected data from an XML dataset
    DefaultTable expectedTable = new DefaultTable("sb_coordinates_coordinates", new Column[]{
          new Column("coordinatesid", DataType.INTEGER),
          new Column("nodeid", DataType.INTEGER),
          new Column("coordinatesleaf", DataType.VARCHAR),
          new Column("coordinatesdisplayorder", DataType.INTEGER),
          new Column("instanceid", DataType.VARCHAR)});

    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1603), "0", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1604), "1", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1612), "1", Integer.
          valueOf(2), "kmax1144"});

    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1603), "0", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1604), "1", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1612), "0", Integer.
          valueOf(2), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1613), "1", Integer.
          valueOf(2), "kmax1144"});

    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1057), "0", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1058), "1", Integer.
          valueOf(2), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1061), "1", Integer.
          valueOf(1), "kmax888"});

    Assertion.assertEquals(expectedTable, actualTable);
    dataSetConnection.close();
  }

  /**
   * Test of removeCoordinatesByPoints method, of class CoordinatesDAO.
   * @throws Exception 
   */
  @Test
  public void testRemoveCoordinatesByPoints() throws Exception {
    Connection con = getConnection().getConnection();
    String instanceId = "kmax888";
    CoordinatePK pk = new CoordinatePK(null, null, instanceId);
    List<String> coordinateIds = new ArrayList<String>(2);
    coordinateIds.add("1");
    coordinateIds.add("2");
    @SuppressWarnings("unchecked")
    Collection<Coordinate> result = CoordinatesDAO.selectCoordinatesByCoordinateIds(con,
        coordinateIds, pk);
    assertNotNull(result);
    assertEquals("We should have 2 coordinates", 2, result.size());
    Coordinate coordinate1 = new Coordinate();
    coordinate1.setCoordinateId(1);
    List<CoordinatePoint> points1 = new ArrayList<CoordinatePoint>();
    points1.add(new CoordinatePoint(1, 1060, true));
    points1.add(new CoordinatePoint(1, 1064, true));
    coordinate1.setCoordinatePoints(points1);
    Assert.assertThat(result, JUnitMatchers.hasItem(coordinate1));

    Coordinate coordinate2 = new Coordinate();
    coordinate2.setCoordinateId(2);
    List<CoordinatePoint> points2 = new ArrayList<CoordinatePoint>();
    points2.add(new CoordinatePoint(2, 1060, true));
    points2.add(new CoordinatePoint(2, 1058, true));
    coordinate2.setCoordinatePoints(points2);
    Assert.assertThat(result, JUnitMatchers.hasItem(coordinate2));
    List<String> removedPoints = new ArrayList<String>();
    removedPoints.add("1064");
    removedPoints.add("1060");
    removedPoints.add("1058");
    CoordinatesDAO.removeCoordinatesByPoints(con, pk, removedPoints);


    IDatabaseConnection dataSetConnection = getConnection();
    IDataSet databaseDataSet = dataSetConnection.createDataSet();
    ITable actualTable = databaseDataSet.getTable("sb_coordinates_coordinates");

    // Load expected data from an XML dataset
    DefaultTable expectedTable = new DefaultTable("sb_coordinates_coordinates", new Column[]{
          new Column("coordinatesid", DataType.INTEGER),
          new Column("nodeid", DataType.INTEGER),
          new Column("coordinatesleaf", DataType.VARCHAR),
          new Column("coordinatesdisplayorder", DataType.INTEGER),
          new Column("instanceid", DataType.VARCHAR)});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1057), "0", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1603), "0", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1604), "1", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1612), "1", Integer.
          valueOf(2), "kmax1144"});

    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1057), "0", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1603), "0", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1604), "1", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1612), "0", Integer.
          valueOf(2), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1613), "1", Integer.
          valueOf(2), "kmax1144"});

    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1057), "0", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1061), "1", Integer.
          valueOf(1), "kmax888"});

    Assertion.assertEquals(expectedTable, actualTable);
    dataSetConnection.close();
  }

  /**
   * Test of selectCoordinatesByCoordinateIds method, of class CoordinatesDAO.
   * @throws Exception 
   */
  @Test
  public void testSelectCoordinatesByCoordinateIds() throws Exception {
    Connection con = getConnection().getConnection();
    String instanceId = "kmax888";
    List<String> coordinateIds = new ArrayList<String>(2);
    coordinateIds.add("1");
    coordinateIds.add("2");
    @SuppressWarnings("unchecked")
    Collection<Coordinate> result = CoordinatesDAO.selectCoordinatesByCoordinateIds(con,
        coordinateIds,
        new CoordinatePK(null, null, instanceId));
    assertNotNull(result);
    assertEquals("We should have 2 coordinates", 2, result.size());
    Coordinate coordinate1 = new Coordinate();
    coordinate1.setCoordinateId(1);
    List<CoordinatePoint> points1 = new ArrayList<CoordinatePoint>();
    points1.add(new CoordinatePoint(1, 1060, true));
    points1.add(new CoordinatePoint(1, 1064, true));
    coordinate1.setCoordinatePoints(points1);
    Assert.assertThat(result, JUnitMatchers.hasItem(coordinate1));

    Coordinate coordinate2 = new Coordinate();
    coordinate2.setCoordinateId(2);
    List<CoordinatePoint> points2 = new ArrayList<CoordinatePoint>();
    points2.add(new CoordinatePoint(2, 1060, true));
    points2.add(new CoordinatePoint(2, 1058, true));
    coordinate2.setCoordinatePoints(points2);
    Assert.assertThat(result, JUnitMatchers.hasItem(coordinate2));
  }

  /**
   * Test of addPointToAllCoordinates method, of class CoordinatesDAO.
   * @throws Exception 
   */
  @Test
  public void testAddPointToAllCoordinates() throws Exception {
    Connection con = getConnection().getConnection();
    String instanceId = "kmax888";
    CoordinatePK pk = new CoordinatePK(null, null, instanceId);
    CoordinatePoint point = new CoordinatePoint(-1, 1000, false);
    CoordinatesDAO.addPointToAllCoordinates(con, pk, point);
    IDatabaseConnection dataSetConnection = getConnection();
    IDataSet databaseDataSet = dataSetConnection.createDataSet();
    ITable actualTable = databaseDataSet.getTable("sb_coordinates_coordinates");

    // Load expected data from an XML dataset
    DefaultTable expectedTable = new DefaultTable("sb_coordinates_coordinates", new Column[]{
          new Column("coordinatesid", DataType.INTEGER),
          new Column("nodeid", DataType.INTEGER),
          new Column("coordinatesleaf", DataType.VARCHAR),
          new Column("coordinatesdisplayorder", DataType.INTEGER),
          new Column("instanceid", DataType.VARCHAR)});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1000), "0", Integer.
          valueOf(3), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1057), "0", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1058), "0", Integer.
          valueOf(2), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1060), "1", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1064), "1", Integer.
          valueOf(2), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1603), "0", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1604), "1", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(1), Integer.valueOf(1612), "1", Integer.
          valueOf(2), "kmax1144"});

    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1000), "0", Integer.
          valueOf(3), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1057), "0", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1058), "1", Integer.
          valueOf(2), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1060), "1", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1603), "0", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1604), "1", Integer.
          valueOf(1), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1612), "0", Integer.
          valueOf(2), "kmax1144"});
    expectedTable.addRow(new Object[]{Integer.valueOf(2), Integer.valueOf(1613), "1", Integer.
          valueOf(2), "kmax1144"});

    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1000), "0", Integer.
          valueOf(3), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1057), "0", Integer.
          valueOf(1), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1058), "1", Integer.
          valueOf(2), "kmax888"});
    expectedTable.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(1061), "1", Integer.
          valueOf(1), "kmax888"});

    Assertion.assertEquals(expectedTable, actualTable);
    dataSetConnection.close();
  }

  /**
   * Test of getCoordinateIdsByNodeId method, of class CoordinatesDAO.
   * @throws Exception 
   */
  @Test
  public void testGetCoordinateIdsByNodeId() throws Exception {
    Connection con = getConnection().getConnection();
    String instanceId = "kmax888";
    CoordinatePK pk = new CoordinatePK(null, null, instanceId);
    String nodeId = "1060";
    @SuppressWarnings("unchecked")
    Collection<String> result = CoordinatesDAO.getCoordinateIdsByNodeId(con, pk, nodeId);
    assertNotNull(result);
    assertEquals("We should have 2 coordinates", 2, result.size());
    Assert.assertThat(result, JUnitMatchers.hasItem("1"));
    Assert.assertThat(result, JUnitMatchers.hasItem("2"));
  }

  /**
   * Test of getCoordinateIdsByNodeId method, of class CoordinatesDAO.
   * @throws Exception 
   */
  @Test
  public void testGetCoordinateIds() throws Exception {
    Connection con = getConnection().getConnection();
    String instanceId = "kmax888";
    CoordinatePK pk = new CoordinatePK(null, null, instanceId);
    @SuppressWarnings("unchecked")
    Collection<String> result = CoordinatesDAO.getCoordinateIds(con, pk);
    assertNotNull(result);
    assertEquals("We should have 3 coordinates", 3, result.size());
    Assert.assertThat(result, JUnitMatchers.hasItem("1"));
    Assert.assertThat(result, JUnitMatchers.hasItem("2"));
    Assert.assertThat(result, JUnitMatchers.hasItem("3"));
  }

  @Override
  protected String getDatasetFileName() {
    return "coordinates-test-dataset.xml";
  }
}
