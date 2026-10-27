package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.components.AllergenBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.catalog.PresetSamples
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.components.CameraPreview
import com.example.ui.theme.Bone
import com.example.ui.theme.BrandScanOrange
import com.example.ui.theme.BrandSmokyBlack
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OnScanOrange
import com.example.ui.theme.ScanOrange
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticSuccessBg
import com.example.ui.theme.SmokyBlack

/**
 * Screens 6 & 7 of the 15-screen ScanSafe UI/UX Reference:
 * - Screen 6 (Scanner): Full-bleed CameraX preview, high-contrast orange corner brackets (`#F5A623`),
 *   top-left solid dark Close button, top-right Flash toggle, dark instruction pill
 *   "Align the ingredient list within the frame", bottom Gallery, White Shutter Ring, and Manual/Sample options.
 * - Screen 7 (Crop & Align Review): Post-capture review with orange crop handles and "Confirm Image ->" CTA.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(viewModel: ScanViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var imageCaptureInstance by remember { mutableStateOf<ImageCapture?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }
    var zoomRatio by remember { mutableFloatStateOf(1.0f) }
    var frameLuminosity by remember { mutableFloatStateOf(135f) }
    var isCapturing by remember { mutableStateOf(false) }
    var showTestSamplesTray by remember { mutableStateOf(false) }
    var reviewBeforeAnalyze by remember { mutableStateOf(false) }
    var capturedReviewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showAllergenSheet by remember { mutableStateOf(false) }

    val preferences by viewModel.preferences.collectAsState()
    val customAllergens by viewModel.customAllergens.collectAsState()
    val activeProfile by viewModel.activeAllergenProfile.collectAsState()
    val activeAllergenCount = preferences.count { it.isEnabled } + customAllergens.count { it.isEnabled }
    var isCameraXReady by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val decoded = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (decoded != null) {
                    if (uiState.isBatchScanMode && !reviewBeforeAnalyze) {
                        viewModel.enqueueBatchBitmap(decoded)
                    } else if (reviewBeforeAnalyze) {
                        capturedReviewBitmap = decoded
                    } else {
                        viewModel.onRealPhotoCapturedOrPicked(decoded)
                    }
                } else {
                    viewModel.showToast("Could not decode selected image")
                }
            } catch (e: Exception) {
                Log.e("ScannerScreen", "Failed to read gallery photo: ${e.message}", e)
                viewModel.showToast("Error reading image: ${e.message}")
            }
        }
    }

    val batchPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            var addedCount = 0
            uris.forEachIndexed { idx, uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val decoded = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (decoded != null) {
                        viewModel.enqueueBatchBitmap(
                            decoded,
                            "Gallery Label #${uiState.batchQueue.size + idx + 1}"
                        )
                        addedCount++
                    }
                } catch (e: Exception) {
                    Log.e("ScannerScreen", "Failed to read batch gallery photo: ${e.message}", e)
                }
            }
            if (addedCount == 0) {
                viewModel.showToast("Could not decode selected gallery images")
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_anim"
    )

    val isLowLight = frameLuminosity < 65f && !isFlashOn

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandSmokyBlack)
            .testTag("scanner_screen")
    ) {
        SimulatedPackagingBackdrop()

        if (hasCameraPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                isTorchEnabled = isFlashOn,
                zoomRatio = zoomRatio,
                onZoomChanged = { zoomRatio = it },
                onLuminosityEvaluated = { frameLuminosity = it },
                onCameraReadyChanged = { isCameraXReady = it },
                onCameraBound = { cam, imgCap, pv ->
                    cameraInstance = cam
                    imageCaptureInstance = imgCap
                    previewViewRef = pv
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF20211B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandScanOrange.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(BrandScanOrange.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = BrandScanOrange,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Camera Access for Live Scanning",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Grant camera permission to capture real ingredient labels, or use the gallery button below to analyze a product photo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD8CFBC),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandScanOrange,
                                contentColor = BrandSmokyBlack
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("grant_camera_permission_button")
                        ) {
                            Text(
                                text = "Enable Camera",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Top Bar Controls (Top-Left Close X, Center Single/Batch Mode Toggle, Top-Right Flash Toggle)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrandSmokyBlack.copy(alpha = 0.78f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .testTag("scanner_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Scanner",
                        tint = Color.White
                    )
                }

                // Single Scan vs Batch Scan Mode Pill Selector
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = BrandSmokyBlack.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                    modifier = Modifier.testTag("scanner_mode_toggle_bar")
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (!uiState.isBatchScanMode) BrandScanOrange else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.setBatchScanMode(false) }
                                .testTag("scanner_mode_single")
                        ) {
                            Text(
                                text = "Single",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (!uiState.isBatchScanMode) BrandSmokyBlack else Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (uiState.isBatchScanMode) BrandScanOrange else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.setBatchScanMode(true) }
                                .testTag("scanner_mode_batch")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Batch Scan",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isBatchScanMode) BrandSmokyBlack else Color.White
                                )
                                if (uiState.batchQueue.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = if (uiState.isBatchScanMode) BrandSmokyBlack else BrandScanOrange
                                    ) {
                                        Text(
                                            text = "${uiState.batchQueue.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (uiState.isBatchScanMode) Color.White else BrandSmokyBlack,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = {
                        val newFlashState = !isFlashOn
                        isFlashOn = newFlashState
                        val hasHardwareFlash = cameraInstance?.cameraInfo?.hasFlashUnit() ?: false
                        if (newFlashState && !hasHardwareFlash && hasCameraPermission) {
                            viewModel.showToast("Flash mode enabled for capture")
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFlashOn) BrandScanOrange else BrandSmokyBlack.copy(alpha = 0.78f)
                        )
                        .border(
                            1.dp,
                            if (isFlashOn) BrandScanOrange else Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .testTag("flash_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = if (isFlashOn) "Disable Flash" else "Enable Flash",
                        tint = if (isFlashOn) BrandSmokyBlack else Color.White
                    )
                }
            }

            // Quick Allergy Filter Pill in Scanner
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BrandSmokyBlack.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (activeAllergenCount > 0) BrandScanOrange else Color.White.copy(alpha = 0.22f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showAllergenSheet = true }
                    .testTag("scanner_allergy_filter_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (activeAllergenCount > 0) BrandScanOrange else Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buildString {
                            append("Allergy Filter: $activeAllergenCount active")
                            activeProfile?.let { append(" • ${it.profileName}") }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (isFlashOn) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrandScanOrange,
                    modifier = Modifier.testTag("flash_active_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = BrandSmokyBlack,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Flash On",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrandSmokyBlack
                        )
                    }
                }
            } else if (isLowLight) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrandSmokyBlack.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandScanOrange),
                    modifier = Modifier
                        .clickable { isFlashOn = true }
                        .testTag("low_light_prompt_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = BrandScanOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Low Light • Tap for Flash",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Center Viewfinder with ScanOrange Corner Brackets (Screen 6)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .testTag("viewfinder_frame")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cornerLen = 44.dp.toPx()
                    val strokeW = 4.dp.toPx()
                    val radius = 16.dp.toPx()

                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.22f),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Top-Left orange corner bracket
                    drawLine(BrandScanOrange, Offset(0f, cornerLen), Offset(0f, radius / 2), strokeWidth = strokeW, cap = StrokeCap.Round)
                    drawLine(BrandScanOrange, Offset(radius / 2, 0f), Offset(cornerLen, 0f), strokeWidth = strokeW, cap = StrokeCap.Round)

                    // Top-Right orange corner bracket
                    drawLine(BrandScanOrange, Offset(w - cornerLen, 0f), Offset(w - radius / 2, 0f), strokeWidth = strokeW, cap = StrokeCap.Round)
                    drawLine(BrandScanOrange, Offset(w, radius / 2), Offset(w, cornerLen), strokeWidth = strokeW, cap = StrokeCap.Round)

                    // Bottom-Left orange corner bracket
                    drawLine(BrandScanOrange, Offset(0f, h - cornerLen), Offset(0f, h - radius / 2), strokeWidth = strokeW, cap = StrokeCap.Round)
                    drawLine(BrandScanOrange, Offset(radius / 2, h), Offset(cornerLen, h), strokeWidth = strokeW, cap = StrokeCap.Round)

                    // Bottom-Right orange corner bracket
                    drawLine(BrandScanOrange, Offset(w - cornerLen, h), Offset(w - radius / 2, h), strokeWidth = strokeW, cap = StrokeCap.Round)
                    drawLine(BrandScanOrange, Offset(w, h - cornerLen), Offset(w, h - radius / 2), strokeWidth = strokeW, cap = StrokeCap.Round)

                    // Animated orange scan line inside viewfinder
                    val scanY = h * laserProgress
                    drawLine(
                        color = BrandScanOrange.copy(alpha = 0.85f),
                        start = Offset(16.dp.toPx(), scanY),
                        end = Offset(w - 16.dp.toPx(), scanY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Zoom Ratio Selector (1x / 1.5x / 2x)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(1.0f to "1x", 1.5f to "1.5x", 2.0f to "2x").forEach { (ratio, label) ->
                    val isSelected = kotlin.math.abs(zoomRatio - ratio) < 0.2f
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) BrandScanOrange else BrandSmokyBlack.copy(alpha = 0.78f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) BrandScanOrange else Color.White.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .clickable { zoomRatio = ratio }
                            .testTag("zoom_button_$label")
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) BrandSmokyBlack else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // High-contrast solid dark instruction pill below frame (Design.md Outdoor Readability)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BrandSmokyBlack.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
            ) {
                Text(
                    text = if (uiState.isBatchScanMode) {
                        "Batch Mode: Capture multiple labels in sequence,\nthen tap Analyze Batch below"
                    } else {
                        "Align the ingredient list\nwithin the frame"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp)
                )
            }
        }

        // Bottom Controls Area (Screen 6: Batch Queue Tray, Left Gallery, Center Shutter Ring, Right Crop/Sample Options)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Batch Scan Thumbnail Queue Tray
            AnimatedVisibility(visible = uiState.isBatchScanMode) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BrandSmokyBlack.copy(alpha = 0.94f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandScanOrange.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .testTag("batch_queue_tray")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(BrandScanOrange)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Batch Queue (${uiState.batchQueue.size} captured)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            if (uiState.batchQueue.isNotEmpty()) {
                                Text(
                                    text = "Clear All",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD8CFBC),
                                    modifier = Modifier
                                        .clickable { viewModel.clearBatchQueue() }
                                        .testTag("batch_clear_button")
                                )
                            } else {
                                Text(
                                    text = "+ Add 3 Sample Labels",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandScanOrange,
                                    modifier = Modifier
                                        .clickable {
                                            PresetSamples.samples.take(3).forEach { sample ->
                                                viewModel.enqueueBatchSample(sample)
                                            }
                                        }
                                        .testTag("batch_add_demo_set_button")
                                )
                            }
                        }

                        if (uiState.batchQueue.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.batchQueue, key = { it.id }) { item ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF2B2C25),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            Color.White.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier.testTag("batch_item_${item.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (item.bitmap != null) {
                                                Image(
                                                    bitmap = item.bitmap.asImageBitmap(),
                                                    contentDescription = item.labelHint,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Column {
                                                Text(
                                                    text = item.labelHint,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = item.presetSample?.category ?: uiState.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFD8CFBC),
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.14f))
                                                    .clickable { viewModel.removeBatchQueueItem(item.id) }
                                                    .testTag("batch_remove_${item.id}"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove ${item.labelHint}",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.analyzeBatchQueue() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("analyze_batch_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandScanOrange,
                                    contentColor = BrandSmokyBlack
                                )
                            ) {
                                Text(
                                    text = "Analyze Batch (${uiState.batchQueue.size} Product${if (uiState.batchQueue.size == 1) "" else "s"})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = showTestSamplesTray) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrandSmokyBlack.copy(alpha = 0.92f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.isBatchScanMode) "Tap to Queue Demo Labels:" else "Quick Demo Labels & Options:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD8CFBC)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (reviewBeforeAnalyze) "Crop & Review: ON" else "Crop & Review: OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (reviewBeforeAnalyze) BrandScanOrange else Color(0xFFD8CFBC),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { reviewBeforeAnalyze = !reviewBeforeAnalyze }
                                        .testTag("toggle_photo_review_mode")
                                )
                                Text(
                                    text = "Type Manually",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandScanOrange,
                                    modifier = Modifier
                                        .clickable { viewModel.startManualEntry() }
                                        .testTag("manual_text_entry_button")
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(PresetSamples.samples) { sample ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            com.example.ui.util.HapticsManager.triggerCameraCapture(context)
                                            if (uiState.isBatchScanMode) {
                                                viewModel.enqueueBatchSample(sample)
                                            } else {
                                                viewModel.loadSample(sample)
                                            }
                                        }
                                        .testTag("scanner_preset_${sample.id}"),
                                    color = Color(0xFF2B2C25),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
                                ) {
                                    Text(
                                        text = sample.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dual Capture Workflow Pill: Instant Gemini Analysis vs Crop & OCR Review (Options 1 & 2)
            if (!uiState.isBatchScanMode) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BrandSmokyBlack.copy(alpha = 0.88f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .testTag("capture_flow_mode_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (!reviewBeforeAnalyze) BrandScanOrange else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { reviewBeforeAnalyze = false }
                                .testTag("capture_flow_instant_gemini")
                        ) {
                            Text(
                                text = "Instant Gemini AI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (!reviewBeforeAnalyze) BrandSmokyBlack else Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (reviewBeforeAnalyze) BrandScanOrange else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { reviewBeforeAnalyze = true }
                                .testTag("capture_flow_crop_review")
                        ) {
                            Text(
                                text = "Crop & OCR Review",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (reviewBeforeAnalyze) BrandSmokyBlack else Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (uiState.captureErrorMessage != null) {
                com.example.ui.components.CameraCaptureErrorBanner(
                    errorMessage = uiState.captureErrorMessage!!,
                    onRetryCapture = {
                        viewModel.clearCaptureError()
                        com.example.ui.util.HapticsManager.triggerCameraCapture(context, previewViewRef)
                        viewModel.onCaptureStarted()
                        captureRealFrameOrFallback(
                            context = context,
                            imageCapture = imageCaptureInstance,
                            previewView = previewViewRef,
                            seedIndex = uiState.batchQueue.size,
                            onCapturingState = { isCapturing = it },
                            onBitmapReady = { bitmap ->
                                if (reviewBeforeAnalyze) {
                                    capturedReviewBitmap = bitmap
                                } else if (uiState.isBatchScanMode) {
                                    viewModel.enqueueBatchBitmap(bitmap)
                                } else {
                                    viewModel.onRealPhotoCapturedOrPicked(bitmap)
                                }
                            }
                        )
                    },
                    onPickFromGallery = {
                        viewModel.clearCaptureError()
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onDismiss = { viewModel.clearCaptureError() }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bottom-Left: Gallery Photo Picker Button (supports multi-select in Batch Mode)
                IconButton(
                    onClick = {
                        if (uiState.isBatchScanMode) {
                            batchPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(BrandSmokyBlack.copy(alpha = 0.82f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        .testTag("gallery_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Choose Photo from Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Bottom-Center: Large White Shutter Button with ScanOrange / White Ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(3.5.dp, BrandScanOrange, CircleShape)
                        .clickable(enabled = !isCapturing) {
                            com.example.ui.util.HapticsManager.triggerCameraCapture(context, previewViewRef)
                            viewModel.onCaptureStarted()
                            captureRealFrameOrFallback(
                                context = context,
                                imageCapture = imageCaptureInstance,
                                previewView = previewViewRef,
                                seedIndex = uiState.batchQueue.size,
                                onCapturingState = { isCapturing = it },
                                onBitmapReady = { bitmap ->
                                    if (reviewBeforeAnalyze) {
                                        capturedReviewBitmap = bitmap
                                    } else if (uiState.isBatchScanMode) {
                                        viewModel.enqueueBatchBitmap(bitmap)
                                    } else {
                                        viewModel.onRealPhotoCapturedOrPicked(bitmap)
                                    }
                                }
                            )
                        }
                        .padding(6.dp)
                        .testTag("capture_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.5.dp,
                                color = BrandSmokyBlack
                            )
                        }
                    }
                }

                // Bottom-Right: Manual / Sample Options Button
                IconButton(
                    onClick = { showTestSamplesTray = !showTestSamplesTray },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(BrandSmokyBlack.copy(alpha = 0.82f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        .testTag("scanner_options_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Manual or Sample Input",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Screen 7: Crop & Align Post-Capture Photo Review Overlay
        capturedReviewBitmap?.let { bmp ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrandSmokyBlack.copy(alpha = 0.94f))
                    .padding(24.dp)
                    .testTag("photo_review_overlay"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = FloralWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Crop,
                                    contentDescription = null,
                                    tint = ScanOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Crop & Align",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = SmokyBlack
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SemanticSuccessBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SemanticSuccess,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Label Detected",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SemanticSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Captured Label with Orange Crop Handles (Screen 7)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, BrandScanOrange, RoundedCornerShape(16.dp))
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Captured Label Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                val w = size.width
                                val h = size.height
                                val c = 24.dp.toPx()
                                val sw = 3.5.dp.toPx()
                                drawLine(BrandScanOrange, Offset(0f, 0f), Offset(c, 0f), strokeWidth = sw, cap = StrokeCap.Round)
                                drawLine(BrandScanOrange, Offset(0f, 0f), Offset(0f, c), strokeWidth = sw, cap = StrokeCap.Round)
                                drawLine(BrandScanOrange, Offset(w, 0f), Offset(w - c, 0f), strokeWidth = sw, cap = StrokeCap.Round)
                                drawLine(BrandScanOrange, Offset(w, 0f), Offset(w, c), strokeWidth = sw, cap = StrokeCap.Round)
                                drawLine(BrandScanOrange, Offset(0f, h), Offset(c, h), strokeWidth = sw, cap = StrokeCap.Round)
                                drawLine(BrandScanOrange, Offset(0f, h), Offset(0f, h - c), strokeWidth = sw, cap = StrokeCap.Round)
                                drawLine(BrandScanOrange, Offset(w, h), Offset(w - c, h), strokeWidth = sw, cap = StrokeCap.Round)
                                drawLine(BrandScanOrange, Offset(w, h), Offset(w, h - c), strokeWidth = sw, cap = StrokeCap.Round)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val targetBmp = bmp
                                capturedReviewBitmap = null
                                if (uiState.isBatchScanMode) {
                                    viewModel.enqueueBatchBitmap(targetBmp)
                                } else {
                                    viewModel.onRealPhotoCapturedOrPicked(targetBmp)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("confirm_photo_analyze_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ScanOrange,
                                contentColor = OnScanOrange
                            )
                        ) {
                            Text(
                                text = if (uiState.isBatchScanMode) "Add to Batch Queue" else "Analyze with Gemini AI",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { capturedReviewBitmap = null },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("retake_photo_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = SmokyBlack
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retake", color = SmokyBlack, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val targetBmp = bmp
                                    capturedReviewBitmap = null
                                    viewModel.extractAndReviewFromBitmap(targetBmp)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("review_manual_edit_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = SmokyBlack
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Review OCR Text", color = SmokyBlack, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        if (showAllergenSheet) {
            AllergenBottomSheet(
                sheetState = rememberModalBottomSheetState(),
                viewModel = viewModel,
                onOpenFullSettings = { viewModel.navigateTo(Screen.AllergenSettings) },
                onDismiss = { showAllergenSheet = false }
            )
        }
    }
}

private fun captureRealFrameOrFallback(
    context: android.content.Context,
    imageCapture: ImageCapture?,
    previewView: PreviewView?,
    seedIndex: Int = 0,
    onCapturingState: (Boolean) -> Unit,
    onBitmapReady: (Bitmap) -> Unit
) {
    val livePreviewBitmap = try {
        previewView?.bitmap
    } catch (_: Exception) {
        null
    }

    if (imageCapture == null) {
        val fallbackBmp = livePreviewBitmap ?: createSyntheticLabelBitmap(seedIndex)
        onBitmapReady(fallbackBmp)
        return
    }

    onCapturingState(true)
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                onCapturingState(false)
                val capturedBitmap = try {
                    imageProxyToBitmap(image)
                } catch (e: Exception) {
                    Log.e("ScannerScreen", "Failed converting ImageProxy: ${e.message}")
                    null
                } finally {
                    image.close()
                }
                onBitmapReady(capturedBitmap ?: livePreviewBitmap ?: createSyntheticLabelBitmap(seedIndex))
            }

            override fun onError(exception: ImageCaptureException) {
                Log.w("ScannerScreen", "ImageCapture failed (${exception.message}), using preview bitmap.")
                com.example.ui.util.HapticsManager.triggerErrorState(context)
                onCapturingState(false)
                onBitmapReady(livePreviewBitmap ?: createSyntheticLabelBitmap(seedIndex))
            }
        }
    )
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    val buffer = image.planes.firstOrNull()?.buffer ?: return null
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    val rotationDegrees = image.imageInfo.rotationDegrees
    return if (rotationDegrees != 0) {
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
    } else {
        rawBitmap
    }
}

private fun createSyntheticLabelBitmap(seedIndex: Int = 0): Bitmap {
    val bmp = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    val shift = (seedIndex * 37) % 120
    canvas.drawColor(android.graphics.Color.rgb(245 - shift, (239 - shift / 2).coerceAtLeast(80), (228 - shift).coerceAtLeast(60)))
    return bmp
}

@Composable
private fun SimulatedPackagingBackdrop() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF26231F)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.84f)
                .height(250.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFEAE2D6).copy(alpha = 0.90f)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "INGREDIENTS: AQUA / WATER, GLYCERIN,",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = Color(0xFF2B2925)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CETEARYL ALCOHOL, CAPRYLIC/CAPRIC TRIGLYCERIDE, CETYL ALCOHOL, CETEARETH-20, CERAMIDE NP, CERAMIDE AP, CERAMIDE EOP, CARBOMER, NIACINAMIDE, SODIUM HYALURONATE, TOCOPHEROL, PHENOXYETHANOL.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.5.sp,
                        lineHeight = 16.sp
                    ),
                    color = Color(0xFF3E3B35)
                )
            }
        }
    }
}
