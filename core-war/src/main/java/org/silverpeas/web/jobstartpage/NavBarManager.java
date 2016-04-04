/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of
 * the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with this
 * program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.jobstartpage;

import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.owasp.encoder.Encode;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class NavBarManager {
  // Constants used by urlFactory

  final static int SPACE = 0;
  final static int COMPONENT = 1;
  final static int COMPONENTPOPUP = 7;
  final static int SPACE_COLLAPSE = 2;
  final static int SPACE_EXPANDED = 3;
  final static int SPACE_COMPONENT = 4;
  final static int SUBSPACE_COMPONENT = 5;
  final static int SUBSPACE_LAST_COMPONENT = 6;
  UserDetail m_user = null;
  AdminController m_administrationCtrl = null;
  AbstractComponentSessionController m_SessionCtrl = null;
  String m_sContext;
  HashSet<String> m_ManageableSpaces = new HashSet<>();
  DisplaySorted[] m_Spaces = null;
  String m_CurrentSpaceId = null;
  DisplaySorted[] m_SpaceComponents = null;
  String m_CurrentSubSpaceId = null;
  DisplaySorted[] m_SubSpaces = null;
  DisplaySorted[] m_SubSpaceComponents = null;
  long m_elmtCounter = 0;

  public void resetSpaceCache(String theSpaceId) {

    String spaceId = getShortSpaceId(theSpaceId);
    DisplaySorted elmt = getSpaceCache(spaceId);
    if (elmt != null) {
      elmt.copy(buildSpaceObject(spaceId));
      if (spaceId.equals(m_CurrentSpaceId)) {
        setCurrentSpace(m_CurrentSpaceId);
      } else if (spaceId.equals(m_CurrentSubSpaceId)) {
        setCurrentSubSpace(null);
        setCurrentSubSpace(spaceId);
      }
    }
  }

  public void addSpaceInCache(String theSpaceId) {
    String spaceId = getShortSpaceId(theSpaceId);
    m_ManageableSpaces.add(spaceId);
    DisplaySorted newElmt = buildSpaceObject(spaceId);

    if (newElmt != null) {
      if (newElmt.type == DisplaySorted.TYPE_SPACE) {
        DisplaySorted[] oldSpaces = m_Spaces;
        m_Spaces = new DisplaySorted[oldSpaces.length + 1];
        System.arraycopy(oldSpaces, 0, m_Spaces, 0, oldSpaces.length);
        m_Spaces[oldSpaces.length] = newElmt;
        Arrays.sort(m_Spaces);
      } else { // Sub Space case :
        setCurrentSpace(m_CurrentSpaceId);
      }
    }
  }

  public void removeSpaceInCache(String theSpaceId) {
    String spaceId = getShortSpaceId(theSpaceId);
    DisplaySorted elmt = getSpaceCache(spaceId);

    if (elmt != null) {
      if (elmt.type == DisplaySorted.TYPE_SPACE) {
        DisplaySorted[] oldSpaces = m_Spaces;
        int j = 0;

        m_Spaces = new DisplaySorted[oldSpaces.length - 1];
        for (DisplaySorted oldSpace : oldSpaces) {
          if (!oldSpace.id.equals(spaceId) && (j < m_Spaces.length)) {
            m_Spaces[j++] = oldSpace;
          }
        }
        if (m_CurrentSpaceId != null && m_CurrentSpaceId.equals(spaceId)) {
          setCurrentSpace(null);
        }
      } else {
        DisplaySorted[] oldSpaces = m_SubSpaces;
        int j = 0;

        m_SubSpaces = new DisplaySorted[oldSpaces.length - 1];
        for (DisplaySorted oldSpace : oldSpaces) {
          if (!oldSpace.id.equals(spaceId) && (j < m_SubSpaces.length)) {
            m_SubSpaces[j++] = oldSpace;
          }
        }
        if (m_CurrentSubSpaceId != null && m_CurrentSubSpaceId.equals(spaceId)) {
          setCurrentSubSpace(null);
        }
      }
    }
  }

  public void resetAllCache() {
    String currentSpaceId = m_CurrentSpaceId;
    String currentSubSpaceId = m_CurrentSubSpaceId;


    initWithUser(m_SessionCtrl, m_user);
    if (currentSpaceId != null) {
      setCurrentSpace(currentSpaceId);
    }
    if (currentSubSpaceId != null) {
      setCurrentSubSpace(currentSubSpaceId);
    }
  }

  public void initWithUser(AbstractComponentSessionController msc, UserDetail user) {
    String sUserId = user.getId();


    m_sContext = URLUtil.getApplicationURL();
    m_administrationCtrl = ServiceProvider.getService(AdminController.class);
    m_SessionCtrl = msc;
    m_user = user;
    m_elmtCounter = 0;
    m_CurrentSpaceId = null;
    m_CurrentSubSpaceId = null;
    m_SubSpaces = new DisplaySorted[0];
    m_SpaceComponents = new DisplaySorted[0];
    m_SubSpaceComponents = new DisplaySorted[0];

    if (!m_user.isAccessAdmin()) {
      String[] allManageableSpaceIds = m_administrationCtrl.getUserManageableSpaceIds(sUserId);
      // First of all, add the manageable spaces into the set
      m_ManageableSpaces.clear();
      for (String manageableSpaceId : allManageableSpaceIds) {
        m_ManageableSpaces.add(getShortSpaceId(manageableSpaceId));
      }
    }

    String[] spaceIds = m_administrationCtrl.getAllRootSpaceIds();
    m_Spaces = createSpaceObjects(spaceIds, false);
  }

  // Spaces functions
  // ----------------
  public DisplaySorted[] getAvailableSpaces() {
    return m_Spaces;
  }

  public String getCurrentSpaceId() {
    return m_CurrentSpaceId;
  }

  public DisplaySorted getSpace(String theSpaceId) {
    return getSpaceCache(getShortSpaceId(theSpaceId));
  }

  public boolean setCurrentSpace(String theSpaceId) {
    String spaceId = getShortSpaceId(theSpaceId);

    m_CurrentSpaceId = spaceId;
    // Reset the selected sub space
    m_CurrentSubSpaceId = null;
    m_SubSpaceComponents = new DisplaySorted[0];
    if (StringUtil.isDefined(m_CurrentSpaceId) && getSpaceCache(m_CurrentSpaceId) == null) {
      m_CurrentSpaceId = null;
    }

    if (!StringUtil.isDefined(spaceId) || (m_CurrentSpaceId == null)) {
      m_SpaceComponents = new DisplaySorted[0];
      m_SubSpaces = new DisplaySorted[0];
    } else {
      SpaceInst spaceInst = m_administrationCtrl.getSpaceInstById("WA" + spaceId);
      // Get the space's components and sub-spaces
      if (spaceInst == null) {
        m_SpaceComponents = new DisplaySorted[0];
        m_SubSpaces = new DisplaySorted[0];
        m_CurrentSpaceId = null;
      } else {
        m_SpaceComponents = createComponentObjects(spaceInst, false);
        List<SpaceInst> subspaces = spaceInst.getSubSpaces();
        String[] spaceIds = new String[subspaces.size()];
        for (int i = 0; i < subspaces.size(); i++) {
          spaceIds[i] = subspaces.get(i).getId();
        }
        m_SubSpaces = createSpaceObjects(spaceIds, true);
      }
    }
    for (DisplaySorted ds : m_Spaces) {
      buildSpaceHTMLLine(ds);
    }

    return StringUtil.isDefined(m_CurrentSpaceId);
  }

  public DisplaySorted[] getAvailableSpaceComponents() {
    if (m_CurrentSpaceId == null) {
      return new DisplaySorted[0];
    }
    return m_SpaceComponents;
  }

  // Sub-Spaces functions
  // --------------------
  public DisplaySorted[] getAvailableSubSpaces() {
    if (m_CurrentSpaceId == null) {
      return new DisplaySorted[0];
    }
    return m_SubSpaces;
  }

  public String getCurrentSubSpaceId() {
    return m_CurrentSubSpaceId;
  }

  public boolean setCurrentSubSpace(String theSpaceId) {
    String subSpaceId = getShortSpaceId(theSpaceId);
    SpaceInst sp = null;

    m_CurrentSubSpaceId = subSpaceId;
    if (StringUtil.isDefined(m_CurrentSubSpaceId) && (getSpaceCache(m_CurrentSubSpaceId) == null)) {
      m_CurrentSubSpaceId = null;
    }
    if (StringUtil.isDefined(m_CurrentSubSpaceId)) {
      sp = m_administrationCtrl.getSpaceInstById("WA" + m_CurrentSubSpaceId);
      if (sp == null) {
        m_CurrentSubSpaceId = null;
      }
    }
    for (DisplaySorted m_SubSpace : m_SubSpaces) {
      buildSpaceHTMLLine(m_SubSpace);
    }
    if (StringUtil.isDefined(m_CurrentSubSpaceId)) {
      m_SubSpaceComponents = createComponentObjects(sp, true);
    } else {
      m_SubSpaceComponents = new DisplaySorted[0];
    }
    return StringUtil.isDefined(m_CurrentSubSpaceId);
  }

  public DisplaySorted[] getAvailableSubSpaceComponents() {
    if (m_CurrentSubSpaceId == null) {
      return new DisplaySorted[0];
    }
    return m_SubSpaceComponents;
  }

  protected DisplaySorted getSpaceCache(String spaceId) {
    if (spaceId == null) {
      return null;
    }

    for (DisplaySorted space : m_Spaces) {
      if (spaceId.equals(space.id)) {
        return space;
      }
    }

    for (DisplaySorted subspace : m_SubSpaces) {
      if (spaceId.equals(subspace.id)) {
        return subspace;
      }
    }

    return null;
  }

  protected DisplaySorted[] createSpaceObjects(String[] spaceIds, boolean goRecurs) {
    if (spaceIds == null) {
      return new DisplaySorted[0];
    }
    DisplaySorted[] valret = new DisplaySorted[spaceIds.length];
    for (int j = 0; j < valret.length; j++) {
      valret[j] = buildSpaceObject(spaceIds[j]);
    }
    Arrays.sort(valret);
    if (goRecurs) {
      DisplaySorted[] parents = valret;
      List<DisplaySorted> alValret = new ArrayList<>();
      for (DisplaySorted parent : parents) {
        alValret.add(parent);
        String[] subSpaceIds = m_administrationCtrl.getAllSubSpaceIds(parent.id);
        DisplaySorted[] children = createSpaceObjects(subSpaceIds, true);
        Collections.addAll(alValret, children);
      }
      valret = alValret.toArray(new DisplaySorted[alValret.size()]);
    }
    return valret;
  }

  protected DisplaySorted buildSpaceObject(String spaceId) {
    DisplaySorted valret = new DisplaySorted();

    valret.id = getShortSpaceId(spaceId);
    valret.isVisible = true;
    SpaceInstLight spaceInst = m_administrationCtrl.getSpaceInstLight(spaceId);
    if (spaceInst.isRoot()) {
      valret.type = DisplaySorted.TYPE_SPACE;
      valret.isAdmin = m_user.isAccessAdmin() || m_ManageableSpaces.contains(valret.id);
      if (!valret.isAdmin) { // Rattrapage....
        String[] manageableSubSpaceIds =
            m_administrationCtrl.getUserManageableSubSpaceIds(m_user.getId(), valret.id);
        if ((manageableSubSpaceIds == null) || (manageableSubSpaceIds.length <= 0)) {
          valret.isVisible = false;
        }
      }
    } else {
      valret.type = DisplaySorted.TYPE_SUBSPACE;
      valret.isAdmin = m_user.isAccessAdmin() || isAdminOfSpace(spaceInst);
      if (!valret.isAdmin) { // Rattrapage....
        String[] manageableSubSpaceIds =
            m_administrationCtrl.getUserManageableSubSpaceIds(m_user.getId(), valret.id);
        if ((manageableSubSpaceIds == null) || (manageableSubSpaceIds.length <= 0)) {
          valret.isVisible = false;
        }
      }
    }
    valret.name = spaceInst.getName(m_SessionCtrl.getLanguage());
    valret.orderNum = spaceInst.getOrderNum();
    valret.deep = spaceInst.getLevel();
    buildSpaceHTMLLine(valret);
    return valret;
  }

  protected String getShortSpaceId(String spaceId) {
    if ((spaceId != null) && (spaceId.startsWith("WA"))) {
      return spaceId.substring(2);
    } else {
      return (spaceId == null) ? "" : spaceId;
    }
  }

  protected void buildSpaceHTMLLine(DisplaySorted space) {
    if (space.isVisible) {
      if (space.type == DisplaySorted.TYPE_SUBSPACE) {
        String link;
        int objType;
        String spaceName;
        StringBuilder spacesSpaces = new StringBuilder();

        objType = (space.id.equals(m_CurrentSubSpaceId)) ? SPACE_EXPANDED : SPACE_COLLAPSE;
        link = "GoToSubSpace?SubSpace=" + space.id;
        if (m_SessionCtrl.isSpaceInMaintenance(space.id)) {
          spaceName = space.name + " (M)";
        } else {
          spaceName = space.name;
        }
        for (int i = 0; i < space.deep - 1; i++) {
          spacesSpaces.append("&nbsp;&nbsp;");
        }
        space.htmlLine = spacesSpaces.toString() + "<a name=\"" + space.id + "\"/>" +
            urlFactory(link, "space" + space.id, "", spaceName, SPACE, objType, m_sContext, "");
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("<option ");
        if (space.id.equals(m_CurrentSpaceId)) {
          sb.append("selected ");
        }
        sb.append("value=").append(space.id).append(">").append(space.name);
        if (m_SessionCtrl.isSpaceInMaintenance(space.id)) {
          sb.append(" (M)");
        }
        sb.append("</option>");
        space.htmlLine = sb.toString();
      }
    } else {
      space.htmlLine = "";
    }
  }

  protected boolean isAdminOfSpace(SpaceInstLight spaceInst) {
    boolean valret = m_ManageableSpaces.contains(String.valueOf(spaceInst.getLocalId())) ||
        m_ManageableSpaces.contains(getShortSpaceId(spaceInst.getFatherId()));
    SpaceInstLight parcSpaceInst = spaceInst;

    while (!valret && !parcSpaceInst.isRoot()) {
      parcSpaceInst = m_administrationCtrl.getSpaceInstLight(parcSpaceInst.getFatherId());
      valret = m_ManageableSpaces.contains(String.valueOf(parcSpaceInst.getLocalId()));
    }

    return valret;
  }

  protected DisplaySorted[] createComponentObjects(SpaceInst spaceInst, boolean subSpaces) {
    // Get the space's components
    List<ComponentInst> components = spaceInst.getAllComponentsInst();
    boolean isTheSpaceAdmin =
        m_user.isAccessAdmin() || isAdminOfSpace(new SpaceInstLight(spaceInst));
    List<DisplaySorted> result = new ArrayList<>();
    Iterator<ComponentInst> componentInstIterator = components.iterator();
    while (componentInstIterator.hasNext()) {
      ComponentInst ci = componentInstIterator.next();
      DisplaySorted ds = new DisplaySorted();
      ds.name = ci.getLabel(m_SessionCtrl.getLanguage());
      if (ds.name == null) {
        ds.name = ci.getName();
      }
      ds.orderNum = ci.getOrderNum();
      ds.id = ci.getId();
      ds.type = DisplaySorted.TYPE_COMPONENT;
      ds.isAdmin = isTheSpaceAdmin;
      ds.deep = spaceInst.getLevel();
      ds.isVisible = isTheSpaceAdmin;
      if (ds.isVisible) {
        // Build HTML Line
        String label = ds.name;
        String link = "GoToComponent?ComponentId=" + ci.getId();
        final int objType;
        if (subSpaces) {
          if (componentInstIterator.hasNext()) {
            objType = SUBSPACE_LAST_COMPONENT;
          } else {
            objType = SUBSPACE_COMPONENT;
          }
        } else {
          objType = SPACE_COMPONENT;
        }
        StringBuilder componentsSpaces = new StringBuilder();
        for (int j = 0; j < ds.deep - 1; j++) {
          componentsSpaces.append("&nbsp;&nbsp;");
        }
        String componentIcon = ci.getName();
        if (ci.isWorkflow()) {
          componentIcon = "processManager";
        }
        ds.htmlLine = componentsSpaces.toString() +
            urlFactory(link, "element" + m_elmtCounter++, componentIcon, label, COMPONENT,
                objType, m_sContext, "startPageContent");
      } else {
        ds.htmlLine = "";
      }
      result.add(ds);
    }
    Collections.sort(result);
    return result.toArray(new DisplaySorted[result.size()]);
  }

  protected String urlFactory(String link, String elementLabel, String imageLinked,
      String labelLinked, int elementType, int imageType, String m_sContext, String target) {
    StringBuilder result = new StringBuilder();
    String boldStart = "";
    String boldEnd = "";

    switch (elementType) {
      case SPACE:
        target = "";
        boldStart = "";
        boldEnd = "";
        break;
      case COMPONENT:
        if ((target != null) && (target.length() > 0)) {
          target = "target=\"" + target + "\"";
        }
        boldStart = "";
        boldEnd = "";
        break;
      case COMPONENTPOPUP:
        target = "target=\"_blank\"";
        boldStart = "";
        boldEnd = "";
        break;
    }
    String safeElementLabel = Encode.forHtml(elementLabel);
    imageLinked =
        "<img name=\"" + safeElementLabel + "\" src=\"" + m_sContext + "/util/icons/component/" +
            imageLinked + "Small.gif\" class=\"component-icon\"/>";
    switch (imageType) {
      case SPACE_COLLAPSE:
        result.append("<a href=\"").append(link).append("\"").append(target).append("><img src=\"")
            .append(m_sContext)
            .append("/util/icons/plusTree.gif\" border=\"0\" align=\"absmiddle\"></a>");
        imageLinked = "<img name=\"" + safeElementLabel + "\" src=\"" + m_sContext +
            "/util/icons/colorPix/1px.gif\" width=\"1\" height=\"1\" border=\"0\" " +
            "align=\"absmiddle\">";
        break;
      case SPACE_EXPANDED:
        result.append("<a href=\"").append(link).append("\"").append(target).append("><img src=\"")
            .append(m_sContext)
            .append("/util/icons/minusTree.gif\" border=\"0\" align=\"absmiddle\"></a>");
        imageLinked = "<img name=\"" + safeElementLabel + "\" src=\"" + m_sContext +
            "/util/icons/colorPix/1px.gif\" width=\"1\" height=\"1\" border=\"0\" " +
            "align=\"absmiddle\">";
        break;
      case SPACE_COMPONENT:
        break;
      case SUBSPACE_COMPONENT:
        result.append("<img src=\"").append(m_sContext)
            .append("/util/icons/minusTreeT.gif\" border=\"0\" align=\"absmiddle\">");
        break;
      case SUBSPACE_LAST_COMPONENT:
        result.append("<img src=\"").append(m_sContext)
            .append("/util/icons/minusTreeL.gif\" border=\"0\" align=\"absmiddle\">");
        break;
    }
    String safeLabelLinked = Encode.forHtml(labelLinked);
    result.append("<a href=\"").append(link).append("\" ").append(target).append(">")
        .append(imageLinked).append("&nbsp;</a>");
    result.append("<a href=\"").append(link).append("\" ").append(target).append(">")
        .append(boldStart).append(safeLabelLinked).append(boldEnd).append("</a><br/>");
    return result.toString();
  }
}
