package com.example.engine

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object AdBlockEngine {

    // Common ad, tracking, popup, and crypto-mining domain keywords & hosts
    private val AD_DOMAINS = hashSetOf(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adservice.google.com", "pagead2.googlesyndication.com",
        "popads.net", "popcash.net", "propellerads.com", "adsterra.com",
        "exoclick.com", "trafficjunky.com", "mgid.com", "revcontent.com",
        "outbrain.com", "taboola.com", "criteo.com", "rubiconproject.com",
        "pubmatic.com", "openx.net", "adnxs.com", "adcolony.com",
        "applovin.com", "unity3d.com/ads", "vungle.com", "inmobi.com",
        "chartboost.com", "scorecardresearch.com", "quantserve.com",
        "clicksor.com", "admob.com", "adroll.com", "bet365.com",
        "1xbet.com", "betwinner.com", "parimatch.com", "adreactor.com",
        "yllix.com", "bidvertiser.com", "infolinks.com", "chitika.com",
        "ero-advertising.com", "juicyads.com", "plugrush.com",
        "ad-delivery.net", "adtrue.com", "adkernel.com", "adfox.ru",
        "adsystem.com", "adsupply.com", "adzerk.net", "adnxs.net",
        "streamtape.com/ad", "fembed.com/ad", "doodstream.com/ad",
        "voe.sx/ad", "mixdrop.co/ad", "upstream.to/ad"
    )

    private val AD_KEYWORDS = listOf(
        "/ads.", "/ad.", "/ad_", "/ads_", "adserver", "advert",
        "banner", "popup", "popunder", "clickunder", "tracking",
        "analytics", "telemetry", "monetiz", "syndication", "affiliate",
        "promo-banner", "fake-player", "ad-placement", "sponsored"
    )

    fun isAdUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lowerUrl = url.lowercase()

        // Don't block main video streaming formats or essential web components
        if (lowerUrl.endsWith(".m3u8") || lowerUrl.endsWith(".mp4") ||
            lowerUrl.endsWith(".webm") || lowerUrl.contains(".mp4?") ||
            lowerUrl.contains(".m3u8?") || lowerUrl.contains("/manifest")
        ) {
            return false
        }

        try {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase() ?: ""
            for (adDomain in AD_DOMAINS) {
                if (host.contains(adDomain)) {
                    return true
                }
            }
        } catch (_: Exception) { }

        // Keyword inspection for suspicious script/image tracking endpoints
        for (keyword in AD_KEYWORDS) {
            if (lowerUrl.contains(keyword)) {
                // Safeguard against false positive matches on standard paths
                if (!lowerUrl.contains("video") && !lowerUrl.contains("stream") &&
                    !lowerUrl.contains("player") && !lowerUrl.contains("media")
                ) {
                    return true
                }
            }
        }

        return false
    }

    fun createEmptyResourceResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }

    /**
     * Injected CSS to hide ad banners, overlays, modal popups, and force cinema look
     */
    val INJECT_CSS = """
        /* Hide all ad banners, overlays, popups, and clickjacking masks */
        .ad, .ads, .ad-banner, .advertisement, [id*="ad-"], [class*="ad-"],
        [class*="banner"], [id*="banner"], [class*="pop"], [id*="pop"],
        [class*="sponsor"], [id*="sponsor"], iframe[src*="ad"],
        iframe[src*="track"], iframe[src*="banner"], .adsbygoogle,
        .overlay-ad, .video-ad-overlay, .modal-backdrop-ad, .ad-overlay,
        [class*="floating-ad"], [id*="floating-ad"], .native-ad,
        [data-ad], [data-ad-unit], .affiliate-link,
        .fake-button, a[href*="bet365"], a[href*="1xbet"] {
            display: none !important;
            visibility: hidden !important;
            opacity: 0 !important;
            pointer-events: none !important;
            height: 0 !important;
            width: 0 !important;
            z-index: -9999 !important;
        }

        /* Movie theater dark styling */
        html, body {
            background-color: #0b0e14 !important;
            color: #f1f5f9 !important;
        }

        /* Elevate video element above any click-trap overlays */
        video {
            z-index: 999999 !important;
            position: relative !important;
        }
    """.trimIndent()

    /**
     * Injected JavaScript for active runtime anti-popup, click-trap cleaning,
     * and video player extraction.
     */
    val INJECT_JS = """
        (function() {
            // 1. Intercept popup spawns & redirection
            try {
                window.open = function() {
                    console.log('[Milon Movie Shield] Blocked popup window.open');
                    return null;
                };
                window.alert = function() { return true; };
                window.confirm = function() { return true; };
            } catch(e) {}

            // 2. Inject CSS
            try {
                if (!document.getElementById('milon-movie-shield-style')) {
                    var style = document.createElement('style');
                    style.id = 'milon-movie-shield-style';
                    style.innerHTML = `${INJECT_CSS.replace("\n", " ")}`;
                    (document.head || document.documentElement).appendChild(style);
                }
            } catch(e) {}

            // 3. Remove overlay click-traps sitting above video
            function cleanMovieElements() {
                var videos = document.querySelectorAll('video');
                videos.forEach(function(v) {
                    v.style.zIndex = '999999';
                    v.style.position = 'relative';
                    v.setAttribute('controls', 'true');
                    v.setAttribute('playsinline', 'true');
                    // Remove hidden cover divs that hijack initial play click
                    var parent = v.parentElement;
                    if (parent) {
                        var siblings = parent.children;
                        for (var i = 0; i < siblings.length; i++) {
                            var sib = siblings[i];
                            if (sib !== v && sib.tagName !== 'VIDEO') {
                                var rect = sib.getBoundingClientRect();
                                if (rect.width > 200 && rect.height > 150) {
                                    var style = window.getComputedStyle(sib);
                                    if (style.position === 'absolute' || style.position === 'fixed') {
                                        sib.style.display = 'none';
                                        sib.style.pointerEvents = 'none';
                                    }
                                }
                            }
                        }
                    }
                });

                // Remove known ad elements
                var adSelectors = ['.ad', '.ads', '.ad-banner', '[id*="ad-"]', '[class*="ad-"]', '[class*="pop"]', 'iframe[src*="ad"]'];
                adSelectors.forEach(function(sel) {
                    try {
                        var els = document.querySelectorAll(sel);
                        els.forEach(function(el) {
                            el.remove();
                        });
                    } catch(e) {}
                });
            }

            cleanMovieElements();
            setInterval(cleanMovieElements, 1200);
        })();
    """.trimIndent()
}
