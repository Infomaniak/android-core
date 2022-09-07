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
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getStringOrNull
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.infomaniak.lib.core.BuildConfig
import com.infomaniak.lib.core.InfomaniakCore
import com.infomaniak.lib.core.R
import com.infomaniak.lib.core.databinding.ActivityBugTrackerBinding
import com.infomaniak.lib.core.utils.SnackbarUtils.showSnackbar
import com.infomaniak.lib.core.utils.whenResultIsOk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder


class BugTrackerActivity : AppCompatActivity() {

    private val binding: ActivityBugTrackerBinding by lazy { ActivityBugTrackerBinding.inflate(layoutInflater) }
    private val navigationArgs: BugTrackerActivityArgs by navArgs()

    private val imageAdapter = BugTrackerImageAdapter()
    val types = listOf("bugs", "features") // TODO : What values should be sent ?
    var type = ""

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
                        newImages.add(getImageFromUri(clipData.getItemAt(i).uri))
                    }.onFailure {
                        it.printStackTrace()
                        errorCount++
                    }
                }
            } else if (uri != null) {
                newImages.add(getImageFromUri(uri))
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            errorCount++
        } finally {
            if (errorCount > 0) {
                showSnackbar("Some files failed to be imported: ($errorCount)")
            }
        }

        imageAdapter.addImages(newImages)
    }

    private fun getImageFromUri(uri: Uri): Image {
        val cursor: Cursor =
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
                ?: throw Exception("Cursor is null")
        cursor.moveToFirst()

        val fileName = getFileName(cursor) ?: uri.lastPathSegment ?: throw Exception("Could not find filename of the file")
        val fileSize = getFileSize(cursor)

        val image = Image(fileName, fileSize, uri)

        cursor.close()

        return image
    }

    private fun getFileName(cursor: Cursor): String? {
        val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        return when {
            columnIndex != -1 -> cursor.getStringOrNull(columnIndex)
            else -> null
        }
    }

    private fun getFileSize(cursor: Cursor) = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            setContentView(root)

            toolbar.setNavigationOnClickListener { finish() }

            addFilesButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                val chooserIntent = Intent.createChooser(intent, "Sélectionner des images TODO")
                selectImagesResultLauncher.launch(chooserIntent)
            }

            typeField.setOnItemClickListener { _, _, position, _ ->
                // val bugValue = resources.getStringArray(R.array.bugTrakerType)
                // Log.e("bugValue", bugValue[position])
                type = types[position]
            }

            fileRecyclerView.adapter = imageAdapter

            submitButton.setOnClickListener {
                val bucketIdentifier = "app_mail" // bucket_identifier // TODO : understand iOS code how it works ?
                val subject = subjectField.prefixText.toString() + subjectTextInput.text.toString() // subject
                val description = descriptionTextInput.text.toString() // description
                val priorityString = priorityField.text.toString() // priority[label]
                val priorityValue =
                    (resources.getStringArray(R.array.bugTrakerPriority)
                        .indexOf(priorityString) + 1).toString() // priority[value]
                val extraProject = navigationArgs.projectName // extra[project]
                // val extraRoute = "undefined" // extra[route] // PAS DE SENS
                val userAgent = "InfomaniakBugTracker/1" // extra[userAgent]
                val extraUserId = navigationArgs.user.id.toString() // extra[userId]
                val extraOrganizationId =
                    navigationArgs.user.preferences.organizationPreference.currentOrganizationId.toString() // extra[groupId]
                val extraUserMail = navigationArgs.user.email // extra[userMail]
                val extraUserDisplayName = navigationArgs.user.displayName ?: "undefined" // extra[userDisplayName]
                // val extraPageLink = // extra[pageLink] // PAS DE SENS
                // val extraConsole = // extra[console] // PAS DE SENS
                // val typeIndex = resources.getStringArray(R.array.bugTrakerType).toList().indexOf(typeField.text.toString())
                // val file0 = // file_0

                // From iOS code:
                val platform = android.os.Build.BRAND
                val os_version = android.os.Build.VERSION.SDK_INT
                val device = android.os.Build.DEVICE
                val app_version = navigationArgs.appBuildNumber


                val imagesToSend = mutableListOf<ByteArray>()
                imageAdapter.getImages().forEach { image ->
                    val inputStream = contentResolver.openInputStream(image.uri)
                    inputStream?.readBytes()?.let { imagesToSend.add(it) }
                }


                Log.e("gibran", "onCreate - bucket_identifier: ${bucketIdentifier}")
                Log.e("gibran", "onCreate - subject: ${subject}")
                Log.e("gibran", "onCreate - description: ${description}")
                Log.e("gibran", "onCreate - priorityString: ${priorityString}")
                Log.e("gibran", "onCreate - priorityValue: ${priorityValue}")
                Log.e("gibran", "onCreate - extraProject: ${extraProject}")
                // Log.e("gibran", "onCreate - extraRoute: ${extraRoute}")
                Log.e("gibran", "onCreate - userAgent: ${userAgent}")
                Log.e("gibran", "onCreate - extraUserId: ${extraUserId}")
                Log.e("gibran", "onCreate - extraGroupId: ${extraOrganizationId}")
                Log.e("gibran", "onCreate - extraUserMail: ${extraUserMail}")
                Log.e("gibran", "onCreate - extraUserDisplayName: ${extraUserDisplayName}")
                // Log.e("gibran", "onCreate - extraPageLink: ${extraPageLink}")
                // Log.e("gibran", "onCreate - extraConsole: ${extraConsole}")
                Log.e("gibran", "onCreate - type: ${type}")
                Log.e("gibran", "onCreate: ===");

                Log.e("gibran", "onCreate - platform: ${platform}")
                Log.e("gibran", "onCreate - os_version: ${os_version}")
                Log.e("gibran", "onCreate - device: ${device}")
                Log.e("gibran", "onCreate - app_version: ${app_version}")
                Log.e("gibran", "onCreate: ===");

                imagesToSend.forEachIndexed { index, bytes ->
                    Log.e("gibran", "onCreate - file_${index} -> bytes.size : ${bytes.size}")
                }


                val httpClient = OkHttpClient.Builder().apply {
                    if (BuildConfig.DEBUG) {
                        addNetworkInterceptor(StethoInterceptor())
                    }
                }.run {
                    build()
                }

                val url = "https://welcome.infomaniak.com/api/components/report"
                val formBuilder: MultipartBody.Builder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("bucket_identifier", bucketIdentifier)
                    .addFormDataPart("subject", subject)
                    .addFormDataPart("description", description)
                    .addFormDataPart("priority[label]", priorityString)
                    .addFormDataPart("priority[value]", priorityValue)
                    .addFormDataPart("extra[project]", extraProject)
                    .addFormDataPart("extra[userAgent]", userAgent)
                    .addFormDataPart("extra[userId]", extraUserId)
                    .addFormDataPart("extra[groupId]", extraOrganizationId)
                    .addFormDataPart("extra[userMail]", extraUserMail)
                    .addFormDataPart("extra[userDisplayName]", extraUserDisplayName)
                    .addFormDataPart("type", type)

                imagesToSend.forEachIndexed { index, bytes ->
                    formBuilder.addFormDataPart("file_$index", bytes.toString())
                }

                val request = Request.Builder()
                    .url(url)
                    .headers(getReportHeader())
                    .post(formBuilder.build())
                    .build()

                lifecycleScope.launch(Dispatchers.IO) {
                    val response = httpClient.newBuilder().build().newCall(request).execute()
                    Log.e("gibran", "onCreate - response: ${response}")
                }
            }
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

    data class Image(
        val name: String,
        val size: Long,
        val uri: Uri
    )
}
