package com.innoli.firstthing

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Created by yli on 5/26/2017.
 */
class UltraSonicModule : Activity() {

    val TAG = UltraSonicModule::class.java.name

    val ECHO_PIN = "BCM21"

    val TRIG_PIN = "BCM19"

    var echoPin : Gpio? = null

    var triggerPin : Gpio? = null

    var time1 : Long = 0

    var time2 : Long = 0

    var callbackHandler : Handler? = null

    val callback = object : GpioCallback() {
        var time1 : Long = 0

        var time2 : Long = 0

        override fun onGpioEdge(gpio : Gpio) : Boolean {
            if (!gpio.value) {
                time2 = System.nanoTime()

                val pluseWidth = time2 - time1

                val distance = (pluseWidth / 1000000000.0) * 340.0 / 2.0 * 100.0

                Log.i(TAG, "distance: $distance cm")
            } else {
                time1 = System.nanoTime()
            }

            return true
        }
    }

    private val runnble = object : Runnable {
        override fun run() {
            readDistanceAsync()
            callbackHandler!!.postDelayed(this, 300)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "Start ${UltraSonicModule::class.java.name}")

        val service = PeripheralManagerService()

        Log.i(TAG, "Available ports: ${service.gpioList}")

        echoPin = service.openGpio(ECHO_PIN)
        echoPin!!.setDirection(Gpio.DIRECTION_IN)
        echoPin!!.setEdgeTriggerType(Gpio.EDGE_BOTH)
        echoPin!!.setActiveType(Gpio.ACTIVE_HIGH)

        triggerPin = service.openGpio(TRIG_PIN)
        triggerPin!!.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

        echoPin!!.registerGpioCallback(callback, callbackHandler)

//        fixedRateTimer("measure", false, 100, 300) {
//            readDistanceAsync()
//        }

        val triggerHandlerThread = HandlerThread("triggerHandlerThread")
        triggerHandlerThread.start()
        callbackHandler = Handler(triggerHandlerThread.looper)

        callbackHandler!!.post(runnble)
    }

    fun readDistanceAsync() {
        triggerPin!!.value = false
        Thread.sleep(0, 2000)

        triggerPin!!.value = true
        Thread.sleep(0, 10000)

        triggerPin!!.value = false
    }

    override fun onDestroy() {
        super.onDestroy()

        triggerPin!!.close()
        triggerPin = null

        echoPin!!.close()
        echoPin = null
    }
}