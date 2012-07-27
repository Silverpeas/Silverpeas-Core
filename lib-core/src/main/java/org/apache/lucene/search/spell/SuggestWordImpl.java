/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package org.apache.lucene.search.spell;

/**
 * SuggestWordImpl, used in suggestSimilar method in SpellChecker class.
 */
final class SuggestWordImpl {

  /**
   * the score of the word
   */
  public float score;

  /**
   * The freq of the word
   */
  public int freq;

  /**
   * the suggested word
   */
  public String string;

  public final int compareTo(SuggestWordImpl a) {
    // first criteria: the edit distance
    if (score > a.score) {
      return 1;
    }
    if (score < a.score) {
      return -1;
    }

    // second criteria (if first criteria is equal): the popularity
    if (freq > a.freq) {
      return 1;
    }

    if (freq < a.freq) {
      return -1;
    }
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((string == null) ? 0 : string.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SuggestWordImpl other = (SuggestWordImpl) obj;
    if (string == null) {
      if (other.string != null)
        return false;
    } else if (!string.equals(other.string))
      return false;
    return true;
  }
}
