package com.saleniuk.selectablemarker.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.saleniuk.selectablemarker.SelectableClusterRenderer
import com.saleniuk.selectablemarker.SelectableItem
import com.saleniuk.selectablemarker.SelectableMarkerManager


class ClusterActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
//
        if (googleMap != null) {
            val clusterManager = ClusterManager<SelectableItem>(this, googleMap)
            clusterManager.renderer = object : SelectableClusterRenderer<SelectableItem>(
                    this, googleMap, SelectableMarkerManager.MarkerSelectionMethod.SINGLE,
                    true, clusterManager) {

                override fun createItemForCluster(cluster: Cluster<SelectableItem>): SelectableItem {
                    return SelectableItem(cluster.hashCode().toString(), cluster.position, type = TYPE_CLUSTER)
                }

                override fun bindClusterView(item: Cluster<SelectableItem>?): View {
                    val view = LayoutInflater.from(this@ClusterActivity).inflate(R.layout.view_cluster, null)
                    view.findViewById<TextView>(android.R.id.text1)?.text = "" + item?.size
                    return view
                }

                override fun bindMarkerView(item: SelectableItem): View {
                    return LayoutInflater.from(this@ClusterActivity).inflate(R.layout.view_marker, null)
                }

                override fun shouldRenderAsCluster(cluster: Cluster<SelectableItem>?): Boolean {
                    return (cluster?.size ?: 0) >= 2
                }
            }

            googleMap.setInfoWindowAdapter(clusterManager.markerManager)
            googleMap.setOnInfoWindowClickListener(clusterManager)
            clusterManager.setOnClusterItemInfoWindowClickListener {

            }

            clusterManager.addItem(CustomClusterItem("1", LatLng(52.1, 16.82), "abc", "abc", true))
            clusterManager.addItem(CustomClusterItem("2", LatLng(52.1, 16.84), "abc", "abc", true))
            clusterManager.addItem(CustomClusterItem("3", LatLng(52.1, 16.86), "abc", "abc", true))
            clusterManager.addItem(CustomClusterItem("4", LatLng(52.1, 16.88), "abc", "abc", true))
            clusterManager.addItem(CustomClusterItem("5", LatLng(52.1, 16.90), "abc", "abc", true))

            clusterManager.cluster()

            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(52.1, 16.85), 12f))
        }
    }


}
