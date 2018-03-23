package com.saleniuk.selectablemarker

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

abstract class SelectableMarkerManager<T : SelectableItem> {

    enum class MarkerSelectionMethod {
        SINGLE, MULTI
    }

    private var _context: Context
    private var _googleMap: GoogleMap
    private var _markerSelectionMethod: MarkerSelectionMethod
    private var _unselectOnMapClick: Boolean = true
    private var _markers: HashMap<String, SelectableMarker> = hashMapOf()

    var onMarkerClickListener: GoogleMap.OnMarkerClickListener? = null
    var onMarkerDragListener: GoogleMap.OnMarkerDragListener? = null
    var onMapClickListener: GoogleMap.OnMapClickListener? = null

    constructor(context: Context,
                googleMap: GoogleMap,
                markerSelectionMethod: MarkerSelectionMethod,
                unselectOnMapClick: Boolean) {
        this._context = context
        this._googleMap = googleMap
        this._markerSelectionMethod = markerSelectionMethod
        this._unselectOnMapClick = unselectOnMapClick

        _googleMap.setOnMapClickListener {
            if (_unselectOnMapClick) {
                _markers.values.forEach {
                    it.state = SelectableMarker.MarkerState.DEFAULT
                }
            }
            onMapClickListener?.onMapClick(it)
        }

        _googleMap.setOnMarkerClickListener { marker ->
            onMarkerClick(marker)
        }

        _googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragEnd(marker: Marker?) {
                _markers[marker?.tag]?.marker = marker!!
                _markers[marker?.tag]?.state = SelectableMarker.MarkerState.DEFAULT
                onMarkerDragListener?.onMarkerDragEnd(marker)
            }

            override fun onMarkerDragStart(marker: Marker?) {
                _markers[marker?.tag]?.marker = marker!!
                _markers[marker?.tag]?.state = SelectableMarker.MarkerState.PRESSED
                onMarkerDragListener?.onMarkerDragStart(marker)
            }

            override fun onMarkerDrag(marker: Marker?) {

                onMarkerDragListener?.onMarkerDrag(marker)
            }

        })
    }


    open fun onMarkerClick(marker: Marker?): Boolean {
        _markers[marker?.tag]?.marker = marker!!
        if (_markers[marker?.tag]?.state == SelectableMarker.MarkerState.SELECTED) {
            _markers[marker?.tag]?.state = SelectableMarker.MarkerState.DEFAULT
        } else {
            _markers[marker?.tag]?.state = SelectableMarker.MarkerState.SELECTED
        }
        if (_markerSelectionMethod == MarkerSelectionMethod.SINGLE) {
            _markers.filter { entry -> entry?.value?.selectableItem.markerId != marker?.tag }
                    .values.forEach {
                it.state = SelectableMarker.MarkerState.DEFAULT
            }
        }
        return onMarkerClickListener?.onMarkerClick(marker) ?: true
        return true
    }


    private fun findAnchorView(rootView: View): Anchor? {
        if (rootView is Anchor) {
            return rootView
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

    private fun createMarker(selectableItem: T,
                             markerOptions: MarkerOptions): Marker {

        val marker = _googleMap.addMarker(markerOptions)
        return updateMarker(selectableItem, bindMarkerView(selectableItem), marker)
    }

    private fun updateMarker(selectableItem: T,
                             view: View,
                             marker: Marker): Marker {

        marker.tag = selectableItem.markerId
        marker.zIndex = selectableItem.zIndex
        marker.isDraggable = selectableItem.isDraggable
        val anchor = findAnchorView(view)
        if (anchor != null && view is ViewGroup) {
            view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            val relativeTop = getRelativeTop(anchor, view)
            val relativeLeft = getRelativeLeft(anchor, view)
            marker.setAnchor(relativeLeft.toFloat() / view.width.toFloat(), relativeTop.toFloat() / view.height.toFloat())
        }
        return marker
    }

    private fun createMarkerBitmap(markerState: SelectableMarker.MarkerState, view: View): Bitmap {

        view.isPressed = markerState == SelectableMarker.MarkerState.PRESSED
        view.isSelected = markerState == SelectableMarker.MarkerState.SELECTED
        view.isDrawingCacheEnabled = true
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.requestLayout()
        view.buildDrawingCache(true)
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        return bitmap
    }


    fun prepareMarkerOptions(selectableItem: T, markerOptions: MarkerOptions,
                             state: SelectableMarker.MarkerState = SelectableMarker.MarkerState.DEFAULT): MarkerOptions {
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createMarkerBitmap(state, bindMarkerView(selectableItem))))
        markerOptions.zIndex(selectableItem.zIndex)
        markerOptions.draggable(selectableItem.isDraggable)
        return markerOptions
    }

    private fun addMarker(selectableItem: T,
                          markerOptions: MarkerOptions,
                          state: SelectableMarker.MarkerState = SelectableMarker.MarkerState.DEFAULT): Marker {

        val selectableMarker = SelectableMarker(selectableItem,
                createMarker(selectableItem, markerOptions),
                createMarkerBitmap(SelectableMarker.MarkerState.DEFAULT, bindMarkerView(selectableItem)),
                createMarkerBitmap(SelectableMarker.MarkerState.PRESSED, bindMarkerView(selectableItem)),
                createMarkerBitmap(SelectableMarker.MarkerState.SELECTED, bindMarkerView(selectableItem)))
        selectableMarker.state = state
        _markers[selectableItem.markerId] = selectableMarker
        return selectableMarker.marker
    }

    private fun addMarker(selectableItem: T,
                          marker: Marker,
                          state: SelectableMarker.MarkerState = SelectableMarker.MarkerState.DEFAULT): Marker {

        val selectableMarker = SelectableMarker(selectableItem,
                updateMarker(selectableItem, bindMarkerView(selectableItem), marker),
                createMarkerBitmap(SelectableMarker.MarkerState.DEFAULT, bindMarkerView(selectableItem)),
                createMarkerBitmap(SelectableMarker.MarkerState.PRESSED, bindMarkerView(selectableItem)),
                createMarkerBitmap(SelectableMarker.MarkerState.SELECTED, bindMarkerView(selectableItem)))
        selectableMarker.state = state
        _markers[selectableItem.markerId] = selectableMarker
        return selectableMarker.marker
    }

    fun addItem(selectableItem: T,
                state: SelectableMarker.MarkerState = SelectableMarker.MarkerState.DEFAULT) {
        val markerOptions = MarkerOptions()
                .draggable(selectableItem.isDraggable)
                .position(selectableItem.position)
                .flat(false)
                .zIndex(selectableItem.zIndex)
        addMarker(selectableItem, markerOptions, state)
    }

    fun setItem(selectableItem: T,
                marker: Marker,
                state: SelectableMarker.MarkerState = SelectableMarker.MarkerState.DEFAULT) {
        addMarker(selectableItem, marker, state)
    }

    fun getSelectableMarker(markerId: String): SelectableMarker? {
        return _markers[markerId]
    }

    fun updateView(selectableItem: T, view: View) {

        val selectableMarker = _markers[selectableItem.markerId]
        if (selectableMarker != null) {
            val newSelectableMarker = SelectableMarker(selectableItem,
                    updateMarker(selectableItem, view, selectableMarker.marker),
                    createMarkerBitmap(SelectableMarker.MarkerState.DEFAULT, bindMarkerView(selectableItem)),
                    createMarkerBitmap(SelectableMarker.MarkerState.PRESSED, bindMarkerView(selectableItem)),
                    createMarkerBitmap(SelectableMarker.MarkerState.SELECTED, bindMarkerView(selectableItem)))
            selectableMarker.state = selectableMarker.state
            _markers[selectableItem.markerId] = newSelectableMarker
        }
    }

    fun getMarkers() = _markers

    abstract fun bindMarkerView(item: T): View
}