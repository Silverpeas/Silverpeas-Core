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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.cmis.walkers;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.cmis.util.CmisProperties;
import org.silverpeas.core.cmis.model.CmisFile;
import org.silverpeas.core.cmis.model.CmisFilePath;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

/**
 * A walker of a CMIS objects tree in which each node and leaf are mapped to a given Silverpeas
 * organizational resource or to a given user contribution. The root of the tree is a virtual
 * container of all the Silverpeas root spaces. The walker provides a functional view to access the
 * CMIS objects but behind de scene it walks across the organizational schema of Silverpeas by using
 * its different services. Because the way to access an object in Silverpeas (and hence to browse
 * the CMIS objects tree) depends on the types of the Silverpeas objects implied in a walk of the
 * CMIS tree, the implementation of the walker is left to more concrete walkers specifically
 * designed to handle a peculiar type of Silverpeas objects. And consequently as each type of
 * Silverpeas objects is mapped to a peculiar type of a CMIS object, those concrete walkers are also
 * dedicated to handle, in their functional view, a peculiar type of CMIS objects.
 * @author mmoquillon
 */
public interface CmisObjectsTreeWalker {

  /**
   * Gets an instance of a {@link CmisObjectsTreeWalker}.
   * @return a {@link CmisObjectsTreeWalker} instance
   */
  static CmisObjectsTreeWalker getInstance() {
    return ServiceProvider.getSingleton(CmisObjectsTreeWalkerDelegator.class);
  }

  /**
   * Creates into the specified CMIS folder a new {@link CmisObject} child from the specified CMIS
   * data properties and in the given language. If the child is a document then the content stream
   * must be not null, otherwise, for any other object types, it is ignored even if it is set.
   * The type of the child to create is provided by the
   * {@link org.apache.chemistry.opencmis.commons.PropertyIds#OBJECT_TYPE_ID} property.
   * <p>
   * If the content stream isn't set whereas the object to create is a document, then a
   * {@link org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException} is thrown.
   * </p>
   * <p>
   * If the content stream is set whereas the object to create isn't a document, it is simply
   * ignored.
   * </p>
   * <p>
   * If an error occurs while registering the content from the specified content stream, then
   * a {@link org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException} is thrown.
   * </p>
   * @param folderId the unique identifier of a folder in the CMIS objects tree.
   * @param properties the CMIS properties of the child to create.
   * @param contentStream a stream on a content from which a document has to be created.
   * @param language the ISO 639-1 code of the language in which the textual properties of the
   * object as well as the content (if any) are expressed.
   * @return the created {@link CmisObject} object corresponding to the given CMIS properties.
   */
  CmisObject createChildData(String folderId, CmisProperties properties,
      ContentStream contentStream, String language);


  /**
   * Updates the specified CMIS object from the specified CMIS data properties and in the given
   * language. If the object is a document then the content stream must be not null, otherwise, for
   * any other object types, it is ignored even if it is set.
   * <p>
   * If the content stream isn't set whereas the object to create is a document, then a {@link
   * org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException} is thrown.
   * </p>
   * <p>
   * If the content stream is set whereas the object to create isn't a document, it is simply
   * ignored.
   * </p>
   * <p>
   * If an error occurs while registering the content from the specified content stream, then a
   * {@link org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException} is thrown.
   * </p>
   * @param objectId the unique identifier of an object in the CMIS objects tree.
   * @param properties the CMIS properties of the object to update.
   * @param contentStream a stream on a content from which a document has to be updated.
   * @param language the ISO 639-1 code of the language in which the textual properties of the
   * object as well as the content (if any) are expressed.
   * @return the updated {@link CmisObject} object corresponding to the given CMIS properties.
   */
  CmisObject updateObjectData(String objectId, CmisProperties properties,
      ContentStream contentStream, String language);

  /**
   * Gets the CMIS data of the Silverpeas object uniquely identified by the specified identifier.
   * Only the data satisfying the given filtering rules are returned.
   * @param objectId the unique identifier of a Silverpeas object.
   * @param filtering the filtering rules to apply on the data.
   * @return a {@link CmisObject} instance that provides the CMIS data of the specified Silverpeas
   * object.
   */
  CmisObject getObjectData(String objectId, Filtering filtering);

  /**
   * Gets the CMIS data of the Silverpeas object that is located at the specified path in the CMIS
   * objects tree. Only the data satisfying the given filtering rules are returned. The root of
   * the CMIS objects tree is the {@link CmisFilePath#PATH_SEPARATOR}.
   * @param path the path of the object in Silverpeas to get.
   * @param filtering the filtering rules to apply on the data.
   * @return an {@link ObjectData} instance that provides the CMIS data of the specified Silverpeas
   * object.
   */
  CmisFile getObjectDataByPath(String path, Filtering filtering);

  /**
   * Gets a list of the CMIS data of the Silverpeas objects that are the direct parents of the
   * CMIS object uniquely identified by the specified identifier. Only the data satisfying the given
   * filtering rules are returned. A file-able CMIS object can have one or more parents but in
   * Silverpeas, only publications in an EDM can have more than one parent (through the aliasing
   * mechanism), so unless the object referred by the specified identifier is an aliased
   * publication, this method returns usually a list with one element.
   * @param objectId the unique identifier of a Silverpeas resource or contribution.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @return a list of {@link ObjectParentData} elements, each of them being a wrapper of an
   * {@link ObjectData} instance with the path of the specified object relative to this parent. The
   * CMIS data are carried by the {@link ObjectData} object. If the specified object isn't
   * file-able or it is the root folder (the virtual root space in Silverpeas), then an empty list
   * is returned.
   */
  List<ObjectParentData> getParentsData(String objectId, Filtering filtering);

  /**
   * Gets a list of CMIS data of the Silverpeas objects that are children of the CMIS folder
   * uniquely identified by the specified identifier. The CMIS folder represents a Silverpeas
   * resource that is a container of others Silverpeas resources (space, component instance, ...).
   * If this method is invoked for a document instead of a folder, then an empty
   * {@link ObjectInFolderList} list should be returned.
   * @param folderId the unique identifier of a Silverpeas resource.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @param paging the paging to apply on the elements of the list.
   * @return an {@link ObjectInFolderList} instance that is a list of {@link ObjectInFolderData}
   * elements, each of them being a decorator of an {@link ObjectData} instance with its path in
   * the CMIS repository tree (if asked by the filtering). The CMIS data are carried by the
   * {@link ObjectData} object. If the folder has no children, then an empty
   * {@link ObjectInFolderList} instance is returned.
   */
  ObjectInFolderList getChildrenData(String folderId, Filtering filtering, Paging paging);

  /**
   * Gets the CMIS objects subtree rooted to the CMIS folder uniquely identified by the specified
   * identifier. The CMIS folder represents a Silverpeas resource that is a container of others
   * Silverpeas resources (space, component instance, ...). A list of the direct children of the
   * folder ({@link ObjectInFolderContainer} instances) is returned with, for each of them,
   * if any, their own direct children and so on. Each child is described by their CMIS data
   * (a decorator {@link ObjectInFolderData} of an {@link ObjectData} instance) filtered with the
   * given filtering rules. If this method is invoked for a document instead of a folder, then
   * an empty list should be returned.
   * @param folderId the unique identifier of a Silverpeas resource.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @param depth the maximum depth of the subtree to return from the specified folder. If -1, the
   * subtree will contain all the descendents at all depth levels. If 0, nothing. If 1, only the
   * direct children. If greater than 1 only children of the folder and descendants up to the given
   * levels deep. Any other values throws an {@link IllegalArgumentException} error.
   * @return a list of {@link ObjectInFolderContainer} elements (the direct children), each of them
   * being a container of others {@link ObjectInFolderContainer} objects (recursive walk of
   * children) and described by an {@link ObjectInFolderData} instance that is a decorator of an
   * {@link ObjectData} instance (the CMIS data) with its path in the CMIS repository tree (if asked
   * by the filtering). The CMIS data are carried by the {@link ObjectData} object. If the folder
   * has no children, then an empty list is returned.
   */
  List<ObjectInFolderContainer> getSubTreeData(String folderId, Filtering filtering, long depth);

  /**
   * Gets a stream on the content of the specified object, starting at the given offset position and
   * upto the given length.
   * @param objectId the unique identifier of the object in the CMIS objects tree.
   * @param language the ISO 639-1 code of the language of the content to fetch. If no content
   * exists in the specified language, then it is the content for the first language found that will
   * be returned (see {@link I18n#getSupportedLanguages()}).
   * @param offset the position in bytes in the content to start the stream.
   * @param length the length in bytes of the stream, id est the length of the content to read by
   * the stream.
   * @return a {@link ContentStream} instance.
   */
  ContentStream getContentStream(String objectId, String language, long offset, long length);

}
