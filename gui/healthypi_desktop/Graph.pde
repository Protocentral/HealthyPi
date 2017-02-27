//////////////////////////////////////////////////////////////////////////////////////////
//
//   Graph Class
//      - Draw Axes Lines
//      - Set the Limits for each axes
//      - Function to plot the current pressure value
//
//   Created : Balasundari, Jul 2016
//   
/////////////////////////////////////////////////////////////////////////////////////////

class Graph
{
  int xDiv=5, yDiv=5;                                        // Number of sub divisions
  int xPos, yPos;                                            // location of the top left corner of the graph  
  int Width, Height;                                         // Width and height of the graph
  color GraphColor;                                        // Color for the trace
  float yMax1=10, yMin1=-10;                                 // Default axis dimensions
  float xMax1=10, xMin1=0;
  PFont Font;                                              // Selected font used for text 

  /******************** The main declaration function ************************************/

  Graph(int x, int y, int w, int h) {               
    xPos = x;
    yPos = y;
    Width = w;
    Height = h;
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  //  The DrawAxis() is called repeatedly by the main draw function
  //  This function draw x axis with its respective axis points.
  //  Functions:
  //    - text(string txt,x,y)     :  Draws text to the screen. 
  //                                  Displays the information specified in the first parameter on the screen in the position specified by the additional parameters
  //    - fill(int color)          :  Sets the color used to fill shapes 
  //    - rect(x,y,w,h)            :  Draws a rectangle to the screen in the position specified in the parameters.
  //    - stroke(int color)        :  Sets the color used to draw lines and borders around shapes.
  //    - strokeWeight(int value)  :  Sets the width of the stroke used for lines, points, and the border around shapes.
  //    - noStroke()               :  Disables drawing the stroke (outline)
  //    - noFill()                 :  Disables filling geometry.
  //    - smooth()                 :  Draws all geometry with smooth (anti-aliased) edges.
  //    - line(x1,y1,x2,y2)        :  Draws a line (a direct path between two points) to the screen. The two points are taken from the parameter.
  //    - pushStyle()              :  Saves the current style settings
  //    - popStyle()               :  Restores the prior settings
  ////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  void DrawAxis() {

    if (gStatus)
    {
      int t=60;
      fill(255);
      textAlign(CENTER);
      textSize(14);
      text("Sweep Speed 25 mm/s", xPos+Width/2, yPos+Height+t/2);                     // x-axis Label 
      smooth();
      strokeWeight(1);
      stroke(255);
      line(xPos, yPos+Height, xPos, yPos);                        // y-axis line 
      line(xPos, yPos+Height, xPos+Width-15, yPos+Height);           // x-axis line 
      stroke(255);
      float xPoint = xPos;
      for (int x=0; x<14; x++) {
        stroke(150);
        xPoint = xPoint + 40;
        line(xPoint, yPos, xPoint, yPos+Height);     
        textSize(10);                                      // x-axis Labels
        String xAxis=str((xMin1+float(x)/xDiv*(xMax1-xMin1))); // the only way to get a specific number of decimals 
        String[] xAxisMS=split(xAxis, '.');                 // is to split the float into strings
      }
      for (int y=0; y<=yDiv; y++) {
        stroke(150);
        line(xPos, float(y)/yDiv*Height+yPos, xPos+Width-15, float(y)/yDiv*Height+yPos);              // y-axis lines 
        textAlign(RIGHT);
        fill(255);
        String yAxis=str(yMin1+float(y)/yDiv*(yMax1-yMin1));     // Make y Label a string
        String[] yAxisMS=split(yAxis, '.');                    // Split string
      }
      stroke(0);
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  //  The LineGraph() is called repeatedly by the main draw function
  //  This module trace the graph with the buffer values passed from the main function
  //  Functions:
  //    - stroke(int color)        :  Sets the color used to draw lines and borders around shapes.
  //    - strokeWeight(int value)  :  Sets the width of the stroke used for lines, points, and the border around shapes.
  //    - smooth()                 :  Draws all geometry with smooth (anti-aliased) edges.
  //    - line(x1,y1,x2,y2)        :  Draws a line (a direct path between two points) to the screen. The two points are taken from the parameter.
  //    - rect(x,y,w,h)            :  Draws a rectangle to the screen in the position specified in the parameters.
  //    - fill(int color)          :  Sets the color used to fill shapes
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  void LineGraph(float[] x, float[] y) {

    for (int i=0; i<(x.length-1); i++)
    {
      smooth();
      strokeWeight(3);
      stroke(GraphColor);
      // x & y points
      line(xPos+(x[i]-x[0])/(x[x.length-1]-x[0])*Width, 
        yPos+(Height)-(y[i]/(yMax1-yMin1)*Height)+(yMin1)/(yMax1-yMin1)*Height, 
        xPos+(x[i+1]-x[0])/(x[x.length-1]-x[0])*Width, 
        yPos+(Height)-(y[i+1]/(yMax1-yMin1)*Height)+(yMin1)/(yMax1-yMin1)*Height);
    }

    stroke(0);
    fill(0);
    rect(xPos-22+((time-2)-x[0])/(x[x.length-1]-x[0])*Width, 0, 30, height);    
  }
}