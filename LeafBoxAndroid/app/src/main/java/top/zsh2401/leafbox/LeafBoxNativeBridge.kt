package top.zsh2401.leafbox

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate

class LeafBoxNativeBridge(
    private val context: Context,
    private val webView: WebView
) {

    @JavascriptInterface
    fun getVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }

    @JavascriptInterface
    fun platform(): String {
        return "android";
    }

    @JavascriptInterface
    fun arch(): String {
        return "arm64"
    }

    @JavascriptInterface
    fun retry() {
        webView.post {
            webView.loadUrl(URL)
        };
    }

    @JavascriptInterface
    fun isDarkMode(): Boolean {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
    }

    @JavascriptInterface
    fun setDarkMode(darkTheme: Boolean) {
        if (darkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}