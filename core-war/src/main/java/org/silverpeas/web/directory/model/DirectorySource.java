package org.silverpeas.web.directory.model;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.Group;

public class DirectorySource {

  private static final int TYPE_DOMAIN = 0;
  private static final int TYPE_GROUP = 1;
  private static final int TYPE_CONTACTS_COMPONENT = 2;

  private String id;
  private String label;
  private String description;
  private boolean selected;
  private int type;

  private void init(final String id, final String label, String description, int type) {
    this.id = id;
    this.label = label;
    this.description = description;
    this.type = type;
  }

  public DirectorySource(final Domain domain) {
    init(domain.getId(), domain.getName(), domain.getDescription(), TYPE_DOMAIN);
  }

  public DirectorySource(final SilverpeasComponentInstance component, String lang) {
    init(component.getId(), component.getLabel(lang), component.getDescription(lang),
        TYPE_CONTACTS_COMPONENT);
  }

  public DirectorySource(final Group group) {
    init(group.getId(), group.getName(), group.getDescription(), TYPE_GROUP);
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(final boolean selected) {
    this.selected = selected;
  }

  public String getUniqueId() {
    if (isGroup()) {
      return "group_"+getId();
    }
    return getId();
  }

  public int getType() {
    return type;
  }

  public void setType(final int type) {
    this.type = type;
  }

  public boolean isDomain() {
    return type == TYPE_DOMAIN;
  }

  public boolean isGroup() {
    return type == TYPE_GROUP;
  }

  public boolean isContactsComponent() {
    return type == TYPE_CONTACTS_COMPONENT;
  }
}
