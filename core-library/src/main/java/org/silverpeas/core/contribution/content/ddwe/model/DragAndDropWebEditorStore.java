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

package org.silverpeas.core.contribution.content.ddwe.model;

import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.SilverpeasResource;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.ddwe.DragAndDropWbeFile;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.util.MemoizedSupplier;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.wbe.WbeHostManager;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.exception.NotSupportedException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.silverpeas.kernel.util.StringUtil.EMPTY;
import static org.silverpeas.kernel.util.StringUtil.isNotDefined;

/**
 * Represents the store that register all data manipulated by a Drag & Drop Web Editor.
 * <p>
 *   The content is registered in Silverpeas's home data as structured XML file.
 *   The save MUST be explicitly performed by calling {@link #save()} method.
 * </p>
 * <p>
 *   The structured content is divided into two parts:
 *   <ul>
 *     <li>a temporary part that permits to persists current edition and retrieve data whatever
 *     the context</li>
 *     <li>a final part that represents the content to provide when data are validated</li>
 *   </ul>
 * </p>
 * @author silveryocha
 */
public class DragAndDropWebEditorStore implements SilverpeasResource, Serializable, Securable {
  private static final long serialVersionUID = 996933763895325970L;

  private static final String FILE_PREFIX = "ddwecontent_";
  private final ContributionIdentifier contributionId;
  private final transient MemoizedSupplier<Contribution> foreignContribution =
      new MemoizedSupplier<>(
      () -> ApplicationService.getInstance(getContributionId().getComponentInstanceId())
          .getContributionById(getContributionId())
          .orElse(null));
  private File file;

  public DragAndDropWebEditorStore(final ContributionIdentifier contribution) {
    this.contributionId = contribution;
  }

  /**
   * Gets the Silverpeas's file containing the structured content from which Web Editor can
   * perform Drag & Drop edition.
   * @return a {@link File} instance.
   */
  public File getFile() {
    if (file == null) {
      final Path filePath = Paths.get(getDirectoryPath().toString(),
          FILE_PREFIX + getContributionId().getLocalId() + ".xml");
      file = new File(getContributionId().getComponentInstanceId(), filePath.toString());
    }
    return file;
  }

  /**
   * Saves the structured content into Silverpeas's home data.
   */
  public void save() {
    getFile().save();
  }

  /**
   * Deletes the structured content into Silverpeas's home data.
   * return true id deletion is well performed (false if file not exists)
   */
  public boolean delete() {
    WbeHostManager.get().revokeFile(new DragAndDropWbeFile(this));
    return getFile().delete();
  }


  @Override
  public String getName() {
    return getForeignContribution().map(Contribution::getName).orElse(EMPTY);
  }

  @Override
  public String getDescription() {
    return EMPTY;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ContributionIdentifier getIdentifier() {
    return getContributionId();
  }

  /**
   * Gets the {@link ContributionIdentifier} of the contribution the content is linked to.
   * @return a {@link ContributionIdentifier} instance.
   */
  public ContributionIdentifier getContributionId() {
    return contributionId;
  }

  @Override
  public Date getCreationDate() {
    return getFile().getContainer().getContent()
        .map(Content::getMetadata)
        .map(Content.Metadata::getCreated)
        .map(TemporalConverter::asDate)
        .orElse(null);
  }

  @Override
  public Date getLastUpdateDate() {
    return getFile().getContainer().getContent()
        .map(Content::getMetadata)
        .map(Content.Metadata::getLastUpdated)
        .map(TemporalConverter::asDate)
        .orElse(null);
  }

  @Override
  public User getCreator() {
    return getFile().getContainer().getContent()
        .map(Content::getMetadata)
        .map(Content.Metadata::getCreatedBy)
        .map(User::getById)
        .orElse(null);
  }

  @Override
  public User getLastUpdater() {
    return getFile().getContainer().getContent()
        .map(Content::getMetadata)
        .map(Content.Metadata::getLastUpdatedBy)
        .map(User::getById)
        .orElse(null);
  }

  /**
   * Path to the directory where the file is to be stored.
   * @return the path to the directory where the file is to be stored.
   */
  public Path getDirectoryPath() {
    return Paths.get(FileRepositoryManager.getAbsolutePath(getContributionId().getComponentInstanceId()),
        "ddwe", getContributionId().getType());
  }

  private Optional<Contribution> getForeignContribution() {
    return ofNullable(getContributionId()).map(i -> foreignContribution.get());
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return ComponentAccessControl.get()
        .isUserAuthorized(user.getId(), getContributionId().getComponentInstanceId());
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    return ComponentAccessControl.get()
        .isUserAuthorized(user.getId(), getContributionId().getComponentInstanceId(),
            AccessControlContext.init().onOperationsOf(AccessControlOperation.MODIFICATION));
  }

  /**
   * Representation of the file containing the structured content.
   */
  public static class File extends SilverpeasFile {
    public static final String MIME_TYPE = "text/ddwe";
    private static final long serialVersionUID = -3539881144930179164L;
    private Container container;
    private long lastModificationDateStamp = 0L;

    protected File(final String componentId, final String path) {
      super(componentId, path, MIME_TYPE);
    }

    /**
     * Gets the ROOT TAG container.
     * @return a {@link Container} instance.
     */
    public Container getContainer() {
      if (container == null || lastModificationDateStamp != lastModified()) {
        if (exists()) {
          lastModificationDateStamp = lastModified();
          try (final InputStream in = inputStream()) {
            container = Container.loadFrom(in);
          } catch (IOException e) {
            throw new SilverpeasRuntimeException(e.getMessage(), e);
          }
        } else {
          container = new Container();
        }
      }
      return container;
    }

    protected void save() {
      final User currentUser = Optional
          .ofNullable(User.getCurrentRequester())
          .orElse(User.getSystemUser());
      final ZonedDateTime now = ZonedDateTime.now();
      Stream.of(getContainer().getTmpContent(), getContainer().getContent())
          .flatMap(Optional::stream)
          .forEach(c -> {
            final Content.Metadata metadata = c.getMetadata();
            if (isNotDefined(metadata.getCreatedBy())) {
              metadata.setCreated(now);
              metadata.setCreatedBy(currentUser.getId());
            }
            metadata.setLastUpdated(now);
            metadata.setLastUpdatedBy(currentUser.getId());
      });
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        Container.writeIn(getContainer(), out);
        try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {
          super.writeFrom(in);
        }
      } catch (IOException e) {
        throw new SilverpeasRuntimeException(e.getMessage(), e);
      }
    }

    /**
     * Please use instead {@link DragAndDropWebEditorStore#save()} method.
     */
    @Override
    public void writeFrom(final InputStream stream) {
      throw new NotSupportedException("This low level method is not supported, please use DragAndDropWebEditorContent#save() method");
    }

    @Override
    public boolean equals(final Object o) {
      return super.equals(o);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }
  }

  /**
   * Container that contains the different kinds of content (temporary and final ones).
   * @author silveryocha
   */
  @XmlRootElement(name = "ddwe-container")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Container implements Serializable {
    private static final long serialVersionUID = 4451531724957681559L;

    @XmlElement(name = "tmp-content")
    private Content tmpContent;

    @XmlElement(name = "content")
    private Content content;

    protected static Container loadFrom(final InputStream in) {
      try {
        final JAXBContext context = JAXBContext.newInstance(Container.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        return (Container) unmarshaller.unmarshal(in);
      } catch (JAXBException e) {
        throw new SilverpeasRuntimeException(e.getMessage(), e);
      }
    }

    protected static void writeIn(final Container content, final OutputStream out) {
      try {
        final JAXBContext context = JAXBContext.newInstance(Container.class);
        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(content, out);
      } catch (JAXBException e) {
        throw new SilverpeasRuntimeException(e.getMessage(), e);
      }
    }

    /**
     * Gets the temporary content if any.
     * @return an optional {@link Content} instance.
     */
    public Optional<Content> getTmpContent() {
      return ofNullable(tmpContent);
    }

    /**
     * Gets the temporary content and creating it if it does not yet exist.
     */
    public Content getOrCreateTmpContent() {
      if (tmpContent == null) {
        tmpContent = new Content();
      }
      return tmpContent;
    }

    /**
     * Gets the final content if any.
     * @return an optional {@link Content} instance.
     */
    public Optional<Content> getContent() {
      return ofNullable(content);
    }

    /**
     * Gets the final content and creating it if it does not yet exist.
     */
    public Content getOrCreateContent() {
      if (content == null) {
        content = new Content();
      }
      return content;
    }
  }

  /**
   * Content of Drag&Drop web editor
   * @author silveryocha
   */
  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Content implements Serializable {
    private static final long serialVersionUID = -5418311140838509632L;

    @XmlTransient
    private final Metadata metadata = new Metadata(this);

    @XmlAttribute(required = true)
    private String createdBy;
    @XmlAttribute(required = true)
    private String created;
    @XmlAttribute(required = true)
    private String lastUpdatedBy;
    @XmlAttribute(required = true)
    private String lastUpdated;

    @XmlValue
    private String value;

    public Metadata getMetadata() {
      return metadata;
    }

    public String getValue() {
      return value;
    }

    public void setValue(final String value) {
      this.value = value;
    }

    public static class Metadata implements Serializable {
      private static final long serialVersionUID = 5799777732297383841L;

      private final Content content;

      public Metadata(final Content content) {
        this.content = content;
      }

      public String getCreatedBy() {
        return content.createdBy;
      }

      public void setCreatedBy(final String createdBy) {
        content.createdBy = createdBy;
      }

      public ZonedDateTime getCreated() {
        return ZonedDateTime.parse(content.created);
      }

      public void setCreated(final ZonedDateTime created) {
        content.created = created.toString();
      }

      public String getLastUpdatedBy() {
        return content.lastUpdatedBy;
      }

      public void setLastUpdatedBy(final String lastUpdatedBy) {
        content.lastUpdatedBy = lastUpdatedBy;
      }

      public ZonedDateTime getLastUpdated() {
        return ZonedDateTime.parse(content.lastUpdated);
      }

      public void setLastUpdated(final ZonedDateTime lastUpdated) {
        content.lastUpdated = lastUpdated.toString();
      }
    }
  }
}
