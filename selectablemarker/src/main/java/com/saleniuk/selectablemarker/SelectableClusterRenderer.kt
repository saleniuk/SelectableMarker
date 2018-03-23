package com.saleniuk.selectablemarker

import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

abstract class SelectableClusterRenderer<T : SelectableItem> : DefaultClusterRenderer<T> {

    val TYPE_CLUSTER = 0
    val TYPE_ITEM = 1

    private var _onMarkerDragListener: GoogleMap.OnMarkerDragListener? = null
    private var _onMapClickListener: GoogleMap.OnMapClickListener? = null
    private var _onCameraIdleListener: GoogleMap.OnCameraIdleListener? = null
    private var _onInfoWindowClickListener: GoogleMap.OnInfoWindowClickListener? = null
    private var _onClusterItemClickListener: ClusterManager.OnClusterItemClickListener<T>? = null
    private var _onClusterClickListener: ClusterManager.OnClusterClickListener<T>? = null

    private var _context: Context
    private var _googleMap: GoogleMap
    private var _markerSelectionMethod: SelectableMarkerManager.MarkerSelectionMethod
    private var _unselectOnMapClick: Boolean = false
    private var _clusterManager: ClusterManager<T>
    private var _selectableMarkerManager: SelectableMarkerManager<T>? = null

    constructor(context: Context,
                googleMap: GoogleMap,
                markerSelectionMethod: SelectableMarkerManager.MarkerSelectionMethod,
                unselectOnMapClick: Boolean,
                clusterManager: ClusterManager<T>) : super(context, googleMap, clusterManager) {

        this._context = context
        this._googleMap = googleMap
        this._markerSelectionMethod = markerSelectionMethod
        this._unselectOnMapClick = unselectOnMapClick
        this._clusterManager = clusterManager

        _selectableMarkerManager = object : SelectableMarkerManager<T>(
                context, googleMap, markerSelectionMethod, unselectOnMapClick) {

            override fun bindMarkerView(item: T): View {
                return when (item.type) {
                    TYPE_CLUSTER -> {
                        val cluster = _clusterManager.algorithm.getClusters(googleMap.cameraPosition.zoom.toDouble())
                                .find { cluster -> cluster.hashCode().toString() == item.markerId }
                        this@SelectableClusterRenderer.bindClusterView(cluster)
                    }
                    else -> {
                        this@SelectableClusterRenderer.bindMarkerView(item)
                    }
                }
            }
        }

        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
            _onCameraIdleListener?.onCameraIdle()
        }
        googleMap.setOnInfoWindowClickListener {
            clusterManager.onInfoWindowClick(it)
            _onInfoWindowClickListener?.onInfoWindowClick(it)
        }

        googleMap.setOnMarkerClickListener {
            _selectableMarkerManager?.onMarkerClick(it)
            clusterManager?.onMarkerClick(it)
        }

        clusterManager?.setOnClusterClickListener {
            _onClusterClickListener?.onClusterClick(it) ?: false
        }
        clusterManager?.setOnClusterItemClickListener {
            _onClusterItemClickListener?.onClusterItemClick(it) ?: false
        }

        _selectableMarkerManager?.onMapClickListener = _onMapClickListener
        _selectableMarkerManager?.onMarkerDragListener = object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragEnd(marker: Marker?) {
                _onMarkerDragListener?.onMarkerDragEnd(marker)
                if (getClusterItem(marker) is T && marker != null) {
                    val clusterItem = getClusterItem(marker)
                    if (clusterItem is T) {
                        clusterManager.removeItem(clusterItem)
                        clusterItem.position = marker.position
                        clusterManager.addItem(clusterItem)
                    }
                }
                setAnimation(false)
                clusterManager.cluster()
                setAnimation(true)
            }

            override fun onMarkerDragStart(marker: Marker?) {
                _onMarkerDragListener?.onMarkerDragStart(marker)
            }

            override fun onMarkerDrag(marker: Marker?) {
                _onMarkerDragListener?.onMarkerDrag(marker)
            }
        }
    }

    abstract fun bindClusterView(item: Cluster<T>?): View
    abstract fun bindMarkerView(item: T): View
    abstract fun createItemForCluster(cluster: Cluster<T>): T

    open fun updateView(item: T, view: View) {
        _selectableMarkerManager?.updateView(item, view)
    }

    open fun updateView(cluster: Cluster<T>, view: View) {
        val item = createItemForCluster(cluster)
        _selectableMarkerManager?.updateView(item, view)
    }

    override fun onBeforeClusterRendered(cluster: Cluster<T>?, markerOptions: MarkerOptions?) {
        super.onBeforeClusterRendered(cluster, markerOptions)
        if (cluster != null && markerOptions != null) {
            val item = createItemForCluster(cluster)
            _selectableMarkerManager?.prepareMarkerOptions(item, markerOptions, SelectableMarker.MarkerState.DEFAULT)
        }
    }

    override fun onBeforeClusterItemRendered(item: T?, markerOptions: MarkerOptions?) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        if (item != null && markerOptions != null) {
            _selectableMarkerManager?.prepareMarkerOptions(item, markerOptions, SelectableMarker.MarkerState.DEFAULT)
        }
    }

    override fun onClusterItemRendered(item: T, marker: Marker?) {
        super.onClusterItemRendered(item, marker)
        if (item != null && marker != null) {
            item.type = TYPE_ITEM
            _selectableMarkerManager?.setItem(item, marker)
        }
    }

    override fun onClusterRendered(cluster: Cluster<T>?, marker: Marker?) {
        super.onClusterRendered(cluster, marker)
        if (cluster != null && marker != null) {
            val item = createItemForCluster(cluster)
            _selectableMarkerManager?.setItem(item, marker)
        }
    }

    fun setOnMarkerDragListener(onMarkerDragListener: GoogleMap.OnMarkerDragListener?) {
        this._onMarkerDragListener = onMarkerDragListener
    }

    fun setOnMapClickListener(onMapClickListener: GoogleMap.OnMapClickListener?) {
        this._onMapClickListener = onMapClickListener
    }

    fun setOnCameraIdleListener(onCameraIdleListener: GoogleMap.OnCameraIdleListener?) {
        this._onCameraIdleListener = onCameraIdleListener
    }

    fun setOnInfoWindowClickListener(onInfoWindowClickListener: GoogleMap.OnInfoWindowClickListener?) {
        this._onInfoWindowClickListener = onInfoWindowClickListener
    }

    override fun setOnClusterItemClickListener(onClusterItemClickListener: ClusterManager.OnClusterItemClickListener<T>?) {
        this._onClusterItemClickListener = onClusterItemClickListener
    }

    override fun setOnClusterClickListener(onClusterClickListener: ClusterManager.OnClusterClickListener<T>?) {
        this._onClusterClickListener = onClusterClickListener
    }

    override fun onClustersChanged(clusters: MutableSet<out Cluster<T>>?) {
        super.onClustersChanged(clusters)
    }
}