//////////////////////////////////////////////////////////////////////////////
//
//    HeadWidgets 
//      - This class differentiate the header from the whole application
//
//    Created: Balasundari, Jul 2016
//
//////////////////////////////////////////////////////////////////////////////

class HeaderButton {

  public float x, y, w, h;
  int padding = 5;

  ////////////////////////////////////////////////////////////////////////////////
  //
  //  This Constructor gets the co-ordinate points likes x, y, width and height
  //  The values are passed from the setup function in the main class 
  //    and it is set in this constructor
  //
  ///////////////////////////////////////////////////////////////////////////////

  HeaderButton(float _xPos, float _yPos, float _width, float _height) {
    x = _xPos;
    y = _yPos;
    w = _width;
    h = _height;
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  //  This draw method is called repeatedly by the main draw function
  //  This function creates a rectangle which shows the differentiation like a Header.  
  //  InBuilt Functions:
  //    - fill(int color)          :  Sets the color used to fill shapes 
  //    - rect(x,y,w,h)            :  Draws a rectangle to the screen in the position specified in the parameters.
  //    - noStroke()               :  Disables drawing the stroke (outline)
  //    - pushStyle()              :  Saves the current style settings
  //    - popStyle()               :  Restores the prior settings
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public void draw() {

    pushStyle();
    noStroke();
    fill(255, 255, 255);
    rect(x, y, width, h);
    popStyle();
  }
};