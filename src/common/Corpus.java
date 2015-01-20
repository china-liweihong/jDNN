package common;

import java.util.List;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;

import common.Sentence;
import common.Dictionary;
import data.Channel;

public class Corpus {
  List<Sentence> sents;

  int corpusLength;

  public Corpus() {
    this.sents = new ArrayList<Sentence>();
    this.corpusLength = 0;
  }
  public void addSent(Sentence s) {
    s.setId(corpusLength);
    this.sents.add(s);
    this.corpusLength++;
  }

  public Sentence get(int i) {return this.sents.get(i);}

  public List<Sentence> getSentences() {
    return this.sents;
  }

  public int getSize() {
    return this.corpusLength;
  }

  /** this method will fill corpus, sentence and dict in one go
   * */
  public void load(String file, Channel ch, Dictionary dict) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
    String line = "";
    int id = 0;
    while((line = br.readLine())!= null) {
      Sentence s = new Sentence();
      s.setId(id);
      String text = line;
      text = ch.getTokeniser().parse(text);
      text = ch.getTokeniser().clean(text);
      String[] tokens = text.split("_");
      for(String tok: tokens) {
        dict.addWord(tok);
        s.addWord(dict.getId(tok));
      }
      this.addSent(s);
      id++;
    }
    br.close();
  }
}
