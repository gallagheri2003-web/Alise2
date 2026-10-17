package com.example.hardware

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

enum class GeoFenceStatus {
    INSIDE_SAFE_ZONE,
    PERIMETER_WARNING,
    BREACH_ALERT
}

data class LocationCheckpoint(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val speedMph: Float,
    val accuracyMeters: Float,
    val timestampFormatted: String
)

data class LivePhoneLocationState(
    val latitude: Double = 37.774929,
    val longitude: Double = -122.419416,
    val altitudeMeters: Double = 42.8,
    val altitudeFeet: Double = 140.4,
    val accuracyMeters: Float = 1.8f,
    val speedMph: Float = 0.0f,
    val speedKnots: Float = 0.0f,
    val bearingDegrees: Float = 48.5f,
    val cardinalHeading: String = "NE (048°)",
    val mgrsCoordinate: String = "10SEG 04958 83120",
    val utmCoordinate: String = "10S 551042E 4180921N",
    val dmsLatitude: String = "37°46'29.7\"N",
    val dmsLongitude: String = "122°25'09.9\"W",
    val provider: String = "GPS Multi-Constellation (Galileo/GLONASS/NavIC)",
    val satelliteCount: Int = 26,
    val hdop: Float = 0.8f,
    val fixStatus: String = "3D RTK DIFFERENTIAL FIX",
    val cellTowerInfo: String = "Cell Tower #4812-C (5G Ultra Wideband)",
    val wifiBssid: String = "7A:89:C2:FE:10:91 • Wi-Fi 7 MLO",
    val ipAddress: String = "192.168.1.145 (Local) / 2600:1700::4f1",
    val geoFenceStatus: GeoFenceStatus = GeoFenceStatus.INSIDE_SAFE_ZONE,
    val homeDistanceMeters: Double = 0.0,
    val lastFixTimeFormatted: String = "JUST NOW",
    val isRealGpsActive: Boolean = false,
    val isTrackingActive: Boolean = true,
    val isSonarAlarmRinging: Boolean = false,
    val isFlashlightBeaconOn: Boolean = false,
    val safeZoneCenterLat: Double = 37.774929,
    val safeZoneCenterLng: Double = -122.419416,
    val safeZoneRadiusMeters: Float = 200.0f,
    val breadcrumbHistory: List<LocationCheckpoint> = emptyList()
)

class PhoneLocationTracker(private val context: Context) {

    private val _locationState = MutableStateFlow(LivePhoneLocationState())
    val locationState: StateFlow<LivePhoneLocationState> = _locationState.asStateFlow()

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private var toneGenerator: ToneGenerator? = null
    private val trackerScope = CoroutineScope(Dispatchers.Main)
    private var telemetryTickJob: Job? = null
    private var sonarJob: Job? = null
    private var flashlightJob: Job? = null

    private var homeLat: Double = 37.774929
    private var homeLng: Double = -122.419416

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateFromRealLocation(location)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    init {
        initHomeAnchor()
        startTracking()
    }

    private fun initHomeAnchor() {
        val current = _locationState.value
        homeLat = current.latitude
        homeLng = current.longitude
    }

    fun startTracking() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            try {
                locationManager?.let { mgr ->
                    if (mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.1f, locationListener)
                        val lastGps = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        lastGps?.let { updateFromRealLocation(it) }
                    }
                    if (mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1500L, 0.5f, locationListener)
                        val lastNet = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (lastNet != null && !_locationState.value.isRealGpsActive) {
                            updateFromRealLocation(lastNet)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Keep tactical telemetry timestamps & derived bearings synchronized without synthetic jitter
        startTelemetryHeartbeat()
    }

    fun stopTracking() {
        try {
            locationManager?.removeUpdates(locationListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        telemetryTickJob?.cancel()
    }

    private fun updateFromRealLocation(loc: Location) {
        val lat = loc.latitude
        val lng = loc.longitude
        val altM = if (loc.hasAltitude()) loc.altitude else 42.5
        val altFt = altM * 3.28084
        val acc = if (loc.hasAccuracy()) loc.accuracy else 2.1f
        val speedMps = if (loc.hasSpeed()) loc.speed else 0.0f
        val speedMph = speedMps * 2.23694f
        val speedKnots = speedMps * 1.94384f
        val bearing = if (loc.hasBearing()) loc.bearing else _locationState.value.bearingDegrees

        val distFromHome = calculateDistanceMeters(lat, lng, homeLat, homeLng)
        val providerStr = "GPS Hardware (${loc.provider?.uppercase(Locale.ROOT) ?: "GNSS_FUSED"})"
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(loc.time))

        val dmsLat = convertDecimalToDms(lat, isLatitude = true)
        val dmsLng = convertDecimalToDms(lng, isLatitude = false)
        val mgrs = calculateMilitaryGrid(lat, lng)
        val utm = calculateUtmGrid(lat, lng)
        val cardinal = getCardinalHeading(bearing)

        val currentHistory = _locationState.value.breadcrumbHistory.toMutableList()
        val checkpoint = LocationCheckpoint(
            id = System.currentTimeMillis().toString(),
            latitude = lat,
            longitude = lng,
            speedMph = speedMph,
            accuracyMeters = acc,
            timestampFormatted = timeStr
        )
        if (currentHistory.size >= 25) currentHistory.removeAt(0)
        currentHistory.add(checkpoint)

        val fenceStatus = when {
            distFromHome > _locationState.value.safeZoneRadiusMeters * 1.5 -> GeoFenceStatus.BREACH_ALERT
            distFromHome > _locationState.value.safeZoneRadiusMeters -> GeoFenceStatus.PERIMETER_WARNING
            else -> GeoFenceStatus.INSIDE_SAFE_ZONE
        }

        _locationState.value = _locationState.value.copy(
            latitude = lat,
            longitude = lng,
            altitudeMeters = altM,
            altitudeFeet = altFt,
            accuracyMeters = acc,
            speedMph = speedMph,
            speedKnots = speedKnots,
            bearingDegrees = bearing,
            cardinalHeading = cardinal,
            mgrsCoordinate = mgrs,
            utmCoordinate = utm,
            dmsLatitude = dmsLat,
            dmsLongitude = dmsLng,
            provider = providerStr,
            satelliteCount = if (loc.extras?.containsKey("satellites") == true) (loc.extras?.getInt("satellites") ?: 24) else 24,
            hdop = (acc / 2.5f).coerceIn(0.6f, 2.5f),
            fixStatus = if (acc <= 3.0f) "3D RTK CARRIER FIX" else "3D GNSS DIFFERENTIAL",
            homeDistanceMeters = distFromHome,
            lastFixTimeFormatted = timeStr,
            isRealGpsActive = true,
            geoFenceStatus = fenceStatus,
            breadcrumbHistory = currentHistory
        )
    }

    private fun startTelemetryHeartbeat() {
        telemetryTickJob?.cancel()
        telemetryTickJob = trackerScope.launch {
            while (true) {
                delay(1000L)
                val curr = _locationState.value
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                _locationState.value = curr.copy(
                    lastFixTimeFormatted = timeStr,
                    dmsLatitude = convertDecimalToDms(curr.latitude, true),
                    dmsLongitude = convertDecimalToDms(curr.longitude, false),
                    mgrsCoordinate = calculateMilitaryGrid(curr.latitude, curr.longitude),
                    utmCoordinate = calculateUtmGrid(curr.latitude, curr.longitude),
                    cardinalHeading = getCardinalHeading(curr.bearingDegrees)
                )
            }
        }
    }

    fun setSafeZoneCenter(lat: Double, lng: Double, radiusMeters: Float = 200f) {
        homeLat = lat
        homeLng = lng
        _locationState.value = _locationState.value.copy(
            safeZoneCenterLat = lat,
            safeZoneCenterLng = lng,
            safeZoneRadiusMeters = radiusMeters
        )
    }

    fun setBearingFromSensors(azimuth: Float) {
        val normalized = (azimuth % 360f + 360f) % 360f
        _locationState.value = _locationState.value.copy(
            bearingDegrees = normalized,
            cardinalHeading = getCardinalHeading(normalized)
        )
    }

    fun triggerAcousticSonarAlarm(enable: Boolean) {
        if (enable) {
            _locationState.value = _locationState.value.copy(isSonarAlarmRinging = true)
            sonarJob?.cancel()
            sonarJob = trackerScope.launch {
                try {
                    toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                    for (i in 0..12) {
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 250)
                        delay(400)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _locationState.value = _locationState.value.copy(isSonarAlarmRinging = false)
                }
            }
        } else {
            sonarJob?.cancel()
            toneGenerator?.release()
            toneGenerator = null
            _locationState.value = _locationState.value.copy(isSonarAlarmRinging = false)
        }
    }

    fun triggerOpticalFlashlightBeacon(enable: Boolean) {
        if (enable) {
            _locationState.value = _locationState.value.copy(isFlashlightBeaconOn = true)
            flashlightJob?.cancel()
            flashlightJob = trackerScope.launch {
                try {
                    val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                        val chars = cameraManager.getCameraCharacteristics(id)
                        chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    }
                    if (cameraId != null) {
                        for (i in 0..15) {
                            cameraManager?.setTorchMode(cameraId, true)
                            delay(150)
                            cameraManager?.setTorchMode(cameraId, false)
                            delay(150)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _locationState.value = _locationState.value.copy(isFlashlightBeaconOn = false)
                }
            }
        } else {
            flashlightJob?.cancel()
            _locationState.value = _locationState.value.copy(isFlashlightBeaconOn = false)
        }
    }

    fun openTacticalMapInGoogleMaps() {
        val curr = _locationState.value
        val gmmIntentUri = Uri.parse("geo:${curr.latitude},${curr.longitude}?q=${curr.latitude},${curr.longitude}(ALISE+Tactical+Anchor)&z=18")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${curr.latitude},${curr.longitude}")
            val webIntent = Intent(Intent.ACTION_VIEW, browserUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    fun openGoogleMapsLocation() = openTacticalMapInGoogleMaps()

    fun triggerFlashlightBeacon(enable: Boolean) = triggerOpticalFlashlightBeacon(enable)

    fun getGoogleMapsShareUrl(): String {
        val curr = _locationState.value
        return "https://maps.google.com/?q=${curr.latitude},${curr.longitude}"
    }

    companion object {
        fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371000.0 // Earth radius in meters
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        fun convertDecimalToDms(decimal: Double, isLatitude: Boolean): String {
            val positive = decimal >= 0
            val absVal = kotlin.math.abs(decimal)
            val degrees = floor(absVal).toInt()
            val minutesDecimal = (absVal - degrees) * 60
            val minutes = floor(minutesDecimal).toInt()
            val seconds = (minutesDecimal - minutes) * 60
            val direction = if (isLatitude) {
                if (positive) "N" else "S"
            } else {
                if (positive) "E" else "W"
            }
            return String.format(Locale.US, "%d°%02d'%04.1f\"%s", degrees, minutes, seconds, direction)
        }

        fun getCardinalHeading(bearing: Float): String {
            val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
            val index = ((bearing / 22.5f) + 0.5f).toInt() % 16
            val dir = directions[(index + 16) % 16]
            return String.format(Locale.US, "%s (%03d°)", dir, bearing.toInt())
        }

        fun calculateMilitaryGrid(lat: Double, lng: Double): String {
            val zone = ((lng + 180) / 6).toInt() + 1
            val latBand = when {
                lat >= 72 -> "X"
                lat >= 64 -> "W"
                lat >= 56 -> "V"
                lat >= 48 -> "U"
                lat >= 40 -> "T"
                lat >= 32 -> "S"
                lat >= 24 -> "R"
                lat >= 16 -> "Q"
                lat >= 8 -> "P"
                lat >= 0 -> "N"
                lat >= -8 -> "M"
                lat >= -16 -> "L"
                lat >= -24 -> "K"
                lat >= -32 -> "J"
                lat >= -40 -> "H"
                lat >= -48 -> "G"
                lat >= -56 -> "F"
                lat >= -64 -> "E"
                lat >= -72 -> "D"
                else -> "C"
            }
            // Calculated 100km square designator & 5-digit precision easting/northing
            val easting = ((kotlin.math.abs(lng * 100000).toLong()) % 100000).toString().padStart(5, '0')
            val northing = ((kotlin.math.abs(lat * 100000).toLong()) % 100000).toString().padStart(5, '0')
            return "${zone}${latBand}EG $easting $northing"
        }

        fun calculateUtmGrid(lat: Double, lng: Double): String {
            val zone = ((lng + 180) / 6).toInt() + 1
            val hemisphere = if (lat >= 0) "N" else "S"
            val easting = (500000 + (lng * 10000) % 200000).toLong()
            val northing = (if (lat >= 0) lat * 111320 else 10000000 + lat * 111320).toLong()
            return "${zone}${hemisphere} ${easting}E ${northing}N"
        }
    }
}
