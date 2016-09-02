
// Registers
#define MAX30100_INT_STATUS     0x00  
#define MAX30100_INT_ENABLE     0x01  
#define MAX30100_FIFO_WR_PTR    0x02  
#define MAX30100_OVRFLOW_CTR    0x03  
#define MAX30100_FIFO_RD_PTR    0x04  
#define MAX30100_FIFO_DATA      0x05  
#define MAX30100_MODE_CONFIG    0x06  
#define MAX30100_SPO2_CONFIG    0x07  
#define MAX30100_LED_CONFIG     0x09  
#define MAX30100_TEMP_INTG      0x16  
#define MAX30100_TEMP_FRAC      0x17  
#define MAX30100_REV_ID         0xFE  
#define MAX30100_PART_ID        0xFF  

#define MAX30100_SLAVE_ADDRESS        0x57  // 8bit address converted to 7bit

#define SAMPLE_RATE_50	0	
#define SAMPLE_RATE_100	1	
#define SAMPLE_RATE_167	2	
#define SAMPLE_RATE_200	3	
#define SAMPLE_RATE_400	4	
#define SAMPLE_RATE_600	5	
#define SAMPLE_RATE_800	6	
#define SAMPLE_RATE_100	7

#define PULSE_WIDTH_200u		0
#define PULSE_WIDTH_400u		1	
#define PULSE_WIDTH_800u		2	
#define PULSE_WIDTH_1600u		3	

#define LED_CURRENT_0mA   	0
#define LED_CURRENT_4mA   	1
#define LED_CURRENT_8mA   	2
#define LED_CURRENT_11mA  	3
#define LED_CURRENT_14mA  	4
#define LED_CURRENT_17mA  	5
#define LED_CURRENT_21mA  	6
#define LED_CURRENT_24mA  	7
#define LED_CURRENT_27mA  	8
#define LED_CURRENT_31mA  	9
#define LED_CURRENT_34mA  	10
#define LED_CURRENT_37mA  	11
#define LED_CURRENT_40mA  	12
#define LED_CURRENT_44mA  	13
#define LED_CURRENT_47mA  	14
#define LED_CURRENT_50mA  	15


class PULSE_MAX30100 {
public:
  uint16_t IR = 0;      
  uint16_t RED = 0;     

  PULSE_MAX30100();
  void  setPulsewidth(pulseWidth pw)  // Sets the LED state
  void  setSPO2(sampleRate sr); // Setup the SPO2 sensor, disabled by default
  int   getNumSamp(void);       // Get number of samples
  void  readSensor(void);       // Updates the values
  void  shutdown(void);   // Instructs device to power-save
  void  reset(void);      // Resets the device
  int   getID(void);   // Gets revision ID

private:
  void    I2CwriteByte(uint8_t address, uint8_t subAddress, uint8_t data);
  uint8_t I2CreadByte(uint8_t address, uint8_t subAddress);
  void    I2CreadBytes(uint8_t address, uint8_t subAddress, uint8_t * dest, uint8_t count);
};
