package com.example.ui.scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * CameraX ImageAnalysis.Analyzer that detects QR codes and Barcodes using Google ML Kit.
 */
class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Configure ML Kit scanner specifically for QR codes and standard barcodes
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC,
            Barcode.FORMAT_DATA_MATRIX,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    private var isScanning = true
    private var lastScannedTimestamp = 0L
    private val scanThrottleMs = 1200L

    fun pauseScanning() {
        isScanning = false
    }

    fun resumeScanning() {
        isScanning = true
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        val currentTime = System.currentTimeMillis()

        if (mediaImage != null && isScanning && (currentTime - lastScannedTimestamp > scanThrottleMs)) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            lastScannedTimestamp = currentTime
                            onQrCodeScanned(rawValue)
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    // Ignore transient analysis errors
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
