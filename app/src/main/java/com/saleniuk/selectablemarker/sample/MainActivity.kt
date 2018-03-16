package com.saleniuk.selectablemarker.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var selectableMarkerManager: SelectableMarkerManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            selectableMarkerManager = SelectableMarkerManager(this, googleMap, SelectableMarkerManager.MarkerSelectionMethod.SINGLE, true)
        }
        selectableMarkerManager?.addMarker(MarkerOptions().position(LatLng(52.1, 16.795)), R.layout.view_marker, 1, isDraggable = true)
        selectableMarkerManager?.addMarker(MarkerOptions().position(LatLng(52.1, 16.8)), R.layout.view_marker, 2, isDraggable = true)
        selectableMarkerManager?.addMarker(MarkerOptions().position(LatLng(52.1, 16.805)), R.layout.view_marker, 3, isDraggable = true)

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(52.1, 16.8), 15f))
    }
}
