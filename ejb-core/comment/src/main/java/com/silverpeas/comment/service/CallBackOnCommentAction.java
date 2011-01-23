/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.comment.service;

import com.silverpeas.comment.model.Comment;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import static com.silverpeas.util.StringUtil.*;

/**
 * An abstract class that defines the contract a callback interested about actions on the comment
 * behalf should satisfy.
 * This class defines the information a callback recieves at invocation.
 */
public abstract class CallBackOnCommentAction implements CallBack {

  /**
   * Subscribes to be notified about the comment adding.
   */
  public void subscribeForCommentAdding() {
    CallBackManager.get().subscribeAction(CallBackManager.ACTION_COMMENT_ADD, this);
  }

  /**
   * Subscribes to be notified about the comment removing.
   */
  public void subscribeForCommentRemoving() {
    CallBackManager.get().subscribeAction(CallBackManager.ACTION_COMMENT_REMOVE, this);
  }

  /**
   * Unsubscribes to be not any more notified about the comment adding.
   */
  public void unsubscribeForCommentAdding() {
    CallBackManager.get().unsubscribeAction(CallBackManager.ACTION_COMMENT_ADD, this);
  }

  /**
   * Subscribes to be not any more notified about the comment removing.
   */
  public void unsubscribeForCommentRemoving() {
    CallBackManager.get().unsubscribeAction(CallBackManager.ACTION_COMMENT_REMOVE, this);
  }

  @Override
  public abstract void subscribe();

  @Override
  public final void doInvoke(int action, int iParam, String sParam, Object extraParam) {
    if (!isDefined(sParam) || extraParam == null) {
      String message = "The component name or the comment is'nt defined";
      SilverTrace.error("comment", getClass().getSimpleName() + ".doInvoke()",
          "comment.UNKNOWN_ACTION", message);
    } else {
      if (action == CallBackManager.ACTION_COMMENT_ADD) {
        Comment addedComment = (Comment) extraParam;
        commentAdded(iParam, sParam, addedComment);
      } else if (action == CallBackManager.ACTION_COMMENT_REMOVE) {
        Comment removedComment = (Comment) extraParam;
        commentRemoved(iParam, sParam, removedComment);
      } else {
        SilverTrace.warn("comment", getClass().getSimpleName() + ".doInvoke()",
            "comment.UNKNOWN_ACTION");
      }
    }
  }

  /**
   * A comment, written by the specified author, is added to the specified publication.
   * The implementer implements this method to perform a computation when a comment is added to
   * a publication.
   * @param publicationId the unique identifier of the commented publication.
   * @param componentInstanceId the unique identifier of the component instance in which the
   * publication and the comment live.
   * @param addedComment the comment that is added.
   */
  public abstract void commentAdded(int publicationId, final String componentInstanceId,
      final Comment addedComment);

  /**
   * A comment, that was written by the specified author, is removed from the specified publication.
   * The implementer implements this method to perform a computation when a comment is removed from
   * a publication.
   * @param publicationId the unique identifier of the commented publication.
   * @param componentInstanceId the unique identifier of the component instance in which the
   * publication lives.
   * @param removedComment the comment that is removed.
   */
  public abstract void commentRemoved(int publicationId, final String componentInstanceId,
      final Comment removedComment);
}
