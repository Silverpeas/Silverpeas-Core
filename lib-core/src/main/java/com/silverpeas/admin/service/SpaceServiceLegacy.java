package com.silverpeas.admin.service;

import java.util.Map;

import javax.inject.Named;

import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;

@Named("silverpeasSpaceService")
public class SpaceServiceLegacy implements SpaceService {

  @Override
  public String createSpace(SpaceInst spaceInst, String creatorId) throws AdminException {
    Admin admin = AdminReference.getAdminService();

    Map<String, WAComponent> allComponents = admin.getAllComponents();
    for (ComponentInst component : spaceInst.getAllComponentsInst()) {
      if ( !StringUtil.isDefined(component.getLabel()) ) {
        WAComponent waComponent = allComponents.get(component.getName());
        component.setLabel(waComponent.getLabel().get(spaceInst.getLanguage()));
      }
    }
    return admin.addSpaceInst(creatorId, spaceInst);
  }
}
