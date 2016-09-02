class Graph
{

  boolean Dot=true;            // Draw dots at each data point if true
  boolean RightAxis;            // Draw the next graph using the right axis if true
  boolean ErrorFlag=false;      // If the time array isn't in ascending order, make true  
  boolean ShowMouseLines=true;  // Draw lines and give values of the mouse position

  int     xDiv=5, yDiv=5;            // Number of sub divisions
  int     xPos, yPos;            // location of the top left corner of the graph  
  int     Width, Height;         // Width and height of the graph


  color   GraphColor;
  color   BackgroundColor=color(255);  
  color   StrokeColor=color(180);     

  String  Title="Title";          // Default titles
  String  xLabel="x - Label";
  String  yLabel="y - Label";

  float   yMax, yMin;      // Default axis dimensions
  float   xMax=10, xMin=0;
  float   yMaxRight=1024, yMinRight=0;

  PFont   Font;                   // Selected font used for text 

  Graph(int x, int y, int w, int h) {  // The main declaration function
    xPos = x;
    yPos = y;
    Width = w;
    Height = h;
  }

  void DrawAxis() {



    fill(0); 
    color(0);
    stroke(0);
    strokeWeight(1);
    int t=60;

    // rect(xPos-t*1.6,yPos-t,Width+t*2.5,Height+t*2);            // outline
    textAlign(CENTER);
    textSize(25);

    fill(255);
    // text(Title,xPos+Width/5,yPos+20);                            // Heading Title
    textAlign(CENTER);
    textSize(14);
    text("No.Of Samples  -  "+Title, xPos+Width/2, yPos+Height+t/1.5);                     // x-axis Label 

    rotate(-PI/2);                                               // rotate -90 degrees
    text("", -yPos-Height/2, xPos-t*1.6+40);                   // y-axis Label  
    rotate(PI/2);                                                // rotate back
    fill(255);
    textSize(10); 
    noFill(); 
    stroke(0); 
    smooth();
    strokeWeight(1);
    //Edges
    stroke(255);
    line(xPos-3, yPos+Height, xPos-3, yPos);                        // y-axis line 
    line(xPos-3, yPos+Height, xPos+Width+5, yPos+Height);           // x-axis line 

    stroke(200);

    if (yMin<0) {
      line(xPos-7, // zero line 
        yPos+Height-(abs(yMin)/(yMax-yMin))*Height, // 
        xPos+Width, 
        yPos+Height-(abs(yMin)/(yMax-yMin))*Height
        );
    }

    stroke(255);

    for (int x=0; x<=xDiv; x++) {

      line(float(x)/xDiv*Width+xPos-3, yPos+Height, //  x-axis Sub devisions    
        float(x)/xDiv*Width+xPos-3, yPos+Height+5);     

      textSize(10);                                      // x-axis Labels

      String xAxis=str((xMin+float(x)/xDiv*(xMax-xMin))); // the only way to get a specific number of decimals 
      String[] xAxisMS=split(xAxis, '.');                 // is to split the float into strings 
      text(xAxisMS[0]+"."+xAxisMS[1].charAt(0), // ...
        float(x)/xDiv*Width+xPos-3, yPos+Height+15);   // x-axis Labels
    }

    for (int y=0; y<=yDiv; y++) {
      line(xPos-3, float(y)/yDiv*Height+yPos, // ...
        xPos-7, float(y)/yDiv*Height+yPos);              // y-axis lines 

      textAlign(RIGHT);
      fill(255);

      String yAxis=str(yMin+float(y)/yDiv*(yMax-yMin));     // Make y Label a string
      String[] yAxisMS=split(yAxis, '.');                    // Split string
      //println(yAxisMS);
      text(yAxisMS[0]+"."+yAxisMS[1], // ... 
        xPos-15, float(yDiv-y)/yDiv*Height+yPos+3);       // y-axis Labels
    }
    stroke(0);
  }
  void LineGraph(float[] x, float[] y) {

    for (int i=0; i<(x.length-1); i++)
    {
      smooth();
      strokeWeight(3);
      stroke(GraphColor);
      line(xPos+(x[i]-x[0])/(x[x.length-1]-x[0])*Width, 
        yPos+(Height)-(y[i]/(yMax-yMin)*Height)+(yMin)/(yMax-yMin)*Height, 
        xPos+(x[i+1]-x[0])/(x[x.length-1]-x[0])*Width, 
        yPos+(Height)-(y[i+1]/(yMax-yMin)*Height)+(yMin)/(yMax-yMin)*Height);
     
    }
    
    stroke(0);
    fill(0);
    rect(xPos-22+((time-2)-x[0])/(x[x.length-1]-x[0])*Width,0,50,height);    
  }
}