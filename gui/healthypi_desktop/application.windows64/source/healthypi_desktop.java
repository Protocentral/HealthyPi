import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import g4p_controls.*; 
import processing.serial.*; 
import grafica.*; 
import java.awt.*; 
import javax.swing.*; 
import static javax.swing.JOptionPane.*; 
import javax.swing.JFileChooser; 
import java.io.FileWriter; 
import java.io.BufferedWriter; 
import java.util.Date; 
import java.text.DateFormat; 
import java.text.SimpleDateFormat; 
import java.math.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class healthypi_desktop extends PApplet {

//////////////////////////////////////////////////////////////////////////////////////////
//
//   GUI for controlling the Healthy Pi Hat [ Patient Monitoring System]
//
//   Created: Balasundari, Jul 2016
//
//   Requires g4p_control graphing library for processing.  Built on V4.1
//   Downloaded from Processing IDE Sketch->Import Library->Add Library->G4P Install
//
/////////////////////////////////////////////////////////////////////////////////////////

                       // Processing GUI Library to create buttons, dropdown,etc.,
                  // Serial Library


// Java Swing Package For prompting message




// File Packages to record the data into a text file




// Date Format




// General Java Package


/************** Packet Validation  **********************/
private static final int CESState_Init = 0;
private static final int CESState_SOF1_Found = 1;
private static final int CESState_SOF2_Found = 2;
private static final int CESState_PktLen_Found = 3;

/*CES CMD IF Packet Format*/
private static final int CES_CMDIF_PKT_START_1 = 0x0A;
private static final int CES_CMDIF_PKT_START_2 = 0xFA;
private static final int CES_CMDIF_PKT_STOP = 0x0B;

/*CES CMD IF Packet Indices*/
private static final int CES_CMDIF_IND_LEN = 2;
private static final int CES_CMDIF_IND_LEN_MSB = 3;
private static final int CES_CMDIF_IND_PKTTYPE = 4;
private static int CES_CMDIF_PKT_OVERHEAD = 5;

/************** Packet Related Variables **********************/

int ecs_rx_state = 0;                                        // To check the state of the packet
int CES_Pkt_Len;                                             // To store the Packet Length Deatils
int CES_Pkt_Pos_Counter, CES_Data_Counter;                   // Packet and data counter
int CES_Pkt_PktType;                                         // To store the Packet Type
char CES_Pkt_Data_Counter[] = new char[1000];                // Buffer to store the data from the packet
char CES_Pkt_ECG_Counter[] = new char[4];                    // Buffer to hold ECG data
char CES_Pkt_Resp_Counter[] = new char[4];                   // Respiration Buffer
char CES_Pkt_SpO2_Counter_RED[] = new char[4];               // Buffer for SpO2 RED
char CES_Pkt_SpO2_Counter_IR[] = new char[4];                // Buffer for SpO2 IR
int pSize = 500;                                            // Total Size of the buffer
int arrayIndex = 0;                                          // Increment Variable for the buffer
float time = 0;                                              // X axis increment variable

// Buffer for ecg,spo2,respiration,and average of thos values
float[] xdata = new float[pSize];
float[] ecgdata = new float[pSize];
float[] respdata = new float[pSize];
float[] bpmArray = new float[pSize];
float[] ecg_avg = new float[pSize];                          
float[] resp_avg = new float[pSize];
float[] spo2data = new float[pSize];
float[] spo2Array_IR = new float[pSize];
float[] spo2Array_RED = new float[pSize];
float[] rpmArray = new float[pSize];
float[] ppgArray = new float[pSize];

/************** User Defined Class Objects **********************/

HelpWidget helpWidget;
HeaderButton headerButton;
BPM hr;
RPM rpm1;
SPO2_cal s;

/************** Graph Related Variables **********************/

double maxe, mine, maxr, minr, maxs, mins;             // To Calculate the Minimum and Maximum of the Buffer
double ecg, resp, spo2_ir, spo2_red, spo2, redAvg, irAvg, ecgAvg, resAvg;  // To store the current ecg value
double respirationVoltage=20;                          // To store the current respiration value
boolean startPlot = false;                             // Conditional Variable to start and stop the plot

GPlot plotPPG;
GPlot plotECG;
GPlot plotResp;

int step = 0;
int stepsPerCycle = 100;
int lastStepTime = 0;
boolean clockwise = true;
float scale = 5;

/************** File Related Variables **********************/

boolean logging = false;                                // Variable to check whether to record the data or not
FileWriter output;                                      // In-built writer class object to write the data to file
JFileChooser jFileChooser;                              // Helps to choose particular folder to save the file
Date date;                                              // Variables to record the date related values                              
BufferedWriter bufferedWriter;
DateFormat dateFormat;

/************** Port Related Variables **********************/

Serial port = null;                                     // Oject for communicating via serial port
String[] comList;                                       // Buffer that holds the serial ports that are paired to the laptop
char inString = '\0';                                   // To receive the bytes from the packet
String selectedPort;                                    // Holds the selected port number

/************** Logo Related Variables **********************/

PImage logo;
boolean gStatus;                                        // Boolean variable to save the grid visibility status

int nPoints1 = pSize;
int totalPlotsHeight=0;
int totalPlotsWidth=0;

public void setup() 
{
  println(System.getProperty("os.name"));
  
  GPointsArray pointsPPG = new GPointsArray(nPoints1);
  GPointsArray pointsECG = new GPointsArray(nPoints1);
  GPointsArray pointsResp = new GPointsArray(nPoints1);

  //size(800, 480, JAVA2D);
  
  /* G4P created Methods */
  createGUI();
  customGUI();
  
  totalPlotsHeight=height-50-68-15;

  plotECG = new GPlot(this);
  plotECG.setPos(0,55);
  plotECG.setDim(width, (totalPlotsHeight/3)-10);
  plotECG.setBgColor(0);
  plotECG.setBoxBgColor(0);
  plotECG.setLineColor(color(0, 255, 0));
  plotECG.setLineWidth(3);
  plotECG.setMar(0,0,0,0);
  
  plotPPG = new GPlot(this);
  plotPPG.setPos(0,(totalPlotsHeight/3)+5);
  plotPPG.setDim(width, (totalPlotsHeight/3)-10);
  plotPPG.setBgColor(0);
  plotPPG.setBoxBgColor(0);
  plotPPG.setLineColor(color(255, 255, 0));
  plotPPG.setLineWidth(3);
  plotPPG.setMar(0,0,0,0);

  plotResp = new GPlot(this);
  plotResp.setPos(0,(totalPlotsHeight/3)+(totalPlotsHeight/3)+5+5);
  plotResp.setDim(width, (totalPlotsHeight/3)-10);
  plotResp.setBgColor(0);
  plotResp.setBoxBgColor(0);
  plotResp.setLineColor(color(0,0,255));
  plotResp.setLineWidth(3);
  plotResp.setMar(0,0,0,0);

  for (int i = 0; i < nPoints1; i++) 
  {
    pointsPPG.add(i,0);
    pointsECG.add(i,0);
    pointsResp.add(i,0); 
  }

  plotECG.setPoints(pointsECG);
  plotPPG.setPoints(pointsPPG);
  plotResp.setPoints(pointsPPG);

  /* Object initialization for User-defined classes */
  headerButton = new HeaderButton(0, 0, width, 80);
  helpWidget = new HelpWidget(0, height - 50, width, 68);
    
  hr = new BPM();
  rpm1 = new RPM();
  s = new SPO2_cal();

  /*******  Initializing zero for buffer ****************/

  for (int i=0; i<pSize; i++) 
  {
    time = time + 1;
    xdata[i]=time;
    ecgdata[i] = 0;
    respdata[i] = 0;
    ppgArray[i] = 0;
  }
  time = 0;
}

public void draw() 
{
  background(0);

  GPointsArray pointsPPG = new GPointsArray(nPoints1);
  GPointsArray pointsECG = new GPointsArray(nPoints1);
  GPointsArray pointsResp = new GPointsArray(nPoints1);

  if (startPlot)                             // If the condition is true, then the plotting is done
  {
    for(int i=0; i<nPoints1;i++)
    {    
      pointsECG.add(i,ecgdata[i]);
      pointsPPG.add(i,spo2data[i]); 
      pointsResp.add(i,respdata[i]);  
    }
  } 
  else                                     // Default value is set
  {
    bpm1.setText("Heart Rate: --- bpm");
    BP.setText("BP: ---/---");
    Temp.setText("Temperature: ---");
    SP02.setText("SpO2: --- %");
    rpm.setText("Respiration: --- bpm");
  }

  plotECG.setPoints(pointsECG);
  plotPPG.setPoints(pointsPPG);
  plotResp.setPoints(pointsResp);
  
  plotECG.beginDraw();
  plotECG.drawBackground();
  plotECG.drawLines();
  plotECG.endDraw();
  
  plotPPG.beginDraw();
  plotPPG.drawBackground();
  //plot.drawBox();
  //plot.drawXAxis();
  //plot.drawYAxis();
  //plot.drawTopAxis();
  //plot.drawRightAxis();
  //plot.drawTitle();
  plotPPG.drawLines();
  //plot.drawPoints();
  plotPPG.endDraw();

  plotResp.beginDraw();
  plotResp.drawBackground();
  plotResp.drawLines();
  plotResp.endDraw();

  // Line is drawn to split the graph with the values
/*
  stroke(255);
  strokeWeight(2);

  line(width-(width/8), 0,width-width/8, height-40);
  line(width-300, (height/3)+(height/3), width, (height/3)+(height/3));
  line(width-300, (height/3), width, (height/3));
  line(width-300, (height/3)-(height/3), width, (height/3)-(height/3));
  line(width-150, 305, width-150, height-40);
*/
  // User-defined Class call
  headerButton.draw();
  //helpWidget.draw();
}

/*********************************************** Opening Port Function ******************************************* **************/

public void startSerial()
{
  try
  {
    port = new Serial(this, selectedPort, 57600);
    port.clear();
    startPlot = true;
  }
  catch(Exception e)
  {

    showMessageDialog(null, "Port is busy", "Alert", ERROR_MESSAGE);
    System.exit (0);
  }
}

/*********************************************** Serial Port Event Function *********************************************************/

///////////////////////////////////////////////////////////////////
//
//  Event Handler To Read the packets received from the Device
//
//////////////////////////////////////////////////////////////////

public void serialEvent (Serial blePort) 
{
  inString = blePort.readChar();
  ecsProcessData(inString);
}

/*********************************************** Getting Packet Data Function *********************************************************/

///////////////////////////////////////////////////////////////////////////
//  
//  The Logic for the below function :
//      //  The Packet recieved is separated into header, footer and the data
//      //  If Packet is not received fully, then that packet is dropped
//      //  The data separated from the packet is assigned to the buffer
//      //  If Record option is true, then the values are stored in the text file
//
//////////////////////////////////////////////////////////////////////////

public void ecsProcessData(char rxch)
{
  switch(ecs_rx_state)
  {
  case CESState_Init:
    if (rxch==CES_CMDIF_PKT_START_1)
      ecs_rx_state=CESState_SOF1_Found;
    break;

  case CESState_SOF1_Found:
    if (rxch==CES_CMDIF_PKT_START_2)
      ecs_rx_state=CESState_SOF2_Found;
    else
      ecs_rx_state=CESState_Init;                    //Invalid Packet, reset state to init
    break;

  case CESState_SOF2_Found:
    //    println("inside 3");
    ecs_rx_state = CESState_PktLen_Found;
    CES_Pkt_Len = (int) rxch;
    CES_Pkt_Pos_Counter = CES_CMDIF_IND_LEN;
    CES_Data_Counter = 0;
    break;

  case CESState_PktLen_Found:
    //    println("inside 4");
    CES_Pkt_Pos_Counter++;
    if (CES_Pkt_Pos_Counter < CES_CMDIF_PKT_OVERHEAD)  //Read Header
    {
      if (CES_Pkt_Pos_Counter==CES_CMDIF_IND_LEN_MSB)
        CES_Pkt_Len = (int) ((rxch<<8)|CES_Pkt_Len);
      else if (CES_Pkt_Pos_Counter==CES_CMDIF_IND_PKTTYPE)
        CES_Pkt_PktType = (int) rxch;
    } else if ( (CES_Pkt_Pos_Counter >= CES_CMDIF_PKT_OVERHEAD) && (CES_Pkt_Pos_Counter < CES_CMDIF_PKT_OVERHEAD+CES_Pkt_Len+1) )  //Read Data
    {
      if (CES_Pkt_PktType == 2)
      {
        CES_Pkt_Data_Counter[CES_Data_Counter++] = (char) (rxch);          // Buffer that assigns the data separated from the packet
      }
    } else  //All  and data received
    {
      if (rxch==CES_CMDIF_PKT_STOP)
      {     
        // The Buffer is splitted and assigned separatly in different buffer
        // First 4 bytes for ECG
        // Next 4 bytes for REspiration
        // Next 8 bytes for SpO2/PPG

        CES_Pkt_ECG_Counter[0] = CES_Pkt_Data_Counter[0];
        CES_Pkt_ECG_Counter[1] = CES_Pkt_Data_Counter[1];
        CES_Pkt_ECG_Counter[2] = CES_Pkt_Data_Counter[2];
        CES_Pkt_ECG_Counter[3] = CES_Pkt_Data_Counter[3];

        CES_Pkt_Resp_Counter[0] = CES_Pkt_Data_Counter[4];
        CES_Pkt_Resp_Counter[1] = CES_Pkt_Data_Counter[5];
        CES_Pkt_Resp_Counter[2] = CES_Pkt_Data_Counter[6];
        CES_Pkt_Resp_Counter[3] = CES_Pkt_Data_Counter[7];

        CES_Pkt_SpO2_Counter_IR[0] = CES_Pkt_Data_Counter[8];
        CES_Pkt_SpO2_Counter_IR[1] = CES_Pkt_Data_Counter[9];
        CES_Pkt_SpO2_Counter_IR[2] = CES_Pkt_Data_Counter[10];
        CES_Pkt_SpO2_Counter_IR[3] = CES_Pkt_Data_Counter[11];

        CES_Pkt_SpO2_Counter_RED[0] = CES_Pkt_Data_Counter[12];
        CES_Pkt_SpO2_Counter_RED[1] = CES_Pkt_Data_Counter[13];
        CES_Pkt_SpO2_Counter_RED[2] = CES_Pkt_Data_Counter[14];
        CES_Pkt_SpO2_Counter_RED[3] = CES_Pkt_Data_Counter[15];

        int Temp_Value = (int) CES_Pkt_Data_Counter[16];                // Temperature
        // BP Value Systolic and Diastolic
        int BP_Value_Sys = (int) CES_Pkt_Data_Counter[17];
        int BP_Value_Dia = (int) CES_Pkt_Data_Counter[18];

        int data1 = ecsParsePacket(CES_Pkt_ECG_Counter, CES_Pkt_ECG_Counter.length-1);
        ecg = (double) data1/(Math.pow(10, 3));

        int data2 = ecsParsePacket(CES_Pkt_Resp_Counter, CES_Pkt_Resp_Counter.length-1);
        resp = (double) data2/(Math.pow(10, 3));

        int data3 = ecsParsePacket(CES_Pkt_SpO2_Counter_IR, CES_Pkt_SpO2_Counter_IR.length-1);
        spo2_ir = (double) data3;

        int data4 = ecsParsePacket(CES_Pkt_SpO2_Counter_RED, CES_Pkt_SpO2_Counter_RED.length-1);
        spo2_red = (double) data4;

        ecg_avg[arrayIndex] = (float)ecg;
        ecgAvg = averageValue(ecg_avg);
        ecg = (ecg_avg[arrayIndex] - ecgAvg);

        spo2Array_IR[arrayIndex] = (float)spo2_ir;
        spo2Array_RED[arrayIndex] = (float)spo2_red;
        redAvg = averageValue(spo2Array_RED);
        irAvg = averageValue(spo2Array_IR);
        spo2 = (spo2Array_IR[arrayIndex] - irAvg);

        resp_avg[arrayIndex]= (float)resp;
        resAvg =  averageValue(resp_avg);
        resp = (resp_avg[arrayIndex] - resAvg);

        // Assigning the values for the graph buffers

        time = time+1;
        xdata[arrayIndex] = time;

        ecgdata[arrayIndex] = (float)ecg;
        respdata[arrayIndex]= (float)resp;
        spo2data[arrayIndex] = (float)spo2;
        bpmArray[arrayIndex] = (float)ecg;
        rpmArray[arrayIndex] = (float)resp;
        ppgArray[arrayIndex] = (float)spo2;

        arrayIndex++;

        hr.bpmCalc(bpmArray);                                        // HeartRate Calculation 
        s.rawDataArray(spo2Array_IR, spo2Array_RED, irAvg, redAvg);  // SpO2 Calculation
        rpm1.rpmCalc(rpmArray);                                      // Respiration Calculation
        
        if (arrayIndex == pSize)
        {  
          arrayIndex = 0;
          time = 0;
          if (startPlot)
          {
            BP.setText("BP: ---/---");
            Temp.setText("Temperature:"+Temp_Value+" F");
          }
        }       

        // If record button is clicked, then logging is done

        if (logging == true)
        {
          try {
            date = new Date();
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            bufferedWriter.write(dateFormat.format(date)+","+ecg+","+spo2+","+resp);
            bufferedWriter.newLine();
          }
          catch(IOException e) {
            println("It broke!!!");
            e.printStackTrace();
          }
        }
        ecs_rx_state=CESState_Init;
      } else
      {
        ecs_rx_state=CESState_Init;
      }
    }
    break;

  default:
    break;
  }
}

/*********************************************** Recursion Function To Reverse The data *********************************************************/

public int ecsParsePacket(char DataRcvPacket[], int n)
{
  if (n == 0)
    return (int) DataRcvPacket[n]<<(n*8);
  else
    return (DataRcvPacket[n]<<(n*8))| ecsParsePacket(DataRcvPacket, n-1);
}

/********************************************* User-defined Method for G4P Controls  **********************************************************/

///////////////////////////////////////////////////////////////////////////////
//
//  Customization of controls is done here
//  That includes : Font Size, Visibility, Enable/Disable, ColorScheme, etc.,
//
//////////////////////////////////////////////////////////////////////////////

public void customGUI() 
{  
  comList = port.list();
  String comList1[] = new String[comList.length+1];
  comList1[0] = "SELECT THE PORT";
  for (int i = 1; i <= comList.length; i++)
  {
    comList1[i] = comList[i-1];
  }
  comList = comList1;
  portList.setItems(comList1, 0);
  //done.setVisible(false);
  bpm1.setLocalColor(2, color(0, 255, 0));
  bpm1.setFont(new Font("Arial", Font.PLAIN, 20));

  BP.setLocalColor(2, color(255, 0, 0));
  BP.setFont(new Font("Arial", Font.PLAIN, 20));
  Temp.setLocalColorScheme(255);
  Temp.setFont(new Font("Arial", Font.PLAIN, 20));
  SP02.setLocalColor(2, color(255, 255, 0));
  SP02.setFont(new Font("Arial", Font.PLAIN, 20));
  rpm.setLocalColor(2, color(0, 191, 255));
  rpm.setText("0");
  rpm.setFont(new Font("Arial", Font.PLAIN, 20));
  //  record.setVisible(false);
  // portList.setVisible(false);
}

/*************** Function to Calculate Average *********************/

public double averageValue(float dataArray[])
{

  float total = 0;
  for (int i=0; i<dataArray.length; i++)
  {
    total = total + dataArray[i];
  }
  return total/dataArray.length;
}
//////////////////////////////////////////////////////////////////////////////////////////
//
//   BPM: [ Beats Per Minute]
//      - This Class calculates the Heart Rate According to the ecg data received
//
//   Created : Balasundari, Jul 2016
//   
/////////////////////////////////////////////////////////////////////////////////////////

class BPM
{

  float min, max;                                      // Stores Minimum and Maximum Value
  double threshold;                                    // Stores the threshold 
  float minimizedVolt[] = new float[pSize];            // Stores the absoulte values in the buffer
  int beats = 0, bpm = 0;                              // Variables to store the no.of peaks and bpm

  ////////////////////////////////////////////////////////////////////////////////////////////
  //  - Heart Value is calculated by:
  //          * Setting a threshold value which is between the minimum and maximum value
  //          * Calculating no.of peaks crossing, the threshold value.
  //          * Calculate the Heart rate with the no.of peaks achieved with the no.of seconds
  //   
  ////////////////////////////////////////////////////////////////////////////////////////////

  public void bpmCalc(float[] recVoltage)
  {

    int j = 0, n = 0, cntr = 0;

    // Making the array into absolute (positive values only)

    for (int i=0; i<pSize; i++)
    {
      recVoltage[i] = (float)Math.abs(recVoltage[i]);
    }

    j = 0;
    for (int i = 0; i < pSize; i++)
    {
      minimizedVolt[j++] = recVoltage[i];
    }
    
    // Calculating the minimum and maximum value
    
    min = min(minimizedVolt);
    max = max(minimizedVolt);

    if ((int)min == (int)max)
    {
      bpm1.setText("0");
    } else
    {
      threshold = min+max;                                     // Calculating the threshold value
      threshold = (threshold) * 0.400f;

      if (threshold != 0)
      {
        while (n < pSize)                                      // scan through ECG samples
        {
          if (minimizedVolt[n] > threshold)                    // ECG threshold crossed
          {
            beats++;
            n = n+30;                                          // skipping the some samples to avoid repeatation
          } else
            n++;
        }
        bpm = (beats*60)/8;

        bpm1.setText("Heart Rate:" + bpm+" bpm");                                  // Calculated BPM is displayed
        beats = 0;
      } else
      {
        bpm1.setText("0");
      }
    }
  }
};
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
//////////////////////////////////////////////////////////////////////////////////////////
//
//   RPM: [ Respiration Per Minute]
//      - This Class calculates the Respiratory Peaks According to the respiration data received
//
//   Created : Balasundari, Aug 2016
//   
/////////////////////////////////////////////////////////////////////////////////////////

class RPM
{
  float min, max;                                      // Stores Minimum and Maximum Value
  double threshold;                                    // Stores the threshold
  float minimizedVolt[] = new float[pSize];            // Stores the absoulte values in the buffer
  int peaks = 0, rpm1 = 0;                             // Variables to store the no.of peaks and bpm

  ////////////////////////////////////////////////////////////////////////////////////////////
  //  - Respiration Rate is calculated by:
  //          * Setting a threshold value which is between the minimum and maximum value
  //          * Calculating no.of peaks crossing, the threshold value.
  //   
  ////////////////////////////////////////////////////////////////////////////////////////////

  public void rpmCalc(float[] recVoltage)
  {
    int j = 0, n = 0, cntr = 0;
    // Making the array into absolute (positive values only)
    for (int i=0; i<pSize; i++)
    {
      minimizedVolt[i] = (float)Math.abs(recVoltage[i]);
    }
    min = min(minimizedVolt);
    max = max(minimizedVolt);

    threshold = min+max;                      // Calculating the threshold value
    threshold = (threshold) * 0.400f;

    if (threshold != 0)
    {
      while (n < pSize)                       // scan through ECG samples
      {
        if (recVoltage[n] > threshold)        // ECG threshold crossed
        {
          peaks++;
          n = n+30;                           // skipping the some samples to avoid repeatation
        } else
          n++;
      }
      rpm.setText("Respiration: " + peaks+ " bpm");
      peaks = 0;
    } else
    {
      rpm.setText("Respiration: 0 bpm");
    }
  }
};
//////////////////////////////////////////////////////////////////////////////////////////
//
//   SPO2_cal:
//      - This Class calculates the SpO2 value According to the PPG data received
//
//   Created : Balasundari, Aug 2016
//   
/////////////////////////////////////////////////////////////////////////////////////////

public class SPO2_cal
{
  float Vdc = 0;
  float Vac = 0;
  float spo2_cal_array[] = new float[pSize];

  float SPO2 = 0;

  ///////////////////////////////////////////////////////////////////////////
  //  
  //  To Calulate the Spo2 value any one of the following Emprical Formal are used:
  //    1. float SpO2 = 10.0002*(value)-52.887*(value) + 26.817*(value) + 98.293;
  //    2. float SpO2 =((0.81-0.18*(value))/(0.73+0.11*(value)));
  //    3. float SpO2=110-25*(value);
  //  In this Program, the 3rd formulae is used
  //
  //////////////////////////////////////////////////////////////////////////

  public void rawDataArray(float ir_array[], float red_array[], double ir_avg, double red_avg)
  {
    float RedAC = s.SPO2_Value(red_array);
    float IrAC = s.SPO2_Value(ir_array);
    float value = (RedAC/abs((float)red_avg))/(IrAC/abs((float)ir_avg));
    float SpO2=110-25*(value);
    SpO2 = (int)(SpO2 * 100);
    SpO2 = Math.round(SpO2/100);
    SP02.setText("SpO2: "+ (SpO2+10)+" %");
  }
  
  ////////////////////////////////////////////////////////////////////////////////////////////
  //  SPo2 Value is calculated by:
  //    * Calculate the square of the spo2 values and store it in the buffer
  //    * Sum of the values in the squared buffer is calculated.
  //    * This sum is sent to the main function
  //   
  ////////////////////////////////////////////////////////////////////////////////////////////

  public float SPO2_Value(float spo2_array[])
  {
    SPO2 = 0;
    int k = 0;
    for (int i = 50; i < spo2_array.length; i++)
    {
      spo2_cal_array[k++] = spo2_array[i] * spo2_array[i];
    }
    SPO2 = sum(spo2_cal_array, k);
    return (SPO2);
  }

  public float sum(float array[], int len)
  {
    float spo2 = 0;
    for (int p = 0; p < len; p++)
    {
      spo2 = spo2 + array[p];
    }
    Vac = (float)Math.sqrt(spo2/len);
    return Vac;
  }
}
/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */

public void portList_click(GDropList source, GEvent event) { //_CODE_:portList:640344:
  //println("portList - GDropList >> GEvent." + event + " @ " + millis());
  selectedPort = portList.getSelectedText();
  startSerial();
} //_CODE_:portList:640344:

public void record_click(GButton source, GEvent event) { //_CODE_:record:731936:
  //println("record - GButton >> GEvent." + event + " @ " + millis());
  ////////////////////////////////////////////////////////////////////////////////
  //
  //    Enable the buttons and calls the serial port function
  //    Comselect is made true to call the serial function
  //  
  ///////////////////////////////////////////////////////////////////////////////
  try
  {
    jFileChooser = new JFileChooser();
    jFileChooser.setSelectedFile(new File("log.csv"));
    jFileChooser.showSaveDialog(null);
    String filePath = jFileChooser.getSelectedFile()+"";

    if ((filePath.equals("log.txt"))||(filePath.equals("null")))
    {
    } else
    {    
 //     done.setVisible(true);
      record.setVisible(false);
      close.setEnabled(false);
      close.setLocalColorScheme(GCScheme.CYAN_SCHEME);
      logging = true;
      date = new Date();
      output = new FileWriter(jFileChooser.getSelectedFile(), true);
      bufferedWriter = new BufferedWriter(output);
      bufferedWriter.write(date.toString()+"");
      bufferedWriter.newLine();
      bufferedWriter.write("TimeStramp,ECG,SpO2,Respiration");
      bufferedWriter.newLine();
    }
  }
  catch(Exception e)
  {
    println("File Not Found");
  }
} //_CODE_:record:731936:

public void close_click(GButton source, GEvent event) { //_CODE_:close:222350:
  //println("close - GButton >> GEvent." + event + " @ " + millis());
  int dialogResult = JOptionPane.showConfirmDialog (null, "Would You Like to Close The Application?");
  if (dialogResult == JOptionPane.YES_OPTION) {
    try
    {
      //Runtime runtime = Runtime.getRuntime();
      //Process proc = runtime.exec("sudo shutdown -h now");
      System.exit(0);
    }
    catch(Exception e)
    {
      exit();
    }
  } else
  {
  }
} //_CODE_:close:222350:

public void imgButton1_click1(GImageButton source, GEvent event) { //_CODE_:imgButton1:665258:
  println("imgButton1 - GImageButton >> GEvent." + event + " @ " + millis());
} //_CODE_:imgButton1:665258:



// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
  G4P.setCursor(ARROW);
  surface.setTitle("Healthy Pi");
  portList = new GDropList(this, 5, 7, 200, 385, 10);
  portList.setItems(loadStrings("list_640344"), 0);
  portList.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  portList.addEventHandler(this, "portList_click");
  record = new GButton(this, 319, 7, 100, 40);
  record.setText("RECORD");
  record.setTextBold();
  record.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  record.addEventHandler(this, "record_click");
  close = new GButton(this, 434, 7, 100, 40);
  close.setText("CLOSE");
  close.setTextBold();
  close.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  close.addEventHandler(this, "close_click");
  imgButton1 = new GImageButton(this, (width-500), 5, 473, 70, new String[] { "logo.png", "logo.png", "logo.png" } );
  imgButton1.addEventHandler(this, "imgButton1_click1");
  bpm1 = new GLabel(this, (width-250), 60, 250, 80);
  bpm1.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  bpm1.setText("---");
  bpm1.setTextBold();
  bpm1.setOpaque(false);
  SP02 = new GLabel(this, (width-250), (height/3)+10, 250, 80);
  SP02.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  SP02.setText("---");
  SP02.setTextBold();
  SP02.setOpaque(false);
  BP = new GLabel(this, (width-250), (height-200), 225, 100);
  BP.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  BP.setText("BP: ---/---");
  BP.setTextBold();
  BP.setOpaque(false);
  Temp = new GLabel(this, (width-250), (height-100), 250, 58);
  Temp.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  Temp.setText("Temperature: ---");
  Temp.setTextBold();
  Temp.setOpaque(false);
  rpm = new GLabel(this, (width-250), (height/3)+(height/3)+10, 250, 62);
  rpm.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  rpm.setText("---");
  rpm.setTextBold();
  rpm.setOpaque(false);
}

// Variable declarations 
// autogenerated do not edit
GDropList portList; 
GButton record; 
GButton close; 
GImageButton imgButton1; 
GLabel bpm1; 
GLabel SP02; 
GLabel BP; 
GLabel label1; 
GLabel label4; 
GLabel Temp; 
GLabel label5; 
GLabel label7; 
GLabel label8; 
GLabel rpm; 
GLabel label11; 
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "healthypi_desktop" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
