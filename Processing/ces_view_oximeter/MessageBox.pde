class MessageBox {

  public float x, y, w, h;
  boolean colorValue = true;

  String Mini = "0.0";
  String Max = "0.0";
  String Avg = "0.0";
  String RMS = "0.0";
  String MP = "0.0";
  String MPDATE = ""+new Date();
  int padding = 5;

  void MessageBoxAxis(float _xPos, float _yPos, float _width, float _height) {
    x = _xPos;
    y = _yPos;
    w = _width;
    h = _height;
  }

  public void update() {
  }

  public void draw() {

    pushStyle();
    noStroke();
    strokeWeight(1);
    stroke(color(0, 5, 11));
    fill(color(0));
    rect(x + padding, y + padding, w - padding*2, h - padding *2);

    fill(255);
    textAlign(LEFT, TOP);
    textFont(createFont("Arial Bold", 18)); 


    text("Min", width/3.5, y + padding + 4);
    text(": "+Mini+" Pounds", width/3, y + padding + 4);
    text("Peak", width/1.7, y + padding + 4);
    text(": "+Max+" Pounds", width/1.55, y + padding + 4);
    
    
    text("Avg", width/3.5, y + padding + 34);
    text(": "+Avg+" Pounds", width/3, y + padding + 34);
    text("RMS", width/1.7, y + padding + 34);
    text(": "+RMS+" Pounds", width/1.55, y + padding + 34); 
    
    popStyle();
  }
  public void msg(BigDecimal mini, BigDecimal max, BigDecimal avg, BigDecimal rms) {  

    Mini = mini+"";
    Max = max+"";
    Avg = avg+"";
    RMS = rms+"";
    if (Mini.length() >= 10)
    {
      Mini = Mini.substring(0, 10);
    }
    if (Max.length() >= 10)
    {
      Max = Max.substring(0, 10);
    }
    if (Avg.length() >= 10)
    {
      Avg = Avg.substring(0, 10);
    }
    if (RMS.length() >= 10)
    {
      RMS = RMS.substring(0, 10);
    }
    // prevOutputs.add(_output);
  }
};

/*public void msg(BigDecimal mini, BigDecimal max, BigDecimal avg, BigDecimal rms) {
 msgBox.msg(mini, max, avg, rms);
 }*/