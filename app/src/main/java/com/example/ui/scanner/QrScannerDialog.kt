package com.example.ui.scanner

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.QrAccountPayload

/**
 * Fullscreen Dialog wrapping the CameraX & ML Kit QrScannerView.
 */
@Composable
fun QrScannerDialog(
    onDismiss: () -> Unit,
    onAccountScanned: (QrAccountPayload) -> Unit,
    initialTargetRole: String = "ALL"
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        QrScannerView(
            onAccountScanned = { payload ->
                onAccountScanned(payload)
                onDismiss()
            },
            onDismiss = onDismiss,
            initialTargetRole = initialTargetRole,
            modifier = Modifier.fillMaxSize()
        )
    }
}
