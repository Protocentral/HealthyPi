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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * Base class for independent windows using JAVA2D, P2D or P3D renderers. These 
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
public abstract class GWindow extends PApplet implements GConstants, GConstantsInternal {

	/**
	 * Factory method to create and start a new window. The renderer
	 * must be JAVA2D, P2D or P3D otherwise this method returns null.
	 * 
	 * @param title text to appear in frame title bar
	 * @param px horizontal position of top-left corner
	 * @param py vertical position of top-left corner
	 * @param w width of drawing surface
	 * @param h height of surface
	 * @param r renderer must be JAVA2D, P3D or P3D
	 * @return the window created (in case the user wants its.
	 */
	public static GWindow getWindow(PApplet app, String title, int px, int py, int w, int h, String renderer){
		G4P.registerSketch(app);
		GWindow g3w = null;
		if(renderer.equals(JAVA2D))
			g3w = new GWindowAWT(title, w, h);
		else if(renderer.equals(P2D))
			g3w = new GWindowNEWT(title, w, h, false);
		else if(renderer.equals(P3D)){
			g3w = new GWindowNEWT(title, w, h, true);
		}
		if(g3w != null){
			String spath = "--sketch-path=" + G4P.sketchWindow.sketchPath();
			String loc = "--location=" + px + "," + py;
			String className = g3w.getClass().getName();
			String[] args = { spath, loc, className };
			G4P.registerWindow(g3w);
			PApplet.runSketch(args, g3w);
		}
		return g3w; 
	}

	protected int actionOnClose = KEEP_OPEN;

	/** Used to associated data to a GWindow */
	public GWinData data;

	/** Simple tag that can be used by the user */
	public String tag;

	/** Allows user to specify a number for this component */
	public int tagNo;

	protected final int w, h;
	protected final String title;
	protected String renderer_type;
	protected boolean preparedForClosure = false;

	protected GWindow(String title, int w, int h) {
		super();
		this.title = title;
		this.w = w;
		this.h = h;
		registerMethods();
	}

	/**
	 * Register this window for pre, draw, post, mouseEvent and
	 * keyEvent methods. 
	 */
	protected void registerMethods(){
		registerMethod("pre", this);
		registerMethod("draw", this);
		registerMethod("post", this);
		registerMethod("mouseEvent", this);
		registerMethod("keyEvent", this);
	}

	/**
	 * Unregister this window for pre, draw, post, mouseEvent and
	 * keyEvent methods.
	 * This method is called when the window closes.
	 */
	protected void unregisterMethods(){
		unregisterMethod("pre", this);
		unregisterMethod("draw", this);
		unregisterMethod("post", this);
		unregisterMethod("mouseEvent", this);
		unregisterMethod("keyEvent", this);
		// bely and braces
		preHandlerObject = null;
		drawHandlerObject = null;
		postHandlerObject = null;
		mouseHandlerObject = null;
		keyHandlerObject = null;
	}

	/**
	 * To provide a unique fields for this window create a class that inherits
	 * from GWinData with public access fields. Then use this method to associate
	 * the data with this window.
	 * @param data
	 */
	public void addData(GWinData data){
		this.data = data;
	}

	/**
	 * Add a control to this window, ignoring duplicates.
	 * 
	 * @param control control to be added
	 */
	protected void addToWindow(GAbstractControl control){
		// Avoid adding duplicates
		if(!toAdd.contains(control) && !windowControls.contains(control))
			toAdd.add(control);
	}

	/**
	 * Remove a control to this window.
	 * 
	 * @param control control to be removed
	 */
	protected void removeFromWindow(GAbstractControl control){
		toRemove.add(control);
	}

	/**
	 * Set the colour scheme to be used by all controls on this window.
	 * @param cs colour scheme e.g. G4P.GREEN_SCHEME
	 */
	void invalidateBuffers(){
		for(GAbstractControl control : windowControls)
			control.bufferInvalid = true;
	}

	/**
	 * Set the colour scheme to be used by all controls on this window.
	 * @param cs colour scheme e.g. G4P.GREEN_SCHEME
	 */
	void setColorScheme(int cs){
		for(GAbstractControl control : windowControls)
			control.setLocalColorScheme(cs);
	}

	/**
	 * Set the alpha level for all controls on this window. <br>
	 * 0 = fully transparent <br>
	 * 255 = fully opaque <br>
	 * Controls are disabled when alpha gets below G4P.ALPHA_BLOCK (128)
	 * 
	 * @param alpha 0-255 inclusive
	 */
	void setAlpha(int alpha){
		for(GAbstractControl control : windowControls)
			control.setAlpha(alpha);
	}

	/** Set the windows visibility */
	public void setVisible(boolean visible){
		surface.setVisible(visible);
	}
	
	/** Returns the windows visibility */
	public abstract boolean isVisible();

	/** Set the window title */
	public void setTitle(String title){
		surface.setTitle(title);
	}
	/** Get the windowTitle */
	public abstract String getTitle();
	
	/** Set the windows position */
	public void setLocation(int x, int y){
		surface.setLocation(x, y);
	}
	
	/**
	Returns a PVector with the windows top-left coordinates.
	*/
	public abstract PVector getPosition(PVector pos);
	
	/** 
	 * Sets whether this window should always be above other windows. If there are 
	 * multiple always-on-top windows, their relative order is unspecified and 
	 * platform dependent. 
	 */
	public void setAlwaysOnTop(boolean ontop) {
		surface.setAlwaysOnTop(ontop);
	}
	
	/**
	 * Execute any draw handler for this window.
	 */
	public void draw() {
		pushMatrix();
		if(drawHandlerObject != null){
			try {
				drawHandlerMethod.invoke(drawHandlerObject, new Object[] { this, data });
			} catch (Exception e) {
				GMessenger.message(EXCP_IN_HANDLER,  
						new Object[] {drawHandlerObject, drawHandlerMethodName, e} );
			}
		}
		popMatrix();
	}

	/**
	 * Execute any pre handler associated with this window and its controls
	 */
	public void pre(){
		if(preHandlerObject != null){
			try {
				preHandlerMethod.invoke(preHandlerObject, 
						new Object[] { this, data });
			} catch (Exception e) {
				GMessenger.message(EXCP_IN_HANDLER, 
						new Object[] {preHandlerObject, preHandlerMethodName, e} );
			}
		}
		if(GAbstractControl.controlToTakeFocus != null && GAbstractControl.controlToTakeFocus.getPApplet() == this){
			GAbstractControl.controlToTakeFocus.setFocus(true);
			GAbstractControl.controlToTakeFocus = null;
		}
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & PRE_METHOD) == PRE_METHOD)
				control.pre();
		}
	}

	/**
	 * Execute any post handler associated with this window and its controls. <br>
	 * Add/remove any controls request by user, this is done here outside the drawing 
	 * phase to prevent crashes.
	 */
	public void post() {
		if(postHandlerObject != null){
			try {
				postHandlerMethod.invoke(postHandlerObject, 
						new Object[] { this, data });
			} catch (Exception e) {
				GMessenger.message(EXCP_IN_HANDLER, 
						new Object[] {postHandlerObject, postHandlerMethodName, e} );
			}
		}
		if(G4P.cursorChangeEnabled){
			if(GAbstractControl.cursorIsOver != null && GAbstractControl.cursorIsOver.getPApplet() == this)
				cursor(GAbstractControl.cursorIsOver.cursorOver);			
			else 
				cursor(G4P.mouseOff);
		}
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & POST_METHOD) == POST_METHOD)
				control.post();
		}
		// =====================================================================================================
		// =====================================================================================================
		//  This is where components are removed or added to the window to avoid concurrent access violations 
		// =====================================================================================================
		// =====================================================================================================
		synchronized (this) {
			// Dispose of any unwanted controls
			if(!toRemove.isEmpty()){
				for(GAbstractControl control : toRemove){
					// If the control has focus then lose it
					if(GAbstractControl.focusIsWith == control)
						control.loseFocus(null);
					// Clear control resources
					control.buffer = null;
					if(control.parent != null){
						control.parent.children.remove(control);
						control.parent = null;
					}
					if(control.children != null)
						control.children.clear();
					control.palette = null;
					control.eventHandlerObject = null;
					control.eventHandlerMethod = null;
					control.winApp = null;
					windowControls.remove(control);
					System.gc();			
				}
				toRemove.clear();
			}
			if(!toAdd.isEmpty()){
				for(GAbstractControl control : toAdd)
					windowControls.add(control);
				toAdd.clear();
				Collections.sort(windowControls, G4P.zorder);
			}
		}
	}

	/**
	 * Execute any mouse event handler associated with this window and its controls
	 */
	public void mouseEvent(MouseEvent event) {
		if(mouseHandlerObject != null){
			try {
				mouseHandlerMethod.invoke(mouseHandlerObject, new Object[] { this, data, event });
			} catch (Exception e) {
				GMessenger.message(EXCP_IN_HANDLER,
						new Object[] {mouseHandlerObject, mouseHandlerMethodName, e} );
			}
		}
		for(GAbstractControl control : windowControls){
			if((control.registeredMethods & MOUSE_METHOD) == MOUSE_METHOD)
				control.mouseEvent(event);
		}
	}

	/**
	 * Execute any key event handler associated with this window and its controls
	 */
	public void keyEvent(KeyEvent event) {
		if(keyHandlerObject != null){
			try {
				keyHandlerMethod.invoke(keyHandlerObject, new Object[] { this, data, event });
			} catch (Exception e) {
				GMessenger.message(EXCP_IN_HANDLER,
						new Object[] {keyHandlerObject, keyHandlerMethodName, e} );
			}
		}
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & KEY_METHOD) == KEY_METHOD)
				control.keyEvent(event);
		}			
	}

	/**
	 * Attempt to add the 'draw' handler method. 
	 * The default event handler is a method that returns void and has two
	 * parameters PApplet and GWinData
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addDrawHandler(Object obj, String methodName){
		try{
			drawHandlerMethod = obj.getClass().getMethod(methodName, new Class<?>[] {PApplet.class, GWinData.class } );
			drawHandlerObject = obj;
			drawHandlerMethodName = methodName;
		} catch (Exception e) {
			drawHandlerObject = null;
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { PApplet.class, GWinData.class } } );
		}
	}

	/**
	 * Attempt to add the 'pre' handler method. 
	 * The default event handler is a method that returns void and has two
	 * parameters GWinApplet and GWinData
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addPreHandler(Object obj, String methodName){
		try{
			preHandlerMethod = obj.getClass().getMethod(methodName, new Class<?>[] {PApplet.class, GWinData.class } );
			preHandlerObject = obj;
			preHandlerMethodName = methodName;
		} catch (Exception e) {
			preHandlerMethod = null;
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { PApplet.class, GWinData.class } } );
		}
	}

	/**
	 * Attempt to add the 'mouse' handler method. 
	 * The default event handler is a method that returns void and has three
	 * parameters GWinApplet, GWinData and a MouseEvent
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addMouseHandler(Object obj, String methodName){
		try{
			mouseHandlerMethod = obj.getClass().getMethod(methodName, 
					new Class<?>[] {PApplet.class, GWinData.class, MouseEvent.class } );
			mouseHandlerObject = obj;
			mouseHandlerMethodName = methodName;
		} catch (Exception e) {
			mouseHandlerObject = null;
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { PApplet.class, GWinData.class, MouseEvent.class } } );
		}
	}

	/**
	 * Attempt to add the 'key' handler method. 
	 * The default event handler is a method that returns void and has three
	 * parameters GWinApplet, GWinData and a KeyEvent
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addKeyHandler(Object obj, String methodName){
		try{
			keyHandlerMethod = obj.getClass().getMethod(methodName, 
					new Class<?>[] {PApplet.class, GWinData.class, KeyEvent.class } );
			keyHandlerObject = obj;
			keyHandlerMethodName = methodName;
		} catch (Exception e) {
			keyHandlerObject = null;
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { PApplet.class, GWinData.class, KeyEvent.class } } );
		}
	}

	/**
	 * Attempt to add the 'post' handler method. 
	 * The default event handler is a method that returns void and has two
	 * parameters GWinApplet and GWinData
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addPostHandler(Object obj, String methodName){
		try{
			postHandlerMethod = obj.getClass().getMethod(methodName, 
					new Class<?>[] {PApplet.class, GWinData.class } );
			postHandlerObject = obj;
			postHandlerMethodName = methodName;
		} catch (Exception e) {
			postHandlerObject = null;
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { PApplet.class, GWinData.class } } );
		}
	}

	/**
	 * Attempt to create the on-close-window event handler for this GWindow. 
	 * The default event handler is a method that returns void and has a single
	 * parameter of type GWindow (this will be a reference to the window that is
	 * closing) <br/>
	 * 
	 * The handler will <b>not be called</> if the setActionOnClose flag is set 
	 * to EXIT_APP <br/>
	 * If the flag is set to CLOSE_WINDOW then the handler is called when the window
	 * is closed by clicking on the window-close-icon or using either the close or 
	 * forceClose methods. <br/>
	 * If the flag is set to KEEP_OPEN the window can only be closed using the
	 * forceClose method. In this case the handler will be called.
	 * 
	 * 
	 * @param obj the object to handle the on-close-window event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addOnCloseHandler(Object obj, String methodName){
		try{
			closeHandlerObject = obj;
			closetHandlerMethodName = methodName;
			closetHandlerMethod = obj.getClass().getMethod(methodName, new Class<?>[] { PApplet.class, GWinData.class } );
		} catch (Exception e) {
			GMessenger.message(NONEXISTANT, new Object[] {this, methodName, new Class<?>[] { PApplet.class, GWinData.class } } );
			closeHandlerObject = null;
			closetHandlerMethodName = "";
		}
	}

	/**
	 * This will close the window provided the action-on-close flag is CLOSE_WINDOW
	 * otherwise the window remains open.
	 */
	public void close(){
		if(actionOnClose == KEEP_OPEN || actionOnClose == EXIT_APP ) return;
		prepareWindowForClosure();
		//G4P.markForWindowClosure(this);
	}

	/**
	 * This will close the window provided the action-on-close flag is CLOSE_WINDOW
	 * or KEEP_OPEN otherwise the window remains open.
	 */
	public void forceClose(){
		if(actionOnClose == EXIT_APP) return;
		if(actionOnClose == KEEP_OPEN)
			setActionOnClose(CLOSE_WINDOW);
		prepareWindowForClosure();
		//G4P.markForWindowClosure(this);
	}
	
	/**
	 * Fire a window closing event
	 */
	protected abstract void fireCloseWindowEvent();
	

	/**
	 * Prepare the window for closure by stopping the animation loop, unregistering
	 * the event handlers and firing a window close event using a new thread.
	 */
	protected void prepareWindowForClosure(){
		noLoop();
		unregisterMethods();
		G4P.deregisterWindow(this);
		(new Thread(new GCloseNotifier(this))).start();
	}

	/**
	 * This sets what happens when the users attempts to close the window. <br>
	 * There are 3 possible actions depending on the value passed. <br>
	 * G4P.KEEP_OPEN - ignore attempt to close window (default action), <br>
	 * G4P.CLOSE_WINDOW - close this window,<br>
	 * G4P.EXIT_APP - exit the app, this will cause all windows to close. <br>
	 * @param action the required close action
	 */
	public abstract void setActionOnClose(int action);

	public void settings() {
		size(w, h, renderer_type);
	}

	/**
	 * Set up the 'window' listeners
	 */
	protected abstract void initListeners();

	public void setup(){
		surface.setTitle(title); // does not like this in settings	
		initListeners();
	}

	/**
	 * This method is executed when the window closes. It will call the user defined
	 * on-close-handler method set with 
	 */
	public void performCloseAction(){
		if(closeHandlerObject != null){
			try {
				closetHandlerMethod.invoke(closeHandlerObject, 
						new Object[] { this, data });
			} catch (Exception e) {
				GMessenger.message(EXCP_IN_HANDLER, 
						new Object[] {preHandlerObject, preHandlerMethodName, e} );
			}
		}
	}

	/**
	 * This class is used to call the fireCloseWindowEvent in the appropriate
	 * GWindow sub-class.
	 * @author Peter Lager
	 *
	 */
	protected class GCloseNotifier implements Runnable {
		
		GWindow window = null;

		public GCloseNotifier(GWindow window) {
			super();
			this.window = window;
		}

		@Override
		public void run() {
			if(window != null){
				window.fireCloseWindowEvent();
			}
		}
		
	}
	
	/**
	 * Window adapter class for the JAVA2D renderer
	 * 
	 * @author Peter Lager
	 */
	protected class WindowAdapterAWT extends WindowAdapter {
		GWindow window = null;

		public WindowAdapterAWT(GWindow window){
			this.window = window;
		}

		public void windowClosing(WindowEvent evt) {
			switch(actionOnClose){
			case EXIT_APP:
				System.exit(0);
				break;
			case CLOSE_WINDOW:
				performCloseAction();
				G4P.deregisterWindow(window);
				dispose();
				break;
			}
		}
	}

	/**
	 * Window adapter class for the P2D and P3D renderers
	 * 
	 * @author Peter Lager
	 */
	protected class WindowAdapterNEWT extends com.jogamp.newt.event.WindowAdapter {

		GWindow window = null;

		public WindowAdapterNEWT(GWindow window){
			this.window = window;
		}
		public void windowGainedFocus(com.jogamp.newt.event.WindowEvent arg0) {
			focused = true;
			focusGained();
		}

		public void windowLostFocus(com.jogamp.newt.event.WindowEvent arg0) {
			focused = false;
			focusLost();
		}

		public void windowDestroyNotify(com.jogamp.newt.event.WindowEvent arg0) {
			switch(actionOnClose){
			case EXIT_APP:
				// Next two come from processing.opengl PSurfaceJOGL.java
				// performCloseAction();
				//dispose();
				exitActual();
				break;
			case CLOSE_WINDOW:
				performCloseAction();
				G4P.deregisterWindow(window);
				// Next one comes from processing.opengl PSurfaceJOGL.java
				dispose();
				break;
			}
		}

	}

	public LinkedList<GAbstractControl> windowControls = new LinkedList<GAbstractControl>();
	// These next two lists are for controls that are to be added or remove since these
	// actions must be performed outside the draw cycle to avoid concurrent modification
	// exceptions when changing windowControls
	public LinkedList<GAbstractControl> toRemove = new LinkedList<GAbstractControl>();
	public LinkedList<GAbstractControl> toAdd = new LinkedList<GAbstractControl>();

	/** The object to handle the pre event */
	protected Object preHandlerObject = null;
	/** The method in preHandlerObject to execute */
	protected Method preHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String preHandlerMethodName;

	/** The object to handle the post event */
	protected Object postHandlerObject = null;
	/** The method in postHandlerObject to execute */
	protected Method postHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String postHandlerMethodName;

	/** The object to handle the draw event */
	protected Object drawHandlerObject = null;
	/** The method in drawHandlerObject to execute */
	protected Method drawHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String drawHandlerMethodName;

	/** The object to handle the key event */
	protected Object keyHandlerObject = null;
	/** The method in keyHandlerObject to execute */
	protected Method keyHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String keyHandlerMethodName;

	/** The object to handle the mouse event */
	protected Object mouseHandlerObject = null;
	/** The method in mouseHandlerObject to execute */
	protected Method mouseHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String mouseHandlerMethodName;

	/** The object to handle the window closing event */
	protected Object closeHandlerObject = null;
	/** The method in closeHandlerObject to execute */
	protected Method closetHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String closetHandlerMethodName;

	protected boolean is3D;
}
