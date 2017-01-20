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

import g4p_controls.*;                       // Processing GUI Library to create buttons, dropdown,etc.,
import processing.serial.*;                  // Serial Library

// Java Swing Package For prompting message
import java.awt.*;
import javax.swing.*;
import static javax.swing.JOptionPane.*;

// File Packages to record the data into a text file
import javax.swing.JFileChooser;
import java.io.FileWriter;
import java.io.BufferedWriter;

// Date Format
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// General Java Package
import java.math.*;

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
  //size(800, 480, JAVA2D);
  fullScreen();
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

void startSerial()
{
  try
  {
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

void serialEvent (Serial blePort) 
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

void ecsProcessData(char rxch)
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

void setChartSettings() {
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

double averageValue(float dataArray[])
{

  float total = 0;
  for (int i=0; i<dataArray.length; i++)
  {
    total = total + dataArray[i];
  }
  return total/dataArray.length;
}