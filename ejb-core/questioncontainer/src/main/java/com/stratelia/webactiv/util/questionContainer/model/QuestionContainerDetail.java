package com.stratelia.webactiv.util.questionContainer.model;

import java.util.Collection;
import java.util.Iterator;

import com.stratelia.webactiv.util.question.model.Question;

public class QuestionContainerDetail implements java.io.Serializable {

  private QuestionContainerHeader header = null;
  private Collection questions = null;
  private Collection comments = null;   //Comments Collection
  private Collection votes = null;      //QuestionResult Collection of Current user


  public QuestionContainerDetail() {
    init(null, null, null, null);
  }

  public QuestionContainerDetail(QuestionContainerHeader header,Collection questions,Collection comments,Collection votes) {
      init(header, questions, comments, votes);
  }

  private void init(QuestionContainerHeader header,Collection questions,Collection comments,Collection votes) {
    setHeader(header);
    setQuestions(questions);
    setComments(comments);
    setCurrentUserVotes(votes);
  }

  public void setHeader(QuestionContainerHeader header) {
    this.header = header;
  }

  public void setQuestions(Collection questions) {
    this.questions = questions;
  }

  public void setComments(Collection comments) {
    this.comments = comments;
  }

  public void setCurrentUserVotes(Collection votes) {
    this.votes = votes;
  }

  public QuestionContainerHeader getHeader() {
    return this.header;
  }

  public Collection getQuestions() {
    return this.questions;
  }
  
  public Question getFirstQuestion() {
    Question question = null;
    Collection questions = getQuestions();
    Iterator it = questions.iterator();
    if (it.hasNext()) {
        question = (Question) it.next();
    }
    return question;
  }

  public Collection getComments() {
    return this.comments;
  }

  public Collection getCurrentUserVotes() {
    return this.votes;
  }

}