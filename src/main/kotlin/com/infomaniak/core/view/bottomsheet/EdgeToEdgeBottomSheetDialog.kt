package com.infomaniak.core.view.bottomsheet

import android.app.Dialog
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class EdgeToEdgeBottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        object : BottomSheetDialog(requireContext(), theme) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()

                window?.let {
                    WindowCompat.setDecorFitsSystemWindows(it, false)
                    if (SDK_INT >= 29) it.isNavigationBarContrastEnforced = false
                }

                findViewById<View>(R.id.container)?.apply {
                    fitsSystemWindows = false
                }

                findViewById<View>(R.id.coordinator)?.fitsSystemWindows = false
            }
        }
}
