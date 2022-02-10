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
package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.JoinStatement;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * This class represents the old Content Management API. It is a gateway for all of the silverpeas
 * content management (documentation, ....)
 */
@Service
@Singleton
public class ContentManagementEngine implements Serializable {

  private static final String INSTANCE_TABLE = "SB_ContentManager_Instance";
  private static final String SILVER_CONTENT_TABLE = "SB_ContentManager_Content";
  private static final String CONTENT = "content";
  private static final String DELETE_FROM = "DELETE FROM ";
  private static final String WHERE_SILVER_CONTENT_ID_IS_SET = " WHERE silverContentId = ?";

  private final transient List<ContentPeas> acContentPeas = new ArrayList<>();
  // Container peas
  private final Map<String, String> mapBetweenComponentIdAndInstanceId = new HashMap<>();
  // Association SilverContentId (the key) internalContentId (the value) (cache)
  private final Map<String, String> mapBetweenSilverContentIdAndInternalComponentId =
      new HashMap<>();

  private ContentManagementEngine() {

  }

  @PostConstruct
  private void loadContentPeas() {
    // -------------------------------------------------
    // We don't have enough time to do the parsing !!!
    // We hard coded for this time !!!!
    // -------------------------------------------------

    // Put all the existing contents in the array of contents
    acContentPeas.add(new ContentPeas("whitePages"));
    acContentPeas.add(new ContentPeas("questionReply"));
    acContentPeas.add(new ContentPeas("kmelia"));
    acContentPeas.add(new ContentPeas("survey"));
    acContentPeas.add(new ContentPeas("toolbox"));
    acContentPeas.add(new ContentPeas("quickinfo"));
    acContentPeas.add(new ContentPeas("almanach"));
    acContentPeas.add(new ContentPeas("quizz"));
    acContentPeas.add(new ContentPeas("forums"));
    acContentPeas.add(new ContentPeas("pollingStation"));
    acContentPeas.add(new ContentPeas("bookmark"));
    acContentPeas.add(new ContentPeas("infoLetter"));
    acContentPeas.add(new ContentPeas("webSites"));
    acContentPeas.add(new ContentPeas("gallery"));
    acContentPeas.add(new ContentPeas("blog"));

    try {
      mapBetweenComponentIdAndInstanceId.putAll(loadMapping(null));
    } catch (ContentManagerException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  /**
   * return a list of identifiers of the resources matching the specified identifiers of {@link
   * SilverContent} objects.
   * @param contentIds a list of identifiers of
   * {@link org.silverpeas.core.contribution.contentcontainer.content.SilverContent}
   * objects.
   * @return a list of resource identifiers.
   */
  public List<String> getResourcesMatchingContents(final List<Integer> contentIds) {
    List<String> pks = new ArrayList<>();
    // for each id of SilverContent, we get the identifier of the matching resource.
    for (Integer contentId : contentIds) {
      try {
        String id = getInternalContentId(contentId);
        pks.add(id);
      } catch (ContentManagerException ignored) {
        // ignore unknown item
      }
    }
    return pks;
  }

  /**
   * When a generic component is instanciate, this function is called to register the association
   * between container and content
   * @param connection
   * @param sComponentId
   * @param sContainerType
   * @param sContentType
   * @return
   * @throws ContentManagerException
   */
  public int registerNewContentInstance(Connection connection, String sComponentId,
      String sContainerType, String sContentType) throws ContentManagerException {
    boolean bCloseConnection = false;

    // Check the minimum required
    this.checkParameters(sComponentId, sContainerType, sContentType);
    PreparedStatement prepStmt = null;
    Connection theConnection = connection;
    try {
      if (theConnection == null) {
        theConnection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // Compute the next instanceId
      int newInstanceId = DBUtil.getNextId(INSTANCE_TABLE, "instanceId");
      // Insert the association container - content
      String sSQLStatement = "INSERT INTO " + INSTANCE_TABLE +
          "(instanceId, componentId, containerType, contentType) VALUES (?, ?, ?, ?)";
      // Execute the insertion

      prepStmt = theConnection.prepareStatement(sSQLStatement);
      prepStmt.setInt(1, newInstanceId);
      prepStmt.setString(2, sComponentId);
      prepStmt.setString(3, sContainerType);
      prepStmt.setString(4, sContentType);
      prepStmt.executeUpdate();
      addMapping(sComponentId, newInstanceId);
      return newInstanceId;
    } catch (Exception e) {
      throw new ContentManagerException(
          "Cannot register content instance for component " + sComponentId +
              "  and with content type " + sContentType, e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection) {
        closeConnection(theConnection);
      }
    }
  }

  private void closeConnection(Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  /**
   * When a generic component instance is finalized, this function is called to unregister the
   * association between the container and its contents.
   * @param connection a connection to the database in which is stored the mapping.
   * @param sComponentId the unique identifier of the component instance.
   * @param sContainerType the type of the content container.
   * @param sContentType the type of the contents in the content container.
   * @throws ContentManagerException if an error occurs while unregister the content instance.
   */
  public void unregisterNewContentInstance(Connection connection, String sComponentId,
      String sContainerType, String sContentType) throws ContentManagerException {
    boolean bCloseConnection = false;
    this.checkParameters(sComponentId, sContainerType, sContentType);
    Connection theConnection = connection;
    try {
      if (theConnection == null) {
        theConnection = DBUtil.openConnection();
        bCloseConnection = true;
      }

      final String contentDeletion = DELETE_FROM + SILVER_CONTENT_TABLE + " WHERE " +
          "contentInstanceId IN (SELECT instanceId from " + INSTANCE_TABLE +
          " WHERE componentId = ? AND containerType = ? AND contentType = ?)";

      final String instanceDeletion = DELETE_FROM + INSTANCE_TABLE + " WHERE componentId = ?" +
          " AND containerType = ? AND contentType = ?";

      try (PreparedStatement deletion = theConnection.prepareStatement(contentDeletion)) {
        deleteMappingInDS(deletion, sComponentId, sContainerType, sContentType);
      }
      try (PreparedStatement deletion = theConnection.prepareStatement(instanceDeletion)) {
        deleteMappingInDS(deletion, sComponentId, sContainerType, sContentType);
      }

      removeMapping(sComponentId);
    } catch (Exception e) {
      throw new ContentManagerException(
          "Cannot unregister content instance for component " + sComponentId +
              " and with content type " + sContentType, e);
    } finally {
      if (bCloseConnection) {
        closeConnection(theConnection);
      }
    }
  }

  /**
   * Return the ContentPeas corresponding to the given componentId
   * @param sComponentId
   * @return
   * @throws ContentManagerException
   */
  public ContentPeas getContentPeas(String sComponentId) {
    return getContentPeasByComponentName(
        SilverpeasComponentInstance.getComponentName(sComponentId));
  }

  /**
   * Return the ContentPeas corresponding to the given component
   * @param componentName
   * @return
   * @throws ContentManagerException
   */
  public ContentPeas getContentPeasByComponentName(String componentName) {
    // Get the ContentPeas from the ContentType
    for (ContentPeas s_acContentPea : acContentPeas) {
      if (s_acContentPea.getType().equals(componentName)) {
        return s_acContentPea;
      }
    }
    return null;
  }

  /**
   * Add a silver content Called when a content add a document and register it to get its
   * SilverContentId in return
   */
  public int addSilverContent(Connection connection, String sInternalContentId, String sComponentId,
      String sAuthorId, SilverContentVisibility scv) throws ContentManagerException {
    //
    // creation d'un objet java.sql.Date qui represente la date systeme.
    //
    Date date = new Date();
    long time = date.getTime();
    java.sql.Date systemDate = new java.sql.Date(time);

    boolean bCloseConnection = false;

    SilverContentVisibility visibility = scv;
    if (visibility == null) {
      visibility = new SilverContentVisibility();
    }

    PreparedStatement prepStmt = null;
    Connection conn = connection;
    try {
      if (conn == null) {
        // Open connection
        conn = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // Get the contentInstanceId corresponding to the given componentId
      int nContentInstanceId = this.getContentInstanceId(sComponentId);

      // Compute the next silverContentId
      int newSilverContentId = DBUtil.getNextId(SILVER_CONTENT_TABLE, "silverContentId");

      // Insert the silverContent
      String sSQLStatement = "INSERT INTO " + SILVER_CONTENT_TABLE +
          "(silverContentId, internalContentId, contentInstanceid, authorId, creationDate, " +
          "beginDate, endDate, isVisible) VALUES (?, ?, ?, ?, ?, ? , ? , ? )";

      prepStmt = conn.prepareStatement(sSQLStatement);
      prepStmt.setInt(1, newSilverContentId);
      prepStmt.setString(2, sInternalContentId);
      prepStmt.setInt(3, nContentInstanceId);
      prepStmt.setInt(4, Integer.parseInt(sAuthorId));
      prepStmt.setDate(5, systemDate);
      prepStmt.setString(6, visibility.getBeginDate());
      prepStmt.setString(7, visibility.getEndDate());
      prepStmt.setInt(8, visibility.isVisible());
      prepStmt.executeUpdate();

      return newSilverContentId;
    } catch (Exception e) {
      throw new ContentManagerException(
          SilverpeasExceptionMessages.failureOnAdding(CONTENT, sInternalContentId), e);
    } finally {
      DBUtil.close(prepStmt);
      if (bCloseConnection) {
        closeConnection(conn);
      }
    }
  }

  /**
   * Remove a silver content Called when a content remove a document
   */
  public void removeSilverContent(Connection connection, int nSilverContentId)
      throws ContentManagerException {
    PreparedStatement prepStmt = null;
    try {
      // delete the silverContent
      String sSQLStatement = DELETE_FROM + SILVER_CONTENT_TABLE + WHERE_SILVER_CONTENT_ID_IS_SET;

      // Execute the delete
      prepStmt = connection.prepareStatement(sSQLStatement);
      prepStmt.setInt(1, nSilverContentId);
      prepStmt.executeUpdate();
    } catch (Exception e) {
      throw new ContentManagerException(failureOnDeleting(CONTENT, nSilverContentId), e);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private int getSilverContentId(Connection connection, String sInternalContentId,
      String sComponentId, boolean isGlobalSearch) throws ContentManagerException {
    int nSilverContentId = -1;
    String sSQLStatement = "SELECT silverContentId FROM " + SILVER_CONTENT_TABLE +
        " WHERE internalContentId = ? AND contentInstanceId = ";
    int iComponentId;
    if (isGlobalSearch) {
      sSQLStatement += "(select instanceId from " + INSTANCE_TABLE + " where componentId = ?)";
      iComponentId = -1;
    } else {
      sSQLStatement += "?";
      iComponentId = getContentInstanceId(sComponentId);
    }

    try (PreparedStatement stmt = connection.prepareStatement(sSQLStatement)) {
      stmt.setString(1, sInternalContentId);
      if (isGlobalSearch) {
        stmt.setString(2, sComponentId);
      } else {
        stmt.setInt(2, iComponentId);
      }

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          nSilverContentId = rs.getInt(1);
        }
      }

    } catch (SQLException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }

    return nSilverContentId;
  }

  /**
   * Return the SilverContentId corresponding to the given internalContentId Called when a content
   * remove a document
   */
  public int getSilverContentId(String sInternalContentId, String sComponentId)
      throws ContentManagerException {
    try (Connection connection = DBUtil.openConnection()) {
      return getSilverContentId(connection, sInternalContentId, sComponentId, false);
    } catch (Exception e) {
      throw new ContentManagerException(failureOnGetting(CONTENT, sInternalContentId), e);
    }
  }

  /**
   * Return the sorted list containing SilverContentIds corresponding to the list containing id et
   * instanceId The list is not null and not empty !! Called when a content remove a document
   */
  public SortedSet<Integer> getSilverContentId(List<String> documentFeature)
      throws ContentManagerException {
    SortedSet<Integer> alSilverContentId = new TreeSet<>();
    String sInternalContentId = "";
    String sComponentId = "";
    int silverContentId;
    try (final Connection connection = DBUtil.openConnection()) {
      // main loop to build sql queries
      for (int i = 0; i < documentFeature.size(); i = i + 2) {
        // Get the internamContentId and theinstanceId from the list
        sInternalContentId = documentFeature.get(i);
        sComponentId = documentFeature.get(i + 1);
        // Get the SilverContentId
        silverContentId = getSilverContentId(connection, sInternalContentId, sComponentId, true);

        // add the result into the sortedSet
        if (silverContentId != -1) {
          // le composant dont instanceId et objectId courant fait partie du PDC
          alSilverContentId.add(silverContentId);
        }

      }

      return alSilverContentId;
    } catch (Exception e) {
      throw new ContentManagerException(failureOnGetting(CONTENT, sInternalContentId), e);
    }
  }

  private String getInternalContentIdFromCache(String sSilverContentId) {
    return mapBetweenSilverContentIdAndInternalComponentId.get(sSilverContentId);
  }

  private void putInternalContentIdIntoCache(String sSilverContentId, String sInternalContentId) {
    mapBetweenSilverContentIdAndInternalComponentId.put(sSilverContentId, sInternalContentId);
  }

  /**
   * Return the InternalContentId corresponding to the given SilverContentId Called when a content
   * remove a document
   */
  public String getInternalContentId(int nSilverContentId) throws ContentManagerException {
    String sSilverContentId = String.valueOf(nSilverContentId);
    String sInternalContentId = getInternalContentIdFromCache(sSilverContentId);
    if (sInternalContentId == null) {
      Connection connection = null;
      PreparedStatement prepStmt = null;
      ResultSet resSet = null;
      try {
        // Open connection
        connection = DBUtil.openConnection();

        // Get the InternalContentId
        String sSQLStatement =
            "SELECT internalContentId FROM " + SILVER_CONTENT_TABLE +
                WHERE_SILVER_CONTENT_ID_IS_SET;

        // Execute the search

        prepStmt = connection.prepareStatement(sSQLStatement);
        prepStmt.setInt(1, nSilverContentId);
        resSet = prepStmt.executeQuery();

        // Fetch the result
        if (resSet.next()) {
          sInternalContentId = resSet.getString(1);
        }

        putInternalContentIdIntoCache(sSilverContentId, sInternalContentId);
      } catch (Exception e) {
        throw new ContentManagerException(failureOnGetting("internal content id", nSilverContentId),
            e);
      } finally {
        DBUtil.close(resSet, prepStmt);
        closeConnection(connection);
      }
    }
    return sInternalContentId;
  }

  /**
   * Return the content instance Id corresponding to the componentId
   */
  public int getContentInstanceId(String sComponentId) throws ContentManagerException {
    int contentInstanceId = -1;

    String sContentInstanceId = getInstanceId(sComponentId);
    if (sContentInstanceId != null) {
      contentInstanceId = Integer.parseInt(sContentInstanceId);
    } else {
      // the given instance is not registered. This code is used to maintains
      // compatibility with previous versions.
      String componentName = extractComponentNameFromInstanceId(sComponentId);
      contentInstanceId =
          registerNewContentInstance(null, sComponentId, "containerPDC", componentName);
    }

    return contentInstanceId;
  }

  public JoinStatement getPositionsByGenericSearch(String authorId, String afterDate,
      String beforeDate) {
    StringBuilder sSQLStatement = new StringBuilder(1000);

    JoinStatement joinStatement = new JoinStatement();
    List<String> alGivenTables = new ArrayList<>();
    List<String> alGivenKeys = new ArrayList<>();
    alGivenTables.add(SILVER_CONTENT_TABLE);
    alGivenKeys.add("silverContentId");

    joinStatement.setTables(alGivenTables);
    joinStatement.setJoinKeys(alGivenKeys);

    // works on the author
    if (authorId != null && !"".equals(authorId)) {
      sSQLStatement.append(" CMC.authorId = ").append(authorId);
    }

    // works on the beforeDate
    if (beforeDate != null && beforeDate.length() > 0) {
      if (sSQLStatement.length() > 0) {
        sSQLStatement.append(" AND ");
      }
      sSQLStatement.append(" CMC.creationDate < ? ");
    }

    // works on the afterDate
    if (afterDate != null && afterDate.length() > 0) {
      if (sSQLStatement.length() > 0) {
        sSQLStatement.append(" AND ");
      }
      sSQLStatement.append(" CMC.creationDate > ? ");
    }

    joinStatement.setWhere(sSQLStatement.toString());

    return joinStatement;
  }

  /**
   * retourne une liste d'instanceID a partir d'une Liste de silvercontentId
   * @param alSilverContentId - la liste de silvercontentId silvercontentId
   * @return la liste contenant les instances
   */
  public List<ResourceReference> getResourceReferencesByContentIds(List<Integer> alSilverContentId)
      throws ContentManagerException {
    try {
      final List<ResourceReference> result = new ArrayList<>();
      JdbcSqlQuery.executeBySplittingOn(alSilverContentId,
          (idBatch, ignore) -> JdbcSqlQuery.createSelect("C.internalcontentid, I.componentId ")
              .from(INSTANCE_TABLE + " I")
              .join(SILVER_CONTENT_TABLE + " C")
              .on("I.instanceId = C.contentInstanceId")
              .where("C.silverContentId")
              .in(idBatch)
              .execute(r -> {
                final ResourceReference ref = new ResourceReference(r.getString(1), r.getString(2));
                result.add(ref);
                return null;
              }));
      return result;
    } catch (Exception e) {
      throw new ContentManagerException(
          failureOnGetting("instance id for content", alSilverContentId), e);
    }
  }

  /**
   * Cette méthode retourne une liste de SilverContentId qui se trouve sous une instance de
   * jobPeas.
   * @param instanceId - l'id de l'instance (trucsAstuces978)
   * @return une liste de silvercontentId
   */
  public List<Integer> getSilverContentIdByInstanceId(String instanceId)
      throws ContentManagerException {
    List<Integer> allSilverContentIds = new ArrayList<>();

    final String sSQLStatement =
        "select C.silverContentId from " + INSTANCE_TABLE + " I, " + SILVER_CONTENT_TABLE +
            " C  where I.instanceId = C.contentInstanceId and I.componentId like ?";
    try(Connection con = DBUtil.openConnection();
      PreparedStatement prepStmt = con.prepareStatement(sSQLStatement)) {
      prepStmt.setString(1, instanceId);
      try(ResultSet resSet = prepStmt.executeQuery()) {

        // Fetch the result
        while (resSet.next()) {
          allSilverContentIds.add(resSet.getInt(1));
        }

        return allSilverContentIds;
      }
    } catch (Exception e) {
      throw new ContentManagerException(
          failureOnGetting("silverpeas content id for instance", instanceId), e);
    }
  }

  public void updateSilverContentVisibilityAttributes(final SilverContentVisibility scv,
      final int silverObjectId) throws ContentManagerException {
    if (scv != null) {

      // update the silverContent
      String sSQLStatement =
          "UPDATE " + SILVER_CONTENT_TABLE + " SET beginDate = ? , endDate = ? , isVisible = ? " +
              WHERE_SILVER_CONTENT_ID_IS_SET;

      try (Connection con = DBUtil.openConnection();
           PreparedStatement prepStmt = con.prepareStatement(sSQLStatement)) {
        prepStmt.setString(1, scv.getBeginDate());
        prepStmt.setString(2, scv.getEndDate());
        prepStmt.setInt(3, scv.isVisible());
        prepStmt.setInt(4, silverObjectId);
        prepStmt.executeUpdate();
      } catch (Exception e) {
        throw new ContentManagerException(failureOnUpdate("silverpeas content", silverObjectId), e);
      }

      ServiceProvider.getAllServices(SilverContentPostUpdate.class)
          .forEach(c -> c.postSilverpeasContentUpdate(silverObjectId));
    }
  }

  public SilverContentVisibility getSilverContentVisibility(int silverObjectId)
      throws ContentManagerException {
    SilverContentVisibility scv;

    // Get the SilverContentVisibility
    String sSQLStatement = "SELECT beginDate, endDate, isVisible FROM " + SILVER_CONTENT_TABLE +
        WHERE_SILVER_CONTENT_ID_IS_SET;
    try(Connection connection = DBUtil.openConnection();
    PreparedStatement prepStmt = connection.prepareStatement(sSQLStatement)) {
      prepStmt.setInt(1, silverObjectId);
      try(ResultSet resSet = prepStmt.executeQuery()) {

        // Fetch the result
        if (resSet.next()) {
          String beginDate = resSet.getString(1);
          String endDate = resSet.getString(2);
          int visibility = resSet.getInt(3);
          boolean isVisible = visibility != 0;
          scv = new SilverContentVisibility(beginDate, endDate, isVisible);
        } else {
          scv = null;
        }
      }
    } catch (SQLException e) {
      throw new ContentManagerException(
          failureOnGetting("visibility of silverpeas content", silverObjectId), e);
    }
    return scv;
  }

  private String extractComponentNameFromInstanceId(String instanceId) {
    char character;
    for (int i = 0; i < instanceId.length(); i++) {
      character = instanceId.charAt(i);
      if (character == '0' || character == '1' || character == '2' || character == '3' ||
          character == '4' || character == '5' || character == '6' || character == '7' ||
          character == '8' || character == '9') {

        return instanceId.substring(0, i);
      }
    }
    return instanceId;
  }

  private Map<String, String> getMapping() {
    return mapBetweenComponentIdAndInstanceId;
  }

  private String getInstanceId(String componentId) {
    return getMapping().get(componentId);
  }

  private void addMapping(String componentId, int instanceId) {
    getMapping().put(componentId, java.lang.Integer.toString(instanceId));
  }

  private void removeMapping(String componentId) {
    getMapping().remove(componentId);
  }

  // Load the cache instanceId-componentId
  private Map<String, String> loadMapping(Connection connection) throws ContentManagerException {
    boolean bCloseConnection = false;
    PreparedStatement prepStmt = null;
    ResultSet resSet = null;
    Map<String, String> tempAsso = new HashMap<>();
    Connection conn = connection;
    try {
      if (conn == null) {
        // Open connection
        conn = DBUtil.openConnection();
        bCloseConnection = true;
      }

      // Get the instanceId
      String sSQLStatement = "SELECT instanceId, componentId FROM " + INSTANCE_TABLE;

      // Execute the insertion

      prepStmt = conn.prepareStatement(sSQLStatement);
      resSet = prepStmt.executeQuery();

      // Fetch the results
      while (resSet.next()) {
        tempAsso.put(resSet.getString(2), String.valueOf(resSet.getInt(1)));
      }
    } catch (Exception e) {
      throw new ContentManagerException("Cannot load content mapping cache", e);
    } finally {
      DBUtil.close(resSet, prepStmt);
      try {
        if (bCloseConnection) {
          DBUtil.close(conn);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    }
    return tempAsso;
  }

  private void deleteMappingInDS(PreparedStatement deletion, String... parameters)
      throws SQLException {
    for (int i = 0; i < parameters.length; i++) {
      deletion.setString(i + 1, parameters[i]);
    }
    deletion.execute();
  }

  private void checkParameters(String sComponentId, String sContainerType, String sContentType)
      throws ContentManagerException {
    // Check if the given componentId is not null
    if (isNotDefined(sComponentId)) {
      throw new ContentManagerException("The component instance id should be defined");
    }

    // Check if the given containerType is not null
    if (isNotDefined(sContainerType)) {
      throw new ContentManagerException("The type of the content container should be defined");
    }

    // Check if the given contentType is not null
    if (isNotDefined(sContentType)) {
      throw new ContentManagerException("The content type should be defined");
    }
  }

}