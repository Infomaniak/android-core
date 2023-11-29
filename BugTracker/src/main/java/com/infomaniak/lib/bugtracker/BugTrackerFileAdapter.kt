/*
 * Infomaniak Core - Android
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

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.infomaniak.lib.bugtracker.databinding.ItemBugTrackerFileBinding

class BugTrackerFileAdapter(
    private val onFileDeleted: () -> Unit
) : Adapter<BugTrackerFileAdapter.BugTrackerFileViewHolder>() {

    var files: MutableList<BugTrackerActivity.BugTrackerFile> = mutableListOf()
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BugTrackerFileViewHolder {
        return BugTrackerFileViewHolder(ItemBugTrackerFileBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BugTrackerFileViewHolder, position: Int): Unit = with(holder.binding) {
        val file = files[position]

        fileName.text = file.fileName
        fileSize.text = Formatter.formatShortFileSize(root.context, file.size)
        closeButton.setOnClickListener { removeFile(file) }
    }

    override fun getItemCount(): Int = files.count()

    fun addFiles(newFiles: MutableList<BugTrackerActivity.BugTrackerFile>) {
        val startingPosition = files.count()
        files.addAll(newFiles)
        notifyItemRangeInserted(startingPosition, newFiles.count())
    }

    private fun removeFile(file: BugTrackerActivity.BugTrackerFile) {
        val position = files.indexOf(file)
        files.removeAt(position)
        notifyItemRemoved(position)
        onFileDeleted()
    }

    fun bindToViewModel(newFiles: MutableList<BugTrackerActivity.BugTrackerFile>) {
        files = newFiles
        notifyItemRangeInserted(0, newFiles.count())
    }

    class BugTrackerFileViewHolder(val binding: ItemBugTrackerFileBinding) : ViewHolder(binding.root)
}
