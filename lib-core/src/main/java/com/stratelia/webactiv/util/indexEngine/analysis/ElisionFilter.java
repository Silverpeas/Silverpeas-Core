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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.util.indexEngine.analysis;

import java.io.IOException;
import java.util.*;
import org.apache.lucene.analysis.*;

public class ElisionFilter extends TokenFilter {
  private Set articles;
  private static String apostrophes = "'\u2019";

  public void setArticles(Set articles) {
    this.articles = new HashSet();
    for (Iterator iter = articles.iterator(); iter.hasNext(); this.articles
        .add(((String) iter.next()).toLowerCase())) {
    }
  }

  public ElisionFilter(TokenStream input) {
    super(input);
    articles = null;
    articles = new HashSet(Arrays.asList(new String[] { "l", "m", "t", "qu",
        "n", "s", "j", "d", "c" }));
  }

  public ElisionFilter(TokenStream input, Set articles) {
    super(input);
    if (articles != null) {
      this.articles = null;
      setArticles(articles);
    }
  }

  public ElisionFilter(TokenStream input, String articles[]) {
    super(input);
    if (articles != null) {
      this.articles = null;
      setArticles(new HashSet(Arrays.asList(articles)));
    }
  }

  public Token next() throws IOException {
    Token t = input.next();
    if (t == null) {
      return null;
    }
    String text = t.termText();
    int minPoz = -1;
    for (int i = 0; i < apostrophes.length(); i++) {
      int poz = text.indexOf(apostrophes.charAt(i));
      if (poz != -1) {
        minPoz = minPoz != -1 ? Math.min(poz, minPoz) : poz;
      }
    }

    if (minPoz != -1
        && articles.contains(text.substring(0, minPoz).toLowerCase())) {
      text = text.substring(minPoz + 1);
    }
    return new Token(text, t.startOffset(), t.endOffset(), t.type());
  }

}
