Name: Pi Audio Player

**This is a Mini Open-Source Project and is basically intended for the rasperry-pi-developing community**

You can find the repository to the code here:
https://github.com/Hyper-Node/SSHMediaPlayer

Description:
App which connects from Android to Raspberry Pi (or any another Linux device) to play music. Music is played with mpc (which is a command line media player for linux) on Raspberry. The connection to the Raspberry is made with SSH (Secure Shell).  The Smartphone works as a controller which sends weblinks to the RaspBerry Pi, the weblinks are added to a playlist and played from the internet on the pi.

At the first start the settings pop up. It's mandatory to give the correct ip and ssh-Port of your Raspberry Pi in the network then. Be sure to add username and password if you configured your SSH to use this.

Features:
- Adding music streams and mp3 by pasting a weblink to playlist
- SSH connection to Raspberry Pi
- Volume control with Smartphone audio buttons
- Skip to a percentage of the song with a seekbar
- Saving/reloading preferences

**Requirements (on Android)**
Wifi Connection to Raspberry Pi (Pi is in same LAN)

**Requirements (on Raspberry Pi)**
Raspbian or other Linux-System,   (I tested against Raspbian)
SSH Server active,
Audio-Speakers,
Internet Connection,

mpc, mpd and mp3-Codecs (Install like this):
$ sudo apt-get install mpd mpc
$ sudo apt-get install mpg321 lame

The App uses the jsch SSH library by JCraft(link):
http://www.jcraft.com/jsch/

And the "Android-NewPopupMenu" Library by u1aryz (link):
https://github.com/u1aryz/Android-NewPopupMenu


IDE is Android Studio