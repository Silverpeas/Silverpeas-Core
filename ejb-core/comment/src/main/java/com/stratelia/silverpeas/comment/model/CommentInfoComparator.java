package com.stratelia.silverpeas.comment.model;

import java.util.Comparator;

public class CommentInfoComparator implements Comparator {

  public int compare(Object o1, Object o2) {
    // TODO Auto-generated method stub
    CommentInfo commentInfo1 = (CommentInfo) o1;
    CommentInfo commentInfo2 = (CommentInfo) o2;
    return commentInfo2.getCommentCount() - commentInfo1.getCommentCount();
  }

}
