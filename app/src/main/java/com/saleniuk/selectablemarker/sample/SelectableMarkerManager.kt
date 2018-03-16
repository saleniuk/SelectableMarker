package com.saleniuk.selectablemarker.sample

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import android.content.Context
import android.view.LayoutInflater
import android.graphics.Bitmap
import android.view.View
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.R.attr.left
import android.graphics.Rect


class SelectableMarkerManager(val context: Context, val googleMap: GoogleMap, markerSelectionMethod: MarkerSelectionMethod, unselectOnMapClick: Boolean) {

    enum class MarkerSelectionMethod {
        SINGLE, MULTI
    }

    private val markers: HashMap<Int, SelectableMarker> = hashMapOf()

    var onMarkerClickListener: GoogleMap.OnMarkerClickListener? = null
    var onMarkerDragListener: GoogleMap.OnMarkerDragListener? = null
    var onMapClickListener: GoogleMap.OnMapClickListener? = null

    init {

        googleMap.setOnMapClickListener {
            if (unselectOnMapClick) {
                markers.values.forEach {
                    it.state = SelectableMarker.MarkerState.DEFAULT
                }
            }
            onMapClickListener?.onMapClick(it)
        }

        googleMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker?): Boolean {
                if (markers[marker?.tag]?.state == SelectableMarker.MarkerState.SELECTED) {
                    markers[marker?.tag]?.state = SelectableMarker.MarkerState.DEFAULT
                } else {
                    markers[marker?.tag]?.state = SelectableMarker.MarkerState.SELECTED
                }
                if (markerSelectionMethod == MarkerSelectionMethod.SINGLE) {
                    markers.filter { entry -> entry?.value?.markerId != marker?.tag }.values.forEach {
                        it.state = SelectableMarker.MarkerState.DEFAULT
                    }
                }
                return onMarkerClickListener?.onMarkerClick(marker) ?: false
            }

        })

        googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragEnd(marker: Marker?) {
                markers[marker?.tag]?.state = SelectableMarker.MarkerState.DEFAULT
                onMarkerDragListener?.onMarkerDragEnd(marker)
            }

            override fun onMarkerDragStart(marker: Marker?) {
                markers[marker?.tag]?.state = SelectableMarker.MarkerState.PRESSED
                onMarkerDragListener?.onMarkerDragStart(marker)
            }

            override fun onMarkerDrag(marker: Marker?) {

                onMarkerDragListener?.onMarkerDrag(marker)
            }

        })
    }

    private fun findAnchorView(rootView: View): Anchor? {
        if (rootView is Anchor) {
            return rootView as Anchor
        }
        if (rootView is ViewGroup) {
            for (i in 0 until rootView.childCount) {
                val child = rootView.getChildAt(i)
                val match = findAnchorView(child)
                if (match != null) {
                    return match
                }
            }
        }
        return null
    }

    private fun getRelativeLeft(view: View, rootView: View): Int {
        return if (view.parent === rootView)
            view.left
        else
            view.left + getRelativeLeft(view.parent as View, rootView)
    }

    private fun getRelativeTop(view: View, rootView: View): Int {
        return if (view.parent === rootView)
            view.top
        else
            view.top + getRelativeTop(view.parent as View, rootView)
    }

    private fun getMarker(view: View, markerOptions: MarkerOptions, markerId: Int, isDraggable: Boolean): Marker {
        val marker = googleMap.addMarker(markerOptions)
        marker.tag = markerId
        marker.isDraggable = isDraggable
        val anchor = findAnchorView(view)
        if (anchor != null && view is ViewGroup) {
            view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            val relativeTop = getRelativeTop(anchor, view)
            val relativeLeft = getRelativeLeft(anchor, view)
            marker.setAnchor(relativeTop.toFloat() / view.height.toFloat(), relativeLeft.toFloat() / view.width.toFloat())
        }
        return marker
    }

    private fun getMarkerBitmap(markerState: SelectableMarker.MarkerState, view: View): Bitmap {
        view.isPressed = markerState == SelectableMarker.MarkerState.PRESSED
        view.isSelected = markerState == SelectableMarker.MarkerState.SELECTED
        view.isDrawingCacheEnabled = true
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        view.buildDrawingCache(true)
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        return bitmap
    }

    fun addMarker(markerOptions: MarkerOptions, viewResId: Int, markerId: Int,
                  defaultState: SelectableMarker.MarkerState = SelectableMarker.MarkerState.DEFAULT, isDraggable: Boolean = false) {
        val view = LayoutInflater.from(context).inflate(viewResId, null)
        val selectableMarker = SelectableMarker(markerId,
                getMarker(view, markerOptions, markerId, isDraggable),
                getMarkerBitmap(SelectableMarker.MarkerState.DEFAULT, view),
                getMarkerBitmap(SelectableMarker.MarkerState.PRESSED, view),
                getMarkerBitmap(SelectableMarker.MarkerState.SELECTED, view))
        selectableMarker.state = defaultState
        markers[markerId] = selectableMarker
    }
}