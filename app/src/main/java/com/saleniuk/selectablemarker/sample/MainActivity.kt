package com.saleniuk.selectablemarker.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.saleniuk.selectablemarker.SelectableMarkerManager


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        if (googleMap != null) {

            val selectableMarkerManager = object : SelectableMarkerManager<CustomClusterItem>(
                    this, googleMap, SelectableMarkerManager.MarkerSelectionMethod.SINGLE, true) {

                override fun bindMarkerView(item: CustomClusterItem): View {
                    return LayoutInflater.from(this@MainActivity).inflate(R.layout.view_marker, null)
                }
            }

            selectableMarkerManager.addItem(CustomClusterItem("1", LatLng(52.1, 16.82), "", "", true))
            selectableMarkerManager.addItem(CustomClusterItem("2", LatLng(52.1, 16.84), "", "", true))
            selectableMarkerManager.addItem(CustomClusterItem("3", LatLng(52.1, 16.86), "", "", true))
            selectableMarkerManager.addItem(CustomClusterItem("4", LatLng(52.1, 16.88), "", "", true))
            selectableMarkerManager.addItem(CustomClusterItem("5", LatLng(52.1, 16.90), "", "", true))

            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(52.1, 16.85), 12f))
        }
    }


}
