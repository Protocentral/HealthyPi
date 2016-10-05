HEALTHY PI
==========

![Wave Form in Processing using Raspberry Pi](https://github.com/Protocentral/HealthyPi/blob/master/Processing/output/RPI_HealthyPi.jpg)

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

###Step 1 : Install and Update the OS

* Install the Raspbian OS in Raspberry Pi. The image file can be downloaded from the Raspberry Pi's official website.

* Once the installation finishes, update the system with the following command in the Terminal:
		
		sudo apt-get update
		sudo apt-get upgrade

###Step 2 : Enable the Serial Connection

* Serial communication should be enabled to interface with the ProtoCentral Healthy PI HAT.

* Run the configuration command and follow the instructions below:

		sudo raspi-config

![Enable Serial Port]
(https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/advance_option.png)

![Enable Serial Port]
(https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/Serial_option.png)

![Enable Serial Port]
(https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/enable_serial.png)

![Enable Serial Port]
(https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/ok.png)

![Enable Serial Port]
(https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/reboot.png)

###Step 3 : Disable onboard Pi3 Bluetooth and restore UART0/ttyAMA0

As the ProtoCentral's Healthy Pi Hat communicates with the Raspberry Pi board via GPIO 14/15 which on the Model B, B+ and Pi2 is mapped to UART0. However on the Pi3 these pins are mapped to UART1 since UART0 is now used for the bluetooth module. As UART1 is not stable because it is dependent to clock speed which can change with the CPU load, we have to disable the Bluetooth module and map UART1 back to UART0 (tty/AMA0).

* Run the following command

		sudo nano /boot/config.txt

* Add this line to the end of the file

		dtoverlay=pi3-miniuart-bt

* Save the file and Reboot the Pi.
* To disable the Serial Console edit the file using

		sudo nano /boot/cmdline.txt 

* Remove the word phase "console=serial0,115200" or "console=ttyAMA0,115200".
* Save and Exit the file and Reboot the Pi.
* Now the your Pi is ready to integrate with ProtoCentral's Healthy Pi Hat.

Visualization Software For Patient Monitor
------------------------------------------

Processing IDE is used as visualization software for the patient monitor. It is an open source framework based on Java. The following are the steps to get the visualization software ready for patient monitor:

###Step 1 : Download Processing IDE for your operating system

a. Latest Version of the Processing IDE can be downloaded from the following links:

* [MAC] (http://download.processing.org/processing-3.2.1-macosx.zip)
* [Linux 32-bit] (http://download.processing.org/processing-3.2.1-linux32.tgz)
* [Linux 64-bit] (http://download.processing.org/processing-3.2.1-linux64.tgz)
* [Windows 32-bit] (http://download.processing.org/processing-3.2.1-windows32.zip)
* [Windows 64-bit] (http://download.processing.org/processing-3.2.1-windows64.zip)

b. Unzip the file once downloaded.

###Step 2 : Download the Processing code for Patient Monitor Visualization

a. Download the necessary files & directories or clone to your desktop from GitHub.

b. Unzipping the archive should make a folder by name HealthyPi that contains the visualisation code.

c. Locate the Processing sketchbook directory on your computer. This should have been created automatically when you installed processing. Depending on your operating system, the path will be as follows:

* On Windows : C:/My Documents/Processing
* On MAC : /Users/your_user_name/Documents/Processing
* On Linux : /Home/your_user_name/sketchbook/

**Note:** This directory appears as "Processing" on Windows/Mac, and goes by the name "Sketchbook" on Linux. Create a subdirectory by name "libraries if one doesn't exist already.

d. From the above mentioned "HealthyPi" directory Copy/Move the contents of the HealthyPi/Processing/HealthyPi folder to the Processing sketchbook directory which is also mentioned above (Locate the Processing sketchbook)

e. Finally, copy the "controlP5 & G4P" folders from HealthyPi/Processing/libraries/ and paste them into the libraries directory of your Processing sketchbook.

###Step 3 : Upload the Processing code Raspberry Pi

* In Processing IDE, Select "Tools" from the menu and choose, "Add tools".

* Select "Upload To Pi" tool and click Install button which is present in the button right corner as shown in the below image.

![Upload To Pi in Processing]
(https://github.com/Protocentral/HealthyPi/blob/master/Processing/Final-Output/Upload%20To%20Pi.png)

* Now, connect your raspberry pi to the internet with the same network as your laptop is connected.

* Select "Tools" menu and choose "Upload to Pi" option from the list.

* If any error occured in uploading the code, check for the internet connection and repeat the process.

Connecting the Raspberry Pi to ProtoCentral's Healthy PI Hat
------------------------------------------------------------

Mount ProtoCentral's Healthy PI Hat carefully onto the Raspberry Pi Board's GPIO Pins. And connect this setup with the Raspberry Pi's Display or HDMI Monitor. The Hat communicate with Raspberry Pi using UART interface. Connect the ECG electrodes and SPO2 Probe to the ProtoCentral's Healthy PI Hat.

Now you have your own patient monitor with the help of Raspberry PI and ProtoCentral's Helathy PI Hat.

![Wave Form in Processing](https://github.com/Protocentral/HealthyPi/blob/master/Processing/output/HealthyPi.png)


License Information
-------------------
This product is open source!

Please use, reuse, and modify these files as you see fit. Please maintain attribution to Protocentral and release anything derivative under the same license.
