package spak;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class TCPConnection {
  String host;
  int port;
  Socket nc=null;
  DataOutputStream out=null;
  InputStream in=null;
  Vector stringBuffer = new Vector();
  boolean reading = false;

  public TCPConnection(String h, int p) {
    host=h;
    port=p;
  }

  public boolean connect()
  {
    try {
      nc = new Socket(host,port);
      out = new DataOutputStream(nc.getOutputStream());
      in = nc.getInputStream();
      System.out.println("TCPConnection: connected to "+host+"/"+port);
      stringBuffer.removeAllElements();
      Thread readT = new Thread() {
        public void run() {
          readThread();
        }
      };
      reading = true;
      readT.start();
      return true;
    }
    catch (Exception e) {
      System.err.println("TCPConnection: connect exception: "+e);
    }
    return false;
  }

  public void close() {
    try {
      in.close();
    }
    catch (IOException ex) {
    }
    try {
      out.close();
    }
    catch (IOException ex) {
    }
    try {
      nc.close();
    }
    catch (IOException ex) {
    }
  }
  public void send(String msg) {
    try {
      out.writeBytes(msg);
      out.flush();
    }
    catch(Exception e) {
      System.err.println("TCPConnection:send exception: "+e);
    }
  }

  public void send(byte [] ba) {
    try {
      out.write(ba);
      out.flush();
    }
    catch(Exception e) {
      System.err.println("TCPConnection:send exception: "+e);
    }
  }

  public synchronized String getline() {
    try {
      while(stringBuffer.isEmpty())
        if(reading)
          this.wait();
        else
          return "# Connection Terminated\n";
    }
    catch(Exception e) {}

    String ret = (String)stringBuffer.elementAt(0);
    stringBuffer.removeElementAt(0);
    return ret;
  }

  public synchronized void putline(String s) {
    stringBuffer.add(s);
    notifyAll();
  }

  public void readThread() {
    byte buff[] = new byte[64];

    System.out.println("Read Thread Started...");
    int count;
    int ch = 0;
    try {
      while(true) {
        count=0;
        while(true) {
          ch = in.read();
          if(ch==10)
            break;
          else if(ch!=-1) {
            if(count>=buff.length) {
              byte newbuff[] = new byte[buff.length*2];
              System.arraycopy(buff,0,newbuff,0,count);
              buff = newbuff;
              System.out.println("Buffer size extended to: "+buff.length);
            }
            buff[count++]=(byte)ch;
          }
          else
            break;
        }
        if(ch==-1)
          break;
        if(count>0) {
          String nl = new String(buff,0,count);
          putline(nl);
          // System.out.println("Got "+nl);
        }
      }
    }
    catch (Exception e) {
      System.out.println("TCPConnection readThread: " + e);
    }
    System.out.println("Read Thread Terminated...");
    reading = false;
    putline("# Connection Terminated\n");
  }
}
