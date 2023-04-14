/*
 * Copyright (C) 2022-2023 Infomaniak Network SA
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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.infomaniak.lib.core.R

abstract class LoaderAdapter<T> : RecyclerView.Adapter<ViewHolder>() {

    private var showLoading = false
    val itemList: ArrayList<T> = ArrayList()
    var isComplete = false
    var numberItemLoader = 3

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder

    abstract override fun onBindViewHolder(holder: ViewHolder, position: Int)

    override fun getItemCount() = itemList.size + if (showLoading) numberItemLoader else 0

    override fun getItemViewType(position: Int): Int = if (position < itemList.size) VIEW_TYPE_NORMAL else VIEW_TYPE_LOADING

    fun addAll(newItemList: ArrayList<T>) {
        hideLoading()
        val beforeItemCount = itemCount
        itemList.addAll(newItemList)
        notifyItemRangeInserted(beforeItemCount, newItemList.size)
    }

    fun clean() {
        val itemListSize = itemList.size
        itemList.clear()
        notifyItemRangeRemoved(0, itemListSize)
    }

    fun showLoading() {
        if (!showLoading) {
            showLoading = true
            notifyItemRangeInserted(itemList.size, numberItemLoader)
        }
    }

    private fun hideLoading() {
        if (showLoading) {
            showLoading = false
            notifyItemRangeRemoved(itemList.size, numberItemLoader)
        }
    }

    companion object {

        const val VIEW_TYPE_LOADING = 1
        const val VIEW_TYPE_NORMAL = 2

        fun createLoadingViewHolder(parent: ViewGroup): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false))
        }
    }
}
