/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.beans.admin;

import com.silverpeas.admin.components.Parameter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.util.i18n.AbstractI18NBean;

public class ComponentInst extends AbstractI18NBean implements Serializable, Cloneable,
    Comparable<ComponentInst> {

  private static final long serialVersionUID = 1L;
  public final static String STATUS_REMOVED = "R";
  private String m_sId;
  private String m_sName;
  private String m_sLabel;
  private String m_sDescription;
  private String m_sDomainFatherId;
  private int m_iOrderNum;
  private Date createDate = null;
  private Date updateDate = null;
  private Date removeDate = null;
  private String status = null;
  private String creatorUserId;
  private UserDetail creator;
  private String updaterUserId;
  private UserDetail updater;
  private String removerUserId;
  private UserDetail remover;
  private boolean isPublic = false;
  private boolean isHidden = false;
  private boolean isInheritanceBlocked = false;
  private List<ProfileInst> m_alProfileInst;
  private List<Parameter> parameters = null;

  /** Creates new ComponentInst */
  public ComponentInst() {
    m_sId = "";
    m_sName = "";
    m_sLabel = "";
    m_sDescription = "";
    m_sDomainFatherId = "";
    m_iOrderNum = 0;
    m_alProfileInst = new ArrayList<ProfileInst>();
    isPublic = false;
    isHidden = false;
  }

  public int compareTo(ComponentInst o) {
    return m_iOrderNum - o.m_iOrderNum;
  }

  public Object clone() {
    ComponentInst ci = new ComponentInst();
    Iterator<ProfileInst> it;

    ci.m_sId = m_sId;
    ci.m_sName = m_sName;
    ci.m_sLabel = m_sLabel;
    ci.m_sDescription = m_sDescription;
    ci.m_sDomainFatherId = m_sDomainFatherId;
    ci.m_iOrderNum = m_iOrderNum;
    ci.isPublic = isPublic;
    ci.isHidden = isHidden;
    ci.isInheritanceBlocked = isInheritanceBlocked;

    if (m_alProfileInst == null) {
      ci.m_alProfileInst = null;
    } else {
      ci.m_alProfileInst = new ArrayList<ProfileInst>();
      it = m_alProfileInst.iterator();
      while (it.hasNext()) {
        ci.m_alProfileInst.add((ProfileInst) it.next().clone());
      }
    }
    ci.parameters = new ArrayList<Parameter>(this.parameters);
    return ci;
  }

  protected String[] cloneStringArray(String[] src) {
    String[] valret;

    if (src == null) {
      return null;
    } else {
      valret = new String[src.length];
      for (int i = 0; i < src.length; i++) {
        valret[i] = src[i];
      }
    }
    return valret;
  }

  public void setId(String sId) {
    m_sId = sId;
  }

  public String getId() {
    return m_sId;
  }

  public void setName(String sName) {
    m_sName = sName;
  }

  public String getName() {
    return m_sName;
  }

  public void setLabel(String sLabel) {
    m_sLabel = sLabel;
  }

  public String getLabel() {
    return m_sLabel;
  }

  public void setDescription(String sDescription) {
    m_sDescription = sDescription;
  }

  public String getDescription() {
    return m_sDescription;
  }

  public void setDomainFatherId(String sDomainFatherId) {
    m_sDomainFatherId = sDomainFatherId;
  }

  public String getDomainFatherId() {
    return m_sDomainFatherId;
  }

  public void setOrderNum(int iOrderNum) {
    m_iOrderNum = iOrderNum;
  }

  public int getOrderNum() {
    return m_iOrderNum;
  }

  public Date getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  public Date getRemoveDate() {
    return removeDate;
  }

  public void setRemoveDate(Date removeDate) {
    this.removeDate = removeDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public String getRemoverUserId() {
    return removerUserId;
  }

  public void setRemoverUserId(String removerUserId) {
    this.removerUserId = removerUserId;
  }

  public String getUpdaterUserId() {
    return updaterUserId;
  }

  public void setUpdaterUserId(String updaterUserId) {
    this.updaterUserId = updaterUserId;
  }

  public int getNumProfileInst() {
    return m_alProfileInst.size();
  }

  public void addProfileInst(ProfileInst profileInst) {
    m_alProfileInst.add(profileInst);
  }

  public void deleteProfileInst(ProfileInst profileInst) {
    for (int nI = 0; nI < m_alProfileInst.size(); nI++) {
      if (m_alProfileInst.get(nI).getName().equals(profileInst.getName())) {
        m_alProfileInst.remove(nI);
      }
    }
  }

  public List<ProfileInst> getAllProfilesInst() {
    return m_alProfileInst;
  }

  public List<ProfileInst> getInheritedProfiles() {
    List<ProfileInst> profiles = new ArrayList<ProfileInst>();
    for (int nI = 0; nI < m_alProfileInst.size(); nI++) {
      ProfileInst profile = m_alProfileInst.get(nI);
      if (profile.isInherited()) {
        profiles.add(profile);
      }
    }

    return profiles;
  }

  public List<ProfileInst> getProfiles() {
    List<ProfileInst> profiles = new ArrayList<ProfileInst>();
    for (int nI = 0; nI < m_alProfileInst.size(); nI++) {
      ProfileInst profile = m_alProfileInst.get(nI);
      if (!profile.isInherited()) {
        profiles.add(profile);
      }
    }

    return profiles;
  }

  public void removeAllProfilesInst() {
    m_alProfileInst = new ArrayList<ProfileInst>();
  }

  public ProfileInst getProfileInst(String sProfileName) {
    for (int nI = 0; nI < m_alProfileInst.size(); nI++) {
      ProfileInst profile = m_alProfileInst.get(nI);
      if (!profile.isInherited() && profile.getName().equals(sProfileName)) {
        return profile;
      }
    }
    return null;
  }

  public ProfileInst getInheritedProfileInst(String sProfileName) {
    for (int nI = 0; nI < m_alProfileInst.size(); nI++) {
      ProfileInst profile = m_alProfileInst.get(nI);
      if (profile.isInherited() && profile.getName().equals(sProfileName)) {
        return profile;
      }
    }
    return null;
  }

  public ProfileInst getProfileInst(int nIndex) {
    return m_alProfileInst.get(nIndex);
  }


  public List<Parameter> getParameters() {
    if (parameters == null) {
      parameters = new ArrayList<Parameter>();
    }
    return parameters;
  }

  public void setParameters(List<Parameter> parameters) {
    this.parameters = parameters;
  }

  public Parameter getParameter(String parameterName) {
    for(Parameter parameter : parameters) {
      if(parameter.getName().equalsIgnoreCase(parameterName)){
        return parameter;
      }
    }
    return null;
  }

  public String getParameterValue(String parameterName) {
    Parameter param = getParameter(parameterName);
    if(param != null) {
      return param.getValue();
    }
    return null;
  }

  public String getLabel(String language) {
    ComponentI18N s = (ComponentI18N) getTranslations().get(language);
    if (s != null) {
      return s.getName();
    } else {
      return getLabel();
    }
  }

  public String getDescription(String language) {
    ComponentI18N s = (ComponentI18N) getTranslations().get(language);
    if (s != null) {
      return s.getDescription();
    }
    return getDescription();
  }

  public boolean isHidden() {
    return isHidden;
  }

  public void setHidden(boolean isHidden) {
    this.isHidden = isHidden;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  public boolean isInheritanceBlocked() {
    return isInheritanceBlocked;
  }

  public void setInheritanceBlocked(boolean isInheritanceBlocked) {
    this.isInheritanceBlocked = isInheritanceBlocked;
  }

  public String getCreatorUserId() {
    return creatorUserId;
  }

  public void setCreatorUserId(String creatorUserId) {
    this.creatorUserId = creatorUserId;
  }

  public UserDetail getCreator() {
    return creator;
  }

  public void setCreator(UserDetail creator) {
    this.creator = creator;
  }

  public UserDetail getUpdater() {
    return updater;
  }

  public void setUpdater(UserDetail updater) {
    this.updater = updater;
  }

  public UserDetail getRemover() {
    return remover;
  }

  public void setRemover(UserDetail remover) {
    this.remover = remover;
  }

  public void removeInheritedProfiles() {
    ArrayList<ProfileInst> newProfiles = new ArrayList<ProfileInst>();
    for (ProfileInst profile : m_alProfileInst) {
      if (!profile.isInherited()) {
        newProfiles.add(profile);
      }
    }
    m_alProfileInst = newProfiles;
  }
}