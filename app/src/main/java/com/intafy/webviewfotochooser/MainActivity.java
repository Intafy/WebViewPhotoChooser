package com.intafy.webviewfotochooser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ValueCallback<Uri[]> mFilePathCallBack;
    private String mCameraPhotoPath;
    private final int REQ_CODE = 100;
    private final String TAG = "Error tag";

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
                mFilePathCallBack=filePathCallBack;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Log.d("MyLog","Intent has created");
                if(takePictureIntent.resolveActivity(getPackageManager())!=null){
                    File photoFile = null;
                    try{
                        photoFile=createImageFile();
                        takePictureIntent.putExtra("PhotoPath",mCameraPhotoPath);
                        Log.d("MyLog","Intent and file has created");
                    }catch (IOException e){
                        Log.e(TAG,"Unable to create Image File");
                    }
                    if(photoFile!=null){
                        mCameraPhotoPath = "file:"+photoFile.getAbsolutePath();

                        Log.d("MyLog","Path to file has created");
                        Uri imageUri=FileProvider.getUriForFile(MainActivity.this,"com.intafy.webviewfotochooser.provider",photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    }

                }
                startActivityForResult(takePictureIntent,REQ_CODE);
                return true;
            }
        });
    }
    private File createImageFile()throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,/*prefix*/
                ".jpeg",/*suffix*/
                storageDir/*directory*/
        );
        Log.d("MyLog","File has created");
        return  imageFile;
    }
    private static class MyWeb extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    }
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE) {
            if (mFilePathCallBack == null) return;
        Uri[] results = null;
        if(resultCode == Activity.RESULT_OK){
            if(data==null) {
                if(mCameraPhotoPath!=null){
                    results=new Uri[]{Uri.parse(mCameraPhotoPath)};
                } else {
                    String dataString = data.getDataString();
                    if(dataString!=null) results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                }
            }
        mFilePathCallBack.onReceiveValue(results);

        mFilePathCallBack=null;
        }
    }


    @Override
    public void onBackPressed() {
        if(webView.canGoBack())webView.goBack();
        else super.onBackPressed();
    }
}
