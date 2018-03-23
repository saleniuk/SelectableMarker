package com.saleniuk.selectablemarker.sample

import com.google.android.gms.maps.model.LatLng
import com.saleniuk.selectablemarker.SelectableItem

class CustomClusterItem(markerId: String, position: LatLng, var _snippet: String?, var _title: String?,
                        isDraggable: Boolean = false) : SelectableItem(markerId, position, isDraggable) {

   override fun getSnippet(): String? {
        return _snippet
    }

    fun setSnippet(snippet: String?) {
        this._snippet = snippet
    }

    override fun getTitle(): String? {
        return _title
    }

    fun setTitle(title: String?) {
        this._title = title
    }
}