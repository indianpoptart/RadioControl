package com.nikhilparanjape.radiocontrol

import com.topjohnwu.superuser.BusyBox
import com.topjohnwu.superuser.ContainerApp
import com.topjohnwu.superuser.Shell

class RadioControl: ContainerApp() {

    override fun onCreate() {
        super.onCreate()
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        Shell.Config.verboseLogging(BuildConfig.DEBUG)

        // Use libsu's internal BusyBox
        BusyBox.setup(this)
    }
}