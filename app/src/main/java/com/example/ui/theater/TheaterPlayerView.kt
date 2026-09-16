package com.example.ui.theater

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.MovieSite
import com.example.engine.AdBlockEngine
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TheaterPlayerView(
    site: MovieSite,
    onClose: () -> Unit,
    onToggleFavorite: (MovieSite) -> Unit,
    onAdBlocked: () -> Unit,
    isAmbientMode: Boolean,
    onToggleAmbientMode: () -> Unit,
    isDesktopMode: Boolean,
    onToggleDesktopMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context as? android.app.Activity }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var pageTitle by remember { mutableStateOf(site.title) }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var blockedAdsCount by remember { mutableIntStateOf(site.adBlockCount) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // Fullscreen video custom view state (for HTML5 video tag fullscreen)
    var customVideoView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    // Intercept hardware/gesture back press
    BackHandler {
        if (customVideoView != null) {
            // Exit fullscreen video first
            customViewCallback?.onCustomViewHidden()
            customVideoView = null
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onClose()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            customViewCallback?.onCustomViewHidden()
            customVideoView = null
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            webViewInstance?.destroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("theater_player_view")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sleek Cinema Top Bar (Movie App Header, never a browser address bar)
            AnimatedVisibility(
                visible = !isAmbientMode && customVideoView == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Back to Movie Catalog
                            IconButton(
                                onClick = {
                                    if (customVideoView != null) {
                                        customViewCallback?.onCustomViewHidden()
                                        customVideoView = null
                                    } else {
                                        onClose()
                                    }
                                },
                                modifier = Modifier.testTag("theater_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Movies",
                                    tint = TextPrimary
                                )
                            }

                            // Movie Title & Ad-Free Theater Status
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pageTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = CinemaRed.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "CINEMA THEATER",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = CinemaRed,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }

                                    // Real-time blocked ads shield counter
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = ShieldGreen.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = ShieldGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "$blockedAdsCount ads blocked",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ShieldGreen
                                            )
                                        }
                                    }
                                }
                            }

                            // Right Action Icons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Desktop/Mobile Toggle
                                IconButton(
                                    onClick = onToggleDesktopMode,
                                    modifier = Modifier.testTag("theater_desktop_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isDesktopMode) Icons.Default.DesktopMac else Icons.Default.PhoneAndroid,
                                        contentDescription = if (isDesktopMode) "Desktop Mode" else "Mobile Mode",
                                        tint = if (isDesktopMode) CinemaGold else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Ambient Dark Dim Mode
                                IconButton(
                                    onClick = onToggleAmbientMode,
                                    modifier = Modifier.testTag("theater_ambient_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isAmbientMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Ambient Theater Mode",
                                        tint = if (isAmbientMode) CinemaGold else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Favorite Bookmark
                                IconButton(
                                    onClick = { onToggleFavorite(site) },
                                    modifier = Modifier.testTag("theater_favorite_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (site.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (site.isFavorite) CinemaGold else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Reload
                                IconButton(
                                    onClick = { webViewInstance?.reload() },
                                    modifier = Modifier.testTag("theater_reload_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reload",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Progress bar
            if (isLoading && pageProgress < 1f && customVideoView == null) {
                LinearProgressIndicator(
                    progress = { pageProgress },
                    color = CinemaRed,
                    trackColor = DarkSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )
            }

            // Central Content: Either the Fullscreen HTML5 Video or the Ad-Blocked Webview
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (customVideoView != null) {
                    // Native HTML5 Fullscreen Video Player Container
                    AndroidView(
                        factory = {
                            FrameLayout(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(android.graphics.Color.BLACK)
                                (customVideoView?.parent as? ViewGroup)?.removeView(customVideoView)
                                addView(
                                    customVideoView,
                                    ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Ad-Free Theater Web Container
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(android.graphics.Color.parseColor("#090B10"))

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    mediaPlaybackRequiresUserGesture = false
                                    setSupportMultipleWindows(false)
                                    javaScriptCanOpenWindowsAutomatically = false
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    cacheMode = WebSettings.LOAD_DEFAULT

                                    userAgentString = if (isDesktopMode) {
                                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                                    } else {
                                        "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        pageProgress = newProgress / 100f
                                        isLoading = newProgress < 100
                                    }

                                    override fun onReceivedTitle(view: WebView?, title: String?) {
                                        if (!title.isNullOrBlank() && !title.startsWith("http")) {
                                            pageTitle = title
                                        }
                                    }

                                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                        customVideoView = view
                                        customViewCallback = callback
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    }

                                    override fun onHideCustomView() {
                                        customVideoView = null
                                        customViewCallback?.onCustomViewHidden()
                                        customViewCallback = null
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                    }

                                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                        return true
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun shouldInterceptRequest(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): WebResourceResponse? {
                                        val url = request?.url?.toString()
                                        if (AdBlockEngine.isAdUrl(url)) {
                                            blockedAdsCount++
                                            onAdBlocked()
                                            return AdBlockEngine.createEmptyResourceResponse()
                                        }
                                        return super.shouldInterceptRequest(view, request)
                                    }

                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        isLoading = true
                                        // Inject early shields
                                        view?.evaluateJavascript(AdBlockEngine.INJECT_JS, null)
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                        canGoBack = view?.canGoBack() ?: false
                                        canGoForward = view?.canGoForward() ?: false
                                        // Reinforce anti-popup and element shield injection
                                        view?.evaluateJavascript(AdBlockEngine.INJECT_JS, null)
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url?.toString() ?: return false
                                        // Block non-http schemes (app store redirects, deep links to advertisers)
                                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                            return true
                                        }
                                        if (AdBlockEngine.isAdUrl(url)) {
                                            blockedAdsCount++
                                            onAdBlocked()
                                            return true
                                        }
                                        return false
                                    }
                                }

                                loadUrl(site.url)
                                webViewInstance = this
                            }
                        },
                        update = { webView ->
                            val targetUa = if (isDesktopMode) {
                                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                            } else {
                                "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                            }
                            if (webView.settings.userAgentString != targetUa) {
                                webView.settings.userAgentString = targetUa
                                webView.reload()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Ambient Mode Quick Exit Button (if ambient mode is active, show floating tiny exit button)
                if (isAmbientMode && customVideoView == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .clickable { onToggleAmbientMode() }
                            .testTag("ambient_exit_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = "Exit Ambient Mode",
                            tint = CinemaGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Bottom Movie Navigation Toolbar (Pure cinema controls, no browser address input)
            AnimatedVisibility(
                visible = !isAmbientMode && customVideoView == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { webViewInstance?.goBack() },
                                enabled = canGoBack,
                                modifier = Modifier.testTag("theater_nav_back")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Page",
                                    tint = if (canGoBack) TextPrimary else Color(0xFF475569)
                                )
                            }

                            IconButton(
                                onClick = { webViewInstance?.goForward() },
                                enabled = canGoForward,
                                modifier = Modifier.testTag("theater_nav_forward")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Page",
                                    tint = if (canGoForward) TextPrimary else Color(0xFF475569)
                                )
                            }
                        }

                        // Video Player Maximize Button: extracts HTML5 video into full screen
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = CinemaRed,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    // Trigger video requestFullscreen or trigger HTML5 player
                                    webViewInstance?.evaluateJavascript(
                                        """
                                        (function() {
                                            var v = document.querySelector('video');
                                            if (v) {
                                                if (v.requestFullscreen) {
                                                    v.requestFullscreen();
                                                } else if (v.webkitRequestFullscreen) {
                                                    v.webkitRequestFullscreen();
                                                }
                                                v.play();
                                            }
                                        })();
                                        """.trimIndent(),
                                        null
                                    )
                                }
                                .testTag("theater_maximize_video")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Maximize Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Maximize Video",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Ad-shield status indicator
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = ShieldGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ad-Shield ON",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ShieldGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
