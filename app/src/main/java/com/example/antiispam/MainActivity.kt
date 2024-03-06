package com.example.antiispam

import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.antiispam.ui.theme.AntiispamTheme


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                setAsDefaultApp()
                registerCallStateListener()
            }
        }
    private var resultLauncher = registerForActivityResult(StartActivityForResult()) {}

    @RequiresApi(Build.VERSION_CODES.Q)
    private val customCallScreeningService = CustomCallScreeningService()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        registerCallStateListener()

        setContent {
            AntiispamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        Button(
                            modifier = Modifier.align(Alignment.Center),
                            onClick = {
                                requestPermissionLauncher.launch(android.Manifest.permission.READ_PHONE_STATE)
                            }) {
                            Text(text = "Сделать по умолчанию")
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun registerCallStateListener() {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                telephonyManager.registerTelephonyCallback(
                    mainExecutor, customCallScreeningService.callStateListener!!
                )
            } else {
                Toast.makeText(this, "Необходимо разрешение", Toast.LENGTH_SHORT).show()
            }
        } else {
            telephonyManager.listen(
                customCallScreeningService.phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setAsDefaultApp() {
        val roleManager = getSystemService(RoleManager::class.java)
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        resultLauncher.launch(intent)
    }
}
