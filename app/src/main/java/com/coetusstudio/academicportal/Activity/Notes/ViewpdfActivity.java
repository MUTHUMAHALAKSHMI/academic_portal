package com.coetusstudio.academicportal.Activity.Notes;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;
import com.coetusstudio.academicportal.R;
import java.net.URLEncoder;

public class ViewpdfActivity extends AppCompatActivity {

    WebView pdfview;
    ImageView btnOpenExternal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpdf);

        pdfview = findViewById(R.id.viewpdf);
        btnOpenExternal = findViewById(R.id.btn_open_external);

        WebSettings settings = pdfview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setPluginState(WebSettings.PluginState.ON);

        final String filename = getIntent().getStringExtra("filename");
        final String fileurl = getIntent().getStringExtra("fileurl");

        if (fileurl == null || fileurl.isEmpty()) {
            Toast.makeText(this, "Link not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle(filename != null ? filename : "Notes");
        pd.setMessage("Loading Preview...");
        pd.setCancelable(true);

        pdfview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!isFinishing()) pd.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (pd.isShowing()) pd.dismiss();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (pd.isShowing()) pd.dismiss();
                Toast.makeText(ViewpdfActivity.this, "Preview failed. Use the top-right button to download/open.", Toast.LENGTH_LONG).show();
            }
        });

        pdfview.setWebChromeClient(new WebChromeClient());

        try {
            // Encode the URL properly for the Google Viewer
            String encodedUrl = URLEncoder.encode(fileurl, "UTF-8");
            // Use the most reliable viewer URL
            String viewerUrl = "https://docs.google.com/viewer?embedded=true&url=" + encodedUrl;
            pdfview.loadUrl(viewerUrl);
        } catch (Exception e) {
            pdfview.loadUrl(fileurl);
        }

        if (btnOpenExternal != null) {
            btnOpenExternal.setOnClickListener(v -> {
                try {
                    // Try to open in a PDF viewer/Browser
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(fileurl), "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                } catch (Exception e) {
                    // Fallback to browser if no PDF app is found
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileurl));
                    startActivity(browserIntent);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (pdfview.canGoBack()) {
            pdfview.goBack();
        } else {
            super.onBackPressed();
            finish();
        }
    }
}
