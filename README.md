# RadioControl
![icon](https://nikhilp.org/images/ic_launcher.png)

!!Root Required!!

I created this app because of the lack of apps on the play store that enable airplane mode, while keeping wifi on. Probably because not many carriers have WiFi calling and SMS over WiFi

An app that auto toggles wifi and cell radio for maximum battery life

This app will probably only be useful to Project Fi users as it can disable radios while keeping wifi on if.
This could work for Republic Wireless users, but your device must be rooted, and most of their devices are hard to root without losing functionality.

# Versions

Latest Stable Release: [v1.4](https://github.com/indianpoptart/RadioControl/releases/latest)

Current Alpha Release: [v2.0](https://github.com/indianpoptart/RadioControl/releases/tag/v2.0-alpha3)

Current Beta Release: [N/A]()

# Compatibility
Tested on the following devices
- Nexus 6 ![Motorola](https://nikhilp.org/images/moto.png)
- Nexus 6P ![Huawei](https://nikhilp.org/images/huawei.png)
- Nexus 5X ![LG](https://nikhilp.org/images/lg.png)
- Moto X (2nd Gen.) ![Motorola](https://nikhilp.org/images/moto.png)

#What's New?
v2.0 - Alpha (TBD)
- The app no longer requires the timer, It can turn on airplane mode without turning off WiFi or Bluetooth 
- Timer related bug fixes
- Removed pause timer
- Removed redundant code
- Drawer now works as intended
- Added a scroll feature to whats new dialog
- Added dark theme(Replacing light theme until theme chooser is in place)

v1.4 (12/15/2015)
- Adding the ability to choose a Wifi network you don't want the app to
work on
- Made sharedprefs for all preferences the same pref xml
- Added wifi network disabler so the app will enable or disable itself for certain wifi networks.

v1.3.1 (N/A)
- Added a whats new dialog just for cosmetics

v1.3 (12/12/2015)
- Added feature that checks if user is in a call and roams over to WiFi. The app will wait for the phone call to end to switch to airplane mode
- Changed default timer from 10 to 15 seconds

v1.2 (11/15/2015)
- Added an experimental delay, default is 10 seconds, 15 seconds is the best
- Bug fixes
- Removed wake lock permission, not used
- Fixed redundant code

v1.1.1 (11/9/2015)
- Fixed bluetooth infinite loop problem
- Bug fixes
- Organized code

v1.1 (11/8/2015)
- App now works automatically, no need to open the app
- Added bluetooth support, it will be enabled or disabled as needed
- Bug fixes
- Organized code

#Upcoming Features
- ~~Whats new changelog~~ - Completed in v1.4
- Request root access at the start of the app so the app works when its needed, instead of waiting for it to be granted
- Choose what SSID you want the app to toggle onto - Completed in v2.0 alpha
- Disable only cell radio so the app doesn't have to reenable WiFi after enabling Airplane mode - Completed in v2.0 alpha
- ~~Check if user is in call~~ - Completed in v1.3

# Credits
Thanks to [Mike Penz](https://github.com/mikepenz) for the Material Drawer design

