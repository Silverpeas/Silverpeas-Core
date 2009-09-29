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
