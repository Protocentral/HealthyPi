/*
 This sketch is to demonstrate some features of the GPassword, 
 GTextField and GTextArea controls. 
 
 These features include
 - Tabbing between controls
 - Hidden text in password control between controls
 - Default text
 - Copy and paste text
 
 for Processing V2 and V3
 (c) 2015 Peter Lager
 
 */

import g4p_controls.*;

GTextField txf1, txf2;
GTextArea txa1, txa2;
GPassword pwd1;
GTabManager tt;
GLabel lblPwd;

public void setup() {
  size(500, 300);
  G4P.setGlobalColorScheme(GCScheme.PURPLE_SCHEME);
  // Some start text
  String[] paragraphs = loadStrings("book3.txt");
  String startTextA = PApplet.join(paragraphs, '\n');
  String startTextF = "G4P is a GUI control library created by Peter Lager";

  txf1 = new GTextField(this, 10, 10, 200, 20);
  txf1.tag = "txf1";
  txf1.setPromptText("Text field 1");

  txf2 = new GTextField(this, 290, 10, 200, 30, G4P.SCROLLBARS_HORIZONTAL_ONLY);
  txf2.tag = "txf2";
  txf2.setPromptText("Text field 2");
  txf2.setText(startTextF);

  pwd1 = new GPassword(this, 10, 70, 200, 20);
  pwd1.tag = "pwd1";
  // Change the maximum word length from the default value of 10
  pwd1.setMaxWordLength(20);

  lblPwd = new GLabel(this, 10, pwd1.getY()-20, 200, 18);
  lblPwd.setAlpha(190);
  lblPwd.setTextAlign(GAlign.LEFT, null);
  lblPwd.setOpaque(true);

  txa1 = new GTextArea(this, 10, 120, 200, 160);
  txa1.tag = "txa1";
  txa1.setPromptText("Text area 1");

  txa2 = new GTextArea(this, 290, 100, 200, 180, G4P.SCROLLBARS_BOTH);
  txa2.tag = "txa2";
  txa2.setPromptText("Text area 2");
  txa2.setText(startTextA, 300);

  txf1.setFocus(true);
  // Create the tab manager and add these controls to it
  tt = new GTabManager();
  tt.addControls(txf1, pwd1, txa1, txf2, txa2);
}

public void draw() {
  background(200, 128, 200);
  // Draw tab order
  stroke(0);
  strokeWeight(2);
  line(txf1.getCX()/2, txf1.getCY(), txa1.getCX()/2, lblPwd.getY());
  line(txf1.getCX()/2, pwd1.getCY(), txa1.getCX()/2, txa1.getCY());

  line(txa1.getCX(), txa1.getCY(), txf2.getCX(), txf2.getCY());
  line(txf2.getCX(), txf2.getCY(), txa2.getCX(), txa2.getCY());
    if(pwd1.getPassword().length() == 0)
      lblPwd.setText("Enter password below");
    else
      lblPwd.setText(pwd1.getPassword());
}

public void displayEvent(String name, GEvent event) {
  String extra = " event fired at " + millis() / 1000.0 + "s";
  print(name + "   ");
  switch(event) {
  case CHANGED:
    println("CHANGED " + extra);
    break;
  case SELECTION_CHANGED:
    println("SELECTION_CHANGED " + extra);
    break;
  case LOST_FOCUS:
    println("LOST_FOCUS " + extra);
    break;
  case GETS_FOCUS:
    println("GETS_FOCUS " + extra);
    break;
  case ENTERED:
    println("ENTERED " + extra);  
    break;
  default:
    println("UNKNOWN " + extra);
  }
}

public void handleTextEvents(GEditableTextControl textControl, GEvent event) { 
  displayEvent(textControl.tag, event);
}

public void handlePasswordEvents(GPassword pwordControl, GEvent event) {
  displayEvent(pwordControl.tag, event);
}