package spak;
import java.util.Vector;
import org.apache.xmlrpc.*;

/**
 * <p>Title: SPAK</p>
 * <p>Description: Software Platform for Agent and Knowledge Management</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: National Institute of Informatics</p>
 * @author not attributable
 * @version 1.0
 */

public class RPCServer {
  private static int port = 9091;
  private static final String HANDLER_NAME = "SPAK";
  //private XmlRpcServer server = null;
  //String server_url = "http://localhost:9901/RPC2";
  private WebServer webServer;
  NetworkMonitor nm = null;

  public RPCServer(NetworkMonitor _nm, int _port) {
    nm = _nm;
    port = _port;

    // WebServer (contains its own XmlRpcServer instance)
    try {
      webServer = new WebServer(port);
      webServer.addHandler(HANDLER_NAME, new SPAKHandler());
      webServer.start();
    } catch (Exception e) {
      System.err.println("Exception from WebServer class: "+e);
    }
  }

  protected class SPAKHandler {
    public String setMessage(String msg) {
      String result = nm.processCmd(msg);
      nm.processCmd("induce");
      //System.err.println("processCmd returns: "+result);
      return result;
    }
  }
}