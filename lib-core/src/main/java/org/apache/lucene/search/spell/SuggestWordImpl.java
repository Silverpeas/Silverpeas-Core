/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
