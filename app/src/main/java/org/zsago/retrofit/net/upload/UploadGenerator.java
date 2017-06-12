package org.zsago.retrofit.net.upload;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Retrofit变量初始化
 * Created by SmileXie on 16/7/16.
 */
public class UploadGenerator {
    private Retrofit.Builder retrofitBuilder;
    private OkHttpClient.Builder okHttpClient;
    private UploadFileAPI uploadApi;

    private static UploadGenerator generator;

    public synchronized static UploadGenerator obj() {
        if (generator == null) {
            generator = new UploadGenerator();
        }
        return generator;
    }

    UploadGenerator() {
        Strategy strategy;
        Serializer serializer;
        strategy = new AnnotationStrategy();
        serializer = new Persister(strategy);
        okHttpClient = new OkHttpClient.Builder();
        retrofitBuilder = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create(serializer))
                .baseUrl("http://172.33.33.93:8080");
    }

    public <T> T createService(Class<T> serviceClass) {
        okHttpClient.interceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Content-Type", "text/xml;charset=UTF-8")// 对于SOAP 1.1， 如果是soap1.2 应是Content-Type: application/soap+xml; charset=utf-8
                        .method(original.method(), original.body());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        OkHttpClient client = okHttpClient.connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = retrofitBuilder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public UploadFileAPI getUploadApi() {
        if (uploadApi == null) {
            uploadApi = createService(UploadFileAPI.class);
        }
        return uploadApi;
    }
}
