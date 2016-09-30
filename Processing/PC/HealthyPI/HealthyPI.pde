import processing.serial.*;
import g4p_controls.*;
import java.awt.*;
import javax.swing.JFileChooser;
import javax.swing.*;
import static javax.swing.JOptionPane.*;

import java.math.*;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Date;
import static javax.swing.JOptionPane.*;

import java.io.FileReader;
import java.io.BufferedReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/************** Packet Validation  **********************/
private static final int CESState_Init = 0;
private static final int CESState_SOF1_Found = 1;
private static final int CESState_SOF2_Found = 2;
private static final int CESState_PktLen_Found = 3;

private static final int CESState_EOF_Wait = 98;
private static final int CESState_EOF_Found = 99;

/*CES CMD IF Packet Format*/
private static final int CES_CMDIF_PKT_START_1 = 0x0A;
private static final int CES_CMDIF_PKT_START_2 = 0xFA;
private static final int CES_CMDIF_PKT_STOP = 0x0B;

/*CES CMD IF Packet Indices*/
private static final int CES_CMDIF_IND_START_1 = 0;
private static final int CES_CMDIF_IND_START_2 = 1;
private static final int CES_CMDIF_IND_LEN = 2;
private static final int CES_CMDIF_IND_LEN_MSB = 3;
private static final int CES_CMDIF_IND_PKTTYPE = 4;
private static final int CES_CMDIF_IND_DATA0 = 5;
private static final int CES_CMDIF_IND_DATA1 = 6;
private static final int CES_CMDIF_IND_DATA2 = 7;

/* CES OTA Data Packet Positions */
private static int CES_OTA_DATA_PKT_POS_LENGTH = 0;
private static int CES_OTA_DATA_PKT_POS_CMD_CAT = 1;
private static int CES_OTA_DATA_PKT_POS_DATA_TYPE = 2;
private static int CES_OTA_DATA_PKT_POS_SENS_TYPE = 3;
private static int CES_OTA_DATA_PKT_POS_RSVD = 4;
private static int CES_OTA_DATA_PKT_POS_SENS_ID = 5;
private static int CES_OTA_DATA_PKT_POS_DATA = 6;

private static int CES_OTA_DATA_PKT_OVERHEAD = 6;
private static int CES_CMDIF_PKT_OVERHEAD = 5;

/************** Packet Related Variables **********************/

int ecs_rx_state = 0;
int CES_Pkt_Len;
int CES_Pkt_Pos_Counter, CES_Data_Counter;
int CES_Pkt_PktType;
char CES_Pkt_Data_Counter[] = new char[1000];
char CES_Pkt_ECG_Counter[] = new char[4];
char CES_Pkt_Resp_Counter[] = new char[4];
char CES_Pkt_SpO2_Counter_RED[] = new char[4];
char CES_Pkt_SpO2_Counter_IR[] = new char[4];


/************** ControlP5 Related Variables **********************/

int colorValue;
boolean visibility=false;

/************** Graph Related Variables **********************/

double maxe, mine, maxr, minr, maxs, mins;
double ecg, resp, spo2_ir, spo2_red, spo2, redAvg, irAvg, ecgAvg, resAvg;
double respirationVoltage=20;

/************** File Related Variables **********************/

boolean logging = false;
FileWriter output;
JFileChooser jFileChooser;
Date date;
FileReader readOutput;
String line;
BufferedWriter bufferedWriter;
DateFormat dateFormat;


/************** Port Related Variables **********************/

Serial port = null;
int Ss = -1;
String[] comList;
boolean serialSet;
boolean portSelected = false;
String portName;
char inString = '\0';

/************** Logo Related Variables **********************/

PImage logo;

/************** General Variables **********************/

boolean startPlot = false, Serialevent = false;
String msgs;
int startTime = 0;

int pSize = 1000;

float[] xdata = new float[pSize];

float[] ecgdata = new float[pSize];
float[] ecg_avg = new float[pSize];

float[] respdata = new float[pSize];
float[] resp_avg = new float[pSize];

float[] spo2data = new float[pSize];
float[] spo2Array_IR = new float[pSize];
float[] spo2Array_RED = new float[pSize];


float[] bpmArray = new float[pSize];
float[] rpmArray = new float[pSize];
float[] ppgArray = new float[pSize];


int arrayIndex = 1;

float time = 0;
float sineCurve = 0;

String selectedPort;
boolean gStatus;

/************** Class Objects **********************/

Graph g, g1, g2;
HelpWidget helpWidget;
HeaderButton headerButton;
BPM hr;
RPM rpm1;
SPO2_cal s;

/************** Setup Function **********************/

public void setup() {
  size(800, 480, JAVA2D);  
  createGUI();
  customGUI();

  headerButton = new HeaderButton(0, 0, width, 50);

  helpWidget = new HelpWidget(0, height - 50, width, 68); 
  g = new Graph(10, 60, width-225, 100);
  g1 = new Graph(10, 330, width-220, 100);
  g2 = new Graph(10, 200, width-220, 100);

  setChartSettings();
  for (int i=0; i<pSize; i++) 
  {
    time = time + 1;
    xdata[i]=time;
    ecgdata[i] = 0;
  }
  time = 0;
  g.GraphColor = color(0, 255, 0);
  g1.GraphColor = color(0, 191, 255);
  g2.GraphColor = color(255, 255, 0);
  hr = new BPM();
  rpm1 = new RPM();
  s = new SPO2_cal();
}

public void draw() {
  background(0);
  while (portSelected == true && serialSet == false)
  {
    startSerial();
  }
  g.DrawAxis();
  if (startPlot)
  {
    g.LineGraph(xdata, ecgdata);
    g1.LineGraph(xdata, respdata);
    g2.LineGraph(xdata, ppgArray);
  } else
  {
    bpm1.setText("---");
    BP.setText("---/---");
    Temp.setText("---");
    SP02.setText("---");
    rpm.setText("---");
  }

  stroke(255);
  strokeWeight(2);
  line(595, 0, 595, height-40);
  line(595, 130, width, 130);
  line(595, 210, width, 210);
  line(595, 305, width, 305);
  line(695, 305, 695, height-40);

  headerButton.draw();
  helpWidget.draw();
}

/*********************************************** Opening Port Function ******************************************* **************/

void startSerial()
{
  try
  {
    port = new Serial(this, selectedPort, 57600);
    port.clear();
    serialSet = true;
    msgs = "Port "+selectedPort+" is opened Click Start button";
    portName = "\\"+selectedPort+".txt";
    startPlot = true;
  }
  catch(Exception e)
  {
    msgs = "Port "+selectedPort+" is busy";
    showMessageDialog(null, "Port is busy", "Alert", ERROR_MESSAGE);
    System.exit (0);
  }
}

/*********************************************** Serial Port Event Function *********************************************************/

void serialEvent (Serial blePort) 
{
  Serialevent = true;
  inString = blePort.readChar();
  ecsProcessData(inString);
}

/*********************************************** Getting Packet Data Function *********************************************************/

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
      ecs_rx_state=CESState_Init;
    break;

  case CESState_SOF2_Found:
    ecs_rx_state = CESState_PktLen_Found;
    CES_Pkt_Len = (int) rxch;
    CES_Pkt_Pos_Counter = CES_CMDIF_IND_LEN;
    CES_Data_Counter = 0;
    break;

  case CESState_PktLen_Found:
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
        CES_Pkt_Data_Counter[CES_Data_Counter++] = (char) (rxch);
      }
    } else  //All header and data received
    {
      if (rxch==CES_CMDIF_PKT_STOP)
      {     

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

        int Temp_Value = (int) CES_Pkt_Data_Counter[16];
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

        time = time+1;
        xdata[arrayIndex] = time;

        ecgdata[arrayIndex] = (float)ecg;
        respdata[arrayIndex]= (float)resp;
        spo2data[arrayIndex] = (float)spo2;
        bpmArray[arrayIndex] = (float)ecg;
        rpmArray[arrayIndex] = (float)resp;
        ppgArray[arrayIndex] = (float)spo2;

        arrayIndex++;

        if (arrayIndex == pSize)
        {  
          arrayIndex = 0;
          time = 0;

          if (startPlot)
          {
            hr.bpmCalc(bpmArray);
            s.rawDataArray(spo2Array_IR, spo2Array_RED, irAvg, redAvg);
            BP.setText("---/---");
            Temp.setText("---");
            rpm.setText("---");
          }
        }       

        maxe = max(ecgdata);
        mine = min(ecgdata);
        maxr = max(respdata);
        minr = min(respdata);
        maxs = max(spo2data);
        mins = min(spo2data);

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

        if (logging == true)
        {
          try {
            date = new Date();
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            output = new FileWriter(jFileChooser.getSelectedFile(), true);
            bufferedWriter = new BufferedWriter(output);
            bufferedWriter.write(dateFormat.format(date)+" : " +ecg);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
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


void setChartSettings() {
  g.xLabel="";
  g.yLabel="";
  g.Title="";  
  g.xDiv=11; 
  g.xMax1=pSize; 
  g.xMin1=0;  
  g.yMax1=1300; 
  g.yMin1=-400;

  g1.xLabel="";
  g1.yLabel="";
  g1.Title="";  
  g1.xDiv=10;  
  g1.xMax1=pSize; 
  g1.xMin1=0;  
  g1.yMax1=20; 
  g1.yMin1=0;

  g2.xLabel="";
  g2.yLabel="";
  g2.Title="";  
  g2.xDiv=10;  
  g2.xMax1=pSize; 
  g2.xMin1=0;  
  g2.yMax1=10; 
  g2.yMin1=-10;
}

// Use this method to add additional statements
// to customise the GUI controls
public void customGUI() {
  comList = port.list();
  String comList1[] = new String[comList.length+1];
  comList1[0] = "SELECT THE PORT";
  for (int i = 1; i <= comList.length; i++)
  {
    comList1[i] = comList[i-1];
  }
  comList = comList1;
  portList.setItems(comList1, 0);
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
 
  la.setLocalColor(2, color(255));                                  // Grid Status
  la.setFont(new Font("Arial", Font.PLAIN, 18));
  label10.setLocalColor(2, color(255));                                  // Grid Size
  label10.setFont(new Font("Arial", Font.PLAIN, 18));
  
  settings.setVisible(false);
  grid_size.setVisible(false);
  
}

double averageValue(float dataArray[])
{

  float total = 0;
  for (int i=0; i<dataArray.length; i++)
  {
    total = total + dataArray[i];
  }

  return total/dataArray.length;
}