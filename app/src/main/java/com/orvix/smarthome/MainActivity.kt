package com.orvix.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(32.dp))

                        Text(
                            text = "ORVIX",
                            style = MaterialTheme.typography.headlineLarge,
                            fontSize = 36.sp
                        )

                        Text(
                            text = "Universal Smart Home",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(32.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    "Welcome to ORVIX",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Your home, connected.")
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    "Dashboard",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Your rooms and devices will appear here.")
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    "System Status",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("App initialized")
                                Text("Device connection: Not configured")
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Text(
                            text = "ORVIX 1.0.0",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
