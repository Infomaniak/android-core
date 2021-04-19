/*
 * Copyright (C) 2021 Infomaniak Network SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.infomaniak.lib.core.views

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class LoaderAdapter<T> : RecyclerView.Adapter<ViewHolder>() {

    val itemList: ArrayList<T> = ArrayList()
    var totalPossible = 0
    var numberItemLoader = 3
        get() = if (field > 10) 10 else field
    private var showLoading = true

    override fun getItemCount() = itemList.size + if (showLoading) numberItemLoader else 0

    override fun getItemViewType(position: Int): Int {
        return if (position < itemList.size) VIEW_TYPE_NORMAL
        else VIEW_TYPE_LOADING
    }

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder

    abstract override fun onBindViewHolder(holder: ViewHolder, position: Int)

    fun addAll(newItemList: ArrayList<T>) {
        val beforeItemCount = itemCount
        itemList.addAll(newItemList)
        hideLoading(beforeItemCount)
    }

    fun clean() {
        itemList.clear()
        notifyDataSetChanged()
    }

    fun showLoading() {
        if (!showLoading) {
            showLoading = true
            notifyItemRangeInserted(itemCount - numberItemLoader, itemCount)
        }
    }

    private fun hideLoading(beforeItemCount: Int) {
        if (showLoading) {
            showLoading = false
            if (beforeItemCount > itemCount) {
                notifyItemRangeRemoved(beforeItemCount - numberItemLoader, beforeItemCount)
            } else notifyItemRangeChanged(beforeItemCount - numberItemLoader, beforeItemCount)
        }
        if (beforeItemCount < itemCount) notifyItemRangeInserted(beforeItemCount, itemCount)
    }

    fun loadMorePossible(): Boolean {
        return itemList.size < totalPossible && !showLoading
    }

    fun showSearchResults(newItemList: ArrayList<T>) {
        itemList.clear()
        itemList.addAll(newItemList)
        showLoading = false
        notifyDataSetChanged()
    }

    fun removeSearchResults(oldItemList: ArrayList<T>) {
        itemList.clear()
        itemList.addAll(oldItemList)
        notifyDataSetChanged()
    }

    companion object {
        const val VIEW_TYPE_LOADING = 1
        const val VIEW_TYPE_NORMAL = 2
    }
}