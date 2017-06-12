package org.zsago.retrofit.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Zsago on 2016/7/6.
 * <p/>
 * API规定我们传入拍照得到图片的存储位置的Uri。否则Bimmap将以一个压缩后的形式返回到我们当前Activity
 * <p/>
 * intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
 * 则会把拍照的图片存储到我们传入的Uri对应的File里面
 * <p/>
 * 注：由于调用的是系统相机应用，所以不需要任何权限
 */
public class Camera {
    private Uri imageFileUri;
    private File imageFile;
    private String fileName;

    public Camera() {
    }

    public void start(Activity context, int requestCode) throws NullPointerException, FileNotFoundException {
        if (!checkCameraHardWare(context)) {
            Toast.makeText(context, "There is no camera can be used!", Toast.LENGTH_SHORT).show();
        }

        imageFile = null;
        imageFileUri = null;
        fileName = null;

        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFileUri = getOutFileUri(context);//得到一个File Uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
        context.startActivityForResult(intent, requestCode);
    }

    public Uri getUri() {
        return imageFileUri;
    }

    public File getFile() {
        return imageFile;
    }

    public String getFileName() {
        return fileName;
    }

    private Uri getOutFileUri(Context context) throws NullPointerException, FileNotFoundException {
        return Uri.fromFile(getOutFile(context));
    }

    /**
     * 生成输出文件
     */
    private File getOutFile(Context context) throws NullPointerException, FileNotFoundException, NotFoundException {
        if (context == null) throw new NullPointerException("the context cannot be null!");
        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_REMOVED.equals(storageState)) {
            Toast.makeText(context.getApplicationContext(), "oh,no, SD卡不存在", Toast.LENGTH_SHORT).show();
            return null;
        }
        File mediaStorageDir = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                , "MyPictures");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.i("MyPictures", "创建图片存储路径目录失败");
                Log.i("MyPictures", "mediaStorageDir : " + mediaStorageDir.getPath());
                return null;
            }
        }
        imageFile = new File(getDefaultFilePath(mediaStorageDir));
        return imageFile;
    }

    /**
     * 生成输出文件路径
     */
    private String getDefaultFilePath(File mediaStorageDir) throws FileNotFoundException {
        if (mediaStorageDir == null || !mediaStorageDir.exists()) {
            throw new FileNotFoundException("mediaStorageDir is unuseable!");
        }
        long timeStamp = System.currentTimeMillis();
        String filePath = mediaStorageDir.getPath() + File.separator;
        fileName = "IMG_" + timeStamp + ".jpg";
        filePath += fileName;
        return filePath;
    }

    /**
     * 检测相机是否存在
     */
    private boolean checkCameraHardWare(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}
