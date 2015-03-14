load("UNITTest/agents.js");

function processQuery(type, target, framename) {
  myprint("Entering processQuery with type: "+type+", target: "+target+"\n");
  if ( type == "whatis" ) {
    // find out what it is
    //var is_a_node = Root.findFrame("IS_A");
    //var has_a_node = Root.findFrame("HAS_A");
    // grab all children
    var is_a_child = Root.findInstancesOf("IS_A");
    var has_a_child = Root.findInstancesOf("HAS_A");
    // loop finding the match
    myprint("is_a_child.size is: "+is_a_child.size()+"\n");
    var matchfound = 0;
    if ( is_a_child.size() != 0 || has_a_child.size() != 0 ) {
      for ( var i = 0 ; i < is_a_child.size() ;i++ ) {
	var tmp = is_a_child.get(i);
	if ( tmp.getSlotValue("Object1") == target || tmp.getSlotValue("Object2") == target ) {
	  matchfound++;
	  myprint("Found IS_A match in frame: "+tmp.getSlotValue("Name")+"\n");
	  var text = tmp.getSlotValue("Object1")+" is a "+tmp.getSlotValue("Object2");
	  if (matchfound != 1) {
	    text = ", and "+text;
	  }
	  myprint("Send text to MouthOttBot2 agent:"+text);
	  mouthserver.execute("sayText", text);
	}
      }
      for ( var i = 0 ; i < has_a_child.size() ;i++ ) {
	var tmp = has_a_child.get(i);
	if ( tmp.getSlotValue("Object1") == target || tmp.getSlotValue("Object2") == target ) {
	  matchfound++;
	  myprint("Found HAS_A match in frame: "+tmp.getSlotValue("Name")+"\n");
	  var text = tmp.getSlotValue("Object1")+" has a "+tmp.getSlotValue("Object2");
	  if (matchfound != 1) {
	    text = ", and "+text;
	  }
	  myprint("Send text to MouthOttBot2 agent:"+text);
	  mouthserver.execute("sayText", text);
	}
      }
    } 
    if ( matchfound == 0 ) {
      var text = "I'm sorry, I have no idea about "+target+" at the moment";
      myprint("Send text to MouthOttBot2 agent:"+text);
      mouthserver.execute("sayText", text);
    }
  } else {
    var text = "Sorry I do not understand your request at the moment";
    myprint("Send text to MouthOttBot2 agent:"+text);
    mouthserver.execute("sayText", text);
  }
  
  
  // kill myself
  deleteNode(framename);
}

function processInform(type, target1, target2, framename) {
  myprint("Entering processInform with type: "+type+", target1: "+target1+", target2: "+target2+"\n");
  if ( type == "IS_A" ) {
    // add IS_A relation
    var is_a_node = Root.findFrame("IS_A");
    var newnode  = is_a_node.createInstance();
    newnode.setSlotValue("Name", target1+"_is_a_"+target2);
    newnode.setSlotValue("Object1",target1);
    newnode.setSlotValue("Object2",target2);
    var text = "I see, thank you. Adding relation: "+target1+" IS_A "+target2;
    mouthserver.execute("sayText", text);
    myprint("Send text to MouthOttBot2 agent:"+text);
    is_a_node.redrawPanel();
  } else if ( type == "HAS_A" ) {
    // add HAS_A relation
    var has_a_node = Root.findFrame("HAS_A");
    var newnode  = has_a_node.createInstance();
    newnode.setSlotValue("Name", target1+"_has_a_"+target2);
    newnode.setSlotValue("Object1",target1);
    newnode.setSlotValue("Object2",target2);
    var text = "I see, thank you. Adding relation: "+target1+" HAS_A "+target2;
    mouthserver.execute("sayText", text);
    myprint("Send text to MouthOttBot2 agent:"+text);
    has_a_node.redrawPanel();
  }

  // kill myself
  deleteNode(framename);
}



function onInstantiateGreet(s) {
  myprint("onInstantiateGreet: entering\n");

  var converpartner = Root.findFrame("ConverPartner");
  var iconverpartner =  converpartner.findFirstInstance();
  if ( iconverpartner == null ) {
  	submitToSPAK("ConverPartner-humanname="+s.human.username+"\ninduce\n");
  }
  var text;
  if ( s.human.status.indexOf("arrived") == 0 ) {
  	// already gret
  	text = "Hi "+s.human.username;
  } else {
  	text = "Hi "+s.human.username+", how are you today?";
   	// update this user's status
  	var today = new Date()
  	var arrivereg = /^arrived/;
  	if ( ! arrivereg.test(s.human.status)) {
    		var hour = today.getHours();
    		if ( hour > 12 ) hour = hour-12;
    		var status= "arrived since "+hour;
    		s.human.status = status;
  	} 
  }

  var msg1 = "Say-value="+text+"\ninduce\n";
  submitToSPAK(msg1);

  myprint("onInstantiateGreet: exiting\n");
  
  // kill myself
  deleteNode(s.Name);
}

function greetNewUser(s) {
  var iconverpartner;
  if ( converpartner.findFirstInstance() == null ) {
	submitToSPAK("ConverPartner-humanname=unknown\ninduce\n");
  } else {
  	iconverpartner = converpartner.findFirstInstance();
  }

  var text = "Good morning. We haven't met each other before, have we. My name is Robovie, what is your name?";
  var msg1 = "DialogueAsk-question="+text+"\nDialogueAsk-answertype=name\nRoot-priority=10\nDialogueAsk-parentframe="+s.Name+"\nDialogueAsk-parentslot=username\ninduce\n";
  submitToSPAK(msg1);

  // set speech recognition's choice to people's name
  //$namelist = "My name is Alex\nMy name is Micheal\nMy name is Sebastian\nMy name is Ott\n";
  //speechrecognizer.execute("setChoices", $namelist);
}

function onGreetNewUser(s) {
  myprint("onGreetNewUser "+s.Name+": entering\n");
  // check first if we are busy asking his/her name
  /*
  var cpi = Root.findFrame("cpi");
  if ( cpi.getSlotValue("status") != "nameasked") {
  	greetNewUser();
  }
  */
  greetNewUser(s);
}

function onEvaluateGreetNewUser(s) {
  myprint("onEvaluateGreetNewUser "+s.Name+": entering\n");
  // check where we are now (in case of many states)
  if ( s.username != "" ) {
  	// got the name
	// update ConverPartner frame
	submitToSPAK("ConverPartner-humanname="+s.username+"\ninduce\n");

	// check first if we know him/her
  	var humanname = s.username;
	myprint("Finding "+humanname+" frame ...");
  	var humanframe = Root.findFrame(humanname);
  	var text2="";
	if ( humanframe != null ) {
		myprint("found!\n");
    		// existing user
    		var text = "Hello "+humanname+". Sorry I did not recognize you. How are you today?";

		// say the text
		var msg1 = "Say-value="+text+"\ninduce\n";
		submitToSPAK(msg1);

		if ( humanframe.getSlotValue("status").indexOf("left") == 0 ) {
			// last status was left
			updatestatus(humanname);
		}
	} else {
		myprint("not found, ");
		// new user, add he/she to the database
  		myprint("creating a human frame: "+humanname+"\n");

   		var text = "Hello "+humanname+". Nice to meet you. How are you today?";
		myprint("Saying text: "+text+"\n");
		// say the text
		var msg1 = "Say-value="+text+"\ninduce\n";
		submitToSPAK(msg1);

   		createNewUser(humanname);

		updatestatus(humanname);
		// say who has arrived
		text2 = whoHasArrived(humanname);
		myprint("checking who has arrived: "+text2+"\n");
	}

	// sawaddee
	//roboposeserver.execute("sawaddee", "");

	if ( text2 != "" ) {
		var msg2 = "Say-value="+text2+"\ninduce\n";
		submitToSPAK(msg2);
	}

	// inform the FaceRecognizer agent
	//facerecognizer.execute("setName", username);
	//myprint("Calling facerecognizer::setName("+text+")\n");

	// set speech recognition's choice to default
	//speechrecognizer.execute("resetChoices", "");

  	// delete me 
  	deleteNode(s.Name);
  }
}

function updatestatus(humanname) {
	// update the status
	var humanframe = Root.findFrame(humanname);
	var today = new Date();
	var hour = today.getHours();
	if ( hour > 12 ) hour = hour-12;
	var status= "arrived since "+hour;
	humanframe.setSlotValue("status", status);
}


function onInstantiateAnswerGreet(speechactframe,framename) {
  myprint("onInstantiateAnswerGreet: entering\n");
  // Test code
  //myprint("Send Greeting back ("+speechactframe.rawtext+") via mouthserver...\n");
  //mouthserver.execute("sayText", speechactframe.rawtext);

  var cpi = Root.findFrame("cpi");
  if (cpi.getSlotValue("humanname") == "unknown"  ) {
    greetNewUser();
  } else {
    myprint("Sending text: Hello "+cpi.getSlotValue("humanname")+", how are you");
    mouthserver.execute("sayText", "Hello "+cpi.getSlotValue("humanname")+", how are you");
    

  }
/*
  } else if (newname != "unknown" && newname != cpi.getSlotValue("name") ) {
    // if not unknown and not the current person
    
    // known user
    cpi.setSlotValue("name",newname);
    var text = "Hi, "+newname;
    
    //mouthserver.execute("sayText", text);
    myprint("onInstantiateAnswerGreet: "+text+"\n");
    
    // set the status of the conversation
    cpi.setSlotValue("status","alreadygreeted");
  } 
*/
  myprint("onInstantiateAnswerGreet: exiting\n");
  
  // kill myself and speechAct frames
  myprint("onInstantiateAnswerGreet: deleting "+speechactframe.Name+" and "+framename+"\n");
  deleteNode(speechactframe.Name);
  deleteNode(framename);
}


function onInstantiateAnswerHowAreYou(howareyouact, framename) {
  myprint("onInstantiateAnswerHowAreYou: entering\n");

  var cpi = Root.findFrame("cpi");
  var text;
  if ( cpi.getSlotValue("status") != "howareyouasked" ) {
    // actually I should check my health condition first :)
    text = "I'm okay, how about you, "+cpi.getSlotValue("humanname");
    
    // set the status 
    cpi.setSlotValue("status","howareyouasked");
  } else {
    // actually I should check my health condition first :)
    text = "I'm okay, thank you, "+cpi.getSlotValue("humanname");
      
    // set the status 
    cpi.setSlotValue("oldstatus", "howareyouasked");
    cpi.setSlotValue("status","greeted");
  }
  myprint("onInstantiateAnswerHowAreYou: saying text: "+text+"\n");
  mouthserver.execute("sayText", text);
    
  myprint("onInstantiateAnswerHowAreYou: exiting\n");
  
  // kill myself
  deleteNode(howareyouact.Name);
  deleteNode(framename);
}

function onInstantiateAnswerSeeYou(act, framename) {
  myprint("onInstantiateAnswerSeeYou: entering\n");

  var cpi = Root.findFrame("cpi");
  var text;
  cpi.setSlotValue("status", "seeyousaid" );

  text = "See you";
      
  myprint("onInstantiateAnswerSeeYou: saying text: "+text+"\n");
  mouthserver.execute("sayText", text);
    
  myprint("onInstantiateAnswerHowAreYou: exiting\n");
  
  // kill myself
  deleteNode(act.Name);
  deleteNode(framename);
}

function onInstantiateStartCalculator(act, framename) {
  myprint("onInstantiateStartCalculator: entering\n");

  var cpi = Root.findFrame("cpi");
  var text;
  cpi.setSlotValue("topic", "calculation" );
  cpi.setSlotValue("status", "start to calculate" );

  text = "Starting calculator, please tell a number:";
  myprint("onInstantiateStartCalculator: saying text: "+text+"\n");
  mouthserver.execute("sayText", text);
    
  myprint("onInstantiateStartCalculator: exiting\n");
  
  // kill myself
  deleteNode(act.Name);
  deleteNode(framename);
}

function getStatus() {
  var text;

  // grab all children
  var knownuser_child = Root.findInstancesOf("KnownUser");
  var output="";

 
  // loop printing info
  myprint("knownuser_child.size is: "+knownuser_child.size()+"\n");
  var matchfound = 0;

  if ( knownuser_child.size() > 0 ) {
    for ( var i = 0 ; i < knownuser_child.size() ;i++ ) {
      var tmp = knownuser_child.get(i);
      matchfound++;
      myprint("Knownuser frame: "+tmp.getSlotValue("Name")+"\n");
      var status = tmp.getSlotValue("status");
      text = tmp.getSlotValue("Name")+" "+status;

      if (matchfound != 1) {
	text = ", and "+text;
      }
      output = output+text;
    }
  }
  
  myprint("Return text:"+output);
  return output;
}

function whoHasArrived(me) {
  // grab all children
  var knownuser_child = Root.findInstancesOf("KnownUser");
  var text = "";

  // loop printing info
  myprint("knownuser_child.size is: "+knownuser_child.size()+"\n");
  var matchfound = 0;
  if ( knownuser_child.size() > 0 ) {
    for ( var i = 0 ; i < knownuser_child.size() ;i++ ) {
      var tmp = knownuser_child.get(i);
      var username = tmp.getSlotValue("Name");
      // don't tell me about myself
      if ( username == me ) break;
      myprint("Knownuser frame: "+tmp.getSlotValue("Name")+"\n");
      var status = tmp.getSlotValue("status");
      if ( status != "" ) {
        var checkarrived = /^arrived/;
        if ( checkarrived.test(status) ) {
          matchfound++;
	  text = tmp.getSlotValue("Name")+" has "+ status;
        }
        if (matchfound != 1) {
          text = ", and "+text;
        }
        //myprint("Adding text"+text);
      }
    }
  }
  //myprint("Return text: "+text);
  return text;
}

function answerHowManyStudentLeft() {
  var text;

  // grab all children
  var knownuser_child = Root.findInstancesOf("KnownUser");
  var text="";

  // loop printing info
  myprint("knownuser_child.size is: "+knownuser_child.size()+"\n");
  var matchfound = 0;
  if ( knownuser_child.size() > 0 ) {
    for ( var i = 0 ; i < knownuser_child.size() ;i++ ) {
      var tmp = knownuser_child.get(i);
      myprint("Knownuser frame: "+tmp.getSlotValue("Name")+"\n");
      var status = tmp.getSlotValue("status");
      if ( status != "" ) {
        var checkleft = /^left/;
        var checkarrived = /^arrived/;
        if ( checkleft.test(status) ) {
          matchfound++;
  	  text = tmp.getSlotValue("Name")+"has "+status;
        }   else if ( checkarrived.test(status) ) {
          matchfound++;
	  text = tmp.getSlotValue("Name")+" should still be here.";
        }
        if (matchfound != 1) {
          text = ", and "+text;
        }
        myprint("Adding text"+text);
      }
    }
  }
  myprint("Return text: "+text);
  return text;
}

function onReportStatus(s) {
  myprint("oReportStatus: entering\n");
  var text = answerHowManyStudentLeft();
  myprint("onReportStatus: saying text: "+text+"\n");
  mouthserver.execute("sayText", text);
  myprint("onReportStatus: exiting\n");

  // kill myself
  deleteNode(s.Name);
}

function onReportStatusTask(s) {
  myprint("oReportStatusTask: entering\n");
  submitToSPAK("Reportstatus-start=true\ninduce\n");
  myprint("onReportStatusTask: exiting\n");
  // kill myself
  deleteNode(s.Name);
}

function onInstantiateAnswerBye(s) {
  myprint("onInstantiateAnswerBye: entering\n");
  var username = s.converpartner.humanname;
  var text;
  if (username == "unknown") {
  	text = "Bye bye have a nice day.";
  } else {
  	text = "Bye bye "+username+", have a nice day.";
  }

  // say bye bye
  myprint("onInstantiateAnswerBye: saying text: "+text+"\n");
  submitToSPAK("Say-act=bye\nSay-value="+text+"\ninduce\n");

  //update status
  if ( username != "unknown"  ) {
    var userframe = Root.findFrame(username);
    if ( userframe ) {
      var today = new Date();
      //var status= "left since "+today.toLocaleDateString();
      var hour = today.getHours();
      if ( hour > 12 ) hour = hour - 12;
      var status= "left since "+ hour;
      userframe.setSlotValue("status", status);
    } 
  }

  myprint("onInstantiateAnswerBye: exiting\n");

  // kill myself
  deleteNode(s.Name);
}


function parseSpeech(text, framename) {
  // possible speech acts that can be generated:
  // byebye, reportstudentstatus, newname, greet, howareyou
  myprint("Entering parseSpeech with text: "+text+", framename: "+framename+"\n");
  // send to parser agent
  // which might generate a SpechAct frame
  myprint("parseSpeech: calling speechparser::setInputText...\n");
  speechparser.execute("setInputText", text);
  myprint("parseSpeech: return form speechparser::setInputText...\n");
  myprint("Exit parseSpeech: exiting\n");

  // kill myself
  deleteNode(framename);
}

var autoinstancecount = 0;

function doFrameCreater(framename, cond1, action1) {
  myprint("Entering doFrameCreater... \n");
  var autoactionnode = Root.findFrame("AutoAction");
  var newnode = new KFrame("AutoAction_"+autoinstancecount, null)
  autoinstancecount = autoinstancecount +1;

  newnode.addSlot("required", null, Slot.TYPE_INSTANCE,Slot.COND_INSTANCEOF,cond1,true,true);
  //action1 = action1 + "; deleteNode(s.Name);";
  action1 = action1 + "; setSlotValue('shoulddie', 'true');";
  newnode.addSlot("onInstantiate", action1, Slot.TYPE_STR, Slot.COND_ANY, "", false, true);
  autoactionnode.add(newnode);

  myprint("Exit doFrameCreater: exiting\n");
  // kill myself
  deleteNode(framename);
}

function doGarbageCollection(framename) {
  myprint("Starting Garbage Collection... \n");
  var allnodes = Root.findInstancesOf("Root");
  //myprint("Found total "+allnodes.size()+" nodes.\n");
  for ( i=0 ; i < allnodes.size() ; i++) {
	var tmpframe = allnodes.get(i);
	if ( tmpframe.getSlotValue("shoulddie") == "true" ) {
		myprint("Killing frame: "+tmpframe.getSlotValue("Name")+"\n");;
		deleteNode(tmpframe.getSlotValue("Name"));
	}
  }
  myprint("Exit doGarbageCollection\n");
  // kill myself
  deleteNode(framename);
}

function actionPointEvent(direction, framename) {
  myprint("Entering actionPointEvent("+direction+"\n");
  if ( direction == "left" || direction == "right" ) {
  	myprint("actionPointEvent: say the direction I got...\n");
  	mouthserver.execute("sayText", direction+"?");
	myprint("Calling roboneckserver::moveRight|Left\n");
	if ( direction == "right" ) {
		roboneckserver.execute("moveLeft", 2);
	} else {
		roboneckserver.execute("moveRight", 2);
	}
  } else {
	myprint("Calling roboneckserver::goZeroPosition\n");
	roboneckserver.execute("goZeroPosition", "")
  }

  myprint("Exit actionPointEvent: exiting\n");

  // kill myself
  deleteNode(framename);
}

function onInstantiateRecognizeFace(s) {
  myprint("Entering onInstantiateRecognizeFace ...\n");
  myprint("Contact FaceRecognizer::recognize()....");
  //if ( facerecognizer != 0 ) facerecognizer.execute("recognize", "");
  myprint("Leaving onInstantiateRecognizeFace...\n");
  //myprint("Deleteing meself...\n");
  //deleteNode(s.Name);
}

function doState(s) {
	if ( s.value == "true" ) {
		myprint("Entering state "+s.Name+"\n");
	} else {
		deleteNode(s.Name);
	}
}

function onFaceDetected(s) {
  myprint("Entering onFaceDetected, status is "+s.facedetected_status+"\n");
  if ( s.facedetected_status == "absent" ) {
  	myprint("Deleteing "+s.Name+"...\n");
  	deleteNode(s.Name);
  	myprint("Leaving onFaceDetected...\n");
  }
}

function onFoundHuman(s) {
  myprint("Entering onFoundHuman, status is "+s.foundhuman_fd.facedetected_status+"\n");
/*
  var iconverpartner = converpartner.findFirstInstance();
  if ( iconverpartner == null ) {
  	//submitToSPAK("ConverPartner-humanname=unknown\ninduce\n");
  	var inst1 = createNode("ConverPartner");
	inst1.setSlotValue("humanname", "unknown");
	inst1.updateHash();
  }
*/
  // human is found, so do the recognize
  //var recognizeface = createNode("RecognizeFace");
  //recognizeface.runSpecialSlot("onInstantiate", false);
 	submitToSPAK("RecognizeFace-start=true\ninduce\n");
}

function onHumanNameSpeechEvent(s) {
  // executed by the Evaluator thread
  myprint("Entering onHumanNameSpeechEvent...\n");
  // Human name is in s.subact
  // if this Human not exist then welome new user
  // if exists then check my action history
  // if I just ask for his name then apologize himi
  // otherwise say yes I know
}

function onUpdateConverPartner(s) {
  //myprint("Entering onUpdateConverPartner...\n");
}

function onFaceRecognized(s) {
  myprint("Entering onFaceRecognized...\n");

  if (s.facerecognized_username == "unknown" ) {
	//create an UnknownUser, if not already exists
  	var humaninst = Root.findFrame("UnknownUser").findFirstInstance();
	if ( humaninst == null) {
		humaninst = createNode("UnknownUser");
		humaninst.setSlotValue("username", "unknown");
	} 
	humaninst.setSlotValue("objectseen", "true");
  } else {
// if s.username != unknown
// Find if a Human frame with this username exists
	var theman = existInstance("Human", "username", s.facerecognized_username);
	if ( theman != "" ) {
// if exists, update his objectseen slot to true
		theman.setSlotValue("objectseen", "true");
	} else {
// if not exists (should not occur), create a new KnownUser frame
		var newuser = createNode("KnownUser");
		newuser.setSlotValue("username", s.facerecognized_username);
		newuser.setSlotValue("objectseen", "true");
	}


  }
//myprint("Create/Update a ConverPartner frame...\n");
//submitToSPAK("ConverPartner-humanname="+s.username+"\ninduce\n");
  myprint("Exiting onFaceRecognized...\n");
}


function followFace(faceframe, framename) {
  myprint("Entering followFace with faceframe: ("+faceframe.posX+", "+faceframe.posY+")\n");
  // reduce the traffic a bit
  if ( faceframe.posX > 100 && faceframe.posX < 220 &&
	faceframe.posY > 60 && faceframe.posY < 180) {
  	myprint("Face quite in the middle, do nothing...");
  } else {
    myprint("Contacting roboneckserver::followObject...");
    roboneckserver.execute("followObject", faceframe.posX, faceframe.posY);
  }
  myprint("Leaving followFace...\n");
  myprint("Deleteing "+faceframe.Name+" and "+framename+"...\n");
  // kill faceframe and myself
  deleteNode(faceframe.Name);
  deleteNode(framename);
}


//Create a new Known User
function createNewUser(newname) {
  myprint("Creating new user: "+newname+"\n");
  var knownusernode = Root.findFrame("KnownUser");
  var newnode = knownusernode.createInstance();
  newnode.setSlotValue("Name", newname);
  newnode.setSlotValue("username", newname);
  newnode.setSlotValue("isa", "Human");
/* not used
  //var newnode  = new KFrame(newname,null);
  //newnode.addSlot("username",newname,Slot.TYPE_STR,Slot.COND_EQ,newname,true,true);
  //usernode.add(newnode);
*/
  knownusernode.redrawPanel();
  myprint("New user "+newname+" created\n");
}

function doGreet(humanname) {
  if ( humanname == "unknown" ) {
    myprint("Unknown user found, ask his name..\n");
  } else {
    myprint("Sending text 'Hello "+humanname+", how are you?' to Mouth agent...\n");
  }
}

function onInstantiateSilence(framename) {
  myprint("Entering onInstantiateSilence: ");
  myprint("Leaving onInstantiateSilence: ");

  // kill myself
  deleteNode(framename);
}

function doAnswerFound(s) {
  myprint("Entering "+s.Name+"...\n");
  myprint("Condition: "+s.dialogueask.answeract == s.speechrecognized.act && ( s.dialogueask.answersubact == "any" || s.dialogueask.answersubact == s.speechrecognized.subact)+"\n");
	//var myparent = Root.findFrame(dialogueask.parentframe);
	//myparent.setSlotValue(dialogueask.parentslot, speechrecognized.text);
	var msg1 = s.dialogueask.parentframe.Name +"-"+s.dialogueask.parentslot+"="+s.speechrecognized.data+"\ninduce\n";
	submitToSPAK(msg1);

	var wframes = Root.findInstancesOf("WaitingForAnswer");
      	for ( var i = 0 ; i < wframes.size() ;i++ ) {
		var tmp = wframes.get(i);
		if ( tmp.getSlotValue("dialogueask") == s.dialogueask.Name ) {
			myprint("Deleting "+tmp.getSlotValue("Name")+"\n");
			deleteNode(tmp.getSlotValue("Name"));
		}
	}

	deleteNode(s.dialogueask.Name);
	deleteNode(s.speechrecognized.Name);
	//deleteNode(s.Name);
}

function onInstantiateReserveBus(s) {
  myprint("Entering onInstantiateReserveBus...\n");
	if ( checkReserveBus(s) != 0) {
  	onBTryReserveBus(s);
  } else {
  	myprint("Got all information, nothing to ask...\n");
  }
  myprint("Exiting onInstantiateReserveBus...\n");
}

function onBTryReserveBus(s) {
	// some of these should disappear with the new backward chaining mechanism
	var msg1 = "DialogueAsk-question=Where is your destination?\nDialogueAsk-answeract=Tell\nDialogueAsk-answersubact=Place\nRoot-priority=10\nDialogueAsk-parentframe="+s.Name+"\nDialogueAsk-parentslot=destination\ninduce\nDialogueAsk-question=When would you like to leave?\nDialogueAsk-answeract=Tell\nDialogueAsk-answersubact=Time\nRoot-priority=5\nDialogueAsk-parentframe="+s.Name+"\nDialogueAsk-parentslot=time\ninduce\n";
	submitToSPAK(msg1);
}

function onEvaluateReserveBus(s) {
	if ( checkReserveBus(s) == 0) {
		deleteNode(s.Name);
	}
}

function checkReserveBus(s) {
	if ( s.destination != "" && s.destination != null &&
		s.time != "" && s.time != null ) {
		// got all we want
		myprint("ReserveBus Task: got all the required information.\n");
		myprint("ReserveBus Task: now reserving bus ticket to "+s.destination+" at time "+s.time+"\n");
		if ( s.parentframe != "" && s.parentslot != "") {
			myprint("Inform my parent...\n");
			var msg1 = s.parentframe +"-"+s.parentslot+"=reserved\n";
			submitToSPAK(msg1);
		}
		myprint("ReserveBus Task: Done\n");
		return 0;
	} else {
		return -1;
	}
}

function doAsk(sname) {
	myprint("Entering doAsk("+sname+")...\n");
	var createit = true;
	var S = Root.findFrame(sname);
	// find high-priority processes
	if ( S.getSlotValue("priority") == null ||  S.getSlotValue("priority") == '""' ) {
		var mypriority = 0;
	} else {
		var mypriority = S.getSlotValue("priority");
	}
	myprint("mypriority= "+mypriority+"\n");
	var wframes = Root.findInstancesOf("WaitingForAnswer");
	myprint("wframes.size= "+wframes.size()+"\n");
      	for ( var i = 0 ; i < wframes.size() ;i++ ) {
		var tmp = wframes.get(i);
		myprint(i+": "+tmp.getSlotValue("Name")+", priority= "+tmp.getSlotValue("priority")+"\n");
		if ( tmp.getSlotValue("dialogueask") == sname) {
			// my own frame, not ask again
			myprint("doAsk: Found my own WaitingForAnswer, do nothing...\n");
			createit = false;
			return;
		} else if ( parseInt(tmp.getSlotValue("priority")) >= parseInt(mypriority) ) {
			myprint("doAsk: Found higher or equal priority processes...\n");
			// there exists higher priority processes
			createit = false;
			return;
		}
	}

	if ( createit) {
		myprint("Creating a WaitingForAnswer frame...\n");
		// create the WaitingForAnswer frame

		var wmi = createNode("WaitingForAnswer");
		wmi.setSlotValue("answeract", S.getSlotValue("answeract"));
		wmi.setSlotValue("answersubact", S.getSlotValue("answersubact"));
		wmi.setSlotValue("dialogueask", sname);
		wmi.setSlotValue("priority", mypriority);


		// ask!
		myprint("doAsk: sending text to the mouthserver agent..\n");
		myprint("doAsk: "+S.getSlotValue("question")+"\n");
  		var msg1 = "Say-value="+S.getSlotValue("question")+"\ninduce\n";
  		submitToSPAK(msg1);
	}
}

function onInstantiateDialogueAsk(s) {
	//doAsk(s.Name);
}

function onEvaluateDialogueAsk(s) {
	doAsk(s.Name);
}

function onInstantiateNotUnderstand(s) {
	myprint("onInstantiateNotUnderstand: entering...\n");
	// the robot does not understand what human is saying
	// in s.text
	var text = "Sorry I don't understand what you mean.";
  	var msg1 = "Say-value="+text+"\ninduce\n";
  	submitToSPAK(msg1);
	myprint("onInstantiateNotUnderstand: saying"+text+"\n");
	myprint("onInstantiateNotUnderstand: exiting...\n");
}

function onInstantiateRecognizeMistake(s) {
	myprint("onInstantiateRecognizeMistake: entering...\n");
	// robot just greeted human incorrectly, now get the feedback
	// robot greet action is in s.greetaction
	// human's new name frame is s.humanname

	// check if I know him
        // check first if we know him/her
        var humanname = s.humanname.subact;
        myprint("Finding "+humanname+" frame ...");
        var humanframe = Root.findFrame(humanname);
        var text2;
        if ( humanframe != null ) {
                myprint("found!\n");
                // existing user
                var text = "Hello "+humanname+". Sorry I did not recognize you. How are you today?";

                // say the text
                var msg1 = "Say-value="+text+"\ninduce\n";
                submitToSPAK(msg1);
        } else {
                myprint("not found, ");
                // new user, add he/she to the database
                myprint("creating a human frame: "+humanname+"\n");

                var text = "Oh, sorry. Hello "+humanname+". My name is Robovee. Nice to meet you. How are you today?";
                myprint("Saying text: "+text+"\n");
                // say the text
                var msg1 = "Say-value="+text+"\ninduce\n";
                submitToSPAK(msg1);

                createNewUser(humanname);

                // update the status
                var humanframe = Root.findFrame(humanname);
                var today = new Date();
                var hour = today.getHours();
                if ( hour > 12 ) hour = hour-12;
                var status= "arrived since "+hour;
                humanframe.setSlotValue("status", status);

                // say who has arrived
                text2 = whoHasArrived(humanname);
                myprint("checking who has arrived: "+text2+"\n");

      		// sawaddee
        	//roboposeserver.execute("sawaddee", "");
        }

        if ( text2 != "" ) {
                var msg2 = "Say-value="+text2+"\ninduce\n";
                submitToSPAK(msg2);
        }

        // inform the FaceRecognizer agent
        //facerecognizer.execute("setName", humanname);
        myprint("Calling facerecognizer::setName("+humanname+")\n");

        // set speech recognition's choice to default
        //speechrecognizer.execute("resetChoices", "");

        // delete me
        deleteNode(s.Name);
	myprint("onInstantiateRecognizeMistake: exiting...\n");
}

function onInstantiateConfirmMyAction(s) {
	// FIXME
	// robot just greeted human correctly, doesn't understand what's all the fuss is about
}

/*
var converpartner = Root.findFrame("ConverPartner");
if (!converpartner) {
	myprint("Error, no ConverPartner frame, things will not work properly...\n");
} 
*/

function onInstantiateGreetNewUserTaskAction1(s) {
	myprint("Entering onInstantiateGreetNewUserTaskAction1...\n");
	myprint("First of all say Hello....\n");
  	submitToSPAK("Say-value=Good Morning\ninduce\n");
	myprint("Creating a dialogue asking for his name...\n");
  	var text = "We haven't met each other before, have we. My name is Robovie, what is your name?";
	var msg1 = "DialogueAsk-question="+text+"\nDialogueAsk-answeract=Tell\nDialogueAsk-answersubact=Name\nRoot-priority=10\nDialogueAsk-parentframe="+s.Name+"\nDialogueAsk-parentslot=username\ninduce\n";
  	submitToSPAK(msg1);
	// create dialogue state frame
	stateinst = createNode("GreetNewUserTaskDialogueState");
	stateinst.setSlotValue("currentstate", "nameasked");
	sstateinst = new KFrameScript(stateinst);
	s.stateframe = sstateinst.Name;
}

function onEvaluateGreetNewUserTaskAction1(s) {
	if (s.username != "" && s.stateframe != "" ) { 
		// go on to the next step
		var msg1 = "GreetNewUserTaskAction2-greetnewusertaskaction_username="+s.username+"\ninduce\n";
  	 submitToSPAK(msg1);
	  deleteNode(s.Name)
	}
}

function onInstantiateGreetNewUserTaskAction2(s) {
  var humaninst = Root.findFrame("UnknownUser").findFirstInstance();
	if ( humaninst == null) {
		myprint("onEvaluateGreetNewUserTaskAction1: strange, I got a new name but there is no unknown human frame\n");
	} else {
		myprint("onInstantiateGreetNewUserTaskAction1: set human name to "+s.username+"\n");
		humaninst.setSlotValue("username",s.greetnewusertaskaction_username);
  	var text;
  	text = "Nice to meet you, "+s.converpartner.username+". How are you today?";
  	var msg1 = "Say-value="+text+"\ninduce\n";
  	submitToSPAK(msg1);
		updateStatus(s.converpartner, "arrived");

		//report student status
  	text = answerHowManyStudentLeft();
	  if ( text != "" ) {
  		msg1 = "Say-value="+text+"\ninduce\n";
  		submitToSPAK(msg1);
		}	
	}
	s.stateframe.currentstate = "finished"; 
	deleteNode(s.Name)
}

function updateStatus( cp, state) {
   	// update this user's status
  	var today = new Date()
    var hour = today.getHours();
    if ( hour > 12 ) hour = hour-12;

		if ( state == "arrived" ) {
  		var arrivereg = /^arrived/;
  		if ( ! arrivereg.test(s.converpartner.status)) {
    		var status= "arrived since "+hour;
    		cp.status = status;
  		} 
		} else if ( state == "left" ) {
  		var arrivereg = /^left/;
  		if ( ! arrivereg.test(s.converpartner.status)) {
    		var status= "left since "+hour;
    		cp.status = status;
  		} 
		} 

		cp.lastmet = today.getTime()/1000;
}

function onInstantiateGreetKnownUserTaskAction1(s) {
	myprint("Entering onInstantiateGreetKnownTaskAction1...\n");
	myprint("Found a known user, greet him...\n");
  var text;
  if ( s.converpartner.status.indexOf("arrived") == 0 ) {
  	// already gret
  	text = "Hi "+s.converpartner.username;
  } else {
  	text = "Hi "+s.converpartner.username+", how are you today?";
		updateStatus(s.converpartner, "arrived");

	// create dialogue state frame
		var stateinst = createNode("GreetKnownUserTaskDialogueState");
		stateinst.setSlotValue("currentstate", "howareyouasked");
		sstateinst = new KFrameScript(stateinst);
		s.stateframe = sstateinst.Name;
  }

  var msg1 = "Say-value="+text+"\ninduce\n";
  submitToSPAK(msg1);
  deleteNode(s.Name)
}

function onEvaluateGreetKnownUserTaskAction1(s) {
	var stateframe = Root.findFrameScript(s.stateframe.Name);
	if ( ! stateframe || stateframe.currentstate == "finished"  ) { 
		deleteNode(s.Name)
	}
}

function onInstantiateGreetKnownUserTaskAction2(s) {
 	var text = "That's great, sir. Have a nice day";
  var msg1 = "Say-value="+text+"\ninduce\n";
  submitToSPAK(msg1);
	s.stateframe.currentstate = "finished"; 
  deleteNode(s.Name)
}


function onInstantiateGreetKnownUserTaskAction3(s) {
	myprint("Entering onInstantiateGreetKnownUserTaskAction3...\n");
	myprint("Creating a dialogue asking for his sleep status...\n");
 	var text = "That is not good. Did you sleep well last night?";
	var msg1 = "DialogueAsk-question="+text+"\nDialogueAsk-answeract=YesNo\nDialogueAsk-answersubact=any\nRoot-priority=10\nDialogueAsk-parentframe="+s.Name+"\nDialogueAsk-parentslot=sleepwell\ninduce\n";
  submitToSPAK(msg1);
	s.stateframe.currentstate = "doesSleepWellasked";
}

function onEvaluateGreetKnownUserTaskAction3(s) {

if (s.stateframe.currentstate == "doesSleepWellasked") {
	if (s.sleepwell == "Yes" ) {
 	  var text = "Well, take care of yourself. Don't work too hard then\n";
  	var msg1 = "Say-value="+text+"\ninduce\n";
  	submitToSPAK(msg1);
		s.stateframe.currentstate = "finished"; 
		deleteNode(s.Name);
  } else if (s.sleepwell == "No") {
 		var text = "Sleep is very important to your health. How do you feel now?";
  	var msg1 = "Say-value="+text+"\ninduce\n";
  	submitToSPAK(msg1);

		s.stateframe.currentstate = "howdoyoufeelasked"; 

		deleteNode(s.Name)
	}
}
}

function onInstantiateGreetKnownUserTaskAction4(s) {
	myprint("Entering onInstantiateGreetKnownUserTaskAction4...\n");
	myprint("Creating a dialogue asking for his name...\n");
 	var text = "I see, I should report to the welfare service center to get some advice for you [simulating]";
  var msg1 = "Say-value="+text+"\ninduce\n";
  submitToSPAK(msg1);

	myprint("Creating a dialogue asking if he wants to visit welfare center...\n");
  text = "The doctor advises you to visit the center. Should I reserve a visit for you?";
	msg1 = "DialogueAsk-question="+text+"\nDialogueAsk-answeract=YesNo\nDialogueAsk-answersubact=any\nRoot-priority=10\nDialogueAsk-parentframe="+s.Name+"\nDialogueAsk-parentslot=gowelfare\ninduce\n";
  submitToSPAK(msg1);
	s.stateframe.currentstate = "askedwanttovisitwelfare"; 
}

function onEvaluateGreetKnownUserTaskAction4(s) {
  myprint("Entering onEvaluateGreetKnownUserTaskAction4...\n");
	if (s.gowelfare == "Yes" ) {
    // create a visit welfare task
	  myprint("Creating a visit welfare task...\n");
	  var msg1 = "VisitWelfare-start=true\nTaskAction-parentframe="+s.Name+"\ninduce\n";
  	submitToSPAK(msg1);
		s.stateframe.currentstate = "visitwelfaretaskstarted"; 
		deleteNode(s.Name);
  } else if (s.gowelfare == "No") {
 		var text = "OK, but please take care of your health. If you feel not OK please let me know immediately.";
  	var msg1 = "Say-value="+text+"\ninduce\n";
  	submitToSPAK(msg1);
		s.stateframe.currentstate = "finished"; 
		deleteNode(s.Name)
	}
}

function onInstantiateGreetKnownUserTaskAction5(s) {
}

function onEvaluateGreetKnownUserTaskAction5(s) {
}

function onInstantiateVisitWelfare(s) {
  myprint("Entering onInstantiateVisitWelfare...\n");
  onBTryVisitWelfare(s);
  myprint("Exiting onInstantiateVisitWelfare...\n");
}

function onBTryVisitWelfare(s) {
	// some of these should disappear with the new backward chaining mechanism

	var msg1 = "DialogueAsk-question=Do you want to visit just now or in the afternoon?\nDialogueAsk-answeract=Tell\nDialogueAsk-answersubact=Time\nRoot-priority=10\nDialogueAsk-parentframe="+s.Name+"\nDialogueAsk-parentslot=time\ninduce\n";
	submitToSPAK(msg1);
}

function onEvaluateVisitWelfare(s) {
	if ( s.time != "" && s.time != null ) {
		if ( s.busreserved == "" ) {
			var msg1 = "ReserveBus-start=true\nReserveBus-destination=welfarecenter\nReserveBus-time="+s.time+"\nAction-parentframe="+s.Name+"\nAction-parentslot=busreserved\ninduce\n";
			submitToSPAK(msg1);
			s.busreserved = "processing";
		} else if (s.busreserved == "reserved") {
			// got all we want
			myprint("VisitWelfare Task: got all the required information.\n");
			myprint("VisitWelfare Task: visit welfare task done\n");
 			var text = "I have reserved a visit at the welfare center in the afternoon and also the bus ticket leaving at 3. I will tell you when the bus arrives";
  		var msg1 = "Say-value="+text+"\ninduce\n";
  		submitToSPAK(msg1);
			deleteNode(s.Name);
		}
	} 
}

function timeSinceLastMet(lastmet) {
  	var date = new Date();
		var now = date.getTime()/1000;
		return (now - lastmet);
}
