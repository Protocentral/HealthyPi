HEALTHY PI
==========

![Wave Form in Processing using Raspberry Pi](https://github.com/Protocentral/HealthyPi/blob/master/Processing/Final-Output/RPI_HealthyPi.jpg)

Raspberry Pi boards are used in many applications like Media streamer, Arcade machine, Tablet computer, Home automation, many more. And now Raspberry Pi can be used as patient monitor with the help of ProtoCentral's Healthy Pi Hat. This patient monitor is portable and efficient in monitoring ECG, Heart Rate, SPO2, Respiration, Temperature and Blood Pressure of a patient. This project shows how to use Raspberry Pi Board along with the ProtoCentral's Healthy PI Hat to change into Patient Monitor.

Repository Contents
-------------------
* **/Libraries** - Atmel studio  and example sketches.
* **/Hardware** - All Eagle design files (.brd, .sch)
* **/extras** - includes the datasheet
* **/Processing** - setup  and library file

Setting up Raspberry Pi for UART Communication
----------------------------------------------
The following are the steps involved to get Raspberry Pi ready for a patient monitor.

### 1 : Install and Update the OS

*Install the Raspbian OS in Raspberry Pi. The image file can be downloaded from the Raspberry Pi's official website.
*Once the installation finishes, update the system with the following command in the Terminal.

sudo apt-get update

sudo apt-get upgrade

### 2 : Enable the Serial Connection

*Serial communication should be enabled to interface with the ProtoCentral Healthy PI HAT.
*Run the configuration command and follow the instructions below:

		sudo raspi-config

![Enable Serial Port]
(https://github.com/Protocentral/HealthyPi/blob/master/Processing/Final-Output/advance_option.png)

![Enable Serial Port]
(https://github.com/Protocentral/HealthyPi/blob/master/Processing/Final-Output/Serial_option.png)

![Enable Serial Port]
(https://github.com/Protocentral/HealthyPi/blob/master/Processing/Final-Output/enable_serial.png)

![Enable Serial Port]
(https://github.com/Protocentral/HealthyPi/blob/master/Processing/Final-Output/ok.png)

![Enable Serial Port]
(https://github.com/Protocentral/HealthyPi/blob/master/Processing/Final-Output/reboot.png)

![Wave Form in Processing](https://github.com/Protocentral/HealthyPi/blob/master/Processing/Final-Output/HealthyPi.png)


License Information
-------------------
This product is open source!

Please use, reuse, and modify these files as you see fit. Please maintain attribution to Protocentral and release anything derivative under the same license.
