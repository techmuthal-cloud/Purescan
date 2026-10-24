package com.example.ui.components

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Dedicated production-grade CameraX CameraPreview component:
 * - Binds Preview, ImageCapture, and ImageAnalysis (frame luminosity readiness) to the lifecycle.
 * - Supports functional Torch / Flash toggle via CameraControl.enableTorch & ImageCapture.flashMode.
 * - Supports Tap-to-Focus (FocusMeteringAction) with animated visual focus ring at tap coordinates.
 * - Supports Pinch-to-Zoom and programmatic zoomRatio updates.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    isTorchEnabled: Boolean,
    zoomRatio: Float = 1.0f,
    onZoomChanged: (Float) -> Unit = {},
    onLuminosityEvaluated: (Float) -> Unit = {},
    onCameraBound: (Camera, ImageCapture, PreviewView) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var imageCaptureInstance by remember { mutableStateOf<ImageCapture?>(null) }
    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }

    var focusTapOffset by remember { mutableStateOf<Offset?>(null) }
    var currentPinchZoom by remember(zoomRatio) { mutableFloatStateOf(zoomRatio) }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    // Sync Torch & FlashMode state with CameraX hardware whenever isTorchEnabled changes
    LaunchedEffect(isTorchEnabled, cameraInstance, imageCaptureInstance) {
        try {
            imageCaptureInstance?.flashMode =
                if (isTorchEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
            val camera = cameraInstance
            if (camera != null && camera.cameraInfo.hasFlashUnit()) {
                camera.cameraControl.enableTorch(isTorchEnabled)
            }
        } catch (e: Exception) {
            Log.d("CameraPreview", "Torch toggle sync failed: ${e.message}")
        }
    }

    // Sync Zoom Ratio with CameraX
    LaunchedEffect(zoomRatio, cameraInstance) {
        try {
            val camera = cameraInstance ?: return@LaunchedEffect
            val zoomState = camera.cameraInfo.zoomState.value
            val minZ = zoomState?.minZoomRatio ?: 1.0f
            val maxZ = zoomState?.maxZoomRatio ?: 4.0f
            camera.cameraControl.setZoomRatio(zoomRatio.coerceIn(minZ, maxZ))
        } catch (e: Exception) {
            Log.d("CameraPreview", "Zoom ratio update failed: ${e.message}")
        }
    }

    // Auto-clear focus indicator after 1.1 seconds
    LaunchedEffect(focusTapOffset) {
        if (focusTapOffset != null) {
            delay(1100)
            focusTapOffset = null
        }
    }

    // Ensure torch is disabled and executor shut down on dispose
    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraInstance?.cameraControl?.enableTorch(false)
            } catch (_: Exception) {
            }
            try {
                analysisExecutor.shutdown()
            } catch (_: Exception) {
            }
        }
    }

    val focusRingScale by animateFloatAsState(
        targetValue = if (focusTapOffset != null) 1.0f else 1.35f,
        animationSpec = tween(220),
        label = "focus_ring_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("camera_preview_container")
            .pointerInput(cameraInstance) {
                detectTransformGestures { _, _, zoomChange, _ ->
                    val cam = cameraInstance ?: return@detectTransformGestures
                    val zoomState = cam.cameraInfo.zoomState.value
                    val minZ = zoomState?.minZoomRatio ?: 1.0f
                    val maxZ = (zoomState?.maxZoomRatio ?: 4.0f).coerceAtMost(4.0f)
                    val updated = (currentPinchZoom * zoomChange).coerceIn(minZ, maxZ)
                    currentPinchZoom = updated
                    onZoomChanged(updated)
                }
            }
            .pointerInput(cameraInstance, previewViewInstance) {
                detectTapGestures { tapOffset ->
                    focusTapOffset = tapOffset
                    try {
                        val cam = cameraInstance ?: return@detectTapGestures
                        val pv = previewViewInstance ?: return@detectTapGestures
                        val factory = pv.meteringPointFactory
                        val point = factory.createPoint(tapOffset.x, tapOffset.y)
                        val action = FocusMeteringAction.Builder(
                            point,
                            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                        )
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                        cam.cameraControl.startFocusAndMetering(action)
                    } catch (e: Exception) {
                        Log.d("CameraPreview", "Tap-to-focus failed: ${e.message}")
                    }
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                previewViewInstance = previewView

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setFlashMode(
                                if (isTorchEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                            )
                            .build()
                        imageCaptureInstance = imageCapture

                        // Real-time frame luminosity analyzer for low-light detection and Gemini Vision readiness
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                    try {
                                        val yPlane = imageProxy.planes.firstOrNull()
                                        if (yPlane != null) {
                                            val luma = computeAverageLuminosity(yPlane.buffer)
                                            onLuminosityEvaluated(luma)
                                        }
                                    } catch (_: Exception) {
                                    } finally {
                                        imageProxy.close()
                                    }
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture,
                            imageAnalysis
                        )
                        cameraInstance = camera
                        if (isTorchEnabled && camera.cameraInfo.hasFlashUnit()) {
                            camera.cameraControl.enableTorch(true)
                        }
                        onCameraBound(camera, imageCapture, previewView)
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "CameraX binding failed: ${e.message}", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Animated Tap-to-Focus Reticle
        focusTapOffset?.let { offset ->
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = 28.dp.toPx() * focusRingScale
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = radius,
                    center = offset,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 0.35f),
                    radius = 5.dp.toPx(),
                    center = offset
                )
            }
        }
    }
}

private fun computeAverageLuminosity(buffer: ByteBuffer): Float {
    buffer.rewind()
    val limit = buffer.remaining()
    if (limit <= 0) return 128f
    var sum = 0L
    var count = 0
    val step = (limit / 256).coerceAtLeast(1)
    var idx = 0
    while (idx < limit) {
        sum += buffer.get(idx).toInt() and 0xFF
        count++
        idx += step
    }
    return if (count > 0) sum.toFloat() / count else 128f
}
