importPackage(Packages.javax.swing);
importPackage(Packages.java.awt);
importPackage(Packages.java.awt.event);

var frame = new JFrame("SPAK Demo Console");
var textarea = new JTextArea();
var scroll = new JScrollPane(textarea);

function myprint(str) {
  textarea.append(str);
  frame.repaint();
}

//Create the top-level container and add contents to it.
frame.getContentPane().add(scroll, BorderLayout.CENTER);
frame.setSize(new Dimension(340, 280));
//frame.pack();
frame.setVisible(true);


function delay(gap){ /* gap is in millisecs */
  var then,now; then=new Date().getTime();
  now=then;
  while((now-then)<gap)
    {now=new Date().getTime();}
}

function wait(delay,string){
	setTimeout(string,delay);
}

function deleteNode(framename) {
  var currentnode = Root.findFrame(framename);
  currentnode.redrawPanel();
  myprint("deleting frame: "+framename+"\n");
  currentnode.selfDelete();
}

function createNode(framename) {
  var node = Root.findFrame(framename);
  if ( node == null ) return null;
  //redraw panel but don't run onInstantiate
  var inst =  node.createInstance(false, true); 
  inst.updateHash();
  return inst;
}

function canDie(framename) {
  var currentnode = Root.findFrame(framename);
  currentnode.redrawPanel();
  myprint("set frame to disappear in the next eval: "+framename+"\n");
  currentnode.setSlotValue("onEvaluate", "deleteNode(s.Name)");
}

function submitToSPAK ( _msg ) {
  // send the text to port 9900
  //myprint("Submit to SPAK:\nBEGIN\n");
  //var msg = "reset\n"+ _msg + "\ninduce\n";
  var msg = _msg;
  //myprint(msg+"\nEND\n");

  var conn = new TCPConnection("127.0.0.1", 9900);
  if(conn.connect()) {
    conn.send(msg);
    conn.close();
  }
  else
    myprint("Connection failed\n")
}

// for LSE
function updateLSE(s) {
	s.lse.e10 = s.lse.e9 ;
	s.lse.e9 = s.lse.e8 ;
	s.lse.e8 = s.lse.e7 ;
	s.lse.e7 = s.lse.e6 ;
	s.lse.e6 = s.lse.e5 ;
	s.lse.e5 = s.lse.e4 ;
	s.lse.e4 = s.lse.e3 ;
	s.lse.e3 = s.lse.e2 ;
	s.lse.e2 = s.lse.e1; 
	s.lse.e1 = s.newevent.Name;
}

// for LSA
function updateLSA(s) {
	s.lsa.a10 = s.lsa.a9 ;
	s.lsa.a9 = s.lsa.a8 ;
	s.lsa.a8 = s.lsa.a7 ;
	s.lsa.a7 = s.lsa.a6 ;
	s.lsa.a6 = s.lsa.a5 ;
	s.lsa.a5 = s.lsa.a4 ;
	s.lsa.a4 = s.lsa.a3 ;
	s.lsa.a3 = s.lsa.a2 ;
	s.lsa.a2 = s.lsa.a1; 
	s.lsa.a1 = s.newaction.Name;
}

function calculateSignificanceLSE(s) {
	//myprint("Updating LSE Significance values...\n");

	var agerange=60;
	
	var fe1 = Root.findFrame(s.e1);
	if ( fe1 ) {
		var age=parseInt(fe1.getAge());
		if ( age < agerange ) {
			var ss1 = 100/age;
			s.s1 = ss1.toString();
		} else {
			s.s1 = "0";
		}
	} else {
		// frame was deleted
		s.s1 = "0.1";
	}

	var fe2 = Root.findFrame(s.e2);
	if ( fe2 ) {
		var age=parseInt(fe2.getAge());
		if ( age < agerange ) {
			var ss2 = 100/age;
			s.s2 = ss2.toString();
		} else {
			s.s2 = "0.2";
		}
	} else {
		// frame was deleted
		s.s2 = "0.1";
	}

	var fe3 = Root.findFrame(s.e3);
	if ( fe3 ) {
		var age=parseInt(fe3.getAge());
		if ( age < agerange ) {
			var ss3 = 100/age;
			s.s3 = ss3.toString();
		} else {
			s.s3 = "0.2";
		}
	} else {
		// frame was deleted
		s.s3 = "0.1";
	}

	var fe4 = Root.findFrame(s.e4);
	if ( fe4 ) {
		var age=parseInt(fe4.getAge());
		if ( age < agerange ) {
			var ss4 = 100/age;
			s.s4 = ss4.toString();
		} else {
			s.s4 = "0.2";
		}
	} else {
		// frame was deleted
		s.s4 = "0.1";
	}

	var fe5 = Root.findFrame(s.e5);
	if ( fe5 ) {
		var age=parseInt(fe5.getAge());
		if ( age < agerange ) {
			var ss5 = 100/age;
			s.s5 = ss5.toString();
		} else {
			s.s5 = "0.2";
		}
	} else {
		// frame was deleted
		s.s5 = "0.1";
	}

	var fe6 = Root.findFrame(s.e6);
	if ( fe6 ) {
		var age=parseInt(fe6.getAge());
		if ( age < agerange ) {
			var ss6 = 100/age;
			s.s6 = ss6.toString();
		} else {
			s.s6 = "0.2";
		}
	} else {
		// frame was deleted
		s.s6 = "0.1";
	}

	var fe7 = Root.findFrame(s.e7);
	if ( fe7 ) {
		var age=parseInt(fe7.getAge());
		if ( age < agerange ) {
			var ss7 = 100/age;
			s.s7 = ss7.toString();
		} else {
			s.s7 = "0.2";
		}
	} else {
		// frame was deleted
		s.s7 = "0.1";
	}

	var fe8 = Root.findFrame(s.e8);
	if ( fe8 ) {
		var age=parseInt(fe8.getAge());
		if ( age < agerange ) {
			var ss8 = 100/age;
			s.s5 = ss8.toString();
		} else {
			s.s5 = "0.2";
		}
	} else {
		// frame was deleted
		s.s8 = "0.1";
	}

	var fe9 = Root.findFrame(s.e9);
	if ( fe9 ) {
		var age=parseInt(fe9.getAge());
		if ( age < agerange ) {
			var ss9 = 100/age;
			s.s9 = ss9.toString();
		} else {
			s.s9 = "0.2";
		}
	} else {
		// frame was deleted
		s.s9 = "0.1";
	}

	var fe10 = Root.findFrame(s.e10);
	if ( fe10 ) {
		var age=parseInt(fe10.getAge());
		if ( age < agerange ) {
			var ss10 = 100/age;
			s.s10 = ss10.toString();
		} else {
			s.s10 = "0.2";
		}
	} else {
		// frame was deleted
		s.s10 = "0.1";
	}

}

// for LSSE
function calculateSignificanceLSSE(s) {
	myprint("Updating LSSE Significance values...\n");

	var agerange=60;
	
	var fe1 = Root.findFrame(s.e1);
	if ( fe1 ) {
		var age=parseInt(fe1.getAge())/1000;
		if ( age < agerange ) {
			var ss1 = 100/age;
			s.s1 = ss1.toString();
		} else {
			s.s1 = "0";
		}
	}

	var fe2 = Root.findFrame(s.e2);
	if ( fe2 ) {
		var age=parseInt(fe2.getAge())/1000;
		if ( age < agerange ) {
			var ss2 = 100/age;
			s.s2 = ss2.toString();
		} else {
			s.s2 = "0";
		}
	}

	var fe3 = Root.findFrame(s.e3);
	if ( fe3 ) {
		var age=parseInt(fe3.getAge())/1000;
		if ( age < agerange ) {
			var ss3 = 100/age;
			s.s3 = ss3.toString();
		} else {
			s.s3 = "0";
		}
	}
}

var autoinstancecount = 0;

function doCreateFrame(reqframe, condition, action1, actionvalue) {
  myprint("Entering doCreateFrame... \n");
  var autoactionnode = Root.findFrame("LearnedAction");
  var newnode = new KFrame("LearnedAction_"+autoinstancecount, null)
  autoinstancecount = autoinstancecount +1;

  newnode.addSlot("required", null, Slot.TYPE_INSTANCE,Slot.COND_INSTANCEOF,reqframe, true,true, true);
  var tmp = "createBasicAction(\""+action1+"\",\""+actionvalue+"\"); canDie(s.Name)";
  newnode.addSlot("onInstantiate", tmp, Slot.TYPE_STR, Slot.COND_ANY, "", false, false, false);
  newnode.addSlot("condition", condition, Slot.TYPE_STR, Slot.COND_ANY, "", false, false, false);
  autoactionnode.add(newnode);

  //submitToSPAK("reset\ninduce\n");
  Root.resetWM();
  myprint("Exit doCreateFrame: exiting\n");
}

function doLearn(s) {
  // find out the cause of this action by looking at LSE
  var found = false;
  var i = 1;
  var lsev = Root.findInstancesOf("LSE");

  if ( lsev.size() == 0 ) {
  	myprint("doLearn: error LSE instance not found.\n");
	return;
  } 
  	
  var lse = lsev.get(0); // take the first element

  var causeframe;

  while ( ! found && i <= 10 ) {
	var tmpe = lse.getSlotValue("e"+i);
	myprint("doLearn: checking LSE.e"+i+": "+tmpe+"\n");
	if ( tmpe != "" ) {
		var ftmpe = Root.findFrame(tmpe);
		if ( ftmpe && ftmpe.getSlotValue("isCommand") == "false" ) {
			myprint("isCommand is false, good!\n");
			found = true;
			causeframe = ftmpe;
		} else {
			myprint("isCommand is true, search further!\n");
		}
	}
	i++;
  }
  
  if ( found ) { 
  	var causeframetype = causeframe.getParentName();
	var condition = "";
	// THIS IS QUITE HARD-CODED, FIX ME
	var keyfield = causeframe.getSlotValue("keydata");
	condition = "s.required."+ keyfield + " == \""+causeframe.getSlotValue(keyfield)+"\"";
	doCreateFrame(causeframetype, condition, s.action.action, s.action.actionvalue);
	canDie(causeframe.getSlotValue("Name"));
  } else {
	myprint("doLearn: could not find the cause ...\n");
	// HOW DO I DECIDE IF I SHOULD ASK THE USER OR NOT
	//myprint("Sending text to mouthserver agent: Sorry I don't understand. What do you mean?");

  }

  canDie(s.Name);
}

function clearLearnedActions() {
	var all = Root.findInstancesOf("LearnedAction");
	if ( all != null ) {
	  for ( var i = 0 ; i < all.size() ; i++) {
		deleteNode(all.get(i).getSlotValue("Name"));
	  }
	}
}

function createBasicAction(action, value) {
	// E.g. Say, Hello
	submitToSPAK("BasicAction-action="+action+"\nBasicAction-value="+value+"\ninduce\n");
}

function doParse(s) {
	// support for external parser
	if (s.act != "") {
		return;
	}
	// IMITATE a parser
	if ( s.recognized_text == "Hello" || 
	s.recognized_text == "Hi" || 
	s.recognized_text == "Good morning" ||
	s.recognized_text == "Good afternoon" || 
	s.recognized_text == "Good evening" ) {
		s.act = "Greet";
		s.data = s.recognized_text;
	} else if ( s.recognized_text == "Weather tomorrow" || 
		s.recognized_text == "Weather" ) {
		s.act = "Ask";
		s.subact = "AskWeatherForecast";
	} else if ( s.recognized_text == "Is there any students left" ) {
		s.act = "Ask";
		s.subact = "ReportStatus";
	} else if ( s.recognized_text == "Not very well" || s.recognized_text == "Not so fine") {
		s.act = "Tell";
		s.subact = "NotFeelingWell";
	} else if ( s.recognized_text == "Fine" ) {
		s.act = "Tell";
		s.subact = "FeelingFine";
	} else if ( s.recognized_text == "I have a headache" ) {
		s.act = "Tell";
		s.subact = "HumanCondition";
		s.data = "HaveHeadAche";
	} else if ( s.recognized_text == "Yes" ) {
		s.act = "YesNo";
		s.subact = "Yes";
		s.data = s.recognized_text;
	} else if ( s.recognized_text == "No" ) {
		s.act = "YesNo";
		s.subact = "No";
		s.data = s.recognized_text;
	} else if ( s.recognized_text == "Tokyo" || s.recognized_text == "Chiba") {
		s.act = "Tell";
		s.subact = "Place";
		s.data = s.recognized_text;
	} else if ( s.recognized_text.indexOf("My name is ") != -1 ) {
		s.act = "Tell";
		s.subact = "Name";
		s.data = s.recognized_text.substring(11, s.recognized_text.length);
	} else if ( s.recognized_text.indexOf("Bye") != -1 || s.recognized_text.indexOf("by") != -1 ) {
		s.act = "Bye";
		s.data = s.recognized_text;
	} else if ( s.recognized_text.search(/\d/) != -1) { // search for number for now
		s.act = "Tell";
		s.subact = "Time";
		s.data = s.recognized_text;
	} else if ( s.recognized_text == "Afternoon is better" ) { 
		s.act = "Tell";
		s.subact = "Time";
		s.data = "Afternoon";
	} else {
		s.act = "unknown";
		s.subact = s.recognized_text;
	}
}

function calcLength(ax1,ay1,ax2,ay2) {
  return Math.sqrt(Math.pow(ax1-ax2,2)+Math.pow(ay1-ay2,2));
}

function lineLength(A) {
  var ax1 = A["X1"];
  var ax2 = A["X2"];
  var ay1 = A["Y1"];
  var ay2 = A["Y2"];
  return calcLength(ax1,ay1,ax2,ay2);
}

function existInstance(framename, slotname, slotvalue) {
	// check if there exists a frame with  provided information
	var frames = Root.findInstancesOf(framename);

  	if ( frames.size() == 0 ) {
		return false;
  	} 
  	
	for (i=0; i < frames.size();i++) {
		var tmpi = frames.get(i);
		if ( tmpi.getSlotValue(slotname) == slotvalue ) {
			//return tmpi.getName();
			return tmpi;
		}
	}
	return false;
}

function showKVector(v) {
	var output = "[";
	for (i=0; i < v.size();i++) {
    if ( i != 0 ) {
			output = output + ", ";
    }
		output = output + v.get(i).getName();
  }
  output = output + "]";
 	return output;
}

clearLearnedActions();
var lse = Root.findFrame("LSE");
if (lse) submitToSPAK("LSE-startlse=true\nLSA-startlsa=true\ninduce\n");

