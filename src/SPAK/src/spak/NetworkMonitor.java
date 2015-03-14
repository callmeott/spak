package spak;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class NetworkMonitor extends ForwardChaining {
  int port;
  Thread listenThread=null;
  Thread xmlrpcListenThread = null;
  Hashtable threadSocket = new Hashtable();
  RPCServer rpcserver = null;
  ScriptEngine se = null;
  JScriptShell jss = null;

  public NetworkMonitor(WorkingMemory wm) {
    super(wm);
  }

  public void start(int portnum) {
    port = portnum;
    listenThread = new Thread() {
      public void run() {
        listen();
      }
    };
    xmlrpcListenThread = new Thread() {
      public void run() {
        xmlrpcListen();
      }
    };

    // JavaScript stuff
    se = wm.getScriptEngine();
    jss = new JScriptShell(se);
    se.setShell(jss);

    // out("Thread Created");
    listenThread.start();
    xmlrpcListenThread.start();
  }

  public void listen() {
    // out("Thread Started");
    ServerSocket  agent_socket = null;
    Socket inbound = null;
    Thread  agent_thread;
    out("TCP Server at: "+port);

    try {
      agent_socket = new ServerSocket(port);
      while(true) {
        inbound = agent_socket.accept();
        out("Connection: "+
                           inbound.getInetAddress().toString());
        agent_thread = new Thread() {
          public void run() {
            process();
          }
        };
        threadSocket.put(agent_thread, inbound);
        agent_thread.start();
      }
    }
    catch (IOException e) {
      out("Got Exception: "+e);
    }
  }

  public void xmlrpcListen() {
    int port = 9901;
    out("XML-RPC server at: "+port);
    rpcserver = new RPCServer(this, port);

  }
  synchronized public void process() {
    Socket in = (Socket)threadSocket.get(Thread.currentThread());
    out("Thread started for: "+in.getInetAddress().toString());
    byte [] buff = new byte[100000];
    int size;
    try {
      DataInputStream istream = new DataInputStream(in.getInputStream());
      DataOutputStream ostream = new DataOutputStream(in.getOutputStream());

      while(true) {
        if((size=getdata(istream,buff))>0) {
          String cmd = new String(buff, 0, size);
          // sendMessage(ostream, "Got: "+cmd);
          String result = processCmd(cmd);
          if (! result.equals("") ) {
            sendMessage(ostream, result);
          }
        }
        else
          break;
      }
    }
    catch(IOException e) { System.err.println("Process: "+e);}

    try {
      in.close();
    }
    catch(Exception e) {}
    out("Thread terminated for: "+in.getInetAddress().toString());
  }

  void sendMessage(DataOutputStream ostream, String str)
  {
    try {
        ostream.writeBytes(str);
        ostream.flush();
    }
    catch (Exception e) {
    }
  }

  private int getdata(DataInputStream istream, byte buff[]) throws IOException {
    int count;
    int ch;
    count=0;
    while(true) {
      ch = istream.read();
      if(ch==13) {}
      else if(ch==10) break;
      else if(ch!=-1)
          buff[count++]=(byte)ch;
      else
        break;
    }
    return count;
  }

    public String processCmd(String cmd) {
    String result = new String();
    out("Got: "+cmd);
    if(cmd.startsWith("help")) {
      result =  "--HELP--\n" +
          "help: This message\n" +
          "frames: List all frame names and slots\n" +
          "Frame-Slot=value: Enter a slot value\n" +
          "Instancename-Slot=value: Modify a slot value of an instance\n" +
          "list: Show current slot buffer\n" +
          "induce: Try to instantiate frame from given slot value(s)\n"+
          "reinduce: Try to reinduce the existing instance(s)\n"+
          "remove: Try to remove instance with given slot value(s)\n"+
          "show: Show instances with given slot value(s)\n"+
          "removeall [framename]: Remove all instances of type Framename\n"+
          "reset: Reset the induction engine\n"+
          "$ xxx: Run JavaScript code xxx, e.g., $ i=1;i\n"+
          "# xxx: Comment Line\n"+
          "--ENDHELP--\n";
    }
    else  if(cmd.startsWith("frames")) {
      result = dumpFrameSlot();
    }
    else if(cmd.startsWith("list")) {
      result = wm.printMySlots();
    }
    else if(cmd.startsWith("removeall ")) {
      wm.lock();
      removeInstancesOfFrame(cmd.substring(10));
      wm.getRootFrame().redrawPanel();
      wm.unlock();
    }
    else if(cmd.startsWith("remove")) {
      wm.lock();
      removeInstances();
      wm.getRootFrame().redrawPanel();
      wm.unlock();
    }
    else if(cmd.startsWith("show")) {
      result = dumpInstances();
    }
    else if(cmd.startsWith("reset")) {
      // Hard reset: Also clear WorkingMemory, and used Slot Hash
      // reset(root);
      // Soft Reset: Clear only Frame Hierarchy
      wm.lock();
      wm.rereadRoot();
      wm.unlock();
    }
    else if(cmd.startsWith("induce") || cmd.startsWith("add")) {
      int idcount=0;
      while(induce()>0) {  // Repeat til we get no more instances
        idcount++;
        if(idcount>=50) {
          out("Induce: infinite instances possible.");
          break;
        }
      }
      // make sure all conditions are valid
      idcount=0;
      while(reInduce()>0) {  // Repeat til we get no more instances
        idcount++;
        if(idcount>=50) {
          out("ReInduce: infinite instances possible.");
          break;
        }
      }
      wm.getRootFrame().redrawPanel();
    }
    else if(cmd.startsWith("reinduce") ) {
      reInduce();
      wm.getRootFrame().redrawPanel();
    }
    else if(cmd.startsWith("#")) { // Comment
      out(cmd);
    }
    else if(cmd.startsWith("$")) { // JavaScript
      wm.lock();
      cmd = cmd.substring(1);
      se.putCommand(cmd);
      se.waitDone();
      String ret = se.getResult();
      String errstr = se.getErrorString();
      if (errstr != "") {
        ret += errstr;
      }
      wm.unlock();
      return ret+"\n";
    } else {
      wm.lock();
      int eqsign = cmd.indexOf("=");
      if(eqsign>=0) {
        String name = cmd.substring(0, eqsign);
        String value = cmd.substring(eqsign+1);

        // check first if it is an instance
        int minussign = name.indexOf("-");
        boolean isInstance = false;
        String framename = new String();
        String slotname = new String();
        if ( minussign >=0 ) {
          framename = name.substring(0, minussign);
          slotname = name.substring(minussign+1);
          if ( wm.getRootFrame().findFrame(framename) != null &&
                    wm.getRootFrame().findFrame(framename).isInstance()) {
            isInstance = true;
          }
        }
        if ( isInstance ) {
          try {
            wm.getRootFrame().findFrame(framename).setSlotValue(slotname, value);
            out("Modified ("+name+", "+value+")");
          } catch( Exception e) {
            result = "Exception occurred when setting new slot value, reinduce...\n";
            reInduce();
            wm.getRootFrame().redrawPanel();
          }
        } else {
          addSlotValue(name, value);
          out("Added ("+name+","+value+")");
        }

      }
      wm.unlock();
    }
    return result;
  }
}