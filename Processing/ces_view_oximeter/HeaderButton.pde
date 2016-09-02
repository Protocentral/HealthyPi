class HeaderButton {

  public float x, y, w, h;
  int padding = 5;

  HeaderButton(float _xPos, float _yPos, float _width, float _height) {
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

    // draw background of header
    fill(0,102,204);
    rect(x, y, width, h);

    //draw background for the buttons to be placed
    strokeWeight(1);
    stroke(color(0, 5, 11));
    fill(color(0, 5, 11));
    
    popStyle();
  }
  
};