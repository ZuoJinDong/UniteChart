package com.zjd.demo.net;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class HeadInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response;
        Request originalRequest = chain.request();
        try {
            Request.Builder requestBuilder = originalRequest.newBuilder();
            requestBuilder.removeHeader("User-Agent");
            requestBuilder.removeHeader("appId");
            requestBuilder.addHeader("appId","27");
            if (originalRequest.headers().get("cngoldId") != null && originalRequest.headers().get("sign") != null) {
                response = chain.proceed(requestBuilder.build());
            } else {
                if (originalRequest.headers().get("cngoldId") == null) {
                    requestBuilder = requestBuilder.addHeader("cngoldId", "0");
                }
                if (originalRequest.headers().get("sign") == null) {
                    String sign = EncryptorUtil.encryptSHA1ToString("0").toLowerCase();
                    requestBuilder = requestBuilder.addHeader("sign", sign);
                }
                if (originalRequest.headers().get("udid") == null) {
                    requestBuilder = requestBuilder.addHeader("udid", "000000");
                }
                Request request = requestBuilder.build();
                response = chain.proceed(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = chain.proceed(originalRequest);
        }
        return response;
    }
}
