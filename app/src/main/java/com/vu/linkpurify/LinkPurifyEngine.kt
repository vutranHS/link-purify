package com.vu.linkpurify

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

object LinkPurifyEngine {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Entry point for cleaning a URL.
     * Note: This is an 'object' function, but in production, consider injecting the client.
     */
    suspend fun clean(inputUrl: String): String {
        return try {
            val urlOnly = extractUrl(inputUrl) ?: return inputUrl
            
            // Phase 1: Resolve HTTP redirects
            var resolvedUrl = resolveRedirect(urlOnly)
            
            // Phase 2: Peek into 'next' or 'redir' params if we ended up on a login/interstitial page
            resolvedUrl = extractNestedUrl(resolvedUrl)
            
            // Phase 3: Recursive cleaning (in case the nested URL also had params)
            cleanUrlParams(resolvedUrl)
        } catch (e: Exception) {
            inputUrl
        }
    }

    private fun extractUrl(text: String): String? {
        val regex = Regex("https?://[^\\s]+")
        return regex.find(text)?.value
    }

    private fun resolveRedirect(url: String, depth: Int = 0): String {
        if (depth > 2) return url // Prevent infinite loops

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()
        
        return try {
            client.newCall(request).execute().use { response ->
                val finalUrl = response.request.url.toString()
                
                // If we are on a known bridge domain or the URL hasn't changed, peek into HTML for hidden redirects
                if (finalUrl.contains("s.lazada.vn") || finalUrl.contains("s.shopee.vn") || finalUrl == url) {
                    val body = response.body?.string() ?: ""
                    val extractedUrl = peekHtmlForRedirect(body)
                    if (extractedUrl != null && extractedUrl != finalUrl) {
                        return resolveRedirect(extractedUrl, depth + 1)
                    }
                }
                finalUrl
            }
        } catch (e: Exception) {
            url
        }
    }

    private fun peekHtmlForRedirect(html: String): String? {
        // Pattern 1: Meta Refresh <meta http-equiv="refresh" content="...url=(...)">
        val metaRegex = Regex("url=([^\"'> ]+)", RegexOption.IGNORE_CASE)
        val metaMatch = metaRegex.find(html)
        if (metaMatch != null) {
            return metaMatch.groupValues[1].replace("&amp;", "&")
        }

        // Pattern 2: JS window.location.href = '...' or location.replace('...')
        val jsRegex = Regex("(?:location\\.href|location\\.replace)\\s*=\\s*['\"]([^'\"]+)['\"]", RegexOption.IGNORE_CASE)
        val jsMatch = jsRegex.find(html)
        if (jsMatch != null) {
            return jsMatch.groupValues[1].replace("\\/", "/")
        }

        return null
    }

    private fun extractNestedUrl(url: String): String {
        val uri = try { URL(url) } catch (e: Exception) { return url }
        val query = uri.query ?: return url
        
        // Shopee often hides the target product URL in 'next' or 'redir' params on login pages
        val params = query.split("&")
        for (param in params) {
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].lowercase()
                if (key == "next" || key == "redir" || key == "target") {
                    val decoded = try { URLDecoder.decode(parts[1], "UTF-8") } catch (e: Exception) { parts[1] }
                    if (decoded.contains("shopee.vn") || decoded.contains("lazada.vn")) {
                        return decoded
                    }
                }
            }
        }
        return url
    }

    fun cleanUrlParams(url: String): String {
        val uri = try { URL(url) } catch (e: Exception) { return url }
        val query = uri.query ?: return url
        
        val unwantedPrefixes = listOf("af_", "utm_", "spm", "click_id", "pid", "c", "is_from_webapp", "smtt", "mmp_", "uls_", "scm", "trackInfo", "abbucket", "clickTrackInfo", "sec_user_id", "trackParams")
        val unwantedExact = listOf("spm_id", "source", "short_key", "version", "__mobile__", "exp_group", "gads_t_sig", "btn", "cc", "search", "trade_index", "user_id", "unique_id")

        val params = query.split("&")
        val cleanParams = params.filter { param ->
            val key = param.split("=").firstOrNull()?.lowercase() ?: return@filter true
            !unwantedPrefixes.any { key.startsWith(it) } && !unwantedExact.contains(key)
        }

        val cleanQuery = if (cleanParams.isEmpty()) "" else "?" + cleanParams.joinToString("&")
        
        // Shopee: Fully strip query for product pages
        if (uri.host.contains("shopee.vn")) {
            val path = uri.path ?: ""
            // Pattern 1: /product/123/456
            val productMatch1 = Regex("/product/(\\d+)/(\\d+)").find(path)
            if (productMatch1 != null) {
                return "https://${uri.host}/product/${productMatch1.groupValues[1]}/${productMatch1.groupValues[2]}"
            }
            // Pattern 2: /slug-i.123.456
            val productMatch2 = Regex("-i\\.(\\d+)\\.(\\d+)").find(path)
            if (productMatch2 != null) {
                return "https://${uri.host}/product/${productMatch2.groupValues[1]}/${productMatch2.groupValues[2]}"
            }
            // Pattern 3: /slug/123/456
            val productMatch3 = Regex("/[^/]+/(\\d{5,})/(\\d{5,})").find(path)
            if (productMatch3 != null) {
                return "https://${uri.host}/product/${productMatch3.groupValues[1]}/${productMatch3.groupValues[2]}"
            }
        }

        // Lazada: Fully strip query for product pages
        if (uri.host.contains("lazada.vn")) {
            val path = uri.path ?: ""
            if (path.contains("/products/") && path.endsWith(".html")) {
                return "${uri.protocol}://${uri.host}${uri.path}"
            }
        }

        // TikTok Shop: Aggressively strip query from product pages
        if (uri.host.contains("tiktok.com")) {
            val path = uri.path ?: ""
            if (path.contains("/view/product/")) {
                return "${uri.protocol}://${uri.host}${uri.path}"
            }
        }

        return "${uri.protocol}://${uri.host}${uri.path}$cleanQuery"
    }
}
