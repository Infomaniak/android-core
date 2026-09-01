/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.infomaniak.core.common.backup

import android.app.backup.BackupAgent
import android.os.ParcelFileDescriptor
import android.system.ErrnoException
import android.system.Os
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Copied from `android.app.backup.FullBackup`, then converted to Kotlin so we don't need to enable Java compilation.
 *
 *
 * Global constant definitions et cetera related to the full-backup-to-fd
 * binary format.  Nothing in this namespace is part of any API; it's all
 * hidden details of the current implementation gathered into one location.
 *
 */
// Ignore warnings from code copied from AOSP.
object FullBackup {
    const val TAG: String = "FullBackup"

    /**
     * Copy data from a socket to the given File location on permanent storage.  The
     * modification time and access mode of the resulting file will be set if desired,
     * although group/all rwx modes will be stripped: the restored file will not be
     * accessible from outside the target application even if the original file was.
     * If the `type` parameter indicates that the result should be a directory,
     * the socket parameter may be `null`; even if it is valid, no data will be
     * read from it in this case.
     *
     *
     * If the `mode` argument is negative, then the resulting output file will not
     * have its access mode or last modification time reset as part of this operation.
     *
     * @param data Socket supplying the data to be copied to the output file.  If the
     * output is a directory, this may be `null`.
     * @param size Number of bytes of data to copy from the socket to the file.  At least
     * this much data must be available through the `data` parameter.
     * @param type Must be either [BackupAgent.TYPE_FILE] for ordinary file data
     * or [BackupAgent.TYPE_DIRECTORY] for a directory.
     * @param mode Unix-style file mode (as used by the chmod(2) syscall) to be set on
     * the output file or directory.  group/all rwx modes are stripped even if set
     * in this parameter.  If this parameter is negative then neither
     * the mode nor the mtime values will be applied to the restored file.
     * @param mtime A timestamp in the standard Unix epoch that will be imposed as the
     * last modification time of the output file.  if the `mode` parameter is
     * negative then this parameter will be ignored.
     * @param outFile Location within the filesystem to place the data.  This must point
     * to a location that is writeable by the caller, preferably using an absolute path.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun restoreFile(
        data: ParcelFileDescriptor,
        size: Long,
        type: Int,
        mode: Long,
        mtime: Long,
        outFile: File?
    ) {
        var size = size
        var mode = mode
        if (type == BackupAgent.TYPE_DIRECTORY) {
            // Canonically a directory has no associated content, so we don't need to read
            // anything from the pipe in this case.  Just create the directory here and
            // drop down to the final metadata adjustment.
            outFile?.mkdirs()
        } else {
            var out: FileOutputStream? = null

            // Pull the data from the pipe, copying it to the output file, until we're done
            try {
                if (outFile != null) {
                    val parent = outFile.getParentFile()
                    if (!parent!!.exists()) {
                        // in practice this will only be for the default semantic directories,
                        // and using the default mode for those is appropriate.
                        // This can also happen for the case where a parent directory has been
                        // excluded, but a file within that directory has been included.
                        parent.mkdirs()
                    }
                    out = FileOutputStream(outFile)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Unable to create/open file " + outFile!!.path, e)
            }

            val buffer = ByteArray(64 * 1024)
            val origSize = size
            val `in` = FileInputStream(data.fileDescriptor)
            while (size > 0) {
                val toRead = if (size > buffer.size) buffer.size else size.toInt()
                val got = `in`.read(buffer, 0, toRead)
                if (got <= 0) {
                    Log.w(
                        TAG, ("Incomplete read: expected " + size + " but got "
                                + (origSize - size))
                    )
                    break
                }
                if (out != null) {
                    try {
                        out.write(buffer, 0, got)
                    } catch (e: IOException) {
                        // Problem writing to the file.  Quit copying data and delete
                        // the file, but of course keep consuming the input stream.
                        Log.e(TAG, "Unable to write to file " + outFile!!.path, e)
                        out.close()
                        out = null
                        outFile.delete()
                    }
                }
                size -= got.toLong()
            }
            out?.close()
        }

        // Now twiddle the state to match the backup, assuming all went well
        if (mode >= 0 && outFile != null) {
            try {
                // explicitly prevent emplacement of files accessible by outside apps
                mode = mode and 448L // 0700 in octal notation.
                Os.chmod(outFile.path, mode.toInt())
            } catch (e: ErrnoException) {
                throw IOException(e.message).also { it.initCause(e) }
            }
            outFile.setLastModified(mtime)
        }
    }
}
