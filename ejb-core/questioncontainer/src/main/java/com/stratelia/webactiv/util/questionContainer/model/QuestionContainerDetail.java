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

package com.stratelia.webactiv.util.questionContainer.model;

import java.util.Collection;
import java.util.Iterator;

import com.stratelia.webactiv.util.question.model.Question;
import com.stratelia.webactiv.util.questionResult.model.QuestionResult;

public class QuestionContainerDetail implements java.io.Serializable {

  private static final long serialVersionUID = -2502073007907742590L;
  private QuestionContainerHeader header = null;
  private Collection<Question> questions = null;
  private Collection<Comment> comments = null; // Comments Collection
  private Collection<QuestionResult> votes = null; // QuestionResult Collection of Current user

  public QuestionContainerDetail() {
    init(null, null, null, null);
  }

  public QuestionContainerDetail(QuestionContainerHeader header,
      Collection<Question> questions, Collection<Comment> comments, Collection<QuestionResult> votes) {
    init(header, questions, comments, votes);
  }

  private void init(QuestionContainerHeader header, Collection<Question> questions,
      Collection<Comment> comments, Collection<QuestionResult> votes) {
    setHeader(header);
    setQuestions(questions);
    setComments(comments);
    setCurrentUserVotes(votes);
  }

  public void setHeader(QuestionContainerHeader header) {
    this.header = header;
  }

  public void setQuestions(Collection<Question> questions) {
    this.questions = questions;
  }

  public void setComments(Collection<Comment> comments) {
    this.comments = comments;
  }

  public void setCurrentUserVotes(Collection<QuestionResult> votes) {
    this.votes = votes;
  }

  public QuestionContainerHeader getHeader() {
    return this.header;
  }

  public Collection<Question> getQuestions() {
    return this.questions;
  }

  public Question getFirstQuestion() {
    Question question = null;
    Collection<Question> questions = getQuestions();
    Iterator<Question> it = questions.iterator();
    if (it.hasNext()) {
      question = it.next();
    }
    return question;
  }

  public Collection<Comment> getComments() {
    return this.comments;
  }

  public Collection<QuestionResult> getCurrentUserVotes() {
    return this.votes;
  }

}