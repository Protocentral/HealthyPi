ProtoCentral HealthyPi HAT for Raspberry Pi
==========

![Wave Form in Processing using Raspberry Pi](https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/rpi_healthypi.jpg)

This HAT for Raspberry Pi 3 / 2 includes vital patient monitoring including ECG, respiration, pulse oximetry and features options to add blood pressure and body temperature sensing.

This is a one-of-a-kind, all-in-one HAT for your Raspberry Pi 2 /3 computer. Just plug it into the Raspberry Pi, install the graphical visualisation software and start using. Unlike available shields and breakouts that we carry for Arduino, this board combines all the functions into a a single board.

Repository Contents
-------------------
* **/firmware** - Atmel studio  and example sketches.
* **/Hardware** - All Eagle design files (.brd, .sch)
* **/extras** - includes the datasheet
* **/gui** - HealthyPi GUI for Raspberry Pi and PC
* **/docs** - Additional Documentation

Board Overview
--------------
![Healthy Pi Hat](https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/healthypi.jpg)

Getting Started:
---------------

Mount ProtoCentral's Healthy PI Hat carefully onto the Raspberry Pi Board's GPIO Pins. And connect this setup with the Raspberry Pi's Display or HDMI Monitor. The Hat communicate with Raspberry Pi using UART interface. Connect the ECG electrodes and SPO2 Probe to the ProtoCentral's Healthy PI Hat.

It's now easy to get started with our brand-new installation script. Just follow the following steps on your Raspberry Pi.

To start, open up the terminal window (Menu -> Accessories -> Terminal) on Raspbian running on your Raspberry Pi:

![Open the terminal](extras/Images/terminal.jpg)

In the terminal window, just type this:

```bash
curl -sS http://pi.protocentral.com/pi.sh | sudo bash
```

This code will install all the required overlays, configuration and application files to get the Healthy Pi up and running.

After the script reboots, you should be able to see the GUI display on the screen

![GUI in Processing](extras/Images/gui_healthypi.png)

##This completes the install!

If the above script does not work for you, you can manually do the installation by following these steps:

Setting up Raspberry Pi for UART Communication
----------------------------------------------
The following are the steps involved to get Raspberry Pi ready for a patient monitor.

###Step 1 : Install and Update the OS

* Install the Raspbian OS in Raspberry Pi. The image file can be downloaded from the [Raspberry Pi's official website](https://www.raspberrypi.org/downloads/).

* Once the installation finishes, update the system with the following command in the Terminal:
		
		sudo apt-get update
		sudo apt-get upgrade

###Step 2 : Enable the UART0 device tree overlay

As the ProtoCentral's Healthy Pi Hat communicates with the Raspberry Pi board via GPIO 14/15 which on the Model B, B+ and Pi2 is mapped to UART0. However on the Pi3 these pins are mapped to UART1 since UART0 is now used for the bluetooth module. As UART1 is not stable because it is dependent to clock speed which can change with the CPU load, we have to disable the Bluetooth module and map UART1 back to UART0 (tty/AMA0).

* Run the following command

		sudo nano /boot/config.txt

* Add this line to the end of the file

		dtoverlay=pi3-miniuart-bt

###Step 3 : Disable Serial command line console

* To disable the Serial Console edit the file using

		sudo nano /boot/cmdline.txt 

* Remove the word phase "console=serial0,115200" or "console=ttyAMA0,115200".

* Save the file and close

###Step 4 : Reboot your Raspberry Pi

* Now your Pi is ready to integrate with ProtoCentral's Healthy Pi Hat.

HealthyPi GUI based on Processing
---------------------------------

[Processing](https://processing.org/) has been used to develop the visualization software for the HealthyPi. 

Processing is an open source framework based on Java. The following are the steps to get the visualization software ready for patient monitor:

###Step 1 : Download Processing IDE for your HOST COMPUTER operating system

a. Latest Version of the Processing IDE can be downloaded from the following link:

[Download Processing](https://processing.org/download/?processing)

b. Unzip and install the file once downloaded.

###Step 2 : Download the Processing code for the HealthyPi GUI

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

Firmware:
---------

The board is pre-loaded with the firmware which works stand alone with the Rpi without any firmware development or loading required. 

However, if you want to edit the firmware, you can follow the steps in this [document] (/docs/recompiling-firmware.md)


License Information
-------------------
This product is open source!

Please use, reuse, and modify these files as you see fit. Please maintain attribution to Protocentral and release anything derivative under the same license.
