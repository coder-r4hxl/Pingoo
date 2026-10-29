package com.pingoo.app.ui.map

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmMapView(
    initialLatitude: Double = 28.6139,
    initialLongitude: Double = 77.2090,
    selectedLatitude: Double,
    selectedLongitude: Double,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(selectedLatitude, selectedLongitude))
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                overlays.clear()

                // Add tap event overlay for selecting pin coordinates
                val eventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let {
                            onLocationSelected(it.latitude, it.longitude)
                        }
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }
                overlays.add(MapEventsOverlay(eventsReceiver))

                // Add selection marker pin
                val marker = Marker(this).apply {
                    position = GeoPoint(selectedLatitude, selectedLongitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Selected Destination"
                }
                overlays.add(marker)
                invalidate()
            }
        },
        update = { map ->
            map.overlays.clear()

            val eventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    p?.let {
                        onLocationSelected(it.latitude, it.longitude)
                    }
                    return true
                }

                override fun longPressHelper(p: GeoPoint?): Boolean = false
            }
            map.overlays.add(MapEventsOverlay(eventsReceiver))

            val marker = Marker(map).apply {
                position = GeoPoint(selectedLatitude, selectedLongitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Selected Destination"
            }
            map.overlays.add(marker)
            map.invalidate()
        },
        modifier = modifier
    )
}
