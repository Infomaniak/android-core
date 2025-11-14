/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2025 Infomaniak Network SA
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
package com.infomaniak.core.legacy.bugtracker

import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.infomaniak.core.appversionchecker.data.models.AppVersion
import com.infomaniak.core.legacy.bugtracker.databinding.ActivityBugTrackerBinding
import com.infomaniak.core.legacy.utils.FilePicker
import com.infomaniak.core.legacy.utils.SnackbarUtils.showSnackbar
import com.infomaniak.core.legacy.utils.getFileNameAndSize
import com.infomaniak.core.legacy.utils.hideProgressCatching
import com.infomaniak.core.legacy.utils.initProgress
import com.infomaniak.core.legacy.utils.setMargins
import com.infomaniak.core.legacy.utils.showProgressCatching
import com.infomaniak.core.legacy.utils.showToast
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class BugTrackerActivity : AppCompatActivity() {

    private fun WindowInsetsCompat.cutout() = getInsets(WindowInsetsCompat.Type.displayCutout())
    private fun WindowInsetsCompat.systemBars() = getInsets(WindowInsetsCompat.Type.systemBars())
    private fun WindowInsetsCompat.safeArea() = Insets.max(systemBars(), cutout())

    private val binding: ActivityBugTrackerBinding by lazy { ActivityBugTrackerBinding.inflate(layoutInflater) }
    private val bugTrackerViewModel: BugTrackerViewModel by viewModels()
    private val navigationArgs: BugTrackerActivityArgs by navArgs()

    private val fileAdapter = BugTrackerFileAdapter { updateFileTotalSize() }
    var type = DEFAULT_REPORT_TYPE

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

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            with(insets.safeArea()) { view.setMargins(top = top, left = left, right = right, bottom = bottom) }
            WindowInsetsCompat.CONSUMED
        }

        setContentView(root)

        toolbar.setNavigationOnClickListener { finish() }

        val filePicker = FilePicker(activity = this@BugTrackerActivity).apply { initCallback(::addFiles) }
        addFilesButton.setOnClickListener { filePicker.open() }

        typeField.apply {
            setOnItemClickListener { _, _, position, _ ->
                type = ReportType.entries[position]
            }

            setText(adapter.getItem(ReportType.entries.indexOf(DEFAULT_REPORT_TYPE)) as String, false)
        }

        priorityField.apply {
            setText(adapter.getItem(DEFAULT_PRIORITY_TYPE) as String, false)
        }

        fileRecyclerView.adapter = fileAdapter
        fileAdapter.bindToViewModel(bugTrackerViewModel.files)
        updateFileTotalSize()

        submitButton.apply {
            initProgress(this@BugTrackerActivity)
            setOnClickListener {
                if (bugTrackerViewModel.files.sumOf { it.size } >= FILE_SIZE_32_MB) {
                    showSnackbar(R.string.bugTrackerFileTooBig)
                } else if (descriptionTextInput.text.isNullOrBlank() || subjectTextInput.text.isNullOrBlank()) {
                    missingFieldsError.isVisible = true
                } else {
                    showProgressCatching()
                    sendBugReport()
                }
            }
        }

        hideErrorWhenNeeded()

        observeBugReportResult()

        checkLastAppVersion()
    }

    private fun ActivityBugTrackerBinding.checkLastAppVersion() {
        lifecycleScope.launch {
            bugTrackerViewModel.mustRequireUpdate(
                appId = navigationArgs.appId,
                appVersion = navigationArgs.appBuildNumber,
                store = AppVersion.Store.PLAY_STORE,
                channelFilter = AppVersion.VersionChannel.Internal
            ).flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { updateRequired ->
                appNotUpToDate.isGone = !updateRequired
            }
        }

        // gitHubViewModel.getLastRelease(navigationArgs.repoGitHub).observe(this@BugTrackerActivity) { lastRelease ->
        //     appNotUpToDate.isGone = lastRelease?.name == navigationArgs.appBuildNumber
        // }
    }

    private fun ActivityBugTrackerBinding.observeBugReportResult() {
        bugTrackerViewModel.bugReportResult.observe(this@BugTrackerActivity) { isSuccessful ->
            if (isSuccessful) {
                showToast(R.string.bugTrackerFormSubmitSuccess)
                finish()
            } else {
                submitButton.hideProgressCatching(R.string.bugTrackerSubmit)
                showSnackbar(R.string.bugTrackerFormSubmitError)
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
        val (fileName, fileSize) = getFileNameAndSize(uri) ?: return null
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase()
        val mimeType = contentResolver.getType(uri) ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        val bytes = contentResolver.openInputStream(uri).use { it?.readBytes() }

        return bytes?.let { BugTrackerFile(fileName, fileSize, uri, mimeType, it) }
    }

    private fun ActivityBugTrackerBinding.hideErrorWhenNeeded() {
        descriptionTextInput.doOnTextChanged { text, _, _, _ -> if ((text?.count() ?: 0) > 0) missingFieldsError.isGone = true }
        subjectTextInput.doOnTextChanged { text, _, _, _ -> if ((text?.count() ?: 0) > 0) missingFieldsError.isGone = true }
    }

    private fun sendBugReport() = with(binding) {
        val bucketIdentifier = navigationArgs.bucketIdentifier
        val subject = subjectField.prefixText.toString() + subjectTextInput.text.toString()
        val description = descriptionTextInput.text.toString()
        val priorityLabel = "Priorité: " + priorityField.text.toString()
        val priorityValue = (resources.getStringArray(R.array.bugTrackerPriorityArray).indexOf(priorityLabel) + 1).toString()

        val extraProject = navigationArgs.projectName
        val extraRoute = "undefined"
        val extraUserAgent = "InfomaniakBugTracker/1"

        val extraUserId = navigationArgs.userId.toString()
        val extraOrganizationId = navigationArgs.userCurrentOrganizationId.toString()
        val extraUserMail = navigationArgs.userEmail
        val extraUserDisplayName = navigationArgs.userDisplayName ?: "undefined"

        val brand = Build.BRAND
        val osVersion = SDK_INT.toString()
        val device = Build.DEVICE
        val appVersion = navigationArgs.appBuildNumber

        val fullDescription = description + "\n" +
                "\n---\n" +
                "\n**App name:** $extraProject" +
                "\n**App version:** $appVersion" + "\n" +
                "\n**User display name:** $extraUserDisplayName" +
                "\n**User mail:** $extraUserMail" +
                "\n**User ID:** $extraUserId" +
                "\n**User organization ID:** $extraOrganizationId" + "\n" +
                "\n**Device brand:** $brand" +
                "\n**Device:** $device" +
                "\n**Device OS version:** $osVersion" + "\n" +
                "\n---\n"

        val formBuilder: MultipartBody.Builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("bucket_identifier", bucketIdentifier)
            .addFormDataPart("subject", subject)
            .addFormDataPart("description", fullDescription)
            .addFormDataPart("priority[label]", priorityLabel)
            .addFormDataPart("priority[value]", priorityValue)
            .addFormDataPart("extra[project]", extraProject)
            .addFormDataPart("extra[route]", extraRoute)
            .addFormDataPart("extra[userAgent]", extraUserAgent)
            .addFormDataPart("type", type.apiValue)

        bugTrackerViewModel.sendBugReport(formBuilder)
    }

    data class BugTrackerFile(
        val fileName: String,
        val size: Long,
        val uri: Uri,
        val mimeType: String?,
        val bytes: ByteArray,
    )

    enum class ReportType(val apiValue: String) {
        BUGS("bugs"),
        FEATURES("features")
    }

    private companion object {

        val DEFAULT_REPORT_TYPE = ReportType.BUGS
        const val DEFAULT_PRIORITY_TYPE = 1

        const val FILE_SIZE_32_MB = 32 * 1_024 * 1_024 // 32 MB
    }
}
