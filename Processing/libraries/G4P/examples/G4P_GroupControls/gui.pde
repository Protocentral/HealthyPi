// Action controls
GLabel lblDelay, lblDuration, LblPromptDur, lblDurationLeft; 
GLabel lblAlpha, lblGroup1Controls, lblActionControls, lblPromptDelay; 
GButton btnFadeIn, btnFadeOut, btnEnable, btnDisable, btnInvisible; 
GButton btnVisible, btnColor, btnFadeTo; 
GDropList drpColor;  
GSlider sdrDelay, sdrDuration, sdrAlpha; 
GToggleGroup togGroupSelect; 
GOption optPickGroup1, optPickGroup2; 

// Group 1 controls
GToggleGroup togG1Options; 
GOption grp1_a, grp1_b, grp1_c; 
GDropList grp1_d; 
GKnob grp1_e; 
GButton grp1_f; 
GCustomSlider grp1_g; 

// Group 2 controls
GWindow window;
GLabel lblGroup2Controls;
GImageToggleButton grp2_a;
GButton grp2_b; 
GPanel grp2_c; 
GTextArea grp2_textArea; 
GLabel grp2_d; 
GImageButton grp2_e;

synchronized public void drawWin(PApplet appc, GWinData data) { //_CODE_:window:378292:
  appc.background(213, 213, 245);
  appc.stroke(0);
  appc.strokeWeight(2);
  appc.fill(200, 200, 210);
  appc.rect(10, 10, appc.width-20, appc.height-20);
} //_CODE_:window:378292:


// Create all the GUI controls. 
public void createGUI() {
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
  G4P.setCursor(ARROW);
  surface.setTitle("Test Group Actions in G4P");
  createGroup1Controls();
  createGroup2Controls();
  createGroupActionControls();
}

public void createGroup1Controls() {
  togG1Options = new GToggleGroup();
  grp1_a = new GOption(this, 270, 50, 120, 20);
  grp1_a.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  grp1_a.setText("option text");
  grp1_b = new GOption(this, 270, 70, 120, 20);
  grp1_b.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  grp1_b.setText("option text");

  grp1_c = new GOption(this, 270, 90, 120, 20);
  grp1_c.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  grp1_c.setText("option text");
  togG1Options.addControl(grp1_a);
  grp1_a.setSelected(true);
  togG1Options.addControl(grp1_b);
  togG1Options.addControl(grp1_c);
  grp1_d = new GDropList(this, 270, 120, 120, 110, 5);
  grp1_d.setItems(loadStrings("list_679847"), 0);
  grp1_g = new GCustomSlider(this, 460, 50, 250, 50, "grey_blue");
  grp1_g.setRotation(PI/2, GControlMode.CORNER);
  grp1_g.setLimits(0.5, 0.0, 1.0);
  grp1_g.setNbrTicks(20);
  grp1_g.setShowTicks(true);
  grp1_g.setEasing(6.0);
  grp1_g.setNumberFormat(G4P.DECIMAL, 2);
  grp1_e = new GKnob(this, 270, 150, 120, 110, 0.8);
  grp1_e.setTurnRange(110, 70);
  grp1_e.setTurnMode(GKnob.CTRL_HORIZONTAL);
  grp1_e.setSensitivity(1);
  grp1_e.setShowArcOnly(false);
  grp1_e.setOverArcOnly(false);
  grp1_e.setIncludeOverBezel(false);
  grp1_e.setShowTrack(true);
  grp1_e.setLimits(0.5, 0.0, 1.0);
  grp1_e.setShowTicks(true);
  grp1_f = new GButton(this, 270, 270, 120, 30);
  grp1_f.setText("Go For It !");
}

public void createGroup2Controls() {
  window = GWindow.getWindow(this, "Window created by G4P", 0, 0, 480, 230, JAVA2D);
  window.addDrawHandler(this, "drawWin");
  grp2_a = new GImageToggleButton(window, 20, 50);
  String[] imgs = new String[] { 
    "data/tjoff.jpg", "data/tjover.jpg", "data/tjdown.jpg"
  };
  grp2_e = new GImageButton(window, 250, 50, imgs, "data/tjmask.png");
  grp2_c = new GPanel(window, 70, 50, 174, 80, "Tab bar text");
  grp2_c.setText("Tab bar text");
  grp2_textArea = new GTextArea(window, 4, 23, 164, 50, G4P.SCROLLBARS_NONE);
  grp2_textArea.setPromptText("Why not enter some text?");
  grp2_c.addControl(grp2_textArea);
  grp2_d = new GLabel(window, 70, 140, 170, 61);
  grp2_d.setIcon("ghost2.png", 1, GAlign.RIGHT, GAlign.MIDDLE);
  grp2_d.setText("Casperov");
  grp2_d.setTextBold();
  grp2_b = new GButton(window, 20, 110, 40, 90);
  grp2_b.setText("OK!");
  lblGroup2Controls = new GLabel(window, 20, 20, 440, 20);
  lblGroup2Controls.setText("GROUP 2 CONTROLS");
  lblGroup2Controls.setTextBold();
}

public void createGroupActionControls() {
  lblGroup1Controls = new GLabel(this, 270, 20, 190, 20);
  lblGroup1Controls.setText("GROUP 1 CONTROLS");
  lblGroup1Controls.setTextBold();
  togGroupSelect = new GToggleGroup();
  optPickGroup1 = new GOption(this, 160, 20, 30, 20);
  optPickGroup1.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  optPickGroup1.setText("1");
  optPickGroup1.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  optPickGroup1.addEventHandler(this, "optGroup1Select");
  optPickGroup2 = new GOption(this, 190, 20, 30, 20);
  optPickGroup2.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  optPickGroup2.setText("2");
  optPickGroup2.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  optPickGroup2.addEventHandler(this, "optGroup2Select");
  togGroupSelect.addControl(optPickGroup1);
  optPickGroup1.setSelected(true);
  togGroupSelect.addControl(optPickGroup2);
  lblActionControls = new GLabel(this, 40, 20, 120, 20);
  lblActionControls.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  lblActionControls.setText("Action for Group -");
  lblActionControls.setTextBold();
  lblActionControls.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  lblPromptDelay = new GLabel(this, 10, 50, 110, 20);
  lblPromptDelay.setText("Delay (ms)");
  lblPromptDelay.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  sdrDelay = new GSlider(this, 10, 70, 230, 20, 10.0);
  sdrDelay.setLimits(1, 0, 5000);
  sdrDelay.setNbrTicks(26);
  sdrDelay.setStickToTicks(true);
  sdrDelay.setShowTicks(true);
  sdrDelay.setEasing(5.0);
  sdrDelay.setNumberFormat(G4P.INTEGER, 0);
  sdrDelay.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  sdrDelay.addEventHandler(this, "sdrDelayChange");
  lblDelay = new GLabel(this, 130, 50, 50, 20);
  lblDelay.setText("0");
  lblDelay.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  btnFadeIn = new GButton(this, 10, 240, 110, 20);
  btnFadeIn.setText("Fade In");
  btnFadeIn.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  btnFadeIn.addEventHandler(this, "btnFadeInClick");
  btnFadeOut = new GButton(this, 130, 240, 110, 20);
  btnFadeOut.setText("Fade Out");
  btnFadeOut.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  btnFadeOut.addEventHandler(this, "btnFadeOutClick");
  btnEnable = new GButton(this, 10, 100, 110, 20);
  btnEnable.setText("Enable");
  btnEnable.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  btnEnable.addEventHandler(this, "btnEnableClick");
  btnDisable = new GButton(this, 130, 100, 110, 20);
  btnDisable.setText("Disable");
  btnDisable.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  btnDisable.addEventHandler(this, "btnDisableClick");
  btnInvisible = new GButton(this, 130, 130, 110, 20);
  btnInvisible.setText("Visible - false");
  btnInvisible.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  btnInvisible.addEventHandler(this, "btnInvisibleClick");
  btnVisible = new GButton(this, 10, 130, 110, 20);
  btnVisible.setText("Visible - true");
  btnVisible.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  btnVisible.addEventHandler(this, "btnVisibleClick");
  LblPromptDur = new GLabel(this, 10, 190, 110, 20);
  LblPromptDur.setText("Duration (ms)");
  LblPromptDur.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  btnColor = new GButton(this, 10, 160, 110, 20);
  btnColor.setText("Color Scheme");
  btnColor.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  btnColor.addEventHandler(this, "btnColorClick");
  drpColor = new GDropList(this, 130, 160, 110, 80, 3);
  drpColor.setItems(loadStrings("list_493061"), 0);
  drpColor.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  drpColor.addEventHandler(this, "drpColorSelect");
  lblDuration = new GLabel(this, 130, 190, 50, 20);
  lblDuration.setText("1000");
  lblDuration.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  sdrDuration = new GSlider(this, 10, 210, 230, 20, 10.0);
  sdrDuration.setLimits(1000, 0, 5000);
  sdrDuration.setNbrTicks(11);
  sdrDuration.setStickToTicks(true);
  sdrDuration.setShowTicks(true);
  sdrDuration.setEasing(5.0);
  sdrDuration.setNumberFormat(G4P.INTEGER, 0);
  sdrDuration.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  sdrDuration.addEventHandler(this, "sdrDurationChange");
  btnFadeTo = new GButton(this, 10, 270, 110, 20);
  btnFadeTo.setText("Fade To    >");
  btnFadeTo.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  btnFadeTo.addEventHandler(this, "btnFadeToClick");
  lblAlpha = new GLabel(this, 130, 270, 50, 20);
  lblAlpha.setText("255");
  lblAlpha.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  sdrAlpha = new GSlider(this, 10, 290, 230, 20, 10.0);
  sdrAlpha.setLimits(255, 0, 256);
  sdrAlpha.setNbrTicks(17);
  sdrAlpha.setStickToTicks(true);
  sdrAlpha.setEasing(5.0);
  sdrAlpha.setNumberFormat(G4P.INTEGER, 0);
  sdrAlpha.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  sdrAlpha.addEventHandler(this, "sdrAlphaChange");
  lblDurationLeft = new GLabel(this, 180, 190, 60, 20);
  lblDurationLeft.setText("0");
  lblDurationLeft.setLocalColorScheme(GCScheme.GREEN_SCHEME);
}