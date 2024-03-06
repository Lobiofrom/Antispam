package com.example.antiispam

import android.app.AlertDialog
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.Q)
class CustomCallScreeningService : CallScreeningService() {

    private val blackList = listOf(
        "6505551212"
    )

    @RequiresApi(api = Build.VERSION_CODES.S)
    abstract class CallStateListener : TelephonyCallback(),
        TelephonyCallback.CallStateListener {
        abstract override fun onCallStateChanged(state: Int)
    }

    val callStateListener: CallStateListener? =
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)) object : CallStateListener() {
            override fun onCallStateChanged(state: Int) {
                when (state) {
                    TelephonyManager.CALL_STATE_IDLE -> {
                        Log.d("CALL_STATE_IDLE-1", "CALL_STATE_IDLE")
                    }

                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        Log.d("CALL_STATE_OFFHOOK-1", "CALL_STATE_OFFHOOK")
                    }

                    TelephonyManager.CALL_STATE_RINGING -> {
                        Log.d("CALL_STATE_RINGING-1", "CALL_STATE_RINGING")
                    }
                }
            }
        }
        else null

    val phoneStateListener: PhoneStateListener? =
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.S)) object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_IDLE -> {
                        Log.d("CALL_STATE_IDLE-2", "CALL_STATE_IDLE")
                    }

                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        Log.d("CALL_STATE_OFFHOOK-2", "CALL_STATE_OFFHOOK")
                    }

                    TelephonyManager.CALL_STATE_RINGING -> {
                        Log.d("CALL_STATE_RINGING-2", "CALL_STATE_RINGING")
                    }
                }
            }
        }
        else null


    override fun onScreenCall(details: Call.Details) {
        if (details.callDirection == Call.Details.DIRECTION_INCOMING) {
            details.handle?.schemeSpecificPart?.let { phoneNumber ->
                Log.d("phoneNumber", "phoneNumber======$phoneNumber")
                if (phoneNumber.isNotEmpty()) {
                    showCallAlertDialog(phoneNumber, blackList)
                    respondToCall(details, CallResponse.Builder().build())
                }
            }
        }
    }

    private fun showCallAlertDialog(number: String, blackList: List<String>) {
        val builder = AlertDialog.Builder(applicationContext)

        if (blackList.contains(number)) {
            builder.setTitle("Входящий звонок")
            builder.setMessage("$number - подозрение на спам")
            val alertDialog = builder.create()
            alertDialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            alertDialog.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
            alertDialog.show()
        } else {
            builder.setTitle("Входящий звонок")
            builder.setMessage("Номер - $number")
            val alertDialog = builder.create()
            alertDialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            alertDialog.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background_green)
            alertDialog.show()
        }
    }
}