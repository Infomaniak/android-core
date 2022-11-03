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
package com.infomaniak.lib.bugtracker

import android.text.format.Formatter.formatShortFileSize
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.infomaniak.lib.bugtracker.databinding.ItemBugTrackerFileBinding

class BugTrackerFileAdapter(
    private val onFileDeleted: () -> Unit
) : RecyclerView.Adapter<BugTrackerFileAdapter.BugTrackerFileViewHolder>() {

    var files: MutableList<BugTrackerActivity.BugTrackerFile> = mutableListOf()
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BugTrackerFileViewHolder {
        return BugTrackerFileViewHolder(ItemBugTrackerFileBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BugTrackerFileViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.firstOrNull() == Unit) {
            holder.binding.closeButton.setOnClickListener { removeFileAt(position) }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: BugTrackerFileViewHolder, position: Int): Unit = with(holder.binding) {
        val file = files[position]

        fileName.text = file.fileName
        fileSize.text = formatShortFileSize(root.context, file.size)
        closeButton.setOnClickListener { removeFileAt(position) }
    }

    override fun getItemCount(): Int = files.count()

    fun addFiles(newFiles: MutableList<BugTrackerActivity.BugTrackerFile>) {
        val startingPosition = files.count()
        files.addAll(newFiles)
        notifyItemRangeInserted(startingPosition, newFiles.count())
    }

    private fun removeFileAt(position: Int) {
        files.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position, Unit)
        onFileDeleted()
    }

    fun bindToViewModel(newFiles: MutableList<BugTrackerActivity.BugTrackerFile>) {
        files = newFiles
        notifyItemRangeInserted(0, newFiles.count())
    }

    class BugTrackerFileViewHolder(val binding: ItemBugTrackerFileBinding) : RecyclerView.ViewHolder(binding.root)
}