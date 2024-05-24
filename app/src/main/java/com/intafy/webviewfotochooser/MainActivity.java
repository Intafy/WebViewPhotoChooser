package com.intafy.webviewfotochooser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ValueCallback<Uri[]> mFilePathCallBack;
    private ValueCallback<Uri> mUploadMsg;
    private Uri uri;
    private final int REQ_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView=findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://postimages.org/ru/");
        webView.setWebViewClient(new MyWeb());
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onShowFileChooser(
                    WebView view,ValueCallback<Uri[]> filePathCallBack,
                    WebChromeClient.FileChooserParams fileChooserParams){
            super.onShowFileChooser(view,filePathCallBack,fileChooserParams);
//                if(mFilePathCallBack!=null) mFilePathCallBack.onReceiveValue(null);
//                Intent intent = fileChooserParams.createIntent();
                Intent intent = fileChooserParams.createIntent();
                mFilePathCallBack=filePathCallBack;
                startActivityForResult(intent,REQ_CODE);
                return true;
            }
        });
    }
    private class MyWeb extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    }
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQ_CODE){
            if(mFilePathCallBack==null) return;
            mFilePathCallBack.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,data));
            mFilePathCallBack=null;
        }
    }
    @Override
    public void onBackPressed() {
        if(webView.canGoBack())webView.goBack();
        else super.onBackPressed();
    }
}