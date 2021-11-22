/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.webapi.selection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.ApplicationServiceProvider;
import org.silverpeas.core.BasicIdentifier;
import org.silverpeas.core.NotFoundException;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.SilverpeasResource;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.Thumbnail;
import org.silverpeas.core.contribution.model.WithPermanentLink;
import org.silverpeas.core.contribution.model.WithThumbnail;
import org.silverpeas.core.webapi.util.UserEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Item in the {@link org.silverpeas.core.selection.SelectionBasket}. It is the web entity
 * representing in the more generic way a Silverpeas resource, whatever it is.
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasketItem implements SilverpeasResource, Serializable {

  @XmlElement(required = true)
  private String id;
  private String type;
  private String name;
  private String description;
  private Date creationDate;
  private Date lastUpdateDate;
  private UserEntity creator;
  private UserEntity lastUpdater;
  private String thumbnailURI;
  private String permalink;

  private BasketItem() {
  }

  public static BasketItem from(final SilverpeasResource resource) {
    if (resource instanceof BasketItem) {
      return (BasketItem) resource;
    }
    BasketItem item = new BasketItem();
    item.id = resource.getIdentifier()
        .asString();
    item.type = resource.getClass()
        .getSimpleName();
    item.creationDate = resource.getCreationDate();
    item.creator = new UserEntity(resource.getCreator());
    item.lastUpdateDate = resource.getLastUpdateDate();
    item.lastUpdater = new UserEntity(resource.getLastUpdater());
    item.description = resource.getDescription();
    item.name = resource.getName();
    if (resource instanceof Contribution) {
      setFrom((Contribution) resource, item);
    }
    return item;
  }

  private static void setFrom(final Contribution contribution, final BasketItem item) {
    item.type = contribution.getContributionType();
    if (contribution instanceof WithThumbnail) {
      Thumbnail thumbnail = ((WithThumbnail) contribution).getThumbnail();
      item.thumbnailURI = thumbnail == null ? null : thumbnail.getURL();
    }
    if (contribution instanceof WithPermanentLink) {
      item.permalink = ((WithPermanentLink) contribution).getPermalink();
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ResourceIdentifier> T getIdentifier() {
    if (ContributionIdentifier.isValid(id)) {
      return (T) ContributionIdentifier.decode(id);
    } else {
      return (T) new BasicIdentifier(id);
    }
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public Date getLastUpdateDate() {
    return lastUpdateDate;
  }

  @Override
  public User getCreator() {
    return User.getById(creator.getId());
  }

  @Override
  public User getLastUpdater() {
    return User.getById(lastUpdater.getId());
  }

  public String getThumbnailURI() {
    return thumbnailURI;
  }

  public String getPermalink() {
    return permalink;
  }

  public String getType() {
    return type;
  }

  /**
   * Is this item represent a collaborative space in Silverpeas?
   * @return true if this object represents a space in Silverpeas. False otherwise.
   */
  public boolean isSpace() {
    return id.startsWith("WA");
  }

  /**
   * Is this item represent an instance of a Silverpeas application?
   * @return true if this object represents a component instance. False otherwise.
   */
  public boolean isComponentInstance() {
    return type.equals(ComponentInst.class.getSimpleName()) ||
        type.equals(ComponentInstLight.class.getSimpleName());
  }

  /**
   * Is this {@link BasketItem} instance represent a contribution in Silverpeas?
   * @return true if this object represents a contribution in Silverpeas. False if it represents an
   * organisational resource in Silverpeas (application instance, space, ...)
   */
  public boolean isContribution() {
    return ContributionIdentifier.isValid(id);
  }

  /**
   * Finds the contribution that is mapped by this {@link BasketItem} instance. If no contributions
   * exists with the identifier of this object or if this object doesn't represent a contribution,
   * then a {@link NotFoundException} is thrown.
   * @return the contribution in Silverpeas that is related by this {@link BasketItem} instance.
   */
  public <T extends Contribution> T toContribution() {
    if (isContribution()) {
      ContributionIdentifier identifier = ContributionIdentifier.decode(id);
      Optional<ApplicationService<T>> maybeService = ApplicationServiceProvider.get()
          .getApplicationServiceById(identifier.getComponentInstanceId());
      ApplicationService<T> service = maybeService.orElseThrow(() -> new NotFoundException(
          "No application service available for id " + identifier.getComponentInstanceId()));
      return service.getContributionById(identifier.getLocalId());
    }
    throw new NotFoundException("No such contribution of id " + id);
  }

  /**
   * Finds the resource in Silverpeas that is mapped by this {@link BasketItem} instance. If there
   * is no resources nor no contributions in Silverpeas mapped by this object, then a {@link
   * NotFoundException} is thrown.
   * @return the Silverpeas resource mapped by this object. It can be either a contribution or a
   * collaborative space or an application instance.
   */
  @SuppressWarnings("unchecked")
  public <T extends SilverpeasResource> T toResource() {
    if (isContribution()) {
      return toContribution();
    } else if (isSpace()) {
      OrganizationController organizationController = OrganizationController.get();
      SpaceInst space = organizationController.getSpaceInstById(id);
      if (space == null) {
        throw new NotFoundException("No such collaborative space with id " + id);
      }
      return (T) space;
    } else if (isComponentInstance()) {
      OrganizationController organizationController = OrganizationController.get();
      ComponentInst app = organizationController.getComponentInst(id);
      if (app == null) {
        throw new NotFoundException("No such component instance with id " + id);
      }
      return (T) app;
    }
    throw new NotFoundException("This item doesn't represent any resource in Silverpeas");
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SilverpeasResource)) {
      return false;
    }
    final SilverpeasResource that = (SilverpeasResource) o;
    return id.equals(that.getIdentifier().asString());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
