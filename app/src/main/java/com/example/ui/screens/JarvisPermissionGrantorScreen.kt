package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.liquidGlassSurface
import com.example.ui.theme.AlienwareWhiteIce
import com.example.ui.theme.AlienwareWhitePure
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBorderActive
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.LightBlueCorrection
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.JarvisViewModel

data class PermissionItem(
    val key: String,
    val title: String,
    val description: String,
    val manifestPermission: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isSpecial: Boolean = false
)

val SYSTEM_PERMISSIONS_LIST = buildList {
    add(PermissionItem("MICROPHONE", "Audio Speech & Microphone", "Allows JARVIS to listen to direct voice directives and process local speech recognition.", Manifest.permission.RECORD_AUDIO, Icons.Default.Mic))
    add(PermissionItem("CAMERA", "HUD Vision & Spatial Analysis", "Grants camera access for live vision object detection, optical text scanning, and HUD sight.", Manifest.permission.CAMERA, Icons.Default.CameraAlt))
    add(PermissionItem("LOCATION", "GPS & Live Telemetry Tracking", "Enables real-time satellite GPS tracking, speed estimation, and geofenced automation.", Manifest.permission.ACCESS_FINE_LOCATION, Icons.Default.Place))
    add(PermissionItem("CONTACTS", "Contacts Resolution & Messaging", "Allows JARVIS to resolve contact names for phone calls, messaging, and email directives.", Manifest.permission.READ_CONTACTS, Icons.Default.Contacts))
    add(PermissionItem("PHONE", "Cellular Phone Dial & Call Control", "Enables automated voice phone dialing and call log categorization.", Manifest.permission.CALL_PHONE, Icons.Default.Phone))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(PermissionItem("NOTIFICATIONS", "System Notifications & HUD Alerts", "Delivers JARVIS autonomous alert banners and status updates.", Manifest.permission.POST_NOTIFICATIONS, Icons.Default.Notifications))
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        add(PermissionItem("BLUETOOTH", "Bluetooth Scan & Device Connect", "Allows scanning for nearby BLE beacons, vehicle docks, and audio devices.", Manifest.permission.BLUETOOTH_CONNECT, Icons.Default.Bluetooth))
    }
    add(PermissionItem("OVERLAY", "System Floating HUD Overlay", "Allows JARVIS floating arc reactor overlay over other system apps.", Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Icons.Default.Security, isSpecial = true))
}

@Composable
fun JarvisPermissionGrantorScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val permissionStatus = remember {
        mutableStateMapOf<String, Boolean>().apply {
            SYSTEM_PERMISSIONS_LIST.forEach { item ->
                put(
                    item.key,
                    if (item.isSpecial) {
                        if (item.key == "OVERLAY") Settings.canDrawOverlays(context) else false
                    } else {
                        ContextCompat.checkSelfPermission(context, item.manifestPermission) == PackageManager.PERMISSION_GRANTED
                    }
                )
            }
        }
    }

    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (perm, isGranted) ->
            val matchingKey = SYSTEM_PERMISSIONS_LIST.find { it.manifestPermission == perm }?.key
            if (matchingKey != null) {
                permissionStatus[matchingKey] = isGranted
            }
        }
        viewModel.logSystemEvent("PERMISSIONS", "Updated system runtime permission states", "SECURITY")
    }

    val singlePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Refresh statuses
        SYSTEM_PERMISSIONS_LIST.forEach { item ->
            if (!item.isSpecial) {
                permissionStatus[item.key] = ContextCompat.checkSelfPermission(context, item.manifestPermission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JarvisVoid,
                        JarvisSurfaceDark,
                        JarvisVoid
                    )
                )
            )
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassSurface(
                        blurRadius = 18.dp,
                        cornerRadius = 20.dp,
                        transparencyAlpha = 0.35f,
                        accentColor = LightBlueCorrection
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(JarvisSurfaceElevated)
                            .border(1.dp, LightBlueCorrection, CircleShape)
                            .testTag("permissions_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AlienwareWhitePure
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "JARVIS SYSTEM PERMISSIONS",
                            style = Typography.displayMedium,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlienwareWhitePure
                        )
                        Text(
                            text = "GRANULAR HARDWARE ACCESS GRANTOR",
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            color = LightBlueCorrection
                        )
                    }
                }

                Button(
                    onClick = {
                        val missingManifest = SYSTEM_PERMISSIONS_LIST
                            .filter { !it.isSpecial && permissionStatus[it.key] == false }
                            .map { it.manifestPermission }
                            .toTypedArray()

                        if (missingManifest.isNotEmpty()) {
                            multiplePermissionLauncher.launch(missingManifest)
                        } else {
                            Toast.makeText(context, "All system permissions are active!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBlueCorrection, contentColor = JarvisVoid),
                    modifier = Modifier.testTag("grant_all_permissions_button")
                ) {
                    Text("GRANT ALL", style = Typography.labelLarge, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Permission Items List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(SYSTEM_PERMISSIONS_LIST, key = { it.key }) { item ->
                    val isGranted = permissionStatus[item.key] ?: false

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassSurface(
                                blurRadius = 14.dp,
                                cornerRadius = 16.dp,
                                transparencyAlpha = if (isGranted) 0.35f else 0.20f,
                                accentColor = if (isGranted) JarvisSuccessGreen else JarvisAlertRed
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background((if (isGranted) JarvisSuccessGreen else JarvisAlertRed).copy(alpha = 0.20f))
                                        .border(1.dp, if (isGranted) JarvisSuccessGreen else JarvisAlertRed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isGranted) JarvisSuccessGreen else JarvisAlertRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.title,
                                            style = Typography.titleMedium,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AlienwareWhitePure
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.description,
                                        style = Typography.bodyMedium,
                                        fontSize = 10.sp,
                                        color = JarvisTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (item.isSpecial) {
                                        if (item.key == "OVERLAY") {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                            context.startActivity(intent)
                                        }
                                    } else {
                                        singlePermissionLauncher.launch(item.manifestPermission)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isGranted) JarvisSuccessGreen.copy(alpha = 0.25f) else JarvisAlertRed.copy(alpha = 0.25f),
                                    contentColor = if (isGranted) JarvisSuccessGreen else JarvisAlertRed
                                ),
                                modifier = Modifier
                                    .border(1.dp, if (isGranted) JarvisSuccessGreen else JarvisAlertRed, RoundedCornerShape(8.dp))
                                    .testTag("grant_permission_${item.key.lowercase()}")
                            ) {
                                Icon(
                                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isGranted) "GRANTED" else "REQUEST",
                                    style = Typography.labelLarge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
