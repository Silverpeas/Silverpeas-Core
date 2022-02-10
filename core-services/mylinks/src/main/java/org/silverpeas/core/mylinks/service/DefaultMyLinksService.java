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
package org.silverpeas.core.mylinks.service;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.mylinks.MyLinksRuntimeException;
import org.silverpeas.core.mylinks.dao.CategoryDAO;
import org.silverpeas.core.mylinks.dao.LinkDAO;
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.model.CategoryDetailComparator;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.model.LinkDetailComparator;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.List;

import static java.lang.Integer.parseInt;

@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultMyLinksService implements MyLinksService, ComponentInstanceDeletion {

  @Inject
  private LinkDAO linkDao;

  @Inject
  private CategoryDAO categoryDAO;

  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try {
      linkDao.deleteComponentInstanceData(componentInstanceId);
    } catch (SQLException e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public void deleteUserData(final String userId) {
    try {
      linkDao.deleteUserData(userId);
      categoryDAO.deleteUserData(userId);
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  public List<CategoryDetail> getAllCategoriesByUser(final String userId) {
    try {
      final List<CategoryDetail> categories = categoryDAO.getAllCategoriesByUser(userId);
      return CategoryDetailComparator.sort(categories);
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public CategoryDetail createCategory(final CategoryDetail category) {
    try {
      return categoryDAO.create(category);
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public CategoryDetail getCategory(final String categoryId) {
    try {
      return categoryDAO.getCategory(parseInt(categoryId));
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public void deleteCategories(final String[] categoryIds) {
    try {
      for (String categoryId : categoryIds) {
        categoryDAO.deleteCategory(parseInt(categoryId));
      }
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public CategoryDetail updateCategory(final CategoryDetail category) {
    try {
      return categoryDAO.update(category);
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  public List<LinkDetail> getAllLinks(String userId) {
    return getAllLinksByUser(userId);
  }

  @Override
  public List<LinkDetail> getAllLinksByUser(String userId) {
    try {
      final List<LinkDetail> links = linkDao.getAllLinksByUser(userId);
      return LinkDetailComparator.sort(links);
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  public List<LinkDetail> getAllLinksByInstance(String instanceId) {
    try {
      final List<LinkDetail> links = linkDao.getAllLinksByInstance(instanceId);
      return LinkDetailComparator.sort(links);
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  public List<LinkDetail> getAllLinksByObject(String instanceId, String objectId) {
    try {
      final List<LinkDetail> links = linkDao.getAllLinksByObject(instanceId, objectId);
      return LinkDetailComparator.sort(links);
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public LinkDetail createLink(LinkDetail link) {
    try {
      return linkDao.createLink(link);
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public void deleteLinks(String[] links) {
    try {
      for (String linkId : links) {
        linkDao.deleteLink(linkId);
      }
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public LinkDetail updateLink(LinkDetail link) {
    try {
      return linkDao.updateLink(link);
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }

  @Override
  public LinkDetail getLink(String linkId) {
    try {
      return linkDao.getLink(parseInt(linkId));
    } catch (Exception e) {
      throw new MyLinksRuntimeException(e);
    }
  }
}
