package g4p_controls;

import java.util.Collections;
import java.util.LinkedList;

import processing.core.PApplet;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * This class calls the appropriate methods for G4P controls for the main applet window.
 * 
 * This will be created when the first control is added to the main window.
 * 
 * @author Peter Lager
 *
 */
public class GWindowImpl implements GConstants, GConstantsInternal {

	public LinkedList<GAbstractControl> windowControls = new LinkedList<GAbstractControl>();
	// These next two lists are for controls that are to be added or remove since these
	// actions must be performed outside the draw cycle to avoid concurrent modification
	// exceptions when changing windowControls
	public LinkedList<GAbstractControl> toRemove = new LinkedList<GAbstractControl>();
	public LinkedList<GAbstractControl> toAdd = new LinkedList<GAbstractControl>();


	PApplet app;
	PMatrix orgMatrix = null;

	public GWindowImpl(PApplet app){
		this.app = app;
		PMatrix mat = app.getMatrix();
		if(mat instanceof PMatrix3D)
			orgMatrix = mat;
		registerMethods();
	}

	protected void registerMethods(){
		app.registerMethod("pre", this);
		app.registerMethod("draw", this);
		app.registerMethod("post", this);
		app.registerMethod("mouseEvent", this);
		app.registerMethod("keyEvent", this);
	}

	protected void unregisterMethods(){
		app.unregisterMethod("pre", this);
		app.unregisterMethod("draw", this);
		app.unregisterMethod("post", this);
		app.unregisterMethod("mouseEvent", this);
		app.unregisterMethod("keyEvent", this);
	}

	protected void addToWindow(GAbstractControl control){
		// Avoid adding duplicates
		if(!toAdd.contains(control) && !windowControls.contains(control)){
			toAdd.add(control);
		}
	}

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

	public void draw() {
		app.pushMatrix();
		if(orgMatrix != null)
			app.setMatrix(orgMatrix);
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & DRAW_METHOD) == DRAW_METHOD )
				control.draw();
		}		
		app.popMatrix();
	}

	/**
	 * The mouse method registered with Processing
	 * 
	 * @param event
	 */
	public void mouseEvent(MouseEvent event){
		for(GAbstractControl control : windowControls){
			if((control.registeredMethods & MOUSE_METHOD) == MOUSE_METHOD)
				control.mouseEvent(event);
		}
	}

	/**
	 * The key method registered with Processing
	 */	
	public void keyEvent(KeyEvent event) {
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & KEY_METHOD) == KEY_METHOD)
				control.keyEvent(event);
		}			
	}

	/**
	 * The pre method registered with Processing
	 */
	public void pre(){
		if(GAbstractControl.controlToTakeFocus != null && GAbstractControl.controlToTakeFocus.getPApplet() == app){
			GAbstractControl.controlToTakeFocus.setFocus(true);
			GAbstractControl.controlToTakeFocus = null;
		}
		for(GAbstractControl control : windowControls){
			if( (control.registeredMethods & PRE_METHOD) == PRE_METHOD)
				control.pre();
		}
	}

	public void post() {
		//System.out.println("POST");
		if(G4P.cursorChangeEnabled){
			if(GAbstractControl.cursorIsOver != null && GAbstractControl.cursorIsOver.getPApplet() == app)
				app.cursor(GAbstractControl.cursorIsOver.cursorOver);			
			else 
				app.cursor(G4P.mouseOff);
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
}
