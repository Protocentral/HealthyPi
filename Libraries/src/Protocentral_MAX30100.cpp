#include "Arduino.h"
#include <Wire.h>
#include "Protocentral_MAX30100.h"


void PULSE_MAX30100::setLEDsPulseWidth(uint8_t pw){
 
  I2CwriteByte(MAX30100_ADDRESS, MAX30100_SPO2_CONFIG,  pw);     // Mask LED_PW
}

void PULSE_MAX30100::setLEDsCurrent(uint8_t red, uint8_t ir){
 
  I2CwriteByte(MAX30100_ADDRESS, MAX30100_LED_CONFIG, (red<<4) | ir); // write LED configs
}

void PULSE_MAX30100::setSampleRate(uint8_t sr){
  I2CwriteByte(MAX30100_ADDRESS, MAX30100_SPO2_CONFIG, reg | (sr<<2)); // Mask SPO2_SR
  reg = I2CreadByte(MAX30100_ADDRESS, MAX30100_MODE_CONFIG);
  reg = reg & 0xf8; // Set Mode to 000
  I2CwriteByte(MAX30100_ADDRESS, MAX30100_SPO2_CONFIG, reg | 0x03); // Mask MODE
}

int PULSE_MAX30100::getNumSamp(void){
    uint8_t wrPtr = I2CreadByte(MAX30100_ADDRESS, MAX30100_FIFO_WR_PTR);
    uint8_t rdPtr = I2CreadByte(MAX30100_ADDRESS, MAX30100_FIFO_RD_PTR);
    return (abs( 16 + wrPtr - rdPtr ) % 16);
}

void PULSE_MAX30100::readRED_IR(void){
  uint8_t temp[4] = {0};  // Temporary buffer for read values
  I2CreadBytes(MAX30100_ADDRESS, MAX30100_FIFO_DATA, &temp[0], 4);  // Read four times from the FIFO
  IR = (temp[0]<<8) | temp[1];    // Combine values to get the actual number
  RED = (temp[2]<<8) | temp[3];   // Combine values to get the actual number
}

void PULSE_MAX30100::shutdown(void){
  uint8_t reg = I2CreadByte(MAX30100_ADDRESS, MAX30100_MODE_CONFIG);  // Get the current register
  I2CwriteByte(MAX30100_ADDRESS, MAX30100_MODE_CONFIG, reg | 0x80);   // mask the SHDN bit
}

void PULSE_MAX30100::reset(void){
  uint8_t reg = I2CreadByte(MAX30100_ADDRESS, MAX30100_MODE_CONFIG);  // Get the current register
  I2CwriteByte(MAX30100_ADDRESS, MAX30100_MODE_CONFIG, reg | 0x40);   // mask the RESET bit
}

void MAX30100::startup(void){
  uint8_t reg = I2CreadByte(MAX30100_ADDRESS, MAX30100_MODE_CONFIG);  // Get the current register
  I2CwriteByte(MAX30100_ADDRESS, MAX30100_MODE_CONFIG, reg & 0x7F);   // mask the SHDN bit
}

int PULSE_MAX30100::getID(void){
  return I2CreadByte(MAX30100_ADDRESS, MAX30100_REV_ID);
}

// Wire.h read and write protocols
void PULSE_MAX30100::I2CwriteByte(uint8_t address, uint8_t subAddress, uint8_t data)
{
	Wire.beginTransmission(address);  // Initialize the Tx buffer
	Wire.write(subAddress);           // Put slave register address in Tx buffer
	Wire.write(data);                 // Put data in Tx buffer
	Wire.endTransmission();           // Send the Tx buffer
}

uint8_t PULSE_MAX30100::I2CreadByte(uint8_t address, uint8_t subAddress)
{
	uint8_t data; // `data` will store the register data
	Wire.beginTransmission(address);         // Initialize the Tx buffer
	Wire.write(subAddress);	                 // Put slave register address in Tx buffer
	Wire.endTransmission(false);             // Send the Tx buffer, but send a restart to keep connection alive
	Wire.requestFrom(address, (uint8_t) 1);  // Read one byte from slave register address
	data = Wire.read();                      // Fill Rx buffer with result
	return data;                             // Return data read from slave register
}

void PULSE_MAX30100::readBytes(uint8_t address, uint8_t reg, uint8_t * readdata, uint8_t readcount)
{
	Wire.beginTransmission(address);   
	Wire.write(reg);    
	Wire.endTransmission(false);     
	uint8_t i = 0;
	Wire.requestFrom(address, readcount);  // Read bytes from slave register address
	while (Wire.available())
	{
		dest[i++] = Wire.read(); // Put read results in the Rx buffer
	}
}
