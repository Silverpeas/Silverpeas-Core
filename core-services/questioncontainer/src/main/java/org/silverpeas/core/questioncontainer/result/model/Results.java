package org.silverpeas.core.questioncontainer.result.model;

import org.silverpeas.core.admin.user.model.User;

import java.util.HashMap;
import java.util.Map;

public class Results {

  private Map<String, QuestionResult> questionsResults = new HashMap<>();
  private Map<String, User> users = new HashMap<>();

  public void addQuestionResult(QuestionResult qr) {
    String key = qr.getQuestionPK().getId()+"-"+qr.getUserId()+"-"+qr.getParticipationId();
    questionsResults.put(key, qr);
    String userId = qr.getUserId();
    users.computeIfAbsent(userId, User::getById);
  }

  public QuestionResult getQuestionResultByQuestion(String questionId, String userId,
      int participationId) {
    String key = questionId+"-"+userId+"-"+participationId;
    return questionsResults.get(key);
  }

  public User getUser(String userId) {
    return users.get(userId);
  }
}
