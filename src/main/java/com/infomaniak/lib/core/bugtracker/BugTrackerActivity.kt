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

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.infomaniak.lib.core.R
import com.infomaniak.lib.core.databinding.ActivityBugTrackerBinding

class BugTrackerActivity : AppCompatActivity() {

    private val binding: ActivityBugTrackerBinding by lazy { ActivityBugTrackerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.typeField.setOnItemClickListener { _, _, position, _ ->
            val bugValue = resources.getStringArray(R.array.bugTrakerType)
            Log.e("bugValue", "${bugValue[position]}")
        }
    }
}
