/*
 * Infomaniak kMail - Android
 * Copyright (C) 2022 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.core.bugtracker

import android.text.format.Formatter.formatShortFileSize
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.infomaniak.lib.core.databinding.ItemBugTrackerImageBinding

class BugTrackerImageAdapter : RecyclerView.Adapter<BugTrackerImageAdapter.BugTrackerImageViewHolder>() {

    private var images: MutableList<BugTrackerActivity.Image> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BugTrackerImageViewHolder {
        return BugTrackerImageViewHolder(ItemBugTrackerImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BugTrackerImageViewHolder, position: Int): Unit = with(holder.binding) {
        val image = images[position]

        Log.e("gibran", "onBindViewHolder - binding position: ${position}")

        fileName.text = image.name
        fileSize.text = formatShortFileSize(root.context, image.size)
        closeButton.setOnClickListener { removeImage(image) }
    }

    override fun getItemCount(): Int = images.count()

    fun addImages(newImages: MutableList<BugTrackerActivity.Image>) {
        val startingPosition = images.count()
        Log.e("gibran", "addImages - newImages: ${newImages}")
        Log.e("gibran", "addImages - newImages.count(): ${newImages.count()}")
        images.addAll(newImages)
        notifyItemRangeInserted(startingPosition, newImages.count())
    }

    fun getImages() = images

    private fun removeImage(image: BugTrackerActivity.Image) {
        val position = images.indexOf(image)
        Log.e("gibran", "removeImage - removing at position: ${position}")
        images.removeAt(position)
        notifyItemRemoved(position)
    }

    class BugTrackerImageViewHolder(val binding: ItemBugTrackerImageBinding) : RecyclerView.ViewHolder(binding.root)
}