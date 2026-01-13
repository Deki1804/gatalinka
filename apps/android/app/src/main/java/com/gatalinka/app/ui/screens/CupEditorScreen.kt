package com.gatalinka.app.ui.screens

import android.net.Uri
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.gatalinka.app.ui.design.BeanCTA
import com.gatalinka.app.ui.design.GataUI
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class ReadingMode(val displayName: String, val description: String) {
    INSTANT("Instant", "Brzo i direktno čitanje"),
    MYSTIC("Mistično", "Duboko i detaljno čitanje"),
    DEEP("Duboko", "Najdetaljnije čitanje")
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun CupEditorScreen(
    onBack: () -> Unit,
    onAnalyze: (String, String) -> Unit, // imageUri, readingMode
    initialReadingMode: String = "instant",
    openGallery: Boolean = false // Flag za automatsko otvaranje galerije
) {
    // Koristi rememberSaveable s custom saver za Uri da se osigura pravilno spremanje
    // VAŽNO: Uri se mora spremiti kao String jer rememberSaveable ne može direktno spremiti Uri
    // Koristi key da se osigura da se state ne resetira nakon recompozicije
    val uriSaver = remember {
        Saver<Uri?, String>(
            save = { uri ->
                val uriString = uri?.toString() ?: ""
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    android.util.Log.d("CupEditorScreen", "Saving URI to state: $uriString")
                }
                uriString
            },
            restore = { value ->
                if (value.isNotEmpty()) {
                    try {
                        android.net.Uri.parse(value)
                    } catch (e: Exception) {
                        if (com.gatalinka.app.BuildConfig.DEBUG) {
                            android.util.Log.e("CupEditorScreen", "Error parsing URI from saved state: ${e.message}", e)
                        }
                        null
                    }
                } else {
                    null
                }
            }
        )
    }
    
    // VAŽNO: Koristi rememberSaveable da se state ne resetira nakon recompozicije
    // ALI: Resetiraj state eksplicitno prije launchanja pickera da se ne koriste cached selectioni
    var imageUri by rememberSaveable(
        stateSaver = uriSaver
    ) { mutableStateOf<Uri?>(null) }
    var isCameraMode by remember { mutableStateOf(false) }
    var baseScale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var flipHorizontal by remember { mutableStateOf(false) }
    var flipVertical by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var selectedMode by remember(initialReadingMode) { 
        mutableStateOf(
            when (initialReadingMode.lowercase()) {
                "deep" -> ReadingMode.DEEP
                "mystic" -> ReadingMode.MYSTIC
                else -> ReadingMode.INSTANT
            }
        )
    }
    var showModeSelector by remember { mutableStateOf(false) }
    
    // Animatable za smooth zoom animacije
    val animatedScale = remember { Animatable(1f) }
    val animatedRotation = remember { Animatable(0f) }
    val animatedOffsetX = remember { Animatable(0f) }
    val animatedOffsetY = remember { Animatable(0f) }
    
    // Sync animatable s state
    LaunchedEffect(baseScale, offsetX, offsetY) {
        animatedScale.animateTo(baseScale, animationSpec = spring())
        animatedOffsetX.animateTo(offsetX, animationSpec = spring())
        animatedOffsetY.animateTo(offsetY, animationSpec = spring())
    }
    
    // Smooth rotation animation
    LaunchedEffect(rotation) {
        animatedRotation.animateTo(rotation, animationSpec = spring(dampingRatio = 0.8f))
    }

    val context = LocalContext.current
    
    // Odvojeni permissions za kameru i galeriju
    val cameraPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(android.Manifest.permission.CAMERA)
    )
    
    val galleryPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(android.Manifest.permission.READ_MEDIA_IMAGES)
    )

    // Koristi GetContent() za single selection - garantira single selection bez multi UI
    // VAŽNO: Launcher MORA biti na top-levelu composable-a, NE u if-u ili LaunchedEffect
    // VAŽNO: GetContent() nema "2 selected" badge i "Gotovo" gumb - to je single picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (com.gatalinka.app.BuildConfig.DEBUG) {
            android.util.Log.d("CupEditorScreen", "=== Image picker result ===")
            android.util.Log.d("CupEditorScreen", "uri: $uri")
            android.util.Log.d("CupEditorScreen", "uri != null: ${uri != null}")
            android.util.Log.d("CupEditorScreen", "uri.toString(): ${uri?.toString()}")
        }
        
        // VAŽNO: onResult mora REPLACE-ati state, ne appendati
        // Provjeri da URI nije null i da je valjan
        if (uri != null && uri.toString().isNotEmpty()) {
            try {
                // VAŽNO: Callback se već poziva na UI thread-u, tako da možemo direktno postaviti imageUri
                // VAŽNO: NE resetiraj imageUri na null prije postavljanja - to može uzrokovati da se UI resetira
                // Samo REPLACE state direktno
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    android.util.Log.d("CupEditorScreen", "=== Image picker result ===")
                    android.util.Log.d("CupEditorScreen", "uri: $uri")
                    android.util.Log.d("CupEditorScreen", "imageUri BEFORE set: $imageUri")
                }
                // REPLACE state direktno - ne resetiraj na null prije
                imageUri = uri
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    android.util.Log.d("CupEditorScreen", "✅ Image URI set successfully: $imageUri")
                    android.util.Log.d("CupEditorScreen", "✅ imageUri != null after set: ${imageUri != null}")
                    android.util.Log.d("CupEditorScreen", "✅ imageUri.toString(): ${imageUri?.toString()}")
                }
                // Resetiraj transformacije kada se odabere nova slika
                baseScale = 1f
                rotation = 0f
                flipHorizontal = false
                flipVertical = false
                offsetX = 0f
                offsetY = 0f
            } catch (e: Exception) {
                if (com.gatalinka.app.BuildConfig.DEBUG) {
                    android.util.Log.e("CupEditorScreen", "❌ Error setting image URI: ${e.message}", e)
                    e.printStackTrace()
                }
            }
        } else {
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                android.util.Log.w("CupEditorScreen", "⚠️ Image selection cancelled or failed - uri is null or empty")
                android.util.Log.w("CupEditorScreen", "uri: $uri")
                android.util.Log.w("CupEditorScreen", "uri?.toString(): ${uri?.toString()}")
            }
            // VAŽNO: Ako je odabir otkazan, NE resetiraj imageUri - zadrži postojeću sliku ako postoji
            // Resetiranje na null može uzrokovati da se UI resetira na početni screen
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top bar
        TopAppBar(
            title = { Text("Uredi šalicu") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Nazad")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.7f),
                titleContentColor = Color.White
            )
        )

        // Automatski otvori galeriju ako je flag postavljen
        LaunchedEffect(openGallery, imageUri, galleryPermissionState.allPermissionsGranted) {
            if (openGallery && imageUri == null) {
                // Provjeri permissions i otvori galeriju
                if (galleryPermissionState.allPermissionsGranted) {
                    if (com.gatalinka.app.BuildConfig.DEBUG) {
                        android.util.Log.d("CupEditorScreen", "Auto-opening gallery from retry")
                    }
                    imagePickerLauncher.launch("image/*")
                } else {
                    galleryPermissionState.launchMultiplePermissionRequest()
                }
            }
        }
        
        // Loguj trenutno stanje imageUri za debug
        LaunchedEffect(imageUri) {
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                android.util.Log.d("CupEditorScreen", "=== LaunchedEffect: imageUri changed ===")
                android.util.Log.d("CupEditorScreen", "imageUri: $imageUri")
                android.util.Log.d("CupEditorScreen", "imageUri != null: ${imageUri != null}")
                android.util.Log.d("CupEditorScreen", "imageUri?.toString(): ${imageUri?.toString()}")
            }
        }
        
        if (imageUri == null) {
            if (com.gatalinka.app.BuildConfig.DEBUG) {
                android.util.Log.d("CupEditorScreen", "=== Rendering picker options (imageUri is null) ===")
            }
            if (!isCameraMode) {
                // No image - show picker options
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = GataUI.ScreenPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Odaberi sliku šalice",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                // VAŽNO: Log da vidimo koji launcher se stvarno pali
                                if (com.gatalinka.app.BuildConfig.DEBUG) {
                                    android.util.Log.d("CupEditorScreen", "=== CLICK gallery -> launching GET_CONTENT ===")
                                    android.util.Log.d("CupEditorScreen", "Current imageUri: $imageUri")
                                }
                                
                                // GetContent() ne treba permission na Android 13+, ali provjerimo za starije verzije
                                if (galleryPermissionState.allPermissionsGranted) {
                                    // GetContent() garantira single selection bez multi UI (nema "2 selected" badge)
                                    if (com.gatalinka.app.BuildConfig.DEBUG) {
                                        android.util.Log.d("CupEditorScreen", "Launching GetContent() with single selection")
                                    }
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    galleryPermissionState.launchMultiplePermissionRequest()
                                }
                            }
                        ) {
                            Text("Iz galerije")
                        }

                        Button(
                            onClick = {
                                if (cameraPermissionState.allPermissionsGranted) {
                                    isCameraMode = true
                                } else {
                                    cameraPermissionState.launchMultiplePermissionRequest()
                                }
                            }
                        ) {
                            Text("Uslikaj")
                        }
                    }
                }
            } else {
                // Camera mode with overlay
                CameraPreviewWithOverlay(
                    onCapture = { uri ->
                        imageUri = uri
                        isCameraMode = false
                    },
                    onCancel = {
                        isCameraMode = false
                    },
                    permissionsState = cameraPermissionState
                )
            }
        } else {
            // Image editor
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Hint text
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D1B4E).copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "💡 Poravnaj šalicu unutar kruga za najbolji rezultat",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEFE3D1).copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Visual circle mask overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                ),
                                radius = 800f
                            )
                        )
                )
                
                // Circular mask indicator
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val radius = size.minDimension * 0.4f
                    
                    // Draw circle border
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.5f),
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                    )
                }
                
                // Image with gestures
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(imageUri)
                            .memoryCacheKey(imageUri.toString())
                            .diskCacheKey(imageUri.toString())
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Šalica",
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            // Double-tap zoom
                            detectTapGestures(
                                onDoubleTap = { tapOffset ->
                                    val targetScale = if (baseScale > 1.5f) {
                                        1f
                                    } else {
                                        2.5f
                                    }
                                    
                                    if (targetScale > 1f) {
                                        val centerX = size.width / 2f
                                        val centerY = size.height / 2f
                                        val newOffsetX = (centerX - tapOffset.x) * (targetScale - 1f) / targetScale
                                        val newOffsetY = (centerY - tapOffset.y) * (targetScale - 1f) / targetScale
                                        baseScale = targetScale
                                        offsetX = newOffsetX
                                        offsetY = newOffsetY
                                    } else {
                                        baseScale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            // Pinch-to-zoom
                            detectTransformGestures { _, pan, zoom, _ ->
                                baseScale = (baseScale * zoom).coerceIn(0.5f, 5f)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                            
                            // Auto-center on release
                            detectDragGestures(
                                onDragEnd = {
                                    // Snap to center if close enough
                                    val threshold = 50f
                                    if (kotlin.math.abs(offsetX) < threshold && kotlin.math.abs(offsetY) < threshold) {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            ) { _, _ -> }
                        }
                        .graphicsLayer(
                            scaleX = animatedScale.value * if (flipHorizontal) -1f else 1f,
                            scaleY = animatedScale.value * if (flipVertical) -1f else 1f,
                            rotationZ = animatedRotation.value,
                            translationX = animatedOffsetX.value,
                            translationY = animatedOffsetY.value
                        ),
                    contentScale = ContentScale.Fit
                )

                // Validation feedback
                var validationWarning by remember { mutableStateOf<String?>(null) }
                
                // Check for validation issues
                LaunchedEffect(baseScale, offsetX, offsetY) {
                    validationWarning = when {
                        baseScale > 3f -> "⚠️ Slika je previše zumirana"
                        baseScale < 0.7f -> "⚠️ Slika je premala"
                        kotlin.math.abs(offsetX) > 200f || kotlin.math.abs(offsetY) > 200f -> "⚠️ Šalica nije u centru"
                        else -> null
                    }
                }
                
                // Show validation warning
                validationWarning?.let { warning ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF6B6B).copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            warning,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rotate button
                        IconButton(
                            onClick = { rotation = (rotation + 90f) % 360f },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.RotateRight, 
                                "Rotiraj", 
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Flip horizontal button
                        IconButton(
                            onClick = { flipHorizontal = !flipHorizontal },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Flip, 
                                "Okreni horizontalno", 
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Flip vertical button
                        IconButton(
                            onClick = { flipVertical = !flipVertical },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.FlipCameraAndroid, 
                                "Okreni vertikalno", 
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Reset button
                        IconButton(
                            onClick = {
                                baseScale = 1f
                                offsetX = 0f
                                offsetY = 0f
                                rotation = 0f
                                flipHorizontal = false
                                flipVertical = false
                            },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh, 
                                "Resetiraj", 
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    BeanCTA(
                        label = "Analiziraj šalicu",
                        onClick = {
                            if (com.gatalinka.app.BuildConfig.DEBUG) {
                                android.util.Log.d("CupEditorScreen", "=== Analiziraj button clicked ===")
                                android.util.Log.d("CupEditorScreen", "imageUri: $imageUri")
                                android.util.Log.d("CupEditorScreen", "imageUri != null: ${imageUri != null}")
                            }
                            if (imageUri != null) {
                                val uriString = imageUri.toString()
                                if (com.gatalinka.app.BuildConfig.DEBUG) {
                                    android.util.Log.d("CupEditorScreen", "Calling onAnalyze with: $uriString")
                                }
                                onAnalyze(uriString, selectedMode.name.lowercase())
                            } else {
                                if (com.gatalinka.app.BuildConfig.DEBUG) {
                                    android.util.Log.e("CupEditorScreen", "ERROR: imageUri is null, cannot analyze!")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun CameraPreviewWithOverlay(
    onCapture: (Uri) -> Unit,
    onCancel: () -> Unit,
    permissionsState: com.google.accompanist.permissions.MultiplePermissionsState
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    // CameraX setup
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    
    // Temp file za capture
    val captureFile = remember {
        File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    }
    
    // Helper funkcija za dobivanje ProcessCameraProvider s coroutines
    suspend fun getCameraProvider(context: android.content.Context): ProcessCameraProvider {
        return suspendCancellableCoroutine { continuation ->
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                try {
                    continuation.resume(future.get()) {}
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    
                    // Setup camera kada je PreviewView kreiran
                    if (permissionsState.allPermissionsGranted) {
                        coroutineScope.launch(Dispatchers.Main) {
                            try {
                                val cameraProvider = getCameraProvider(ctx)
                                
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(surfaceProvider)
                                }
                                
                                val activity = ctx as? android.app.Activity
                                val rotation = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                    activity?.display?.rotation ?: android.view.Surface.ROTATION_0
                                } else {
                                    @Suppress("DEPRECATION")
                                    activity?.windowManager?.defaultDisplay?.rotation ?: android.view.Surface.ROTATION_0
                                }
                                
                                imageCapture = ImageCapture.Builder()
                                    .setTargetRotation(rotation)
                                    .build()
                                
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                // Update logic if needed
            }
        )
        
        // Scrim overlay (tamni layer oko okvira)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
        )
        
        // Cup overlay (bijeli okvir) – koristimo Compose border umjesto drawable-a
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
                .border(
                    width = 2.dp,
                    color = Color.White,
                    shape = CircleShape
                )
        )
        
        // Top hint text
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2D1B4E).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Postavi šalicu unutar okvira\nDrži mobitel iznad šalice, što ravnije.",
                color = Color(0xFFEFE3D1),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Bottom controls - professional design
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Capture button - professional style
            Card(
                onClick = {
                    val imageCaptureInstance = imageCapture
                    if (imageCaptureInstance != null) {
                        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(captureFile).build()
                        imageCaptureInstance.takePicture(
                            outputFileOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        captureFile
                                    )
                                    onCapture(uri)
                                }
                                
                                override fun onError(exception: ImageCaptureException) {
                                    exception.printStackTrace()
                                }
                            }
                        )
                    }
                },
                shape = CircleShape,
                modifier = Modifier
                    .size(80.dp)
                    .shadow(12.dp, CircleShape),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer ring
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .border(
                                width = 4.dp,
                                color = Color(0xFF1A0B2E),
                                shape = CircleShape
                            )
                    )
                    // Inner circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A0B2E))
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Cancel button - subtle style
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    "Odustani",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

