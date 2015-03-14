package spak;

import java.util.*;

/**
 * <p>Title: SPAK</p>
 * <p>Description: Software Platform for Agent and Knowledge Management</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: National Institute of Informatics</p>
 * @author not attributable
 * @version 1.0
 */

public class Evaluator extends Thread {
  Timer timer;
  static KFrame root;
  static WorkingMemory wm;
  long period;

  Evaluator(KFrame rn, WorkingMemory _wm) {
    root=rn;
    wm = _wm;
    period = 3000;
  }

  public void setPeriod(int _period) {
    period = _period;
  }

  public void doEvaluate() {
    Date d = new Date();
    System.out.println("*** Evaluator started at time:"+d.getTime());

    // Run onEvaluate
    Vector instances = wm.getInstances();
    System.out.println("Checking onEvaluate of total "+instances.size()+ " instances");
    for ( int i = 0; i < instances.size(); i++ ) {
      KFrame fr = (KFrame) (instances.elementAt(i));
      System.out.println("Examining " + fr.getSlotValue("Name"));
      fr.runSpecialSlot("onEvaluate");
    }

    // reinduce
    ForwardChaining ic = new ForwardChaining(wm);
    ic.setZeroConsole(wm.getZeroConsole());
    System.out.println("Now inducing instances...");
    int idcount=0;
    while(ic.induce()>0) {  // Repeat til we get no more instances
      System.out.println("induce() returns more than 0, do it again");
      idcount++;
      if(idcount>=50) {
        System.err.println("Induce: infinite instances possible.");
        break;
      }
    }

    System.out.println("Now reInducing instances...");
    idcount=0;
    while(ic.reInduce()>0) {  // Repeat til we get no more instances
      System.out.println("reInduce() returns more than 0, do it again");
      idcount++;
      if(idcount>=50) {
        System.err.println("ReInduce: infinite instances possible.");
        break;
      }
    }


  }

  class EvaluateTask extends TimerTask {
    public void run() {
      doEvaluate();
    }
  }

  public void run() {
    threadMain();
  }

  public void stopRun() {
    if ( timer != null) timer.cancel();
  }
  void threadMain() {
    System.out.println("Evaluator thread started...");
    timer = new Timer();
    timer.schedule(new EvaluateTask(), period, period);
  }
}