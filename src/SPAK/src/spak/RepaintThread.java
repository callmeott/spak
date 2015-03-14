package spak;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class RepaintThread extends Thread {
  private KnowledgePanel kpanel;
  public RepaintThread(KnowledgePanel kp) {
    kpanel=kp;
  }

  public void run() {
    while(true) {
      doWait();
      kpanel.repaint();
    }
  }

  private synchronized void doWait() {
    try {
      wait();   // Sleep until repaint request arrives
    }
    catch(Exception ex){
      System.out.println("doRepaint WaitEx: "+ex);
    }
  }

  public synchronized void doRepaint() {
    try {
      notifyAll();
    }
    catch(Exception ex) {
      System.out.println("doRepaint Ex: "+ex);
    }
  }
}