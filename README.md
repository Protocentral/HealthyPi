ProtoCentral HealthyPi HAT for Raspberry Pi
==========

![Wave Form in Processing using Raspberry Pi](https://github.com/Protocentral/HealthyPi/blob/master/extras/Images/rpi_healthypi.jpg)

This HAT for Raspberry Pi 3 / 2 includes vital patient monitoring including ECG, respiration, pulse oximetry and features options to add blood pressure and body temperature sensing.

This is a one-of-a-kind, all-in-one HAT for your Raspberry Pi 2 /3 computer. Just plug it into the Raspberry Pi, install the graphical visualisation software and start using. Unlike available shields and breakouts that we carry for Arduino, this board combines all the functions into a a single board.

You can buy [HealthyPi at ProtoCentral](https://www.protocentral.com/healthypi)

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

If the above script does not work for you and would like to do a manual installation or would like to customize the code, check out [Advanced HealthyPi] (/docs/advanced-healthypi.md).

License Information
-------------------
This product is open source!

Please use, reuse, and modify these files as you see fit. Please maintain attribution to Protocentral and release anything derivative under the same license.
