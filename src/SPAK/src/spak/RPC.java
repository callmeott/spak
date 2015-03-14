package spak;
import java.util.Vector;
import org.apache.xmlrpc.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class RPC {
  String server_url = null;  // Ex: "http://localhost:8080/RPC2"
  XmlRpcClient server = null;

  public RPC(String url) {
    server_url = url;
    try {
      // Create an object to represent our server.
      server = new XmlRpcClient(server_url);
    }
    catch (Exception exception) {
        System.err.println("JavaClient: " + exception.toString());
    }
  }

  public void execute(String command, String param) {
    Vector params = new Vector();
    params.addElement(param);
    this.execute(command, params);
  }

  public void execute(String command, Vector params) {
    try {
      server.execute(command, params);
    }
    catch(Exception ex) {
      System.err.println("XML Execute: "+ex);
    }
  }
}