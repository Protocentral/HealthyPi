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

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import processing.core.PVector;

/**
 * Class for independent windows using the JAVA2D renderer. These 
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
public class GWindowAWT extends GWindow{

	protected GWindowAWT(String title, int w, int h) {
		super(title, w, h);
		is3D = false;
		renderer_type = JAVA2D;
	}

	public void draw() {
		super.draw();
		pushMatrix();
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & DRAW_METHOD) == DRAW_METHOD )
				control.draw();
		}		
		popMatrix();
	}

	/**
	 * Remove all existing window listeners and add our own custom listener.
	 */
	protected void initListeners(){
		Frame awtCanvas = getCanvas();
		for(WindowListener l : awtCanvas.getWindowListeners())
			awtCanvas.removeWindowListener(l);
		awtCanvas.addWindowListener(new WindowAdapterAWT(this));
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
		JFrame awtCanvas = getCanvas();
		if(action == KEEP_OPEN)
			awtCanvas.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		else
			awtCanvas.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		actionOnClose = action;
	}

	private JFrame getCanvas(){
		return (JFrame) ((processing.awt.PSurfaceAWT.SmoothCanvas) surface.getNative()).getFrame();
	}

	/**
	 * Fire a window closing event. This method will be called from a new THread, do not call directly
	 */
	protected void fireCloseWindowEvent(){
		Frame awtCanvas = getCanvas();
		awtCanvas.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(awtCanvas, WindowEvent.WINDOW_CLOSING));
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
	 * Get the window position
	 */
	public PVector getPosition(PVector pos) {
		if(pos == null)
			pos = new PVector();
		Point p = getCanvas().getLocation();
		pos.x = p.x;
		pos.y = p.y;
		return pos;
	}

}
