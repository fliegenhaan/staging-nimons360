package com.tubes.nimons360.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.tubes.nimons360.R
import com.tubes.nimons360.data.local.AppDatabase
import com.tubes.nimons360.data.local.FavouriteLocationEntity
import com.tubes.nimons360.databinding.FragmentMapBinding
import com.tubes.nimons360.model.MemberPresence
import com.tubes.nimons360.service.LocationWebSocketService
import com.tubes.nimons360.utils.LocationState
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

    private var currentUserMarker: Marker? = null
    private val memberMarkers = mutableMapOf<Int, Marker>()
    private val lastUpdateMap = mutableMapOf<Int, Long>()

    private val favouriteDao by lazy {
        AppDatabase.getDatabase(requireContext()).favouriteLocationDao()
    }
    private val favouriteMarkers = mutableMapOf<Int, Marker>()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            startLocationUpdates()
            startWebSocketService()
        } else {
            val canShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            if (!canShowRationale) {
                Snackbar.make(
                    binding.root,
                    "Izin lokasi diperlukan. Aktifkan di Pengaturan.",
                    Snackbar.LENGTH_LONG
                ).setAction("Buka Pengaturan") {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireContext().packageName, null)
                    })
                }.show()
            } else {
                Snackbar.make(binding.root, "Izin lokasi diperlukan untuk fitur peta.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            LocationState.currentLat = location.latitude
            LocationState.currentLon = location.longitude
            updateCurrentUserMarker(location.latitude, location.longitude)
            binding.mapView.controller.animateTo(
                GeoPoint(location.latitude, location.longitude)
            )
        }
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                val azimuth = event.values[0]
                LocationState.currentAzimuth = azimuth
                currentUserMarker?.rotation = -azimuth
                binding.mapView.invalidate()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireContext().getSystemService(SensorManager::class.java)
        locationManager = requireContext().getSystemService(LocationManager::class.java)

        setupMap()
        checkLocationPermission()
        observeWebSocketUpdates()
        loadFavouriteMarkers()

        binding.fabRecenter.setOnClickListener {
            if (LocationState.currentLat != 0.0) {
                binding.mapView.controller.animateTo(
                    GeoPoint(LocationState.currentLat, LocationState.currentLon)
                )
            }
        }
    }

    private fun setupMap() {
        org.osmdroid.config.Configuration.getInstance().apply {
            userAgentValue = requireContext().packageName
            osmdroidTileCache = java.io.File(requireContext().cacheDir, "osmdroid")
        }
        val defaultLat = LocationState.currentLat.coerceIn(-90.0, 90.0)
        val defaultLon = LocationState.currentLon.coerceIn(-180.0, 180.0)
        val centerPoint = if (defaultLat == 0.0 && defaultLon == 0.0) {
            GeoPoint(-6.8915, 107.6107)
        } else {
            GeoPoint(defaultLat, defaultLon)
        }
        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(centerPoint)

            val mapEventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                override fun longPressHelper(p: GeoPoint?): Boolean {
                    p ?: return false
                    SaveFavouriteBottomSheet.newInstance(p.latitude, p.longitude)
                        .show(parentFragmentManager, "save_favourite")
                    return true
                }
            }
            overlays.add(0, MapEventsOverlay(mapEventsReceiver))
        }
    }

    private fun checkLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            startLocationUpdates()
            startWebSocketService()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun startLocationUpdates() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return

        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return
        }

        locationManager.requestLocationUpdates(provider, 1000L, 1f, locationListener)

        // Get last known location immediately
        val lastLocation = locationManager.getLastKnownLocation(provider)
        if (lastLocation != null) {
            LocationState.currentLat = lastLocation.latitude
            LocationState.currentLon = lastLocation.longitude
            updateCurrentUserMarker(lastLocation.latitude, lastLocation.longitude)
            binding.mapView.controller.animateTo(GeoPoint(lastLocation.latitude, lastLocation.longitude))
        }
    }

    private fun startWebSocketService() {
        val intent = Intent(requireContext(), LocationWebSocketService::class.java)
        requireContext().startForegroundService(intent)
    }

    private fun updateCurrentUserMarker(lat: Double, lon: Double) {
        val map = binding.mapView
        if (currentUserMarker == null) {
            currentUserMarker = Marker(map).apply {
                title = "Saya"
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_user)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            map.overlays.add(currentUserMarker)
        }
        currentUserMarker?.position = GeoPoint(lat, lon)
        map.invalidate()
    }

    private fun updateMemberMarker(presence: MemberPresence) {
        val map = binding.mapView
        lastUpdateMap[presence.userId] = System.currentTimeMillis()

        val marker = memberMarkers.getOrPut(presence.userId) {
            Marker(map).apply {
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_member)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                map.overlays.add(this)
            }
        }
        marker.position = GeoPoint(presence.latitude, presence.longitude)
        marker.rotation = -presence.rotation
        marker.title = presence.fullName
        marker.snippet = presence.email
        marker.setOnMarkerClickListener { _, _ ->
            UserInfoBottomSheet.newInstance(presence)
                .show(parentFragmentManager, "user_info")
            true
        }
        map.invalidate()
    }

    private fun removeStaleMarkers() {
        val now = System.currentTimeMillis()
        val staleIds = lastUpdateMap.filter { now - it.value > 5000 }.keys.toList()
        staleIds.forEach { userId ->
            memberMarkers.remove(userId)?.let { marker ->
                binding.mapView.overlays.remove(marker)
            }
            lastUpdateMap.remove(userId)
        }
        if (staleIds.isNotEmpty()) binding.mapView.invalidate()
    }

    private fun observeWebSocketUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            LocationWebSocketService.presenceUpdates.collect { presence ->
                updateMemberMarker(presence)
                removeStaleMarkers()
            }
        }
    }

    private fun loadFavouriteMarkers() {
        viewLifecycleOwner.lifecycleScope.launch {
            favouriteDao.getAll().collect { favourites ->
                val currentIds = favourites.map { it.id }.toSet()
                favouriteMarkers.keys.filter { it !in currentIds }.forEach { id ->
                    favouriteMarkers.remove(id)?.let { binding.mapView.overlays.remove(it) }
                    binding.mapView.invalidate()
                }
                favourites.forEach { fav ->
                    if (fav.id !in favouriteMarkers) {
                        addFavouriteMarker(fav)
                    }
                }
            }
        }
    }

    private fun addFavouriteMarker(fav: FavouriteLocationEntity) {
        val marker = Marker(binding.mapView).apply {
            position = GeoPoint(fav.latitude, fav.longitude)
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_favourite)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = fav.name
            setOnMarkerClickListener { _, _ ->
                val sheet = FavouriteInfoBottomSheet.newInstance(fav.id, fav.name)
                sheet.setOnDeletedListener {
                    favouriteMarkers.remove(fav.id)?.let { m ->
                        binding.mapView.overlays.remove(m)
                        binding.mapView.invalidate()
                    }
                }
                sheet.show(parentFragmentManager, "fav_info")
                true
            }
        }
        favouriteMarkers[fav.id] = marker
        binding.mapView.overlays.add(marker)
        binding.mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        val orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        if (orientationSensor != null) {
            sensorManager.registerListener(
                sensorEventListener,
                orientationSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationManager.removeUpdates(locationListener)
        binding.mapView.onDetach()
        _binding = null
    }
}
