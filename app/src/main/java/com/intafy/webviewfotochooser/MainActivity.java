package com.intafy.webviewfotochooser;

import static com.google.android.material.internal.ContextUtils.getActivity;

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
            if(mFilePathCallBack!=null) {
                mFilePathCallBack.onReceiveValue(null);
            }
                mFilePathCallBack=filePathCallBack;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(getApplicationContext().getPackageManager())!=null){
                    File photoFile = null;
                    try{
                        photoFile=createImageFile();
                        takePictureIntent.putExtra("PhotoPath",mCameraPhotoPath);
                    }catch (IOException e){
                        Log.e(TAG,"Unable to create Image File");
                    }
                    if(photoFile!=null){
                        mCameraPhotoPath = "file:"+photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photoFile));
                    }else takePictureIntent = null;
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");
                Intent[] intentArray;
                if(takePictureIntent!=null){
                    intentArray=new Intent[]{takePictureIntent};
                }else intentArray = new Intent[0];

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT,contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE,"Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,intentArray);
                startActivityForResult(contentSelectionIntent,REQ_CODE);
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
        return imageFile;
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
        if(requestCode!=REQ_CODE||mFilePathCallBack==null){
            super.onActivityResult(requestCode,resultCode,data);
            return;
        }
        Uri[] results = null;

        if(resultCode == Activity.RESULT_OK){
            if(data==null) {
                if(mCameraPhotoPath!=null){
                    results=new Uri[]{Uri.parse(mCameraPhotoPath)};
                } else {
                    String dataString = data.getDataString();
                    if(dataString!=null) results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
        mFilePathCallBack.onReceiveValue(results);
        mFilePathCallBack=null;
    }
    @Override
    public void onBackPressed() {
        if(webView.canGoBack())webView.goBack();
        else super.onBackPressed();
    }
}
