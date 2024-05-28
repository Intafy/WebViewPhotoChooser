package com.intafy.webviewfotochooser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ValueCallback<Uri[]> mFilePathCallBack;
    private String mCameraPhotoPath;
    private final int REQ_CODE = 100;
    private final String TAG = "Error tag";
    private final ArrayList<Intent> dataList= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView=findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://postimages.org/ru/");
//        webView.loadUrl("https://ru.imgbb.com/");
        webView.setWebViewClient(new MyWeb());
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onShowFileChooser(
                    WebView view,ValueCallback<Uri[]> filePathCallBack,
                    WebChromeClient.FileChooserParams fileChooserParams){
            super.onShowFileChooser(view,filePathCallBack,fileChooserParams);
//            if(mFilePathCallBack!=null) {
//                mFilePathCallBack.onReceiveValue(null);
//            }
                ArrayList<Intent>intentArray =new ArrayList<>();
                Intent takePictureIntent;
                for (int i=0;i<2;i++) {
                    mFilePathCallBack=filePathCallBack;
                    takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intentArray.add(takePictureIntent);
                    Log.d("MyLog","Intent has created");
                    if(takePictureIntent.resolveActivity(getPackageManager())!=null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                            Log.d("MyLog", "Intent and file has created");
                        } catch (IOException e) {
                            Log.e(TAG, "Unable to create Image File");
                        }
                        if (photoFile != null) {
                            mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                            Log.d("MyLog", "Path to file has created");
                            Uri imageUri = FileProvider.getUriForFile(MainActivity.this, "com.intafy.webviewfotochooser.provider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            takePictureIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        }
                    }
                }
//                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
//                contentSelectionIntent.setType("image*");
//                Intent[] intentArray;
//                if(takePictureIntent!=null){
//                    intentArray=new Intent[]{takePictureIntent};
//                }else intentArray = new Intent[0];
//
//                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
//                chooserIntent.putExtra(Intent.EXTRA_INTENT,contentSelectionIntent);
//                chooserIntent.putExtra(Intent.EXTRA_TITLE,"Image Chooser");
//                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,intentArray);

                for(int i=0;i<intentArray.size();i++) {
                    startActivityForResult(intentArray.get(i), REQ_CODE);
                }
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
        dataList.add(data);
        Uri[] results = new Uri[0];
        if (requestCode == REQ_CODE) {
            if (mFilePathCallBack == null) return;
            results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    } else {
                        for(int i=0;i<dataList.size();i++) {
                            String dataString = dataList.get(i).getDataString();
                            if (dataString != null) {
                                results = new Uri[]{Uri.parse(dataString)};
                            }
                        }
                    }
                }
            }
        }
            mFilePathCallBack.onReceiveValue(results);
            mFilePathCallBack = null;

    }



    @Override
    public void onBackPressed() {
        if(webView.canGoBack())webView.goBack();
        else super.onBackPressed();
    }
}
