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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.cmis;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.silverpeas.cmis.util.CmisProperties;
import org.silverpeas.cmis.walkers.AbstractCmisObjectsTreeWalker;
import org.silverpeas.cmis.walkers.CmisObjectsTreeWalker;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.model.CmisFile;
import org.silverpeas.core.cmis.model.CmisObject;

import java.util.List;

/**
 * A manager of CMIS objects that are stored into Silverpeas. It is responsible to map the
 * organizational resources (spaces, component instances, etc.) and contributions in Silverpeas to
 * the organizational tree of CMIS objects (folders, documents, relationships, and so on). It is the
 * one that has the knowledge of how the Silverpeas objects are mapped to the CMIS objects while
 * satisfying the CMIS specification requirements. It is dedicated to be used by a CMIS repository
 * implementation.
 * <p>
 * In order to facilitate the mapping between the organizational tree of CMIS objects and the
 * organization of the Silverpeas resources, the manager defines a one-to-one correspondence of its
 * methods with those of the CMIS services. Hence, the client of the manager has just to delegate
 * the CMIS service's operation by invoking one of the manager's method with the correct parameters
 * correspondence.
 * </p>
 * <p>
 * The walking through the tree of CMIS objects and the modification operations on those objects
 * (and therefore their corresponding Silverpeas resources and contributions) is performed by a
 * {@link AbstractCmisObjectsTreeWalker} instance that knows both how to walk through a given
 * subtree and how to perform CRUD operations on the objects of that subtree according to the type
 * of these objects. For example, the walking down a subtree isn't done identically when rooted to a
 * space than when rooted to an application (as they don't accept the same children type) and don't
 * support necessary the same operations.
 * </p>
 * @author mmoquillon
 */
@Service
public class SilverpeasCmisObjectManager {

  private static final String FOLDER = "folder";

  /**
   * Creates into the specified parent folder a {@link org.silverpeas.core.cmis.model.CmisObject}
   * object from its specified CMIS properties expressed in the given language. The concrete type of
   * the CMIS object to create is given by the
   * {@link org.apache.chemistry.opencmis.commons.PropertyIds#OBJECT_TYPE_ID}
   * property. If the creation of instances of such concrete type isn't supported, then a {@link
   * org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException} exception is
   * thrown.
   * @param folderId the unique identifier of the parent folder.
   * @param properties the CMIS properties of the object to create.
   * @param language the ISO 639-1 code of the language in which the textual properties are
   * expressed.
   * @return the unique identifier of the newly created CMIS object.
   */
  public CmisObject createFolder(final String folderId, final Properties properties,
      final String language) {
    checkArgumentNotNull(FOLDER, folderId);
    checkArgumentNotNull("properties", properties);

    CmisProperties cmisProperties = new CmisProperties(properties);
    checkTypeIdIsCorrect(BaseTypeId.CMIS_FOLDER, cmisProperties.getObjectTypeId().getBaseTypeId());
    return CmisObjectsTreeWalker.getInstance()
        .createChildData(folderId, cmisProperties, null, language);
  }

  /**
   * Creates into the specified parent folder a {@link org.silverpeas.core.cmis.model.DocumentFile}
   * object from its specified CMIS properties and from the specified content both expressed in the
   * given language. The concrete type of the CMIS object to create is given by the {@link
   * org.apache.chemistry.opencmis.commons.PropertyIds#OBJECT_TYPE_ID} property and must be {@link
   * org.silverpeas.core.cmis.model.TypeId#SILVERPEAS_DOCUMENT} otherwise a {@link
   * org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException} exception is
   * thrown. If an error occurs while storing the content stream, then a {@link
   * CmisStorageException} exception is thrown.
   * @param folderId the unique identifier of the parent folder.
   * @param properties the CMIS properties of the document to create.
   * @param content a stream on the document's content to store into a file in Silverpeas. The
   * stream is consumed but not closed by this method.
   * @param language the ISO 639-1 code of the language in which the textual properties are
   * expressed.
   * @return the unique identifier of the newly created CMIS object.
   */
  public CmisObject createDocument(final String folderId, final Properties properties,
      final ContentStream content, final String language) {
    checkArgumentNotNull(FOLDER, folderId);
    checkArgumentNotNull("properties", properties);

    CmisProperties cmisProperties = new CmisProperties(properties);
    checkTypeIdIsCorrect(cmisProperties.getObjectTypeId()
        .getBaseTypeId(), BaseTypeId.CMIS_DOCUMENT);
    return CmisObjectsTreeWalker.getInstance()
        .createChildData(folderId, cmisProperties, content, language);
  }

  /**
   * Updates the specified document with the new content provided by the given stream.
   * @param documentId the unique identifier of the document to update.
   * @param overwrite indicates if the document's content must be overwritten by the new one or just
   * to set the content for an empty document. In this last case, if the document isn't really empty
   * then a
   * {@link org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException}
   * is thrown.
   * @param contentStream a stream on the document's content to store into a file in Silverpeas. The
   * stream is consumed but not closed by this method.
   * @param language the ISO 639-1 code of the language in which the textual properties are
   * expressed.
   */
  public void updateDocument(final String documentId, final boolean overwrite,
      final ContentStream contentStream, final String language) {
    checkArgumentNotNull(FOLDER, documentId);
    checkArgumentNotNull("content stream", contentStream);

    // by default, in Silverpeas, a document must have a content!
    if (!overwrite) {
      throw new CmisContentAlreadyExistsException("The content of the document " + documentId +
          " already exist");
    }
    CmisObjectsTreeWalker.getInstance()
        .updateObjectData(documentId, new CmisProperties(), contentStream, language);
  }

  /**
   * Gets the specified object in Silverpeas.
   * @param objectId the unique identifier of the object.
   * @param filtering filtering parameters to apply on the characteristics to include with the
   * object.
   * @return the {@link CmisObject} instance matching the given identifier.
   */
  public CmisObject getObject(final String objectId, final Filtering filtering) {
    checkArgumentNotNull("object", objectId);
    return CmisObjectsTreeWalker.getInstance().getObjectData(objectId, filtering);
  }

  /**
   * Gets the object that is defined at the specified path in the organizational CMIS tree.
   * @param path the path of the object from the root. The path is made up of segments, each of
   * them separated by the '/' character. A segment can be either the name of a CMIS fileable
   * object or its unique identifier. For better performance we recommend strongly to use
   * object's identifiers for segments.
   * @param filtering parameters to apply on the characteristics to include with the
   * object.
   * @return the {@link CmisFile} instance located at the given path.
   */
  public CmisFile getObjectByPath(final String path, final Filtering filtering) {
    checkArgumentNotNull("path", path);
    if (path.trim().isEmpty() || path.charAt(0) != '/') {
      throw new CmisInvalidArgumentException("The path isn't valid!");
    }
    return CmisObjectsTreeWalker.getInstance().getObjectDataByPath(path, filtering);
  }

  /**
   * Gets the objects that are children of the specified folder.
   * @param folderId the unique identifier of a folder, whatever its concrete type in Silverpeas.
   * @param filtering the filtering parameters to apply on the characteristics to include with each
   * object.
   * @param paging paging parameters to apply on the list of objects to return.
   * @return a list of {@link org.apache.chemistry.opencmis.commons.data.ObjectInFolderData}
   * objects, each of them carrying a {@link CmisFile} instance as a child of the given folder.
   */
  public ObjectInFolderList getChildren(final String folderId, final Filtering filtering,
      final Paging paging) {
    checkArgumentNotNull(FOLDER, folderId);
    return CmisObjectsTreeWalker.getInstance().getChildrenData(folderId, filtering, paging);
  }

  /**
   * Gets the file-able objects that are the descendents of the specified folder down to the
   * given depth of its offspring tree.
   * @param folderId the unique identifier of a folder, whatever its concrete type in Silverpeas.
   * @param filtering the filtering parameters to apply on the characteristics to include with each
   * object.
   * @param depth the depth of the offspring tree to walk across. If 1, only
   * the direct children of the specified folder are returned, if negative all descendant objects at
   * all depth levels in the CMIS hierarchy are returned, any positive value means only objects that
   * are children of the folder and descendants down to the given levels deep.
   * @return the subtree rooted at the specified folder, upto the given depth value. The tree is a
   * list of containers of both a
   * {@link org.apache.chemistry.opencmis.commons.data.ObjectInFolderData} object and the children
   * of that object. Each {@link org.apache.chemistry.opencmis.commons.data.ObjectInFolderData}
   * instance carries a {@link CmisFile} instance as a descendent.
   */
  public List<ObjectInFolderContainer> getDescendants(final String folderId,
      final Filtering filtering, final long depth) {
    checkArgumentNotNull(FOLDER, folderId);
    return CmisObjectsTreeWalker.getInstance().getSubTreeData(folderId, filtering, depth);
  }

  /**
   * Gets the parents of the specified object.
   * @param objectId the unique identifier of an object in Silverpeas, whatever its concrete type
   * in Silverpeas.
   * @param filtering the filtering parameters to apply on the list of objects to return.
   * @return a list of {@link org.silverpeas.core.cmis.model.CmisFolder} instances that are the
   * direct parents of the given object.
   */
  public List<ObjectParentData> getParents(final String objectId, final Filtering filtering) {
    checkArgumentNotNull("object", objectId);
    return CmisObjectsTreeWalker.getInstance().getParentsData(objectId, filtering);
  }

  /**
   * Gets the content of the specified object starting at the given position and at the specified
   * size.
   * @param objectId the unique identifier of an object in Silverpeas.
   * @param language the ISO 639-1 code of the language of the object's content to get.
   * @param start the starting position in bytes of the content to get.
   * @param size the size of the content, from the starting position, to get.
   * @return a stream on the object's content.
   */
  public ContentStream getContentStream(final String objectId, final String language,
      final long start, final long size) {
    return CmisObjectsTreeWalker.getInstance().getContentStream(objectId, language, start, size);
  }

  private static void checkArgumentNotNull(final String argName, final Object arg) {
    if (arg == null) {
      final String name = argName.substring(0, 1).toUpperCase() + argName.substring(1);
      throw new CmisInvalidArgumentException(name + " should be set!");
    }
  }

  private static void checkTypeIdIsCorrect(final BaseTypeId expected, final BaseTypeId actual) {
    if (expected != actual) {
      throw new CmisInvalidArgumentException("Object type should be a " + expected.value());
    }
  }
}
  