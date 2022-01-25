package org.silverpeas.core.questioncontainer.result.model;

import org.silverpeas.core.admin.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Results {

  private Map<String, List<QuestionResult>> questionsResults = new HashMap<>();
  private Map<String, User> users = new HashMap<>();

  public void addQuestionResult(QuestionResult qr) {
    String key = qr.getQuestionPK().getId()+"-"+qr.getUserId()+"-"+qr.getParticipationId();
    List<QuestionResult> results = questionsResults.computeIfAbsent(key, k -> new ArrayList<>());
    results.add(qr);
    String userId = qr.getUserId();
    users.computeIfAbsent(userId, User::getById);
  }

  public List<QuestionResult> getQuestionResultByQuestion(String questionId, String userId,
      int participationId) {
    String key = questionId+"-"+userId+"-"+participationId;
    return questionsResults.get(key);
  }

  public User getUser(String userId) {
    return users.get(userId);
  }
}
