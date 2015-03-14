// my IP
var myip = "136.187.128.121";

myprint("Initialization..\n")

//rpcserver = new RPC("http://136.187.128.164:8080/RPC2");
//roboserver = new TCPConnection("136.187.128.164", 8021);

mouthserver = 0;
//mouthserver = new RPC("http://136.187.128.164:8080/RPC2"); // RobovieIP
//mouthserver = new RPC("http://136.187.88.96:8088/RPC2"); // WebLS
if (mouthserver != 0) {
	mouthserver.execute("sayText","Hello, my name is Robovie, I am waking up");
}

roboposeserver = new RPC("http://136.187.128.164:8089/RPC2"); // Robovie IP
//roboposeserver = new RPC("http://136.187.88.96:8089/RPC2"); // WebLS

roboneckserver = new RPC("http://136.187.128.164:8084/RPC2"); // Robovie IP
//roboneckserver = new RPC("http://136.187.88.96:8084/RPC2"); // WebLS

facerecognizer = 0;
//facerecognizer = new RPC("http://136.187.128.164:8091/RPC2"); // Robovie IP
//facerecognizer = new RPC("http://136.187.88.96:8091/RPC2"); // webls 

speechparser = new RPC("http://136.187.128.153:8087/RPC2");
//speechparser = new RPC("http://136.187.88.96:8087/RPC2");

speechrecognizer=0;
//speechrecognizer = new RPC("http://136.187.128.138:8080/");
if (speechrecognizer != 0) {
	speechrecognizer.execute("setSPAKIP", myip);
}
