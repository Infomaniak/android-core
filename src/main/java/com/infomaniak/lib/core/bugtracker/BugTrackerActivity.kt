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

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getStringOrNull
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.infomaniak.lib.core.InfomaniakCore
import com.infomaniak.lib.core.R
import com.infomaniak.lib.core.databinding.ActivityBugTrackerBinding
import com.infomaniak.lib.core.networking.HttpClient.okHttpClient
import com.infomaniak.lib.core.utils.SnackbarUtils.showSnackbar
import com.infomaniak.lib.core.utils.whenResultIsOk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder

class BugTrackerActivity : AppCompatActivity() {

    private val binding: ActivityBugTrackerBinding by lazy { ActivityBugTrackerBinding.inflate(layoutInflater) }
    private val bugTrackerViewModel: BugTrackerViewModel by viewModels()
    private val navigationArgs: BugTrackerActivityArgs by navArgs()

    private val imageAdapter = BugTrackerImageAdapter() { updateImageTotalSize() }
    val types = listOf("bugs", "features")
    var type = types[DEFAULT_REPORT_TYPE]

    private val selectImagesResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it.whenResultIsOk { data -> onImagesImported(data) }
    }

    private fun onImagesImported(importIntent: Intent?) {
        val clipData = importIntent?.clipData
        val uri = importIntent?.data
        var errorCount = 0

        val newImages = mutableListOf<Image>()

        try {
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    runCatching {
                        getImageFromUri(clipData.getItemAt(i).uri)?.let(newImages::add)
                    }.onFailure {
                        it.printStackTrace()
                        errorCount++
                    }
                }
            } else if (uri != null) {
                getImageFromUri(uri)?.let(newImages::add)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            errorCount++
        } finally {
            if (errorCount > 0) {
                showSnackbar("Some images failed to be imported: ($errorCount)")
            }
        }

        imageAdapter.addImages(newImages)
        updateImageTotalSize()
    }

    private fun updateImageTotalSize() {
        if (bugTrackerViewModel.images.count() <= 1) {
            binding.totalSize.isGone = true
        } else {
            val imagesSize = bugTrackerViewModel.images.sumOf { it.size }
            binding.totalSize.text = Formatter.formatShortFileSize(this, imagesSize)
            binding.totalSize.isVisible = true
        }
    }

    private fun getImageFromUri(uri: Uri): Image? {
        val cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
        return cursor?.use { cursor ->
            cursor.moveToFirst()

            val fileName = getFileName(cursor) ?: uri.lastPathSegment ?: throw Exception("Could not find filename of the file")
            val fileSize = getFileSize(cursor)
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase()
            val mimeType = contentResolver.getType(uri) ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            val bytes = contentResolver.openInputStream(uri).use { it?.readBytes() }

            bytes?.let { Image(fileName, fileSize, uri, mimeType, it) }
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

    override fun onCreate(savedInstanceState: Bundle?) = with(binding) {
        super.onCreate(savedInstanceState)
        setContentView(root)

        toolbar.setNavigationOnClickListener { finish() }

        addFilesButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            val chooserIntent = Intent.createChooser(intent, getString(R.string.bugTrackerAddFiles))
            selectImagesResultLauncher.launch(chooserIntent)
        }

        typeField.apply {
            setOnItemClickListener { _, _, position, _ ->
                type = types[position]
            }

            setText(adapter.getItem(DEFAULT_REPORT_TYPE) as String, false)
        }

        priorityField.apply {
            setText(adapter.getItem(DEFAULT_PRIORITY_TYPE) as String, false)
        }

        fileRecyclerView.adapter = imageAdapter
        imageAdapter.bindToViewModel(bugTrackerViewModel.images)
        updateImageTotalSize()

        submitButton.setOnClickListener {
            if (bugTrackerViewModel.images.sumOf { it.size } < MB_32) {
                sendBugReport()
            } else {
                showSnackbar(R.string.bugTrackerFileTooBig)
            }
        }
    }

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

        imageAdapter.getImages().forEachIndexed { index, image ->
            formBuilder.addFormDataPart(
                "file_$index",
                image.name,
                image.bytes.toRequestBody(image.mimeType?.toMediaTypeOrNull())
            )
        }

        val request = Request.Builder()
            .url(url)
            .headers(getReportHeader())
            .post(formBuilder.build())
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            val response = okHttpClient.newBuilder().build().newCall(request).execute()
        }
    }

    private fun getReportHeader(): Headers {
        return Headers.Builder().apply {
            add("accept", "*/*")
            add("accept-encoding", "gzip, deflate, br")
            add("accept-language", "fr-FR,fr;q=0.9")
            add("App-Version", "Android ${InfomaniakCore.appVersionName}")
            add("Authorization", "Bearer ${InfomaniakCore.bearerToken}")
            add("Cache-Control", "no-cache")
            InfomaniakCore.deviceIdentifier?.let { add("Device-Identifier", URLEncoder.encode(it, "UTF-8")) }
        }.run {
            build()
        }
    }

    class BugTrackerViewModel : ViewModel() {
        val images = mutableListOf<Image>()
    }

    data class Image(
        val name: String,
        val size: Long,
        val uri: Uri,
        val mimeType: String?,
        val bytes: ByteArray,
    )

    companion object {
        const val REPORT_URL = "https://welcome.infomaniak.com/api/components/report"
        const val MAIL_BUCKET_ID = "app_mail"

        const val DEFAULT_REPORT_TYPE = 0
        const val DEFAULT_PRIORITY_TYPE = 1

        const val MB_32 = 32 * 1024 * 1024
    }
}
