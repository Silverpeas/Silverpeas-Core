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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.jcr.util.SilverpeasProperty;
import org.silverpeas.test.DateRandom;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static javax.jcr.nodetype.NodeType.MIX_SIMPLE_VERSIONABLE;
import static javax.jcr.nodetype.NodeType.MIX_VERSIONABLE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Class with utility methods for testing purpose when handling JCR node properties and mixins.
 * @author mmoquillon
 */
final public class JCRUtil {

  public static Value convertToJCRValue(final Session session, final Date date)
      throws RepositoryException {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return session.getValueFactory().createValue(calendar);
  }

  public static Node fillDocumentNode(final Session session, final NodeDocProperties properties,
      final Node document) throws RepositoryException {
    Value date = convertToJCRValue(session, properties.getDate());
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_FOREIGN_KEY, properties.getForeignKey());
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_VERSIONED, properties.isVersionable());
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_ORDER, 0);
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_OLD_ID, properties.getOldId());
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_INSTANCEID, properties.getInstanceId());
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_OWNER, properties.getUser().getId());
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_COMMENT, "");
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_STATUS, "0");
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_ALERT_DATE, date);
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_EXPIRY_DATE, date);
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_RESERVATION_DATE, date);
    document.setProperty(SilverpeasProperty.SLV_PROPERTY_CLONE, "");

    //downloadable mixin
    if (properties.isDownloadable()) {
      document.addMixin(SilverpeasProperty.SLV_DOWNLOADABLE_MIXIN);
      document.setProperty(SilverpeasProperty.SLV_PROPERTY_FORBIDDEN_DOWNLOAD_FOR_ROLES,
          SilverpeasRole.asString(SilverpeasRole.READER_ROLES));
    }

    // viewable mixin
    if (properties.isDisplayable()) {
      document.addMixin(SilverpeasProperty.SLV_VIEWABLE_MIXIN);
      document.setProperty(SilverpeasProperty.SLV_PROPERTY_DISPLAYABLE_AS_CONTENT, true);
    }

    // versionable mixin
    if (properties.isVersionable()) {
      document.addMixin(MIX_VERSIONABLE);
    }

    return document;
  }

  public static Node fillFileNode(final Session session, final NodeFileProperties properties,
      final Node file) throws RepositoryException {
    Value creationDate = convertToJCRValue(session, properties.getCreationDate());
    Value modificationDate = convertToJCRValue(session, properties.getLastModificationDate());
    file.setProperty(SilverpeasProperty.SLV_PROPERTY_CREATION_DATE, creationDate);
    file.setProperty(Property.JCR_LAST_MODIFIED, modificationDate);
    file.setProperty(SilverpeasProperty.SLV_PROPERTY_CREATOR, properties.getCreator().getId());
    file.setProperty(Property.JCR_LAST_MODIFIED_BY, properties.getLastModifier().getId());
    file.setProperty(SilverpeasProperty.SLV_PROPERTY_NAME, properties.getName());
    file.setProperty(Property.JCR_TITLE, properties.getTitle());
    file.setProperty(Property.JCR_DESCRIPTION, properties.getDescription());
    file.setProperty(Property.JCR_LANGUAGE, properties.getLanguage());
    file.setProperty(SilverpeasProperty.SLV_PROPERTY_XMLFORM_ID, properties.getFormId());
    file.setProperty(Property.JCR_MIMETYPE, properties.getMimeType());
    file.setProperty(SilverpeasProperty.SLV_PROPERTY_SIZE, properties.getSize());

    return file;
  }

  public static void assertDocumentNodeEquals(final Node expected, final Node actual) {
    assertDoesNotThrow(() -> {
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_FOREIGN_KEY).getString(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_FOREIGN_KEY).getString()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_VERSIONED).getBoolean(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_VERSIONED).getBoolean()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_ORDER).getLong(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_ORDER).getLong()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_OLD_ID).getString(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_OLD_ID).getString()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_INSTANCEID).getString(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_INSTANCEID).getString()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_OWNER).getString(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_OWNER).getString()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_COMMENT).getString(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_COMMENT).getString()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_STATUS).getString(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_STATUS).getString()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_CLONE).getString(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_CLONE).getString()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_ALERT_DATE).getDate(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_ALERT_DATE).getDate()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_EXPIRY_DATE).getDate(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_EXPIRY_DATE).getDate()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_RESERVATION_DATE).getDate(),
          is(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_RESERVATION_DATE).getDate()));

      if (Arrays.stream(actual.getMixinNodeTypes())
          .anyMatch(m -> m.isNodeType(SilverpeasProperty.SLV_DOWNLOADABLE_MIXIN))) {
        assertThat(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_FORBIDDEN_DOWNLOAD_FOR_ROLES).getString(),
            is(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_FORBIDDEN_DOWNLOAD_FOR_ROLES).getString()));
      }

      if (Arrays.stream(actual.getMixinNodeTypes())
          .anyMatch(m -> m.isNodeType(SilverpeasProperty.SLV_VIEWABLE_MIXIN))) {
        assertThat(expected.getProperty(SilverpeasProperty.SLV_PROPERTY_DISPLAYABLE_AS_CONTENT).getBoolean(),
            is(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_DISPLAYABLE_AS_CONTENT).getBoolean()));
      }

      if (actual.getProperty(SilverpeasProperty.SLV_PROPERTY_VERSIONED).getBoolean()) {
        assertThat(Arrays.stream(expected.getMixinNodeTypes())
            .anyMatch(m -> m.isNodeType(MIX_SIMPLE_VERSIONABLE)), is(true));
      }

    });
  }

  public static final class NodeDocProperties {
    private final String instanceId;
    private final User user;

    private String foreignKey = String.valueOf(new Random().nextInt(100));

    private String oldId = String.valueOf(new Random().nextInt(1000000));

    private Date date = new DateRandom().nextDate(0);
    private boolean versionable = false;
    private boolean downloadable = true;
    private boolean displayable = true;

    public NodeDocProperties(final String instanceId, final User user) {
      this.instanceId = instanceId;
      this.user = user;
    }

    public String getInstanceId() {
      return instanceId;
    }

    public User getUser() {
      return user;
    }

    public boolean isVersionable() {
      return versionable;
    }

    public NodeDocProperties setVersionable(final boolean versionable) {
      this.versionable = versionable;
      return this;
    }

    public boolean isDownloadable() {
      return downloadable;
    }

    public NodeDocProperties setDownloadable(final boolean downloadable) {
      this.downloadable = downloadable;
      return this;
    }

    public boolean isDisplayable() {
      return displayable;
    }

    public NodeDocProperties setDisplayable(final boolean displayable) {
      this.displayable = displayable;
      return this;
    }

    public NodeDocProperties setForeignKey(final String foreignKey) {
      this.foreignKey = foreignKey;
      return this;
    }

    public NodeDocProperties setOldId(final String oldId) {
      this.oldId = oldId;
      return this;
    }

    public NodeDocProperties setDate(final Date date) {
      this.date = date;
      return this;
    }

    public String getForeignKey() {
      return foreignKey;
    }

    public String getOldId() {
      return oldId;
    }

    public Date getDate() {
      return date;
    }
  }

  public static final class NodeFileProperties {

    private final Date creationDate;
    private final User creator;
    private String description;
    private final String name;
    private String title;
    private String language = "en";
    private Date lastModificationDate;
    private User lastModifier;
    private String formId = "-1";
    private String mimeType;
    private long size;

    public NodeFileProperties(final String name, final Date creationDate, final User author) {
      this.name = name;
      this.title = name;
      this.creator = author;
      this.lastModifier = author;
      this.creationDate = creationDate;
      this.lastModificationDate = creationDate;
    }

    public Date getCreationDate() {
      return creationDate;
    }

    public User getCreator() {
      return creator;
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public NodeFileProperties setDescription(final String description) {
      this.description = description;
      return this;
    }

    public String getTitle() {
      return title;
    }

    public NodeFileProperties setTitle(final String title) {
      this.title = title;
      return this;
    }

    public String getLanguage() {
      return language;
    }

    public NodeFileProperties setLanguage(final String language) {
      this.language = language;
      return this;
    }

    public Date getLastModificationDate() {
      return lastModificationDate;
    }

    public NodeFileProperties setLastModificationDate(final Date lastModificationDate) {
      this.lastModificationDate = lastModificationDate;
      return this;
    }

    public User getLastModifier() {
      return lastModifier;
    }

    public NodeFileProperties setLastModifier(final User lastModifier) {
      this.lastModifier = lastModifier;
      return this;
    }

    public String getFormId() {
      return formId;
    }

    public NodeFileProperties setFormId(final String formId) {
      this.formId = formId;
      return this;
    }

    public String getMimeType() {
      return mimeType;
    }

    public NodeFileProperties setMimeType(final String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    public long getSize() {
      return size;
    }

    public NodeFileProperties setSize(final long size) {
      this.size = size;
      return this;
    }
  }
}
