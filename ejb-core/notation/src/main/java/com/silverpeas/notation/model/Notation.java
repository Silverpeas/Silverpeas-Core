package com.silverpeas.notation.model;

import java.io.Serializable;

/**
 * 
 */
public class Notation implements Serializable {

  public static final int ID_UNDEFINED = -1;

  /**
   * Elements types referenced by notations.
   */
  public static final int TYPE_UNDEFINED = -1;
  public static final int TYPE_PUBLICATION = 0;
  public static final int TYPE_FORUM = 1;
  public static final int TYPE_MESSAGE = 2;

  private int id;
  private String instanceId;
  private String externalId;
  private int externalType;
  private String author;
  private int note;

  public Notation(int id, String instanceId, String externalId,
      int externalType, String author, int note) {
    this.id = id;
    this.instanceId = instanceId;
    this.externalId = externalId;
    this.externalType = externalType;
    this.author = author;
    this.note = note;
  }

  public Notation(NotationPK pk, int note) {
    this.id = ID_UNDEFINED;
    this.instanceId = pk.getInstanceId();
    this.externalId = pk.getId();
    this.externalType = pk.getType();
    this.author = pk.getUserId();
    this.note = note;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public int getExternalType() {
    return externalType;
  }

  public void setExternalType(int externalType) {
    this.externalType = externalType;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public int getNote() {
    return note;
  }

  public void setNote(int note) {
    this.note = note;
  }

}