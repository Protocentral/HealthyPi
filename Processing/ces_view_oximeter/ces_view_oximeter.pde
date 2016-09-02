// Need G4P library
import processing.serial.*;
import g4p_controls.*;
import java.awt.*;
import javax.swing.JFileChooser;

import java.math.*;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Date;
import static javax.swing.JOptionPane.*;

import java.io.FileReader;
import java.io.BufferedReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import signal.library.*;

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
int CES_Pkt_Pos_Counter, CES_Pkt_Data_Counter1, CES_Pkt_Data_Counter2;
int CES_Pkt_PktType;
char DataRcvPacket1[] = new char[1000];
char DataRcvPacket2[] = new char[1000];

/************** ControlP5 Related Variables **********************/

int colorValue;
HelpWidget helpWidget;
HeaderButton headerButton;
MessageBox msgBox;
SPO2_cal s;
boolean visibility=false;

/************** Graph Related Variables **********************/

double maxAxis_ir, minAxis_ir, maxAxis_red, minAxis_red;
double receivedVoltage_RED, receivedVoltage_IR;
BigDecimal min, max;

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
String selectedPort;

/************** Logo Related Variables **********************/

PImage logo;

/************** General Variables **********************/

boolean startPlot, Serialevent = false;
String msgs;
int startTime = 0;

int pSize = 400;
float[] xdata = new float[pSize];
float[] ydata = new float[pSize];
float[] AvgYdata = new float[pSize];
float[] zdata = new float[pSize];
float[] AvgZdata = new float[pSize];
int arrayIndex = 1;
Graph g, g1;
float time =0;
BigDecimal avg, rms, a;
double additionFactor_red, additionFactor_ir;
float value1, value2;
float RedAC = 0, RedDC = 0, IrAC = 0, IrDC = 0;
SignalFilter myFilter;

public void setup() {
  size(1000, 700, JAVA2D);
  //fullScreen();
  createGUI();
  customGUI();
  // Place your setup code here

  date = new Date();
  logo = loadImage("logo.png");

  headerButton = new HeaderButton(0, 0, width, 60);
  helpWidget = new HelpWidget(0, height - 30, width, 40); 
  msgBox = new MessageBox();
  s = new SPO2_cal();
  g = new Graph(100, 100, width-120, 200);
  g1 = new Graph(100, 350, width-120, 200);
  setChartSettings();
  for (int i=0; i<pSize; i++) 
  {
    time = time + 2;
    xdata[i]=time;
    ydata[i] = 0;
    zdata[i] = 0;
  }
  time = 0;
  g.GraphColor = color(0, 255, 0);
  g.Title = "RED";
  g1.GraphColor = color( 0, 255, 0);
  g1.Title = "IR";
  myFilter = new SignalFilter(this);
}

/*********************************************** Draw Function *********************************************************/

public void draw() {
  background(0);
  while (portSelected == true && serialSet == false)
  {
    startSerial(comList);
  }
  background(0);
  if (startPlot)
  {
    g.LineGraph(xdata, ydata);
    g1.LineGraph(xdata, zdata);
  }

  g.DrawAxis();
  g1.DrawAxis();

 // msgBox.MessageBoxAxis(0, height - 100, width, 70);
 // msgBox.draw();
  headerButton.draw();
  helpWidget.draw();
}

/*********************************************** Opening Port Function ******************************************* **************/

void startSerial(String[] theport)
{
  try
  {
    port = new Serial(this, selectedPort, 57600);
    port.clear();
    serialSet = true;
    msgs = "Port "+selectedPort+" is opened Click Start button";
    portName = "\\"+selectedPort+".txt";
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
    CES_Pkt_Data_Counter1 = 0;
    CES_Pkt_Data_Counter2 = 0;
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
        if (CES_Pkt_Data_Counter1 < 4)                           
        {
          DataRcvPacket1[CES_Pkt_Data_Counter1]= (char) (rxch);
          CES_Pkt_Data_Counter1++;
        } else
        {
          DataRcvPacket2[CES_Pkt_Data_Counter2]= (char) (rxch);
          CES_Pkt_Data_Counter2++;
        }
      }
    } else  //All header and data received
    {
      if (rxch==CES_CMDIF_PKT_STOP)
      {     
        int data1 = ecsParsePacket(DataRcvPacket1, DataRcvPacket1.length-1);
        int data2 = ecsParsePacket(DataRcvPacket2, DataRcvPacket2.length-1);
        receivedVoltage_RED = data1 * (0.00000057220458984375) ;
        receivedVoltage_IR = data2 * (0.00000057220458984375) ;

        time = time+0.1;
        xdata[arrayIndex] = time;
        
        //receivedVoltage_RED = myFilter.filterUnitFloat((float)receivedVoltage_RED);
        //receivedVoltage_IR = myFilter.filterUnitFloat((float)receivedVoltage_IR);
        
        
        AvgYdata[arrayIndex] = (float)receivedVoltage_RED;
        AvgZdata[arrayIndex] = (float)receivedVoltage_IR;
        value1 = (float)( AvgYdata[arrayIndex] - averageValue(AvgYdata));
        value2 = (float)( AvgZdata[arrayIndex] - averageValue(AvgZdata));
        ydata[arrayIndex] = value1;
        zdata[arrayIndex] = value2;

        float RedDC = (float) averageValue(AvgYdata);
        float IrDC = (float) averageValue(AvgZdata);

        arrayIndex++;
        if (arrayIndex == pSize)
        {  
          arrayIndex = 0;
          time = 0;
          RedAC = s.SPO2_Value(ydata);
          IrAC = s.SPO2_Value(zdata);
          float value = (RedAC/abs(RedDC))/(IrAC/abs(IrDC));

          /********  Emprical Formalae  *********/
          //float SpO2 = 10.0002*(value)-52.887*(value) + 26.817*(value) + 98.293;
          //  float SpO2 =((0.81-0.18*(value))/(0.73+0.11*(value)));
          float SpO2=110-25*(value);

          SpO2 = (int)(SpO2 * 100);
          SpO2 = SpO2/100;
          oxygenSaturation.setText(SpO2+"");
        }
        if (startPlot) {
        }
        a = new BigDecimal(averageValue(ydata));
        avg = a.setScale(5, BigDecimal.ROUND_HALF_EVEN); 
        a = new BigDecimal(RMSValue(ydata));
        rms = a.setScale(5, BigDecimal.ROUND_HALF_EVEN); 
        a = new BigDecimal(max(ydata));
        max = a.setScale(5, BigDecimal.ROUND_HALF_EVEN); 
        a = new BigDecimal(min(ydata));
        min = a.setScale(5, BigDecimal.ROUND_HALF_EVEN); 
        msgBox.msg(min, max, avg, rms);

        if (logging == true)
        {
          try {
            date = new Date();
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            output = new FileWriter(jFileChooser.getSelectedFile(), true);
            bufferedWriter = new BufferedWriter(output);
            //bufferedWriter.write(dateFormat.format(date)+" : " +receivedVoltage_RED+" , "+receivedVoltage_IR);
            bufferedWriter.write(arrayIndex-1+" , "+value1 +" , "+value2);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
          }
          catch(IOException e) {
            println("It broke!!!");
            e.printStackTrace();
          }
        }

        maxAxis_red = max(ydata);
        minAxis_red = min(ydata);
        // println(maxAxis_red,minAxis_red);
        maxAxis_ir = max(zdata);
        minAxis_ir = min(zdata);

        if (g.yMax != maxAxis_red)
        {
          g.yMax = (float)(maxAxis_red);
        }
        if (g.yMin != minAxis_red)
        {
          g.yMin = (float)(minAxis_red);
        }

        if (g1.yMax != maxAxis_ir)
        {
          g1.yMax = (float)(maxAxis_ir);
        }
        if (g1.yMin != minAxis_ir)
        {
          g1.yMin = (float)(minAxis_ir);
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
  start.setEnabled(false);
  oxygenSaturation.setVisible(false);
  comList = comList1;
  portList.setItems(comList1, 0);

  oxygenSaturation.setFont(new Font("Arial", Font.PLAIN, 55));
  oxygenSaturation.setLocalColor(2, color(255, 255, 255));

  start.setLocalColorScheme(GCScheme.CYAN_SCHEME);
}

void setChartSettings() {
  g.xDiv=10;  
  g.xMax=pSize; 
  g.xMin=0;  
  g.yMax=0.001; 
  g.yMin=0.005;

  g1.xDiv=10;  
  g1.xMax=pSize; 
  g1.xMin=0;  
  g1.yMax=0.001; 
  g1.yMin=0.005;
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

double RMSValue(float dataArray[])
{
  float total = 0;
  for (int i=0; i<dataArray.length; i++)
  {
    total = (float)(total + Math.pow(dataArray[i], 2));
  }
  total /= dataArray.length;
  return Math.sqrt(total);
}