/*
 This sketch demonstrates how to use the GGroup control 
 to make a collection of controls that can be manipulated 
 as a single entity.

 There is support for 
   enabling/disabling controls
   making theme visible/invisible 
   fading in / out (make fully opaque / fully transparent)
   fade to a particular opacity,
 
 for Processing V3
 (c) 2015 Peter Lager

 */
 
import g4p_controls.*;

GGroup grpMain, grpWin, grpSelected;

int groupID = 1;
int delay = 0, duration = 1000, alpha = 255;
int col = 0;

public void setup() {
  size(480, 320, JAVA2D);
  createGUI();
  // Create the groups and add controls
  grpSelected = grpMain = new GGroup(this);
  grpWin = new GGroup(window);
  grpMain.addControls(grp1_a, grp1_b, grp1_c, grp1_d);
  grpMain.addControls(grp1_e, grp1_f, grp1_g);
  grpWin.addControls(grp2_a, grp2_b, grp2_c, grp2_d, grp2_e);
}

public void draw() {
  background(213, 245, 213);
  stroke(0);
  strokeWeight(2);
  fill(200, 210, 200);
  rect(width - 220, 10, 210, height - 20);
  lblDurationLeft.setText("" + grpSelected.timeLeftFading());
}

// ########  Event handlers for action group controls

// Select group 1
public void optGroup1Select(GOption source, GEvent event) {
  grpSelected = grpMain;
}

// Select group 2
public void optGroup2Select(GOption source, GEvent event) {
  grpSelected = grpWin;
}

// Enable all controls in selected group (if alpha >= 128)
public void btnEnableClick(GButton source, GEvent event) {
  grpSelected.setEnabled(delay, true);
}

// Disable all controls in selected group
public void btnDisableClick(GButton source, GEvent event) {
  grpSelected.setEnabled(delay, false);
}

// Make all controls in selected group visible 
// (opacity depends on last used alpha value)
public void btnVisibleClick(GButton source, GEvent event) {
  grpSelected.setVisible(delay, true);
}

// Make all controls in selected group invisible 
public void btnInvisibleClick(GButton source, GEvent event) {
  grpSelected.setVisible(delay, false);
}

// Fade in to max opacity
public void btnFadeInClick(GButton source, GEvent event) {
  grpSelected.fadeIn(delay, duration);
}

// Change the colour sceme used for all controls in selected group
public void btnColorClick(GButton source, GEvent event) {
  grpSelected.setLocalColorScheme(delay, col);
}

// Fade in to min opacity (fully transparent)
public void btnFadeOutClick(GButton source, GEvent event) {
  grpSelected.fadeOut(delay, duration);
}

// Fade to current alpha value
public void btnFadeToClick(GButton source, GEvent event) {
  grpSelected.fadeTo(delay, duration, alpha);
}

// Update delay value
public void sdrDelayChange(GSlider source, GEvent event) {
  delay = source.getValueI();
  lblDelay.setText("" + delay);
}

// Update fade duration value
public void sdrDurationChange(GSlider source, GEvent event) { 
  duration = source.getValueI();
  lblDuration.setText("" + duration);
}

// Update the alpha value to be used
public void sdrAlphaChange(GSlider source, GEvent event) {
  alpha = source.getValueI();
  if (alpha > 255) alpha = 255;
  lblAlpha.setText("" + alpha);
}

// Pick one of the 8 predefined colour schemes
public void drpColorSelect(GDropList source, GEvent event) { 
  col = source.getSelectedIndex();
}