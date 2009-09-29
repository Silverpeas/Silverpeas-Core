package com.stratelia.silverpeas.comment.model;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * This object contains the description of document
 * 
 * @author Georgy Shakirin
 * @version 1.0
 */
public class Comment implements java.io.Serializable {

  private CommentPK pk;
  private WAPrimaryKey foreign_key;
  private int owner_id;
  private String owner;
  private String message;
  private String creation_date;
  private String modification_date;

  private void init(CommentPK pk, WAPrimaryKey foreign_key, int owner_id,
      String owner, String message, String creation_date,
      String modification_date) {
    this.pk = pk;
    this.foreign_key = foreign_key;
    this.owner_id = owner_id;
    this.owner = owner;
    this.message = message;
    this.creation_date = creation_date;
    this.modification_date = modification_date;
  }

  public Comment(CommentPK pk, WAPrimaryKey foreign_key, int owner_id,
      String owner, String message, String creation_date,
      String modification_date) {
    init(pk, foreign_key, owner_id, owner, message, creation_date,
        modification_date);
  }

  public void setCommentPK(CommentPK pk) {
    this.pk = pk;
  }

  public CommentPK getCommentPK() {
    return this.pk;
  }

  public void setForeignKey(WAPrimaryKey foreign_key) {
    this.foreign_key = foreign_key;
  }

  public WAPrimaryKey getForeignKey() {
    return this.foreign_key;
  }

  public void setOwnerId(int owner_id) {
    this.owner_id = owner_id;
  }

  public int getOwnerId() {
    return this.owner_id;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getOwner() {
    return this.owner;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return this.message;
  }

  public void setCreationDate(String creation_date) {
    this.creation_date = creation_date;
  }

  public String getCreationDate() {
    return this.creation_date;
  }

  public void setModificationDate(String modification_date) {
    this.modification_date = modification_date;
  }

  public String getModificationDate() {
    return this.modification_date;
  }

  public String toString() {
    StringBuffer str = new StringBuffer();
    str.append("getCommentPK() = ").append(getCommentPK().toString()).append(
        ", \n");
    str.append("getForeignKey() = ").append(getForeignKey().toString()).append(
        ", \n");
    str.append("getOwnerId() = ").append(getOwnerId()).append(", \n");
    str.append("getMessage() = ").append(getMessage().toString())
        .append(", \n");
    str.append("getCreationDate() = ").append(getCreationDate().toString())
        .append(", \n");
    str.append("getModificationDate() = ").append(
        getModificationDate().toString());
    return str.toString();
  }
}