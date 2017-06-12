package org.zsago.retrofit.net.upload;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by Zsago on 2016/8/24.
 */
public interface UploadFileAPI {
//    @Multipart
//    @POST("PdaService/FileUpLoad")
//    Call<Void> upload(@Part MultipartBody.Part file);

    @Multipart
    @POST("PdaService/FileUpLoad")
    Observable<Void> uploadSingleFile(@Part MultipartBody.Part file, @QueryMap Map<String, String> params);

    @Multipart
    @POST("PdaService/FileUpLoad")
    Observable<Void> uploadMultiFiles(@Part List<MultipartBody.Part> filesBody, @QueryMap Map<String, String> params);
}
