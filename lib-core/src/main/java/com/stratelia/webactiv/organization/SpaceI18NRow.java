package com.stratelia.webactiv.organization;

import com.stratelia.webactiv.beans.admin.SpaceI18N;

public class SpaceI18NRow {
  public int id = -1;
  public int spaceId = -1;
  public String lang = null;
  public String name = null;
  public String description = null;

  public SpaceI18NRow() {
  }

  public SpaceI18NRow(SpaceI18N spaceI18N) {
    id = spaceI18N.getId();
    spaceId = Integer.parseInt(spaceI18N.getSpaceId());
    lang = spaceI18N.getLanguage();
    name = spaceI18N.getName();
    description = spaceI18N.getDescription();
  }

  public SpaceI18NRow(SpaceRow space) {
    spaceId = space.id;
    lang = space.lang;
    name = space.name;
    description = space.description;
  }
}
