package com.silverpeas.workflow.engine.model;

import java.io.Serializable;

import com.silverpeas.workflow.api.model.Item;
import com.silverpeas.workflow.api.model.Participant;
import com.silverpeas.workflow.api.model.RelatedUser;
import com.silverpeas.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;relatedUser&gt; element of a
 * Process Model.
 **/
public class RelatedUserImpl extends AbstractReferrableObject implements
    RelatedUser, Serializable {
  private Participant participant;
  private Item folderItem;
  private String relation;
  private String role;

  /**
   * Constructor
   */
  public RelatedUserImpl() {
    super();
  }

  /**
   * Get the referred participant
   */
  public Participant getParticipant() {
    return participant;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.workflow.api.model.RelatedUser#setParticipant(com.silverpeas
   * .workflow.api.model.Participant)
   */
  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

  /**
   * Get the referred item
   */
  public Item getFolderItem() {
    return folderItem;
  }

  /**
   * Set the referred item
   * 
   * @param folderItem
   *          item to refer
   */
  public void setFolderItem(Item folderItem) {
    this.folderItem = folderItem;
  }

  /**
   * Get the relation between user and participant
   */
  public String getRelation() {
    return this.relation;
  }

  /**
   * Set the relation between user and participant
   * 
   * @param relation
   *          relation as a String
   */
  public void setRelation(String relation) {
    this.relation = relation;
  }

  /**
   * Get the role to which the related user will be affected
   * 
   * @return the role name
   */
  public String getRole() {
    return this.role;
  }

  /**
   * Set the role to which the related user will be affected
   * 
   * @param role
   *          role as a String
   */
  public void setRole(String role) {
    this.role = role;
  }

  /*
   * @see AbstractReferrableObject#getKey()
   */
  public String getKey() {
    StringBuffer sb = new StringBuffer();

    if (participant instanceof AbstractReferrableObject)
      sb.append(((AbstractReferrableObject) participant).getKey());

    sb.append("|");

    if (folderItem instanceof AbstractReferrableObject)
      sb.append(((AbstractReferrableObject) folderItem).getKey());

    sb.append("|");

    if (relation != null)
      sb.append(relation);

    sb.append("|");

    if (role != null)
      sb.append(role);

    return sb.toString();
  }
}