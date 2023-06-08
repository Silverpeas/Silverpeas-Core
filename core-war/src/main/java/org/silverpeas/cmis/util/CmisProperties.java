/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.cmis.util;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.MutableProperties;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyBoolean;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.silverpeas.core.cmis.model.TypeId;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

/**
 * Each object in CMIS are defined by properties whose some of them are common to all of the CMIS
 * objects (name and description for example). This class is dedicated to access these properties
 * gathered in a {@link org.apache.chemistry.opencmis.commons.data.Properties} object for
 * Silverpeas. It provides a centralized and a common way to set and to get the usual CMIS
 * properties of the Silverpeas resources and contributions exposed in the CMIS objects tree.
 */
public class CmisProperties {

  private static final String NOTHING = null;
  private static final List<String> NOTHINGS = null;
  private static final String CONTENT_STREAM_PATH = "contentStreamPath";

  private final Properties properties;
  private final Set<String> filter;

  /**
   * Creates a {@link CmisProperties} instance wrapping by default a {@link MutableProperties}
   * object that can be get with the {@link CmisProperties#getProperties()} method.
   */
  public CmisProperties() {
    this(new PropertiesImpl());
  }

  /**
   * Creates a {@link CmisProperties} instance from the {@link Properties} object so that the CMIS
   * object's properties can be more easily accessed.
   * @param properties properties of a CMIS object.
   */
  public CmisProperties(final Properties properties) {

    this.properties = properties;
    this.filter = Collections.emptySet();
  }

  /**
   * Creates a {@link CmisProperties} instance from the {@link Properties} object so that the CMIS
   * object's properties can be more easily accessed. The specified filter indicates what properties
   * in {@link Properties} have to be taken into account; others properties will be ignored.
   * @param properties properties of a CMIS object.
   * @param filter a set of filtering rules to apply on the properties to access.
   */
  public CmisProperties(final Properties properties, final Set<String> filter) {
    this.properties = properties;
    this.filter = filter;
  }

  /**
   * Gets the underlying {@link Properties} object.
   * @return the underlying and wrapped {@link Properties} instance.
   */
  public Properties getProperties() {
    return properties;
  }

  /**
   * Gets the CMIS type of the object described by the underlying properties.
   * @return a {@link TypeId} value.
   */
  public TypeId getObjectTypeId() {
    String typeId = getId(PropertyIds.OBJECT_TYPE_ID);
    return TypeId.fromValue(typeId);
  }

  /**
   * Gets the unique identifier of the parent of the object.
   * @return a {@link String} representation of the identifier of the parent object.
   */
  public String getParentObjectId() {
    return getId(PropertyIds.PARENT_ID);
  }

  /**
   * Gets the name of the object described by the underlying properties.
   * @return the name of the CMIS object.
   */
  public String getName() {
    return getString(PropertyIds.NAME);
  }

  /**
   * Gets the short description about the object described by the underlying properties.
   * @return the description about the CMIS object.
   */
  public String getDescription() {
    return getString(PropertyIds.DESCRIPTION);
  }

  /**
   * Gets the date at which the object has been created as it was set in the underlying properties.
   * @return the creation date of the CMIS object.
   */
  public Date getCreationDate() {
    return new Date(getDateTime(PropertyIds.CREATION_DATE));
  }

  /**
   * Gets the MIME type of the content of the document.
   * @return the document content MIME Type.
   */
  public String getContentMimeType() {
    return getString(PropertyIds.CONTENT_STREAM_MIME_TYPE);
  }

  /**
   * Gets the name of the file in which is stored the document content.
   * @return the file name.
   */
  public String getContentFileName() {
    return getString(PropertyIds.CONTENT_STREAM_FILE_NAME);
  }

  /**
   * Gets the path of the file in which the content stream has been uploaded. This property is
   * only set at document creation or update. Otherwise it is always null.
   * @return the absolute path of the file with the content of a document.
   */
  public String getContentPath() {
    return getString(CONTENT_STREAM_PATH);
  }

  /**
   * Is this object indexed in Silverpeas?
   * @return true if this object is indexed, false otherwise.
   */
  public boolean isIndexed() {
    Boolean indexed = getValue("IS_INDEXED", PropertyBoolean.class);
    return Objects.requireNonNullElse(indexed, false);
  }

  /**
   * Sets the type and the base type of the CMIS object. Each object is CMIS is defined by a
   * concrete type and a base type from which the concrete type is derived.
   * @param typeId the identifier of a CMIS type to qualify a resource or a contribution of
   * Silverpeas into the CMIS tree.
   * @return itself
   */
  public CmisProperties setObjectTypeId(final TypeId typeId) {
    return setId(PropertyIds.OBJECT_TYPE_ID, typeId.value(), true)
        .setId(PropertyIds.BASE_TYPE_ID, typeId.getBaseTypeId().value(), true);
  }

  /**
   * Sets the unique identifier of the CMIS object in the CMIS tree.
   * @param id the CMIS object's unique identifier.
   * @return itself.
   */
  public CmisProperties setObjectId(final String id) {
    return setId(PropertyIds.OBJECT_ID, id, true);
  }

  /**
   * Sets the default common CMIS object properties as defined in Silverpeas. The CMIS properties
   * not supported or not handled by Silverpeas for its objects exposed in the CMIS tree are set
   * with a null value.
   * @return itself
   */
  public CmisProperties setDefaultProperties() {
    return setString(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, NOTHINGS, false)
        .setString(PropertyIds.CHANGE_TOKEN, NOTHING, false);
  }

  /**
   * Sets the name or the title of the CMIS object.
   * @param name the CMIS object name.
   * @return itself
   */
  public CmisProperties setName(final String name) {
    return setString(PropertyIds.NAME, name, false);
  }

  /**
   * Sets the short description of the CMIS object.
   * @param description a description of the CMIS object.
   * @return itself
   */
  public CmisProperties setDescription(final String description) {
    return setString(PropertyIds.DESCRIPTION, description, false);
  }

  /**
   * Sets the creator display name and the creation date at which the CMIS object was created. In
   * the case the CMIS object is a Private Working Copy of a document, then the creation date must
   * be the date at which the creation of the PWC was spawned and the creator should be set to the
   * name of the user that has checkout-ed the document. In the case the CMIS object is a version in
   * a version series for a document the creation data is about by who and when the PWC was created.
   * In the case of a non-versionable document, like any other CMIS objects, the creation data are
   * about by who and when the original document was created in the CMIS tree.
   * @param userName the full name of the user as to be displayed in the CMIS tree.
   * @param dateTime the creation date of the object in number of milliseconds from Epoch
   * @return itself
   */
  public CmisProperties setCreationData(final String userName, final long dateTime) {
    return setString(PropertyIds.CREATED_BY, userName, true)
        .setDateTime(PropertyIds.CREATION_DATE, dateTime, true);
  }

  /**
   * Sets the update display name and the date at which the CMIS object was lastly updated. In the
   * case of a Private Working Copy of a document, the last modification data are about by who and
   * when the PWC was updated (with the <code>PWCUpdatable</code> repository capability, several
   * users can work on a PWC).In the case of a given version in a version series for a document, the
   * last modification data is about by who and when the version was checked-in and hence became the
   * last version. For a non-versionable document as well as for others CMIS objects, these
   * properties are about by who and when the original document was lastly modified in the CMIS
   * tree.
   * @param userName the full name of the user as to be displayed in the CMIS tree.
   * @param dateTime the last modification date of the object in number of milliseconds from Epoch
   * @return itself
   */
  public CmisProperties setLastModificationData(final String userName, final long dateTime) {
    return setString(PropertyIds.LAST_MODIFIED_BY, userName, true)
        .setDateTime(PropertyIds.LAST_MODIFICATION_DATE, dateTime, true);
  }

  /**
   * Sets the type of all of the children allowed for the CMIS object represented by the underlying
   * properties.
   * @param objectTypeIds the unique identifier of the types of the allowed children objects.
   * @return itself
   */
  public CmisProperties setAllowedChildObjectTypeIds(final List<String> objectTypeIds) {
    return setString(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, objectTypeIds, false);
  }

  /**
   * Sets the unique identifier of the object that is parent of the CMIS object represented by the
   * underlying properties.
   * @param parentId the unique identifier of a CMIS object in the CMIS tree.
   * @return itself.
   */
  public CmisProperties setParentObjectId(final String parentId) {
    return setId(PropertyIds.PARENT_ID, parentId, true);
  }

  /**
   * Sets the path in the CMIS tree of the object represented by the underlying properties. Each
   * node of the path is the name of a parent from the CMIS tree root upto the name of the object.
   * @param path a path of the object in the CMIS tree from the CMIS tree root.
   * @return itself
   */
  public CmisProperties setPath(final String path) {
    return setString(PropertyIds.PATH, path, true);
  }

  public CmisProperties setImmutability(final boolean immutability) {
    return setBoolean(PropertyIds.IS_IMMUTABLE, immutability, true);
  }

  /**
   * Sets the versioning data of the document object exposed in the CMIS tree. Each document has a
   * version series that is an ordered sequence (by their creation date) of one or more document
   * objects that were created from an original document in the CMIS tree. These document objects
   * represent each of them a given version of the related original document. For a non-versionable
   * document, the version series has only one document object, the latest version.
   * @param seriesId the unique identifier of the version series id of the document.
   * @param label a version label (like "version 1.0" for example)
   * @param comment a comment about the version represented by the object
   * @param isLatestVersion is the object the latest version of the document's version series.
   * @param isMajorVersion is the object a major version in the document's version series.
   * @param isLatestMajorVersion is the object the lastest major version in the document's version
   * series.
   * @return itself.
   */
  public CmisProperties setVersioningData(String seriesId, String label, String comment,
      boolean isLatestVersion, boolean isMajorVersion, boolean isLatestMajorVersion) {
    if (isLatestMajorVersion && !isMajorVersion) {
      throw new CmisInvalidArgumentException("A latest major version must be also a major version");
    }
    return setId(PropertyIds.VERSION_SERIES_ID, seriesId, true)
        .setString(PropertyIds.VERSION_LABEL, label, true)
        .setString(PropertyIds.CHECKIN_COMMENT, comment, true)
        .setBoolean(PropertyIds.IS_LATEST_VERSION, isLatestVersion, true)
        .setBoolean(PropertyIds.IS_MAJOR_VERSION, isMajorVersion, true)
        .setBoolean(PropertyIds.IS_LATEST_MAJOR_VERSION, isLatestMajorVersion, true);
  }

  /**
   * Sets the Private Working Copy data if any for the CMIS document. A Private Working Copy is a
   * clone of a given document object created by a checkout operation. This PWC must have a unique
   * identifier different from the original document in the CMIS tree. A Private Working Copy is a
   * CMIS document private to one or more users (for a repository with the <code>PWCUpdatable</code>
   * capability) on which those users works until the modifications are pushed to the document in
   * the CMIS tree by a checkin operation; in this case, the PWC becomes a new version of the
   * original document (id est its latest version).
   * @param isThereAPwc is there exists currently a Private Working Copy for the version series of
   * the document?
   * @param isPwc is the object is a Private Working Copy of the document
   * @param pwcId the unique identifier of the Private Working Copy that has to be different from
   * the document identifier)
   * @param userId the identifier of the user that has created the Private Working Copy.
   * @return itself
   */
  public CmisProperties setPWCData(boolean isThereAPwc, boolean isPwc, String pwcId,
      String userId) {
    return setBoolean(PropertyIds.IS_PRIVATE_WORKING_COPY, isPwc, true)
        .setBoolean(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, isThereAPwc, true)
        .setId(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, pwcId, true)
        .setString(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, userId, true);
  }

  /**
   * Sets the content to the CMIS document.
   * @param id the unique identifier of the content.
   * @param mimeType the MIME type of the content.
   * @param length the length of the content in bytes.
   * @param fileName the name of the file in which is stored the content.
   * @return itself.
   */
  public CmisProperties setContent(String id, String mimeType, long length, String fileName) {
    return setId(PropertyIds.CONTENT_STREAM_ID, id, true)
        .setString(PropertyIds.CONTENT_STREAM_MIME_TYPE, mimeType, true)
        .setBigInteger(PropertyIds.CONTENT_STREAM_LENGTH, length, true)
        .setString(PropertyIds.CONTENT_STREAM_FILE_NAME, fileName, true);
  }

  /**
   * Sets the path at which is located the file into which the content stream has been uploaded.
   * @param contentFilePath the path of the content file.
   * @return itself.
   */
  public CmisProperties setContentPath(String contentFilePath) {
    return setString(CONTENT_STREAM_PATH, contentFilePath, false);
  }

  /**
   * Sets the indexation property of this object.
   * @param indexed the object is indexed or should be indexed.
   * @return itself.
   */
  public CmisProperties setIndexed(boolean indexed) {
    return setBoolean("IS_INDEXED", indexed, false);
  }

  /**
   * Gets the value of the specified identifier property.
   * @param propertyName the name of the property to get.
   * @return the identifier as a {@link String} value.
   */
  private String getId(String propertyName) {
    return getValueOrFail(propertyName, PropertyId.class);
  }

  /**
   * Gets the value of the specified {@link String} property.
   * @param propertyName the name of the property to get.
   * @return the {@link String} value.
   */
  private String getString(String propertyName) {
    return getValue(propertyName, PropertyString.class);
  }

  private <T> T getPropertyValue(PropertyData<T> propertyData) {
    return propertyData != null ? propertyData.getFirstValue() : null;
  }

  /**
   * Gets the value of the specified date time property.
   * @param propertyName the name of the property to get.
   * @return the date time in milliseconds from Epoch.
   */
  private long getDateTime(String propertyName) {
    GregorianCalendar value = getValue(propertyName, PropertyDateTime.class);
    Objects.requireNonNull(value);
    return CmisDateConverter.calendarToMillis(value);
  }

  private <T> T getValueOrFail(String propertyName, Class<? extends PropertyData<?>> propertyType) {
    T value = getValue(propertyName, propertyType);
    if (value == null) {
      throw new IllegalArgumentException(String.format("Property %s should be set!", propertyName));
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  private <T> T getValue(String propertyName, Class<? extends PropertyData<?>> propertyType) {
    PropertyData<?> property = properties.getProperties().get(propertyName);
    if (property != null && propertyType.isAssignableFrom(property.getClass())) {
      var data = propertyType.cast(property);
      return (T) getPropertyValue(data);
    }

    return null;
  }

  /**
   * Sets the specified {@link String} property.
   * @param propertyName the name of the property to set.
   * @param propertyValue the value of the property to set.
   * @param filtering should the filter be applied on the property?
   */
  private CmisProperties setString(String propertyName, String propertyValue, boolean filtering) {
    applyFilter(propertyName,
        props -> props.addProperty(new PropertyStringImpl(propertyName, propertyValue)),
        !filtering);
    return this;
  }

  /**
   * Sets the specified {@link String} multi-valued property.
   * @param propertyName the name of the property to set.
   * @param propertyValue the value of the property to set.
   * @param filtering should the filter be applied on the property?
   */
  private CmisProperties setString(String propertyName, List<String> propertyValue,
      boolean filtering) {
    applyFilter(propertyName,
        props -> props.addProperty(new PropertyStringImpl(propertyName, propertyValue)),
        !filtering);
    return this;
  }

  /**
   * Sets the specified boolean property.
   * @param propertyName the name of the property to set.
   * @param propertyValue the value of the property to set.
   * @param filtering should the filter be applied on the property?
   */
  private CmisProperties setBoolean(String propertyName, boolean propertyValue, boolean filtering) {
    applyFilter(propertyName,
        props -> props.addProperty(new PropertyBooleanImpl(propertyName, propertyValue)),
        !filtering);
    return this;
  }

  /**
   * Sets the specified integer property.
   * @param propertyName the name of the property to set.
   * @param propertyValue the value of the property to set.
   * @param filtering should the filter be applied on the property?
   */
  private CmisProperties setBigInteger(String propertyName, long propertyValue, boolean filtering) {
    applyFilter(propertyName, props -> props
            .addProperty(new PropertyIntegerImpl(propertyName, BigInteger.valueOf(propertyValue))),
        !filtering);
    return this;
  }

  /**
   * Sets the specified identifier property.
   * @param propertyName the name of the property to set.
   * @param propertyValue the value of the property to set.
   * @param filtering should the filter be applied on the property?
   */
  private CmisProperties setId(String propertyName, String propertyValue, boolean filtering) {
    applyFilter(propertyName,
        props -> props.addProperty(new PropertyIdImpl(propertyName, propertyValue)), !filtering);
    return this;
  }

  /**
   * Sets the specified datetime property.
   * @param propertyName the name of the property to set.
   * @param propertyValue the value of the property to set.
   * @param filtering should the filter be applied on the property?
   */
  private CmisProperties setDateTime(String propertyName, long propertyValue, boolean filtering) {
    applyFilter(propertyName, props -> props.addProperty(
        new PropertyDateTimeImpl(propertyName, CmisDateConverter.millisToCalendar(propertyValue))),
        !filtering);
    return this;
  }

  /**
   * Applies the filter on the specified property. If the property isn't referred by any of the
   * filtering rules of the filter, then it isn't set. The filtering rules can be explicitly
   * bypassed and this case the property is set.
   * @param propertyName the name of the property to set.
   * @param function the function that set a value to the property.
   * @param bypass a flag indicating if the filtering rules are applied or not. If true, then the
   * filter is applied on the property, otherwise the filtering rules are bypassed and the property
   * is set.
   */
  private void applyFilter(final String propertyName, Consumer<MutableProperties> function,
      boolean bypass) {
    if (properties instanceof MutableProperties) {
      if (bypass || filter.isEmpty() || (filter.size() == 1 && filter.contains("*")) ||
          filter.contains(propertyName)) {
        function.accept((MutableProperties) properties);
      }
    } else {
      throw new CmisRuntimeException("Properties aren't mutable!");
    }
  }
}
