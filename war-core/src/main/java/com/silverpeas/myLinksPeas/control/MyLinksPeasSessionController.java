/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.myLinksPeas.control;

import com.silverpeas.myLinks.MyLinksRuntimeException;
import com.silverpeas.myLinks.ejb.MyLinksBm;
import com.silverpeas.myLinks.model.LinkDetail;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.mylinks.web.MyLinkEntity;
import org.silverpeas.util.NotifierUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collection;

import static org.silverpeas.mylinks.web.MyLinksResource.checkMandatoryLinkData;

public class MyLinksPeasSessionController extends AbstractComponentSessionController {
  public static final int SCOPE_USER = 0;
  public static final int SCOPE_COMPONENT = 1;
  public static final int SCOPE_OBJECT = 2;

  private int scope = SCOPE_USER;

  private String url = null;
  private String instanceId = null;
  private String objectId = null;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public MyLinksPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.mylinks.multilang.myLinksBundle",
        "org.silverpeas.mylinks.settings.myLinksIcons");
  }

  public Collection<LinkDetail> getAllLinksByUser() {
    Collection<LinkDetail> links;
    try {
      links = getMyLinksBm().getAllLinksByUser(getUserId());
      SilverTrace.debug("myLinks", "MyLinksPeasSessionController.getAllLinksByUser()",
          "root.MSG_GEN_PARAM_VALUE", "nombre de liens = " + links.size());
    } catch (Exception e) {
      // traitement des exceptions
      throw new MyLinksRuntimeException("MyLinksPeasSessionController.getAllLinksByUser",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND", e);
    }
    return links;
  }

  public Collection<LinkDetail> getAllLinksByInstance() {
    Collection<LinkDetail> links;
    try {
      links = getMyLinksBm().getAllLinksByInstance(instanceId);
      SilverTrace.debug("myLinks", "MyLinksPeasSessionController.getAllLinksByInstance()",
          "root.MSG_GEN_PARAM_VALUE", "nombre de liens = " + links.size()
          + " instanceId = " + instanceId);
    } catch (Exception e) {
      // traitement des exceptions
      throw new MyLinksRuntimeException(
          "MyLinksPeasSessionController.getAllLinksByInstance",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND", e);
    }
    return links;
  }

  public Collection<LinkDetail> getAllLinksByObject() {
    Collection<LinkDetail> links;
    try {
      links = getMyLinksBm().getAllLinksByObject(instanceId, objectId);
      SilverTrace.debug("myLinks", "MyLinksPeasSessionController.getAllLinksByObject()",
          "root.MSG_GEN_PARAM_VALUE", "nombre de liens = " + links.size());
    } catch (Exception e) {
      // traitement des exceptions
      throw new MyLinksRuntimeException("MyLinksPeasSessionController.getAllLinksByObject",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND", e);
    }
    return links;
  }

  public LinkDetail getLink(String linkId) {
    LinkDetail link;
    try {
      link = getMyLinksBm().getLink(linkId);
      SilverTrace.debug("myLinks", "MyLinksPeasSessionController.getLink()",
          "root.MSG_GEN_PARAM_VALUE", "linkId = " + linkId);
    } catch (Exception e) {
      // traitement des exceptions
      throw new MyLinksRuntimeException("MyLinksPeasSessionController.getLink",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND", e);
    }
    return link;
  }

  /**
   * This method verify that the owner of the link represented by the given id is the current user.
   * @param linkId
   */
  public void verifyCurrentUserIsOwner(int linkId) {
    verifyCurrentUserIsOwner(String.valueOf(linkId));
  }

  /**
   * This method verify that the owner of the link represented by the given id is the current user.
   * @param linkId
   */
  public void verifyCurrentUserIsOwner(String linkId) {
    LinkDetail userLink = getLink(linkId);
    if (userLink == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else if (!getUserDetail().getId().equals(userLink.getUserId())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  public void createLink(MyLinkEntity newLink) {
    try {
      LinkDetail linkDetail = newLink.toLinkDetail();
      linkDetail.setUserId(getUserId());
      getMyLinksBm().createLink(linkDetail);
      NotifierUtil.addSuccess(getString("myLinks.messageConfirm"));
    } catch (Exception e) {
      throw new MyLinksRuntimeException(
          "MyLinksPeasSessionController.createLink", SilverpeasException.ERROR,
          "root.EX_RECORD_NOT_FOUND", e);
    }
  }

  public void updateLink(MyLinkEntity updatedLink) {
    try {
      verifyCurrentUserIsOwner(updatedLink.getLinkId());
      checkMandatoryLinkData(updatedLink);
      LinkDetail linkDetail = updatedLink.toLinkDetail();
      linkDetail.setUserId(getUserId());
      getMyLinksBm().updateLink(linkDetail);
      NotifierUtil.addSuccess(getString("myLinks.updateLink.messageConfirm"));
    } catch (Exception e) {
      throw new MyLinksRuntimeException(
          "MyLinksPeasSessionController.updateLink", SilverpeasException.ERROR,
          "root.EX_RECORD_NOT_FOUND", e);
    }
  }

  public void deleteLinks(String[] links) {
    try {
      if (links.length > 0) {
        for (String linkId : links) {
          verifyCurrentUserIsOwner(linkId);
        }
        getMyLinksBm().deleteLinks(links);
        NotifierUtil.addSuccess(getString("myLinks.deleteLinks.messageConfirm"), links.length);
      }
    } catch (Exception e) {
      throw new MyLinksRuntimeException(
          "MyLinksPeasSessionController.createLink", SilverpeasException.ERROR,
          "root.EX_RECORD_NOT_FOUND", e);
    }
  }

  protected MyLinksBm getMyLinksBm() {
    MyLinksBm myLinksBm;
    try {
      myLinksBm = EJBUtilitaire.getEJBObjectRef(JNDINames.MYLINKSBM_EJBHOME, MyLinksBm.class);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLInksSessionController.getMyLInksBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return myLinksBm;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
    scope = SCOPE_COMPONENT;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
    scope = SCOPE_OBJECT;
  }

  public void setScope(int scope) {
    this.scope = scope;
    if (scope == SCOPE_USER) {
      instanceId = null;
      objectId = null;
    }
  }

  public int getScope() {
    return scope;
  }

}
