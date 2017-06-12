package org.zsago.retrofit.net.upload;

/**
 * Created by Zsago on 2016/8/24.
 */
public interface UploadProgressListener {
    /**
     * @param progress 已经下载或上传字节数
     * @param total    总字节数
     * @param done     是否完成
     */
    void onProgress(long progress, long total, boolean done);
}
