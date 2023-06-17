package top.zsh2401.leafbox

import android.Manifest
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Environment
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import top.zsh2401.leafbox.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView;

    //    private lateinit var swipe: SwipeRefreshLayout;
    private lateinit var progress: ProgressBar;
    private var loadingPage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureProgressBar()
        configureWebView()
        configureSwipe()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        webView.settings.javaScriptEnabled = true;
    }

    override fun onStop() {
        super.onStop()
        webView.settings.javaScriptEnabled = false;
    }

    private fun configureProgressBar() {
        progress = findViewById(R.id.progress)
    }

    private fun configureSwipe() {
//        swipe = findViewById<SwipeRefreshLayout>(R.id.swipe)
//        swipe.setOnRefreshListener(this)
    }

    private fun configureWebView() {
        webView = findViewById<WebView>(R.id.webview)
        webView.settings.javaScriptEnabled = true;
        webView.settings.useWideViewPort = true;
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.allowScanningByMediaScanner()

            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )

            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,  //Download folder
                "download"
            ) //Name of file


            val dm = getSystemService(
                DOWNLOAD_SERVICE
            ) as DownloadManager

            dm.enqueue(request)
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingPage = true;
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                loadingPage = false;
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val content_url = request!!.url
                if (content_url.toString().startsWith(URL)) {
                    return false;
                } else {
                    val intent = Intent();
                    intent.action = "android.intent.action.VIEW";
                    intent.data = content_url;
                    startActivity(intent);
                    return true;
                }
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                val accept = request!!.requestHeaders["Accept"]
                if (accept?.contains("text/html") == true && loadingPage) {
                    view!!.loadUrl(ERROR_PAGE_URL);
                }
            }

            override fun onReceivedSslError(
                view: WebView?, handler: SslErrorHandler?, error: SslError?
            ) {
                handler?.proceed()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserIntent = fileChooserParams!!.createIntent();
                fileResolveCallback = filePathCallback
                val missed = getMissingPermissionForUploadFiles()
                if (missed.isNotEmpty()) {
                    requestPermissions(missed)
                } else {
                    onFilePermissionResolved()
                }
                return true

            }
        }
        webView.addJavascriptInterface(
            LeafBoxNativeBridge(this, webView),
            "leafboxNativeBridge"
        )
        webView.isVerticalScrollBarEnabled = true;
        webView.isHorizontalScrollBarEnabled = true;
        webView.loadUrl(URL)
    }

    private fun onFilePermissionResolved() {
//        val i = Intent(
//            Intent.ACTION_PICK
////                    MediaStore.
////                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        )
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        if (takePictureIntent.resolveActivity(packageManager) != null) {
//            startActivityForResult(takePictureIntent, FILE_CHOOSER_RESULT_CODE)
//        }
//        startActivityForResult(fileChooserIntent, FILE_CHOOSER_RESULT_CODE)
//        startActivityForResult(i, FILE_CHOOSER_RESULT_CODE)

        var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.type = "*/*"
        chooseFile = Intent.createChooser(chooseFile, "Choose a file")
        startActivityForResult(chooseFile, FILE_CHOOSER_RESULT_CODE)
    }


    override fun onRefresh() {
        val url: String = webView!!.url!!
        if (url == ERROR_PAGE_URL) {
            webView!!.loadUrl(URL)
        } else {
            webView.reload()
        }
    }


    var fileResolveCallback: ValueCallback<Array<Uri>>? = null
    var fileChooserIntent: Intent? = null

    //    var
    val FILE_CHOOSER_RESULT_CODE = 2402

    val REQUEST_PERMISSION_CODE = 2401


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (getMissingPermissionForUploadFiles().isEmpty()) {
                onFilePermissionResolved()
            } else {
                Toast.makeText(this, "请给予LeafBox存储权限，否则文件相关功能将无法使用。", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == FILE_CHOOSER_RESULT_CODE && fileResolveCallback != null) {
            if (resultCode == RESULT_OK && data != null && data.data != null) {
                fileResolveCallback!!.onReceiveValue(arrayOf(data!!.data!!))
            } else {
                fileResolveCallback!!.onReceiveValue(null);
            }
            fileResolveCallback = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun requestPermissions(permissions: List<String>) {
        ActivityCompat.requestPermissions(
            this, permissions.toTypedArray(), REQUEST_PERMISSION_CODE
        )
    }

    private fun getMissingPermissionForUploadFiles(): List<String> {
        val permissions = LinkedList<String>()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.CAMERA)
        }
        return permissions
    }
}