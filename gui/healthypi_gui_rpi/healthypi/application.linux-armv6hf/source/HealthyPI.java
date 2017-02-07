import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import g4p_controls.*; 
import processing.serial.*; 
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

public class HealthyPI extends PApplet {

//////////////////////////////////////////////////////////////////////////////////////////
//
//   GUI for controlling the Healthy Pi Hat [ Patient Monitoring System] in Raspberry Pi
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
int pSize = 1000;                                            // Total Size of the buffer
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

Graph g, g1, g2;
HelpWidget helpWidget;
BPM hr;
RPM rpm1;
SPO2_cal s;

/************** Graph Related Variables **********************/

double maxe, mine, maxr, minr, maxs, mins;             // To Calculate the Minimum and Maximum of the Buffer
double ecg, resp, spo2_ir, spo2_red, spo2, redAvg, irAvg, ecgAvg, resAvg;  // To store the current ecg value
double respirationVoltage=20;                          // To store the current respiration value
boolean startPlot = false;                             // Conditional Variable to start and stop the plot

/************** File Related Variables **********************/

boolean logging = false;                                // Variable to check whether to record the data or not
FileWriter output;                                      // In-built writer class object to write the data to file
JFileChooser jFileChooser;                              // Helps to choose particular folder to save the file
Date date;                                              // Variables to record the date related values                              
BufferedWriter bufferedWriter;
DateFormat dateFormat;

/************** Port Related Variables **********************/

Serial port = null;                                     // Oject for communicating via serial port
char inString = '\0';                                   // To receive the bytes from the packet

/************** Logo Related Variables **********************/

PImage logo;
boolean gStatus;                                        // Boolean variable to save the grid visibility status

/*********************************************** Set Up Function *********************************************************/

////////////////////////////////////////////////////////////////////////////////
//
//  This Function is executed only once
//  The Objects for classes are initialized here
//
///////////////////////////////////////////////////////////////////////////////

public void setup() {
  
  //fullScreen();
  /* G4P created Methods */
  createGUI();
  customGUI();

  /* Object initialization for User-defined classes */
  helpWidget = new HelpWidget(0, height - 50, width, 68); 
  g = new Graph(12, 60, width-225, 100);
  g1 = new Graph(10, 330, width-220, 100);
  g2 = new Graph(10, 200, width-220, 100);
  hr = new BPM();
  rpm1 = new RPM();
  s = new SPO2_cal();

  g.GraphColor = color(0, 255, 0);
  g1.GraphColor = color(0, 191, 255);
  g2.GraphColor = color(255, 255, 0);

  setChartSettings();                                    // graph function to set minimum and maximum for axis

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

/*********************************************** Draw Function *********************************************************/

////////////////////////////////////////////////////////////////////////////////
//
//  This Function is executed repeatedly according the Frame Refresh Rate  
//
///////////////////////////////////////////////////////////////////////////////

public void draw() {
  background(0);
  
  if(!startPlot)                              // Calling the method to connect with the serial port
    startSerial();
    
  g.DrawAxis();                              // Draw the grid for the graph
  if (startPlot)                             // If the condition is true, then the plotting is done
  {
    g.LineGraph(xdata, ecgdata);
    g1.LineGraph(xdata, respdata);
    g2.LineGraph(xdata, ppgArray);
  } else                                     // Default value is set
  {
    bpm1.setText("---");
    BP.setText("---/---");
    Temp.setText("---");
    SP02.setText("---");
    rpm.setText("---");
  }

  // Line is drawn to split the graph with the values

  stroke(255);
  strokeWeight(2);
  line(595, 0, 595, height-40);
  line(595, 100, width, 100);
  line(595, 210, width, 210);
  line(595, 305, width, 305);
  line(695, 305, 695, height-40);

  // User-defined Class call
  helpWidget.draw();
}

/*********************************************** Opening Port Function ******************************************* **************/

public void startSerial()
{
  try
  {
    //port = new Serial(this,"/dev/ttyAMA0", 57600);
    port = new Serial(this,"/dev/ttyAMA0", 57600);
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
    } else  //All header and data received
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
            BP.setText("---/---");
            Temp.setText("---");
          }
        }       

        // Calculating the minimum & maximum of the wave forms for auto scaling

        maxe = max(ecgdata);
        mine = min(ecgdata);
        maxr = max(respdata);
        minr = min(respdata);
        maxs = max(spo2data);
        mins = min(spo2data);

        // Auto Scaling

        if ((maxe != g.yMax1))
          g.yMax1 = (int)maxe;
        if ((maxr != g1.yMax1))
          g1.yMax1 = (int)maxr;
        if ((maxs != g2.yMax1))
          g2.yMax1 = (int)maxs;

        if ((mine != g.yMin1))
          g.yMin1 = (int)mine;
        if ((minr != g1.yMin1))
          g1.yMin1 = (int)minr;
        if ((mins != g2.yMin1))
          g2.yMin1 = (int)mins;

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

/**************** Setting the Limits for the graph **************/

public void setChartSettings() {
  g.xDiv=11; 
  g.xMax1=pSize; 
  g.xMin1=0;  
  g.yMax1=1300; 
  g.yMin1=-400;

  g1.xDiv=10;  
  g1.xMax1=pSize; 
  g1.xMin1=0;  
  g1.yMax1=20; 
  g1.yMin1=0;

  g2.xDiv=10;  
  g2.xMax1=pSize; 
  g2.xMin1=0;  
  g2.yMax1=10; 
  g2.yMin1=-10;
}

/********************************************* User-defined Method for G4P Controls  **********************************************************/

///////////////////////////////////////////////////////////////////////////////
//
//  Customization of controls is done here
//  That includes : Font Size, Visibility, Enable/Disable, ColorScheme, etc.,
//
//////////////////////////////////////////////////////////////////////////////

public void customGUI() {  

  done.setVisible(false);
  bpm1.setLocalColor(2, color(0, 255, 0));
  bpm1.setFont(new Font("Arial", Font.PLAIN, 50));
  label1.setLocalColor(2, color(255, 0, 0));                                  // BP
  label1.setFont(new Font("Arial", Font.PLAIN, 12));
  label2.setLocalColor(2, color(255, 255, 0));                                //SP02
  label2.setFont(new Font("Arial", Font.PLAIN, 12));
  label3.setLocalColor(2, color(0, 255, 0));                                //Heart Rate
  label3.setFont(new Font("Arial", Font.PLAIN, 12));
  label4.setLocalColor(2, color(255, 0, 0));                                  // mmHg
  label4.setFont(new Font("Arial", Font.PLAIN, 12));
  label11.setLocalColorScheme(255);                                          //Temperature
  label11.setFont(new Font("Arial", Font.PLAIN, 12));
  label6.setLocalColor(2, color(0, 255, 0));                                //bpm
  label6.setFont(new Font("Arial", Font.PLAIN, 12));
  label5.setLocalColor(2, color(255, 0, 0));                                  //SYS
  label5.setFont(new Font("Arial", Font.PLAIN, 12));
  label7.setLocalColor(2, color(255, 0, 0));                                  //DIA
  label7.setFont(new Font("Arial", Font.PLAIN, 12));
  label8.setLocalColor(2, color(255, 0, 0));                                  // " / "
  label8.setFont(new Font("Arial", Font.PLAIN, 12));
  label9.setLocalColor(2, color(0, 191, 255));                                  // RPM
  label9.setFont(new Font("Arial", Font.PLAIN, 12));                          
  label9.setTextBold();
  BP.setLocalColor(2, color(255, 0, 0));
  BP.setFont(new Font("Arial", Font.PLAIN, 45));
  Temp.setLocalColorScheme(255);
  Temp.setFont(new Font("Arial", Font.PLAIN, 50));
  SP02.setLocalColor(2, color(255, 255, 0));
  SP02.setFont(new Font("Arial", Font.PLAIN, 50));
  rpm.setLocalColor(2, color(0, 191, 255));
  rpm.setText("0");
  rpm.setFont(new Font("Arial", Font.PLAIN, 50));
  //  record.setVisible(false);
  // portList.setVisible(false);
  la.setLocalColor(2, color(255));                                  // Grid Status
  la.setFont(new Font("Arial", Font.PLAIN, 18));
  label10.setLocalColor(2, color(255));                                  // Grid Size
  label10.setFont(new Font("Arial", Font.PLAIN, 18));
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

        bpm1.setText(bpm+"");                                  // Calculated BPM is displayed
        beats = 0;
      } else
      {
        bpm1.setText("0");
      }
    }
  }
};
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
  int GraphColor;                                        // Color for the trace
  float yMax1=1024, yMin1=0;                                 // Default axis dimensions
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

  public void DrawAxis() {

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
        String xAxis=str((xMin1+PApplet.parseFloat(x)/xDiv*(xMax1-xMin1))); // the only way to get a specific number of decimals 
        String[] xAxisMS=split(xAxis, '.');                 // is to split the float into strings
      }
      for (int y=0; y<=yDiv; y++) {
        stroke(150);
        line(xPos, PApplet.parseFloat(y)/yDiv*Height+yPos, xPos+Width-15, PApplet.parseFloat(y)/yDiv*Height+yPos);              // y-axis lines 
        textAlign(RIGHT);
        fill(255);
        String yAxis=str(yMin1+PApplet.parseFloat(y)/yDiv*(yMax1-yMin1));     // Make y Label a string
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

  public void LineGraph(float[] x, float[] y) {

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
      rpm.setText(peaks+"");
      peaks = 0;
    } else
    {
      rpm.setText("0");
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
    SP02.setText((SpO2+10)+"");
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
      done.setVisible(true);
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

public void done_click(GButton source, GEvent event) { //_CODE_:done:614170:
  //println("done - GButton >> GEvent." + event + " @ " + millis());
  ////////////////////////////////////////////////////////////////////////////////
  //
  //    Save the file and displays a success message
  //  
  ///////////////////////////////////////////////////////////////////////////////
  if (logging == true)
  {
    showMessageDialog(null, "Log File Saved successfully");

    record.setVisible(true);
    done.setVisible(false);

    close.setEnabled(true);
    close.setLocalColorScheme(GCScheme.GREEN_SCHEME);
    record.setEnabled(true);
    record.setLocalColorScheme(GCScheme.GREEN_SCHEME);
    done.setEnabled(true);
    done.setLocalColorScheme(GCScheme.GREEN_SCHEME);
    logging = false;
    try
    {
      bufferedWriter.flush();
      bufferedWriter.close();
    }
    catch(Exception e)
    {
      println(e);
    }
  }
} //_CODE_:done:614170:

public void close_click(GButton source, GEvent event) { //_CODE_:close:222350:
  //println("close - GButton >> GEvent." + event + " @ " + millis());
  int dialogResult = JOptionPane.showConfirmDialog (null, "Would You Like to Close The Application?");
  if (dialogResult == JOptionPane.YES_OPTION) {
    try
    {
      Runtime runtime = Runtime.getRuntime();
      Process proc = runtime.exec("sudo shutdown -h now");
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

public void grid_size_click(GButton source, GEvent event) { //_CODE_:grid_size:708711:
  println("grid_size - GButton >> GEvent." + event + " @ " + millis());
} //_CODE_:grid_size:708711:

public void gridStatus_click(GButton source, GEvent event) { //_CODE_:gridStatus:949626:
  //println("gridStatus - GButton >> GEvent." + event + " @ " + millis());
  if (gStatus)
  {
    gStatus = false;
    la.setText("Grid :OFF");
  } else
  {
    gStatus = true;    
    la.setText("Grid :   ON");
  }
  la.setTextBold();
} //_CODE_:gridStatus:949626:



// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
  G4P.setCursor(ARROW);
  surface.setTitle("Healthy Pi");
  record = new GButton(this, 445, 420, 100, 55);
  record.setText("RECORD");
  record.setTextBold();
  record.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  record.addEventHandler(this, "record_click");
  done = new GButton(this, 445, 420, 100, 55);
  done.setText("DONE");
  done.setTextBold();
  done.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  done.addEventHandler(this, "done_click");
  close = new GButton(this, 560, 420, 100, 55);
  close.setText("CLOSE");
  close.setTextBold();
  close.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  close.addEventHandler(this, "close_click");
  imgButton1 = new GImageButton(this, 694, 420, 100, 55, new String[] { "logo.png", "logo.png", "logo.png" } );
  imgButton1.addEventHandler(this, "imgButton1_click1");
  bpm1 = new GLabel(this, 620, 15, 139, 100);
  bpm1.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  bpm1.setText("---");
  bpm1.setTextBold();
  bpm1.setOpaque(false);
  SP02 = new GLabel(this, 620, 115, 139, 100);
  SP02.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  SP02.setText("---");
  SP02.setTextBold();
  SP02.setOpaque(false);
  BP = new GLabel(this, 590, 215, 225, 100);
  BP.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  BP.setText("---/---");
  BP.setTextBold();
  BP.setOpaque(false);
  label1 = new GLabel(this, 600, 210, 75, 20);
  label1.setText("BP");
  label1.setTextBold();
  label1.setOpaque(false);
  label2 = new GLabel(this, 600, 105, 150, 20);
  label2.setText("SpO2            %");
  label2.setTextBold();
  label2.setOpaque(false);
  label3 = new GLabel(this, 600, 5, 80, 20);
  label3.setText("HR");
  label3.setTextBold();
  label3.setOpaque(false);
  label4 = new GLabel(this, 720, 210, 80, 20);
  label4.setTextAlign(GAlign.RIGHT, GAlign.MIDDLE);
  label4.setText("mmHg");
  label4.setTextBold();
  label4.setOpaque(false);
  label6 = new GLabel(this, 720, 5, 80, 20);
  label6.setTextAlign(GAlign.RIGHT, GAlign.MIDDLE);
  label6.setText("bpm");
  label6.setTextBold();
  label6.setOpaque(false);
  Temp = new GLabel(this, 690, 315, 120, 106);
  Temp.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  Temp.setText("---");
  Temp.setTextBold();
  Temp.setOpaque(false);
  label5 = new GLabel(this, 653, 282, 35, 22);
  label5.setTextAlign(GAlign.RIGHT, GAlign.MIDDLE);
  label5.setText("SYS");
  label5.setTextBold();
  label5.setOpaque(false);
  label9 = new GLabel(this, 600, 305, 100, 20);
  label9.setText("Respiration");
  label9.setTextBold();
  label9.setOpaque(false);
  label7 = new GLabel(this, 698, 282, 28, 22);
  label7.setText("DIA");
  label7.setTextBold();
  label7.setOpaque(false);
  label8 = new GLabel(this, 682, 282, 15, 22);
  label8.setTextAlign(GAlign.RIGHT, GAlign.MIDDLE);
  label8.setText("/");
  label8.setTextBold();
  label8.setOpaque(false);
  rpm = new GLabel(this, 580, 315, 120, 106);
  rpm.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  rpm.setText("---");
  rpm.setTextBold();
  rpm.setOpaque(false);
  label11 = new GLabel(this, 696, 305, 80, 20);
  label11.setText("TEMP ( \u00b0 C)");
  label11.setTextBold();
  label11.setOpaque(false);
  la = new GLabel(this, 6, 427, 84, 46);
  la.setText("Grid : OFF");
  la.setTextBold();
  la.setOpaque(false);
  label10 = new GLabel(this, 120, 427, 129, 46);
  label10.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  label10.setText("Grid Size : 12.5 mm/s");
  label10.setTextBold();
  label10.setOpaque(false);
  grid_size = new GButton(this, 240, 420, 50, 55);
  grid_size.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  grid_size.addEventHandler(this, "grid_size_click");
  gridStatus = new GButton(this, 65, 420, 50, 55);
  gridStatus.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  gridStatus.addEventHandler(this, "gridStatus_click");
}

// Variable declarations 
// autogenerated do not edit
GButton record; 
GButton done; 
GButton close; 
GImageButton imgButton1; 
GLabel bpm1; 
GLabel SP02; 
GLabel BP; 
GLabel label1; 
GLabel label2; 
GLabel label3; 
GLabel label4; 
GLabel label6; 
GLabel Temp; 
GLabel label5; 
GLabel label9; 
GLabel label7; 
GLabel label8; 
GLabel rpm; 
GLabel label11; 
GLabel la; 
GLabel label10; 
GButton grid_size; 
GButton gridStatus; 
  public void settings() {  size(800, 480, JAVA2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "HealthyPI" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
