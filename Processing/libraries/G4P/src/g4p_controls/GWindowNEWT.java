/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2015 Peter Lager

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

package g4p_controls;

import processing.core.PMatrix;
import processing.core.PVector;

/**
 * Class for independent windows using P2D or P3D renderers. These 
 * can be used to hold G4P GUI components or used for drawing or both combined.
 * <br><br>
 * A number of examples are included in the library and more info can be found
 * at www.lagers.org.uk
 * 
 * Updated for Processing V3
 * 
 * @author Peter Lager
 *
 */
public class GWindowNEWT extends GWindow {

	private PMatrix orgMatrix = null;

	protected GWindowNEWT(String title, int w, int h, boolean is3D) {
		super(title, w, h);
		this.is3D = is3D;
		renderer_type = is3D ? P3D : P2D;
	}

	public void setup(){
		super.setup();
		orgMatrix = getMatrix();
	}

	public void draw() {
		super.draw();
		pushMatrix();
		if(is3D) {
			hint(DISABLE_DEPTH_TEST);
			setMatrix(orgMatrix);
		}
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & DRAW_METHOD) == DRAW_METHOD )
				control.draw();
		}		
		if(is3D) hint(ENABLE_DEPTH_TEST);
		popMatrix();
	}

	/**
	 * Remove all existing window listeners added by Processing and add our own custom listener.
	 */
	protected void initListeners(){
		//com.jogamp.newt.opengl.GLWindow newtCanvas = (com.jogamp.newt.opengl.GLWindow) surface.getNative();
		com.jogamp.newt.opengl.GLWindow newtCanvas = getCanvas();
		for(com.jogamp.newt.event.WindowListener l : newtCanvas.getWindowListeners())
			if(l.getClass().getName().startsWith("processing"))
				newtCanvas.removeWindowListener(l);
		newtCanvas.addWindowListener(new WindowAdapterNEWT(this));
	}

	/**
	 * This sets what happens when the users attempts to close the window. <br>
	 * There are 3 possible actions depending on the value passed. <br>
	 * G4P.KEEP_OPEN - ignore attempt to close window (default action), <br>
	 * G4P.CLOSE_WINDOW - close this window,<br>
	 * G4P.EXIT_APP - exit the app, this will cause all windows to close. <br>
	 * @param action the required close action
	 */
	public void setActionOnClose(int action){
		com.jogamp.newt.opengl.GLWindow newtCanvas = getCanvas();	
		if(action == KEEP_OPEN)
			newtCanvas.setDefaultCloseOperation(com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode.DO_NOTHING_ON_CLOSE);
		else
			newtCanvas.setDefaultCloseOperation(com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
		actionOnClose = action;
	}

	private com.jogamp.newt.opengl.GLWindow getCanvas(){
		return (com.jogamp.newt.opengl.GLWindow) surface.getNative();	
	}

	/**
	 * Fire a window closing event
	 */
	protected void fireCloseWindowEvent() {
		getCanvas().destroy();
	}

	/**
	 * Returns true if the window is visible else false
	 */
	public boolean isVisible(){
		return getCanvas().isVisible();
	}

	/**
	 * Get the window title
	 */
	public String getTitle() {
		return getCanvas().getTitle();
	}

	/**
	 * Get the window position.
	 */
	public PVector getPosition(PVector pos) {
		if(pos == null)
			pos = new PVector();
		pos.x = getCanvas().getX();
		pos.y = getCanvas().getY();
		return pos;	
	}

}
