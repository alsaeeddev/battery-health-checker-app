package com.alsaeeddev.batterytemp

import android.content.*
import android.content.res.Configuration
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.alsaeeddev.batterytemp.ui.theme.BatteryTempTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BatteryTemperatureApp()
        }
    }
}

@Composable
fun BatteryTemperatureApp() {
    BatteryTempTheme {
        BatteryTemperatureScreen()
    }
}


@Preview(
    name = "Battery Temperature – Dark",
    showBackground = true,
    device = Devices.PIXEL_7,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun BatteryTemperatureAppPreview() {
    MaterialTheme {
        BatteryTemperatureApp()
    }
}


/* ---------------- DATA ---------------- */

data class BatteryState(
    val temp: Float,
    val level: Int,
    val voltage: Float,
    val charging: Boolean,
    val health: String,

    )

/* ---------------- BATTERY OBSERVER ---------------- */

@Composable
fun rememberBatteryState(): State<BatteryState> {
    val context = LocalContext.current
    val state = remember {
        mutableStateOf(
            BatteryState(0f, 0, 0f, false, "Unknown")
        )
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent) {

                state.value = BatteryState(
                    temp = i.getIntExtra(
                        BatteryManager.EXTRA_TEMPERATURE, 0
                    ) / 10f,
                    level = i.getIntExtra(
                        BatteryManager.EXTRA_LEVEL, 0
                    ),
                    voltage = i.getIntExtra(
                        BatteryManager.EXTRA_VOLTAGE, 0
                    ) / 1000f,
                    charging = i.getIntExtra(
                        BatteryManager.EXTRA_STATUS, -1
                    ) == BatteryManager.BATTERY_STATUS_CHARGING,
                    health = when (
                        i.getIntExtra(
                            BatteryManager.EXTRA_HEALTH, 0
                        )
                    ) {
                        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                        else -> "Unknown"
                    },

                    )
            }
        }
        context.registerReceiver(
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        onDispose { context.unregisterReceiver(receiver) }
    }
    return state
}

/* ---------------- SCREEN ---------------- */

@Composable
fun BatteryTemperatureScreen() {
    val battery by rememberBatteryState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0A0E27),
                        Color(0xFF1A1B2E),
                        Color(0xFF16213E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        GlassCard {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Header()
                Spacer(Modifier.height(20.dp))
                TemperatureGauge(battery.temp)
                Spacer(Modifier.height(20.dp))
                InfoGrid(battery)
                Spacer(Modifier.height(16.dp))
                StatusBar(battery.temp)
            }
        }
    }
}

/* ---------------- UI COMPONENTS ---------------- */

@Composable
fun GlassCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(32.dp)
            )
            .padding(20.dp)
    ) { content() }
}

@Composable
fun Header() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "⚡ Battery Monitor",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Real-time temperature tracking",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
    }
}

/* ---------------- GAUGE ---------------- */

@Composable
fun TemperatureGauge(temp: Float) {
    val progress by animateFloatAsState(
        targetValue = ((temp - 20f) / 40f).coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = ""
    )

    val gradient = when {
        temp < 30 -> listOf(Color(0xFF3B82F6), Color(0xFF06B6D4))
        temp < 40 -> listOf(Color(0xFF10B981), Color(0xFF84CC16))
        temp < 50 -> listOf(Color(0xFFF59E0B), Color(0xFFF97316))
        else -> listOf(Color(0xFFEF4444), Color(0xFFDC2626))
    }

    Box(contentAlignment = Alignment.Center) {
        HeatWaves(gradient.first())

        Canvas(Modifier.size(280.dp)) {
            drawArc(
                color = Color.White.copy(alpha = 0.1f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(14.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(gradient),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(14.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = temp.roundToInt().toString(),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    brush = Brush.linearGradient(gradient)
                )
            )

            Text("°C", color = Color.White.copy(alpha = 0.7f), fontSize = 26.sp)
            Text(
                "TEMPERATURE",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun HeatWaves(color: Color) {
    val infinite = rememberInfiniteTransition(label = "")
    repeat(3) { i ->
        val scale by infinite.animateFloat(
            initialValue = 1f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                tween(3000, delayMillis = i * 1000),
                RepeatMode.Restart
            ),
            label = ""
        )
        Box(
            modifier = Modifier
                .size(190.dp)
                .scale(scale)
                .border(
                    2.dp,
                    color.copy(alpha = 0.3f),
                    CircleShape
                )
        )
    }
}

/* ---------------- INFO GRID ---------------- */

@Composable
fun InfoGrid(b: BatteryState) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        // modifier = Modifier.height(200.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { InfoCard("🔋", "Battery", "${b.level}%") }
        item { InfoCard("⚡", "Voltage", "${"%.2f".format(b.voltage)}V") }
        item { InfoCard("🔌", "Status", if (b.charging) "Charging" else "Discharging") }
        item { InfoCard("❄️", "Health", b.health) }
    }
}

@Composable
fun InfoCard(icon: String, label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 20.sp)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        Text(
            value,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/* ---------------- STATUS BAR ---------------- */


@Composable
fun StatusBar(temp: Float) {
    val (text, badge, color) = when {
        temp < 30 -> Triple("Status", "COOL", Color(0xFF3B82F6))
        temp < 40 -> Triple("Temperature Normal", "OPTIMAL", Color(0xFF10B981))
        temp < 50 -> Triple("Temperature Warm", "WARM", Color(0xFFF59E0B))
        else -> Triple("Temperature High", "HOT", Color(0xFFEF4444))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(top = 6.dp, bottom = 6.dp, start = 10.dp, end = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = Color.White.copy(alpha = 0.85f))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(badge, color = Color.White, fontSize = 12.sp)
        }
    }
}

