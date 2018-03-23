package com.saleniuk.selectablemarker

import com.google.maps.android.clustering.ClusterItem
import com.google.android.gms.maps.model.LatLng

open class SelectableItem(var markerId: String,
                     var markerPosition: LatLng,
                     var isDraggable: Boolean = false,
                     var zIndex: Float = 1f,
                     var type: Int = 0) : ClusterItem {

    override fun getSnippet(): String? = ""

    override fun getTitle(): String? = ""

    override fun getPosition() = markerPosition

    fun setPosition(position: LatLng) {
        markerPosition = position
    }


}