package com.stratelia.webactiv.util.questionContainer.model;

public class Comment implements java.io.Serializable {

  private CommentPK commentPK = null;
  private QuestionContainerPK questionContainerPK = null;
  private String userId = null;
  private String comment = null;
  private boolean isAnonymous = false;
  private String date = null;

  public Comment(CommentPK commentPK, QuestionContainerPK questionContainerPK,
      String userId, String comment, boolean isAnonymous, String date) {
    this.commentPK = commentPK;
    this.questionContainerPK = questionContainerPK;
    this.userId = userId;
    this.comment = comment;
    this.isAnonymous = isAnonymous;
    this.date = date;
  }

  public CommentPK getPK() {
    return commentPK;
  }

  public QuestionContainerPK getQuestionContainerPK() {
    return questionContainerPK;
  }

  public String getUserId() {
    return userId;
  }

  public String getComment() {
    return comment;
  }

  public boolean isAnonymous() {
    return isAnonymous;
  }

  public String getDate() {
    return date;
  }
}