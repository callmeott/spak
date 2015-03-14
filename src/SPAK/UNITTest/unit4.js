importPackage(Packages.javax.swing);
importPackage(Packages.java.awt);
importPackage(Packages.java.awt.event);

var frame = new JFrame("SPAK UnitTest Console");
var textarea = new JTextArea();
var scroll = new JScrollPane(textarea);

function find(fname) {
  return Root.findFrame(fname).getKFrameScript();
}

function findKFrame(fname) {
  return Root.findFrame(fname);
}

function myprint(str) {
  textarea.append(str);
  frame.repaint();
}

function max(a,b) {
  return (a>b)?a:b;
}

function abs(a,b) {
  return (a>0)?a:-a;
}

function calcLength(ax1,ay1,ax2,ay2) {
  return Math.sqrt(Math.pow(ax1-ax2,2)+Math.pow(ay1-ay2,2));
}

function lineLength(A) {
  var ax1 = A["line_X1"];
  var ax2 = A["line_X2"];
  var ay1 = A["line_Y1"];
  var ay2 = A["line_Y2"];
  return calcLength(ax1,ay1,ax2,ay2);
}

function percentDif(a,b) {
  return abs((a-b)*100/max(a,b));
}

function isParallel(A,B) {
  var ax1 = A["line_X1"];
  var ax2 = A["line_X2"];
  var ay1 = A["line_Y1"];
  var ay2 = A["line_Y2"];
  var bx1 = B["line_X1"];
  var bx2 = B["line_X2"];
  var by1 = B["line_Y1"];
  var by2 = B["line_Y2"];

//  myprint("Line1: ("+ax1+","+ay1+")-("+ax2+","+ay2+")\n");
//  myprint("Line2: ("+bx1+","+by1+")-("+bx2+","+by2+")\n");

  var dxa = ax1-ax2;
  var dya = ay1-ay2;
  if(dya<0) { dya=-dya; dxa=-dxa; }
  var anga = Math.atan2(dya,dxa);
    
  var dxb = bx1-bx2;
  var dyb = by1-by2;
  if(dyb<0) { dyb=-dyb; dxb=-dxb; }
  var angb = Math.atan2(dyb,dxb);

  var dif = abs(anga-angb);
  if(dif > 1.5708)
    dif=3.1416-dif;
    
  myprint("Dif: "+dif+"\n");
  return dif<0.1;
}

// pl = RemoteIO
// msg = Message
function sendmsg0(pl,msg) {
  var host = pl["Host"];
  var port = pl["Port"];
  myprint("Send to "+host+":"+port+"\n");
  myprint(": "+msg+"\n");

  var conn = new TCPConnection(host, port);
  if(conn.connect()) {
    conn.send(msg);
    conn.close();
  }
  else 
    myprint("Connection failed\n");
}

function sendmsg(pl,msg) {
  myprint("Say: "+msg+"\n");
  rpc("sayText", msg);
}

function sendaction(actname) {
  myprint("Act: "+actname+"\n");
  roboserver.send(actname+"\n");
}

rs = new RPC("http://136.187.128.164:8080/RPC2");
roboserver = new TCPConnection("136.187.128.164", 8021);

function rpc(cmd, arg) {
  rs.execute(cmd,arg);
}


//Create a new Known User
function createNewUser(newname) {
  var usernode = findKFrame("KnownUser");
  var newnode  = new KFrame(newname,null);
  newnode.addSlot("PName",newname,Slot.TYPE_STR,Slot.COND_EQ,newname,true,true);
  usernode.add(newnode);
  newnode.createInstance();
  usernode.redrawPanel();
  myprint("New user: "+newname+"\n");
}

//Create the top-level container and add contents to it.

frame.getContentPane().add(scroll, BorderLayout.CENTER);

frame.setSize(new Dimension(340, 280));
// frame.pack();
frame.setVisible(true);

myprint("SPAK Demo\n");
// myprint("Connecting to server...\n");

/*
if(roboserver.connect()) {
  myprint("Connection OK\n");
  var x = Root.findFrame("Mouth");
  x.createInstance();
  var y = Root.findFrame("Motor");
  y.createInstance();
}
else
  myprint("Connection Failed\n");
*/

myprint("Ready\n");
