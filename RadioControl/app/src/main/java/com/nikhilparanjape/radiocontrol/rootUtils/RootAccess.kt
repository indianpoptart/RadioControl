package com.nikhilparanjape.radiocontrol.rootUtils

import java.io.DataOutputStream
import java.io.IOException

/**
 * Created by Nikhil on 12/25/2015.
 */
object RootAccess {
    fun runCommands(commands: Array<String>) {
        val p: Process
        try {
            p = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(p.outputStream)
            for (tmpCmd in commands) {
                os.writeBytes(tmpCmd + "\n") //Sends commands to the terminal
            }
            os.writeBytes("exit\n")
            os.flush()
        } catch (e: IOException) {
            e.printStackTrace()

        }

    }
}
