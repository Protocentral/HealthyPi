//////////////////////////////////////////////////////////////////////////////
//
//    HelpWidget 
//      - This class differentiate the footer from the whole application
//      - Shows the status and threshold values of the alarm
//
//    Created : Balasundari, Jul 2016
//
//////////////////////////////////////////////////////////////////////////////

class HelpWidget {

  public float x, y, w, h;                                     // location of the top left corner of the widget with width and height
  int padding = 5;                                             // Constant value to fix the position of the Label

  ////////////////////////////////////////////////////////////////////////////////
  //
  //  This Constructor gets the co-ordinate points likes x, y, width and height
  //  The values are passed from the setup function in the main class 
  //    and it is set in this constructor
  //
  ///////////////////////////////////////////////////////////////////////////////


  HelpWidget(float _xPos, float _yPos, float _width, float _height) {
    x = _xPos;
    y = _yPos;
    w = _width;
    h = _height;
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  //  This draw method is called repeatedly by the main draw function
  //  This function creates a rectangle which shows the differentiation like a Footer.
  //  Functions:
  //    - text(string txt,x,y)     :  Draws text to the screen. 
  //                                  Displays the information specified in the first parameter on the screen in the position specified by the additional parameters
  //    - textSize(int size)       :  Sets the current font size
  //    - fill(int color)          :  Sets the color used to fill shapes 
  //    - rect(x,y,w,h)            :  Draws a rectangle to the screen in the position specified in the parameters.
  //    - stroke(int color)        :  Sets the color used to draw lines and borders around shapes.
  //    - strokeWeight(int value)  :  Sets the width of the stroke used for lines, points, and the border around shapes.
  //    - noStroke()               :  Disables drawing the stroke (outline)
  //    - pushStyle()              :  Saves the current style settings
  //    - popStyle()               :  Restores the prior settings
  //
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public void draw() {

    pushStyle();
    noStroke();
    fill(0, 102, 204);
    rect(x, height-h, width, h);
    strokeWeight(1);
    stroke(color(0, 5, 11));
    fill(color(0, 5, 11));
    rect(x + padding, height-h + padding, width - padding*23, h - padding *2);
    popStyle();
  }
};