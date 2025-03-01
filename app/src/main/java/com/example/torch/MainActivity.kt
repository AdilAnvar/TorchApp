package com.example.torch

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.torch.ui.theme.TorchTheme
import com.example.torch.viewmodel.TorchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val torchViewModel: TorchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TorchTheme {
                TorchScreen(torchViewModel)
            }
        }
    }
}

@Composable
fun TorchScreen(viewModel: TorchViewModel) {
    val isTorchOn by viewModel.isTorchOn.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val isSOSModeOn by viewModel.isSosOn.collectAsState()
    val isStrobeModeOn by viewModel.isStrobeOn.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "TORCH",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FilledTonalButton(
                        onClick = { viewModel.toggleTorch() },
                        modifier = Modifier.size(200.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                contentDescription = "Torch",
                                modifier = Modifier.size(64.dp),
                                tint = if (isTorchOn) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Text(
                                text = if (isTorchOn) "ON" else "OFF",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.LightMode,
                            contentDescription = "Low brightness",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Gray
                        )
                        Slider(
                            value = brightness,
                            onValueChange = { viewModel.updateTorchBrightness(it) },
                            modifier = Modifier.weight(1f),
                            enabled = isTorchOn && !isSOSModeOn && !isStrobeModeOn
                        )
                        Icon(
                            Icons.Filled.LightMode,
                            contentDescription = "High brightness",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Strobe Mode")
                                Switch(
                                    checked = isStrobeModeOn,
                                    onCheckedChange = { viewModel.toggleStrobeMode(it) },
                                    enabled = !isSOSModeOn
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SOS Mode")
                                Switch(
                                    checked = isSOSModeOn,
                                    onCheckedChange = { viewModel.toggleSosMode(it) },
                                    enabled = !isStrobeModeOn
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}