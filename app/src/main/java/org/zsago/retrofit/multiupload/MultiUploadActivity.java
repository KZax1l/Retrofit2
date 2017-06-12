package org.zsago.retrofit.multiupload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.zsago.retrofit.R;
import org.zsago.retrofit.adapter.UploadFilesAdapter;
import org.zsago.retrofit.net.upload.UploadFileAPI;
import org.zsago.retrofit.net.upload.UploadFileRequestBody;
import org.zsago.retrofit.net.upload.UploadGenerator;
import org.zsago.retrofit.net.upload.UploadProgressListener;
import org.zsago.retrofit.util.Camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class MultiUploadActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private EditText etDesc;
    private Subscription subscription;
    private ProgressDialog progressDialog;
    private UploadFilesAdapter uploadAdapter;

    private final int CODE_IMAGE_REQUEST = 101;
    private final int CODE_AUDIO_REQUEST = 102;
    private final int CODE_VIDEO_REQUEST = 103;
    private final int CODE_TAKE_IMAGE = 104;
    private final int CODE_TAKE_AUDIO = 105;
    private final int CODE_TAKE_VIDEO = 106;

    private Camera camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_multi_upload);
        etDesc = (EditText) findViewById(R.id.et_desc);
        Button btnSubmit = (Button) findViewById(R.id.btn_submit);
        GridView gridView = (GridView) findViewById(R.id.gridview);
        TextView tvTakePhoto = (TextView) findViewById(R.id.tv_take_photo);
        uploadAdapter = new UploadFilesAdapter(this);
        gridView.setAdapter(uploadAdapter);
        btnSubmit.setOnClickListener(this);
        tvTakePhoto.setOnClickListener(this);
        gridView.setOnItemClickListener(this);
        camera = new Camera();
    }

    /**
     * 选择照片
     */
    private void choosePhoto() {
        Intent innerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(innerIntent, CODE_IMAGE_REQUEST);
    }

    private void requestImageResult(Intent data) {
        Uri originalUri = data.getData();
        Cursor cursor = getContentResolver().query(originalUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(column_index); // 取出文件路径
            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
            String fileSize = "" + size;
            String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            uploadAdapter.addItem(UploadFilesAdapter.TYPE_IMAGE, filePath, fileName, fileSize);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        choosePhoto();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case CODE_IMAGE_REQUEST:
                    requestImageResult(data);
                    break;
                case CODE_TAKE_IMAGE:
                    takeImageResult();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_submit:
                submitMultiFiles(etDesc.getText().toString());
//                submitSingleFile(uploadAdapter.getAttachmentSize() - 1, etDesc.getText().toString());
                break;
            case R.id.tv_take_photo:
                takePhotos();
                break;
        }
    }

    private void takeImageResult() {
        File mFile = camera.getFile();
        String fileName = camera.getFileName();
        String filePath = mFile.getPath();
        String fileSize = mFile.length() + "";
        uploadAdapter.addItem(UploadFilesAdapter.TYPE_IMAGE, filePath, fileName, fileSize);
    }

    private void submitSingleFile(final int index, final String sceneDesc) {
        if (uploadAdapter.getAttachmentSize() == 0) {
            Toast.makeText(MultiUploadActivity.this, "无可上传文件！", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> mParams = new ArrayMap<>();
        mParams.put("fk_party_id", "企独琼总字第007008号");
        mParams.put("party_name", "海南海艺旅业服务有限公司");
        mParams.put("inspector", "孙东");
        mParams.put("inspector_id", "5CB870C1F71B48A386BBFCAAD15AD32D");
        mParams.put("inspector_record", sceneDesc);
        mParams.put("check_result", "罚款");
        mParams.put("longitude", "104.00696300076247");
        mParams.put("latitude", "30.71194465007023");
        mParams.put("enforcementmode", "明查");
        mParams.put("attachment_name", getPrefix(uploadAdapter.getFileName(index)));
        mParams.put("attachment_suffix", getSuffix(uploadAdapter.getFileName(index)));
        mParams.put("attachment_size", uploadAdapter.getFileSize(index));
        mParams.put("remarks", sceneDesc);

        File mFile = new File(uploadAdapter.getMediaUrl(index));
        UploadFileRequestBody requestBody = new UploadFileRequestBody(mFile, new UploadProgressListener() {
            @Override
            public void onProgress(long progress, long total, boolean done) {
                Log.e("upload progress", "progress=" + progress + ",total=" + total + ",done=" + done);
                if (progress > Integer.MAX_VALUE) return;
                setProgress(progressDialog, (int) progress);
            }
        });
        try {
            progressDialog = initProgressDialog(requestBody.contentLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        RequestBody requestBody = RequestBody.create(MediaType.parse("ultipart/form-data"), mFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("key", mFile.getName(), requestBody);

//        Map<String, RequestBody> mParams = new ArrayMap<>();
//        mParams.put("file\"; filename=\"" + mFile.getName(), requestBody);

//        uploadFileApi.uploadSingleFile(body).enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                Utility.log("onResponse");
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                Utility.log("onFailure");
//            }
//        });

        UploadFileAPI uploadFileApi = UploadGenerator.obj().getUploadApi();
        subscription = uploadFileApi.uploadSingleFile(body, mParams)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        progressDialog.show();
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(MultiUploadActivity.this, "上传成功！", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        uploadAdapter.remove(index);
                        if (index <= 0) return;
                        submitSingleFile(index - 1, sceneDesc);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(MultiUploadActivity.this, "上传失败！", Toast.LENGTH_SHORT).show();
                        if (index <= 0) return;
                        submitSingleFile(index - 1, sceneDesc);
                    }

                    @Override
                    public void onNext(Void mVoid) {
                    }
                });
    }

    private void submitMultiFiles(String desc) {
        if (uploadAdapter.getAttachmentSize() == 0) {
            Toast.makeText(MultiUploadActivity.this, "无可上传文件！", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> mParams = new ArrayMap<>();
        mParams.put("fk_party_id", "企独琼总字第007008号");
        mParams.put("party_name", "海南海艺旅业服务有限公司");
        mParams.put("inspector", "孙东");
        mParams.put("inspector_id", "5CB870C1F71B48A386BBFCAAD15AD32D");
        mParams.put("inspector_record", desc);
        mParams.put("check_result", "罚款");
        mParams.put("longitude", "104.00696300076247");
        mParams.put("latitude", "30.71194465007023");
        mParams.put("enforcementmode", "明查");
        mParams.put("attachment_name", getPrefix(uploadAdapter.getFileName(0)));
        mParams.put("attachment_suffix", getSuffix(uploadAdapter.getFileName(0)));
        mParams.put("attachment_size", uploadAdapter.getFileSize(0));
        mParams.put("remarks", desc);

        List<MultipartBody.Part> arrPart = new ArrayList<>(uploadAdapter.getAttachmentSize());
        for (int i = 0; i < uploadAdapter.getAttachmentSize(); i++) {
            File mFile = new File(uploadAdapter.getMediaUrl(i));
            UploadFileRequestBody requestBody = new UploadFileRequestBody(mFile, new UploadProgressListener() {
                @Override
                public void onProgress(long progress, long total, boolean done) {
                    Log.e("upload progress", "progress=" + progress + ",total=" + total + ",done=" + done);
                    if (progress > Integer.MAX_VALUE) return;
                    setProgress(progressDialog, (int) progress);
                }
            });
            try {
                progressDialog = initProgressDialog(requestBody.contentLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
            MultipartBody.Part part = MultipartBody.Part.createFormData("key", mFile.getName(), requestBody);
            arrPart.add(part);
        }

        UploadFileAPI uploadFileApi = UploadGenerator.obj().getUploadApi();
        subscription = uploadFileApi.uploadMultiFiles(arrPart, mParams)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        progressDialog.show();
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(MultiUploadActivity.this, "上传成功！", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(MultiUploadActivity.this, "上传失败！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Void mVoid) {
                    }
                });
    }

    /**
     * 拍照
     */
    public void takePhotos() {
        try {
            camera.start(this, CODE_TAKE_IMAGE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getSuffix(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    private String getPrefix(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 显示ProgressDialog不能用ApplicationContext
     */
    private ProgressDialog initProgressDialog(long contentLength) {
        if (contentLength > Integer.MAX_VALUE) {
            return null;
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("上传中。。。");
        progressDialog.setCancelable(false);
        progressDialog.setMax((int) contentLength);
        return progressDialog;
    }

    private void setProgress(final ProgressDialog progressDialog, final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setProgress(progress);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null) subscription.unsubscribe();
    }
}
