/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2012 Peter Lager

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

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Defines a number of color schemes for the GUI components. <br/>
 * G4P supports 16 colour schemes and each scheme has a <b>palette</b> of 16 colours. <br/>
 * 
 * When G4P is used it loads an image file with all the colors used by the various colour schemes. <br/>
 * First it will search for a file containing a user defined scheme (user_gui_palette.png) and
 * if it can't find it, will use the library default scheme (default_gui_palette.png).
 * 
 * @author Peter Lager
 *
 */
public class GCScheme implements GConstants, PConstants {

	private static Color[][] palettes = null;

	/**
	 * Set the color scheme to one of the preset schemes
	 * BLUE / GREEN / RED /  PURPLE / YELLOW / CYAN / BROWN
	 * or if you have created your own schemes following the instructions
	 * at gui4processing.lagers.org.uk/colorscheme.html then you can enter
	 * the appropriate numeric value of the scheme.
	 * 
	 * @param schemeNbr scheme number (0-15)
	 * @return the color scheme based on the scheme number
	 */
	public static int[] getPalette(int schemeNbr){
		schemeNbr = Math.abs(schemeNbr) % 16;
		Color[] colScheme = palettes[schemeNbr];
		int[] colSchemeI = new int[16];
		for(int i = 0; i < 16; i++)
			colSchemeI[i] = colScheme[i].getRGB();
		return colSchemeI;
	}

	/**
	 * Change a colour scheme to use the colours passed in the third parameter. <br>
	 * Colour scheme numbers 0-7 inclusive are the default colour schemes and 
	 * schemes 8-15 inclusive are undefined by G4P. <br>
	 * This method will override the previous scheme and will affect all controls 
	 * using the scheme.
	 * 
	 * @param schemeNbr the scheme number
	 * @param colors the colours to use in this palette.
	 */
	public static void changePalette(int schemeNbr, int[] colors){
		schemeNbr = Math.abs(schemeNbr) % 16;
		for(int i = 0; i < Math.min(16, colors.length); i++)
			palettes[schemeNbr][i] = new Color(colors[i], true); // keep alpha
		G4P.invalidateBuffers();
	}
	
	/**
	 * Copies the colours from the source scheme to the destination scheme.
	 * 
	 * @param srcSchemeNbr source scheme number (0-15)
	 * @param dstSchemeNbr destination scheme number (0-15)
	 */
	public static void copyPalette(int srcSchemeNbr, int dstSchemeNbr){
		srcSchemeNbr = Math.abs(srcSchemeNbr) % 16;
		dstSchemeNbr = Math.abs(dstSchemeNbr) % 16;
		if(srcSchemeNbr != dstSchemeNbr){
			int[] palette = getPalette(srcSchemeNbr);
			changePalette(dstSchemeNbr, palette);
		}
	}
	
	/**
	 * Change a single colour within an existing scheme
	 * 
	 * @param schemeNbr the scheme number
	 * @param colorNbr the palette index number for the colour
	 * @param color ARGB colour value
	 */
	public static void changePaletteColor(int schemeNbr, int colorNbr, int color){
		schemeNbr = Math.abs(schemeNbr) % 16;
		colorNbr = Math.abs(colorNbr) % 16;
		palettes[schemeNbr][colorNbr] = new Color(color, true); // keep alpha
		G4P.invalidateBuffers();
	}
	
	/**
	 * Save the current colour schemes as an image in the sketch's data folder. The file will be 
	 * called <pre>"user_gui_palette.png"</pre>
	 * 
	 * @param app the PApplet object
	 */
	public static void savePalettes(PApplet app){
		savePalettes(app, "user_gui_palette.png");
	}
	
	/**
	 * Save the current colour schemes as an image in the sketch's data folder.
	 * 
	 * @param app the PApplet object
	 * @param filename the name of the image file to use
	 */
	public static void savePalettes(PApplet app, String filename){
		PGraphics pg = app.createGraphics(256, 256, JAVA2D);
		pg.beginDraw();
		pg.clear();
		pg.noStroke();
		for(int scheme = 0; scheme < 16; scheme++){
			for(int idx = 0; idx < 16; idx++){
				pg.fill(palettes[scheme][idx].getRGB());
				pg.rect(idx * 16, scheme * 16, 16,16);
			}
		}
		pg.noFill();
		pg.strokeWeight(2);
		pg.stroke(0);
		for(int i = 0; i <= 16; i++){
			int m = i * 16;
			pg.line(0, m, 256, m);
			pg.line(m, 0, m, 256);
		}
		pg.endDraw();
		filename = app.dataPath("") + "/" + filename;
		System.out.println(filename);
		pg.save(filename);
	}
	
	
	/**
	 * Called every time we create a control. The palettes will be made when 
	 * the first control is created.
	 * 
	 * This method is called by
	 * @param app the PApplet using this scheme
	 */
	public static void makeColorSchemes(PApplet app) {
		// If the palettes have not been created then create them
		// otherwise do nothing
		if(palettes != null)
			return;
		// Load the image
		PImage image = null;;
		InputStream is = app.createInput("user_gui_palette.png");
		if(is != null){
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			image = app.loadImage("user_gui_palette.png");
			GMessenger.message(USER_COL_SCHEME, null);
		}
		else {
			// User image not provided
			image = app.loadImage("default_gui_palette.png");
			// Added to 3.4 to hopefully fix problem with OpenProcessing
			if(image == null)
				image = new PImage((new javax.swing.ImageIcon(new GCScheme().getClass().getResource("/data/default_gui_palette.png"))).getImage());
		}
		// Now make the palette
		palettes = new Color[16][16];
		for(int p = 0; p < 16; p++)
			for(int c = 0; c < 16; c++){
				int col =  image.get(c * 16 + 8, p * 16 + 8);
				palettes[p][c] = new Color((col >> 16) & 0xff, (col >> 8) & 0xff, col & 0xff);
			}
	}

	/*
	 * The following methods are ONLY called by GUI Builder do not change their names.
	 */
	
	/**
	 * DO NOT CALL THIS METHOD<br>
	 * 
	 * This method is only to be used by GUI Builder.
	 */
	public static Color[] getJavaColor(int schemeNo){
		schemeNo = Math.abs(schemeNo) % 16;
		return palettes[schemeNo];
	}


	/**
	 * DO NOT CALL THIS METHOD<br>
	 * 
	 * This method is only to be used by GUI Builder.
	 */
	public static void makeColorSchemes() {
		// If the palettes have not been created then create them
		// otherwise do nothing
		if(palettes != null)
			return;
		// Load the image
		PImage image = new PImage((new javax.swing.ImageIcon(new GCScheme().getClass().getResource("/data/default_gui_palette.png"))).getImage());
		// Now make the palette
		palettes = new Color[16][16];
		for(int p = 0; p < 16; p++)
			for(int c = 0; c < 16; c++){
				int col =  image.get(c * 16 + 8, p * 16 + 8);
				palettes[p][c] = new Color((col >> 16) & 0xff, (col >> 8) & 0xff, col & 0xff);
			}
	}

}
