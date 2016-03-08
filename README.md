# RadioControl
![icon](https://nikhilp.org/images/ic_launcher.png)

**Root Required!**

I created this app because of the lack of apps on the play store that enable airplane mode, while keeping wifi on. Probably because not many carriers have WiFi calling and SMS over WiFi

An app that auto toggles wifi and cell radio for maximum battery life

This app will probably only be useful to Project Fi users as it can disable radios while keeping wifi on if.
This could work for Republic Wireless users, but your device must be rooted, and most of their devices are hard to root without losing functionality.

Feel free to ask questions related to Android programming or this project in the public Gitter chat room. The issues page should be primarily used for bug reports and enhancement ideas.

[![Join the chat at https://gitter.im/indianpoptart/RadioControl](https://badges.gitter.im/indianpoptart/RadioControl.svg)](https://gitter.im/indianpoptart/RadioControl?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


# Versions

Latest Stable Release: [v2.0](https://github.com/indianpoptart/RadioControl/releases/latest) (12/28/2015)

Current Release Candidate [v3.0-rc1](https://github.com/indianpoptart/RadioControl/releases/tag/v3.0-rc1) (3/7/2016)

Current Beta Release: [v3.0-beta9](https://github.com/indianpoptart/RadioControl/releases/tag/v3.0-beta9) (3/3/2016)

Current Alpha Release: [v2.1-alpha7](https://github.com/indianpoptart/RadioControl/releases/tag/v2.1-alpha7) (2/8/2016)





# New Features
###### New network notification
<img src="https://nikhilp.org/images/notificationPic.png" width="384">

# Compatibility
Tested on the following devices
- Nexus 6 ![Motorola](https://nikhilp.org/images/moto.png)
- Nexus 6P ![Huawei](https://nikhilp.org/images/huawei.png)
- Nexus 5X ![LG](https://nikhilp.org/images/lg.png)
- Moto X (2nd Gen.) ![Motorola](https://nikhilp.org/images/moto.png)

# What's New?
v3.0 - ReleaseCandidate 1 (3/7/2016)
- Ads work now
- Ads can be disabled in settings
- Ads will be disabled when a donation is made (Untested)

v3.0 - Beta9 (3/2/2016)
- Testing admob for extra revenue
- Allow user to disable ads because we all hate ads
- Possible fix for non working IAP

v3.0 - Beta8 (2/29/2016)
- Ping test now runs in an AsyncTask (Main screen one sends 3 packets now)
- New progressbar(circle) for AsyncTask
- Testing the disconnect from wifi with asynctask, might switch the main root commands to this method

v3.0 - Beta7 (2/26/2016)
- Feedback button works
- Small performance gain with disabled check

v3.0 - Beta6 (2/22/2016)
- Optimized imports
- Removed send feedback button for now
- Close to stable release

v3.0 - Beta5 (2/21/2016)
- Fixed Developer Features
- Check for internet before donating
- Fixed buy, back, buy again crash
- Donation toasts work now
- Finally incrementing version code along with version name

v3.0 - Beta4 (2/21/2016)
- Added donate button in drawer
- Donate has 4 tiers ($0.99, $2.99, $4.99, $9.99)

v3.0 - Beta3 (2/20/2016)
- Fixed toggle switch bug
- Fixed WiFi Receiver/Network Settings null bug
- Added non-working feedback button
- Changed easter egg to developer feature

v3.0 - Beta2 (2/12/2016)
- Added help wiki link in about page
- Internet alert seems to be working without problem. Still experimental/being tested

v3.0 - Beta1 (2/10/2016)
- v2.1 became v3.0 because I added a non backwards compatible feature
- Cleaned up code
- Organized WifiReceiver
- WifiReceiver speed increase
- All settings are marked as experimental
- Network alert gets called while launching, therefore, it gets called each time you join wifi
- Cleaned up imports

v2.1 - Alpha7 (2/8/2016)
- Removed the SSID Disabler from main screen (Caused many many issues)
- Settings SSID disabler semi works
- Cleaned up some wording issues in Settings menu
- Added option to get network alerts even if the app is set to off.
- Added easter egg disabler

v2.1 - Alpha6 (2/6/2015)
- v2.0.1 becomes v2.1 (A lot of stuff added, so not just a patch anymore)
- Added notification if network isn't connected to the internet
- Network settings semi work. Main screen one is still the one to use right now
- Cleaned up some imports
- More testing needed

v2.0.1 - Alpha5 (2/3/2016)
- Added LinkSpeed as an easter egg thing
- Added a ping test, It tests if one packet can reach Google DNS servers. It then displays if the current network is connected to the internet
- Added a utilities class that has a bunch of connection tools (Future features maybe, mainly for debug)
- Forgot about onResume, put that in to check easter egg and the toggle switch (Still WIP)
- Enabled multidex for some reason

v2.0.1 - Alpha4 (2/1/2016)
- Added Linkspeed test, maybe for a future function
- Added easter egg :)
- Changed all Log.d to show up as radio control, easier to find in logcat.

v2.0.1 - Alpha3 (1/30/2016)
- Added about preferencescreen
- Working clear ssid list button in settings preferenceScreen

v2.0.1 - Alpha2 (1/25/2016)
- SSID List in settings gets filled up by configurednetworks
- Added non-working clear button

v2.0.1 - Alpha1 (1/23/2016)
- Added an on off switch so the user can decide whether or not they want
to enable RadioControl or not.
- (WIP) Added ssid disabler in settings. Main screen one is still
active. The one in settings does not work

v2.0 (12/29/2015)
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
- Choose SSIDs from a list
- ~~Whats new changelog~~ - Completed in v1.4
- ~~Request root access at the start of the app so the app works when its needed, instead of waiting for it to be granted~~ - Completed in v2.0
- ~~Disable only cell radio so the app doesn't have to reenable WiFi after enabling Airplane mode~~ - Completed in v2.0
- ~~Check if user is in call~~ - Completed in v1.3

# Credits
Thanks to [Mike Penz](https://github.com/mikepenz) for the Material Drawer design
