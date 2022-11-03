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

import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getStringOrNull
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.infomaniak.lib.bugtracker.databinding.ActivityBugTrackerBinding
import com.infomaniak.lib.core.networking.HttpClient.okHttpClient
import com.infomaniak.lib.core.networking.HttpUtils.getHeaders
import com.infomaniak.lib.core.utils.SnackbarUtils.showSnackbar
import com.infomaniak.lib.core.utils.FilePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class BugTrackerActivity : AppCompatActivity() {

    private val binding: ActivityBugTrackerBinding by lazy { ActivityBugTrackerBinding.inflate(layoutInflater) }
    private val bugTrackerViewModel: BugTrackerViewModel by viewModels()
    private val navigationArgs: BugTrackerActivityArgs by navArgs()

    private val fileAdapter = BugTrackerFileAdapter() { updateFileTotalSize() }
    val types = listOf("bugs", "features")
    var type = types[DEFAULT_REPORT_TYPE]

    private fun updateFileTotalSize() = with(binding) {
        if (bugTrackerViewModel.files.count() <= 1) {
            totalSize.isGone = true
        } else {
            val filesSize = bugTrackerViewModel.files.sumOf { it.size }
            totalSize.apply {
                text = Formatter.formatShortFileSize(this@BugTrackerActivity, filesSize)
                isVisible = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) = with(binding) {
        super.onCreate(savedInstanceState)
        setContentView(root)

        toolbar.setNavigationOnClickListener { finish() }

        val filePicker = FilePicker(this@BugTrackerActivity)
        addFilesButton.setOnClickListener { filePicker.open(callback = ::addFiles) }

        typeField.apply {
            setOnItemClickListener { _, _, position, _ ->
                type = types[position]
            }

            setText(adapter.getItem(DEFAULT_REPORT_TYPE) as String, false)
        }

        priorityField.apply {
            setText(adapter.getItem(DEFAULT_PRIORITY_TYPE) as String, false)
        }

        fileRecyclerView.adapter = fileAdapter
        fileAdapter.bindToViewModel(bugTrackerViewModel.files)
        updateFileTotalSize()

        submitButton.setOnClickListener {
            if (bugTrackerViewModel.files.sumOf { it.size } < FILE_SIZE_32_MB) {
                sendBugReport()
            } else {
                showSnackbar(R.string.bugTrackerFileTooBig)
            }
        }
    }

    private fun addFiles(uris: List<Uri>) {
        val newFiles = mutableListOf<BugTrackerFile>()
        var errorCount = 0

        uris.forEach { uri ->
            runCatching {
                getFileFromUri(uri)?.let(newFiles::add)
            }.onFailure {
                it.printStackTrace()
                errorCount++
            }
        }

        if (errorCount > 0) {
            showSnackbar(resources.getQuantityString(R.plurals.bugTrackerUploadError, errorCount, errorCount))
        }

        fileAdapter.addFiles(newFiles)
        updateFileTotalSize()
    }

    private fun getFileFromUri(uri: Uri): BugTrackerFile? {
        val cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
        return cursor?.use { cursor ->
            cursor.moveToFirst()

            val fileName = getFileName(cursor) ?: uri.lastPathSegment ?: throw Exception("Could not find filename of the file")
            val fileSize = getFileSize(cursor)
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase()
            val mimeType = contentResolver.getType(uri) ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            val bytes = contentResolver.openInputStream(uri).use { it?.readBytes() }

            bytes?.let { BugTrackerFile(fileName, fileSize, uri, mimeType, it) }
        }
    }

    private fun getFileName(cursor: Cursor): String? {
        val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        return when {
            columnIndex != -1 -> cursor.getStringOrNull(columnIndex)
            else -> null
        }
    }

    private fun getFileSize(cursor: Cursor) = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))

    private fun sendBugReport() = with(binding) {
        val bucketIdentifier = navigationArgs.bucketIdentifier
        val subject = subjectField.prefixText.toString() + subjectTextInput.text.toString()
        val description = descriptionTextInput.text.toString()
        val priorityLabel = "Priorité: " + priorityField.text.toString()
        val priorityValue = (resources.getStringArray(R.array.bugTrackerPriority).indexOf(priorityLabel) + 1).toString()
        val extraProject = navigationArgs.projectName
        val extraRoute = "undefined"
        val userAgent = "InfomaniakBugTracker/1"
        val extraUserId = navigationArgs.user.id.toString()
        val extraOrganizationId = navigationArgs.user.preferences.organizationPreference.currentOrganizationId.toString()
        val extraUserMail = navigationArgs.user.email
        val extraUserDisplayName = navigationArgs.user.displayName ?: "undefined"

        val brand = Build.BRAND
        val osVersion = Build.VERSION.SDK_INT.toString()
        val device = Build.DEVICE
        val appVersion = navigationArgs.appBuildNumber

        val url = REPORT_URL
        val formBuilder: MultipartBody.Builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("bucket_identifier", bucketIdentifier)
            .addFormDataPart("subject", subject)
            .addFormDataPart("description", description)
            .addFormDataPart("priority[label]", priorityLabel)
            .addFormDataPart("priority[value]", priorityValue)
            .addFormDataPart("extra[project]", extraProject)
            .addFormDataPart("extra[route]", extraRoute)
            .addFormDataPart("extra[userAgent]", userAgent)
            .addFormDataPart("extra[userId]", extraUserId)
            .addFormDataPart("extra[groupId]", extraOrganizationId)
            .addFormDataPart("extra[userMail]", extraUserMail)
            .addFormDataPart("extra[userDisplayName]", extraUserDisplayName)
            .addFormDataPart("extra[brand]", brand)
            .addFormDataPart("extra[osVersion]", osVersion)
            .addFormDataPart("extra[device]", device)
            .addFormDataPart("extra[appVersion]", appVersion)
            .addFormDataPart("type", type)

        fileAdapter.files.forEachIndexed { index, file ->
            formBuilder.addFormDataPart(
                "file_$index",
                file.fileName,
                file.bytes.toRequestBody(file.mimeType?.toMediaTypeOrNull())
            )
        }

        val request = Request.Builder()
            .url(url)
            .headers(getHeaders())
            .post(formBuilder.build())
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            val response = okHttpClient.newBuilder().build().newCall(request).execute()
        }
    }

    class BugTrackerViewModel : ViewModel() {
        val files = mutableListOf<BugTrackerFile>()
    }

    data class BugTrackerFile(
        val fileName: String,
        val size: Long,
        val uri: Uri,
        val mimeType: String?,
        val bytes: ByteArray,
    )

    companion object {
        const val REPORT_URL = "https://welcome.infomaniak.com/api/components/report"

        const val MAIL_BUCKET_ID = "app_mail"
        const val MAIL_PROJECT_NAME = "mail"

        // const val DRIVE_BUCKET_ID = "???"
        const val DRIVE_PROJECT_NAME = "drive"

        const val DEFAULT_REPORT_TYPE = 0
        const val DEFAULT_PRIORITY_TYPE = 1

        const val FILE_SIZE_32_MB = 32 * 1024 * 1024
    }
}
