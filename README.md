ProtoCentral HealthyPi HAT for Raspberry Pi
==========

![Wave Form in Processing using Raspberry Pi](https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/rpi_healthypi.jpg)

Raspberry Pi boards are used in many applications like Media streamer, Arcade machine, Tablet computer, Home automation, many more. And now Raspberry Pi can be used as patient monitor with the help of ProtoCentral's Healthy Pi Hat. This patient monitor is portable and efficient in monitoring ECG, Heart Rate, SPO2, Respiration, Temperature and Blood Pressure of a patient. This project shows how to use Raspberry Pi Board along with the ProtoCentral's Healthy PI Hat to change into Patient Monitor.

Repository Contents
-------------------
* **/firmware** - Atmel studio  and example sketches.
* **/Hardware** - All Eagle design files (.brd, .sch)
* **/extras** - includes the datasheet
* **/gui** - HealthyPi GUI for Raspberry Pi and PC

Board Overview
--------------
![Healthy Pi Hat](https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/Healthy%20Pi.png)

How To Get Started:
---------------

The board is pre-loaded with the firmware which works stand alone with the Rpi with fewer setup. However you will need some software. Here's what we're using:
* **Atmel Studio 7** :
Please note that Atmel Studio 7 does not support Mac and Linux . This is the IDE software that can do step & memory debugging and its only for Windows. Also you have to make an account on Atmel's site. The IDE can be downloaded from [Atmel Studio's Official Website](http://www.atmel.com/tools/atmelstudio.aspx)

**Load the Healthy Pi Source Code**

* **Step 1 :** Start by launching Atmel Studio 7.

* **Step 2 :** Open the Project [navigate the path where the project is saved].

* **Step 3 :** You'll see the following, where the sketch is in a window, you can edit the code here if you like.

**Debugger**

You need a debugger to load the code into the SAMD21G18A. Atmel-ICE will help get from here http://www.atmel.com/tools/atatmel-ice.aspx

**Step 1 : Set Up and Check Interface**
* Attach the chip & debugger

**Step 2 : Identify Interface**
* OK now you have your debugger plugged in, its good to check that it works, select Device Programming.
* Choose "Device Programmer" from "Tools" Menu
* Programming window will be open.
* Select the programmer from the drop down box. Select interface as “SWD”. Click on “Apply”.
* Now read the Power and Device ID. If you are able to read the Device ID then the interface is fine.

**Step 3 : Build & Start Debugging**
* Ok close out the modal programming window and build the program. If there are no error, then your build will be successful.

**Step 4 : Run & Upload**
* You will see a window appearing to select a debugging tool. Select the tool and interface.
* Once done, go back and Re-run Program without debugging.


Setting up Raspberry Pi for UART Communication
----------------------------------------------
The following are the steps involved to get Raspberry Pi ready for a patient monitor.

###Step 1 : Install and Update the OS

* Install the Raspbian OS in Raspberry Pi. The image file can be downloaded from the [Raspberry Pi's official website](https://www.raspberrypi.org/downloads/).

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
(https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/Upload_To_Pi.png)

* Now, connect your raspberry pi to the internet with the same network as your laptop is connected.

* Select "Tools" menu and choose "Upload to Pi" option from the list.

* If any error occured in uploading the code, check for the internet connection and repeat the process.

Connecting the Raspberry Pi to ProtoCentral's Healthy PI Hat
------------------------------------------------------------

Mount ProtoCentral's Healthy PI Hat carefully onto the Raspberry Pi Board's GPIO Pins. And connect this setup with the Raspberry Pi's Display or HDMI Monitor. The Hat communicate with Raspberry Pi using UART interface. Connect the ECG electrodes and SPO2 Probe to the ProtoCentral's Healthy PI Hat.

Now you have your own patient monitor with the help of Raspberry PI and ProtoCentral's Helathy PI Hat.

![GUI in Processing](https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/gui_healthypi.png)


License Information
-------------------
This product is open source!

Please use, reuse, and modify these files as you see fit. Please maintain attribution to Protocentral and release anything derivative under the same license.
