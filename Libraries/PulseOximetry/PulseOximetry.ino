#include "MAX30100.h"
#include <Wire.h>

PULSE_MAX30100 pulse;
uint8_t data_len=8;
uint8_t DataPacket[15];
volatile unsigned int IRR,REDD;

void setup() {
  Wire.begin();
  Serial.begin(57600);
  while(!Serial);
  pulse.setPulseWidth(PULSE_WIDTH_1600u);             			 // set pulse width to 1.6mS
  pulse.setLEDsCurrent(RED_LED_CURRENT_40mA,IR_LED_CURRENT_40mA);  //  led current
  pulse.setSampleRate(SAMPLE_RATE_100);  //  sample rate
}

void loop() {

    pulse.readRED_IR();   // read the sensor
    IRR = pulse.IR;       // get the IR value
    REDD=pulse.RED;		  // get the RED value
	
	// Packet format for the GUI
    DataPacket[0] = 0x0A;   //  packet header byte0
    DataPacket[1] = 0xFA;   //  packet header byte1
	
    DataPacket[2] = (uint8_t) (data_len);     // data length lsb
    DataPacket[3] = (uint8_t) (data_len>>8);  // data length msb 
    DataPacket[4] = 0x02;                     // packet type
   
    DataPacket[5] = REDD;                    // RED photo diode data
    DataPacket[6] = REDD>>8;
    DataPacket[7] = REDD>>16;
    DataPacket[8] = REDD>>24; 

    
    DataPacket[9] = IRR;					 // IR photo diode data
    DataPacket[10] = IRR>>8;
    DataPacket[11] = IRR>>16;
    DataPacket[12] = IRR>>24; 

    DataPacket[13] = 0x00;                // packet footer byte0
    DataPacket[14] = 0x0b;				// packet footer byte1				

    for(int i=0; i<15; i++) 
    {
       Serial.write(DataPacket[i]); // transmit the data 

     }

  delay(10);
}


