package com.saleniuk.selectablemarker

import com.google.android.gms.maps.model.*
import android.graphics.Bitmap

class SelectableMarker(
        val selectableItem: SelectableItem,
        var marker: Marker,
        val defaultMarkerBitmap: Bitmap,
        val pressedMarkerBitmap: Bitmap,
        val selectedMarkerBitmap: Bitmap) {

    enum class MarkerState {
        DEFAULT, PRESSED, SELECTED
    }

    var state: MarkerState = MarkerState.DEFAULT
        set(value) {
            field = value
            updateState()
        }

    private fun updateState() {
        try {
            when (state) {
                MarkerState.DEFAULT -> marker.setIcon(BitmapDescriptorFactory.fromBitmap(defaultMarkerBitmap))
                MarkerState.SELECTED -> marker.setIcon(BitmapDescriptorFactory.fromBitmap(selectedMarkerBitmap))
                MarkerState.PRESSED -> marker.setIcon(BitmapDescriptorFactory.fromBitmap(pressedMarkerBitmap))
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
}