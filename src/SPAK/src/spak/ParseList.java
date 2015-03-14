package spak;
import java.util.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class ParseList {
  Vector member = new Vector();

  private boolean isWhite(char x) {
    return x==32 || x==10 || x==13;
  }

  private int putOne(char ca[], int i) {
    int j;
    while(isWhite(ca[i])) i++;
    if(ca[i]==')')
      return -1;
    else if(ca[i]=='"') {
      j=i+1;
      while(ca[j]!='"') j++;
      member.add(new String(ca,i+1,j-i-1));
      j++;
    }
    else if(ca[i]=='(') {
      j=i+1;
      int opencount=0;
      while(ca[j]!=')' || (opencount>0)) {
        if(ca[j]=='(') opencount++;
        if(ca[j]==')') opencount--;
        j++;
      }
      j++;
      member.add(new String(ca,i,j-i));
    }
    else {
      j=i+1;
      while(!(isWhite(ca[j]) || ca[j]==')')) {
        j++;
        if(j>=ca.length)
          break;
      }
      member.add(new String(ca,i,j-i));
    }
    return j;
  }

  private void putList(char ca[], int i) {
    int j;

    while((j=putOne(ca,i))>=0)
      i=j;
  }

  public ParseList(String in) {
    char ca [] = in.toCharArray();
    int i=0;
    while(isWhite(ca[i])) i++;
    if(ca[i]=='(')
      putList(ca, i+1);
    else
      putOne(ca,i);
  }

  public int size() {
    return member.size();
  }

  public String elementAt(int i) {
    return (String)member.elementAt(i);
  }
}