package com.silverpeas.myLinksPeas.control;

import java.util.Collection;

import com.silverpeas.myLinks.MyLinksRuntimeException;
import com.silverpeas.myLinks.ejb.MyLinksBm;
import com.silverpeas.myLinks.ejb.MyLinksBmHome;
import com.silverpeas.myLinks.model.LinkDetail;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class MyLinksPeasSessionController extends
    AbstractComponentSessionController {
  public static final int SCOPE_USER = 0;
  public static final int SCOPE_COMPONENT = 1;
  public static final int SCOPE_OBJECT = 2;

  private int scope = SCOPE_USER;

  private String url = null;
  private String instanceId = null;
  private String objectId = null;

  /**
   * Standard Session Controller Constructeur
   * 
   * 
   * @param mainSessionCtrl
   *          The user's profile
   * @param componentContext
   *          The component's profile
   * 
   * @see
   */
  public MyLinksPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.mylinks.multilang.myLinksBundle",
        "com.silverpeas.mylinks.settings.myLinksIcons");
  }

  public Collection getAllLinksByUser() {
    Collection links = null;
    try {
      links = getMyLinksBm().getAllLinksByUser(getUserId());
      SilverTrace.debug("myLinks",
          "MyLinksPeasSessionController.getAllLinksByUser()",
          "root.MSG_GEN_PARAM_VALUE", "nombre de liens = " + links.size());
    } catch (Exception e) {
      // traitement des exceptions
      throw new MyLinksRuntimeException(
          "MyLinksPeasSessionController.getAllLinksByUser",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND", e);
    }
    return links;
  }

  public Collection getAllLinksByInstance() {
    Collection links = null;
    try {
      links = getMyLinksBm().getAllLinksByInstance(instanceId);
      SilverTrace.debug("myLinks",
          "MyLinksPeasSessionController.getAllLinksByInstance()",
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

  public Collection getAllLinksByObject() {
    Collection links = null;
    try {
      links = getMyLinksBm().getAllLinksByObject(instanceId, objectId);
      SilverTrace.debug("myLinks",
          "MyLinksPeasSessionController.getAllLinksByObject()",
          "root.MSG_GEN_PARAM_VALUE", "nombre de liens = " + links.size());
    } catch (Exception e) {
      // traitement des exceptions
      throw new MyLinksRuntimeException(
          "MyLinksPeasSessionController.getAllLinksByObject",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND", e);
    }
    return links;
  }

  public LinkDetail getLink(String linkId) {
    LinkDetail link = null;
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

  public void createLink(LinkDetail link) {

    try {
      String userId = getUserId();
      link.setUserId(userId);

      // ajout de l'instanceId si existe
      if (instanceId != null)
        link.setInstanceId(instanceId);

      getMyLinksBm().createLink(link);
      SilverTrace.debug("myLinks", "MyLinksPeasSessionController.createLink()",
          "root.MSG_GEN_PARAM_VALUE", "liens = " + link.toString());
    } catch (Exception e) {
      // traitement des exceptions
      throw new MyLinksRuntimeException(
          "MyLinksPeasSessionController.createLink", SilverpeasException.ERROR,
          "root.EX_RECORD_NOT_FOUND", e);
    }
  }

  public void updateLink(LinkDetail link) {
    try {
      // ajout de l'instanceId si existe
      if (instanceId != null)
        link.setInstanceId(instanceId);

      String userId = getUserId();
      link.setUserId(userId);
      getMyLinksBm().updateLink(link);
    } catch (Exception e) {
      // traitement des exceptions
      throw new MyLinksRuntimeException(
          "MyLinksPeasSessionController.updateLink", SilverpeasException.ERROR,
          "root.EX_RECORD_NOT_FOUND", e);
    }
  }

  public void deleteLinks(String[] links) {
    try {
      getMyLinksBm().deleteLinks(links);
      SilverTrace.debug("myLinks", "MyLinksPeasSessionController.deleteLink()",
          "root.MSG_GEN_PARAM_VALUE", "links = " + links.toString());
    } catch (Exception e) {
      // traitement des exceptions
      throw new MyLinksRuntimeException(
          "MyLinksPeasSessionController.createLink", SilverpeasException.ERROR,
          "root.EX_RECORD_NOT_FOUND", e);
    }
  }

  private MyLinksBm getMyLinksBm() {
    MyLinksBm myLinksBm = null;
    try {
      MyLinksBmHome myLinksBmHome = (MyLinksBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.MYLINKSBM_EJBHOME, MyLinksBmHome.class);
      myLinksBm = myLinksBmHome.create();
    } catch (Exception e) {
      throw new MyLinksRuntimeException(
          "MyLInksSessionController.getMyLInksBm()",
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
