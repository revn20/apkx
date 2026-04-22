package com.fbsharebx.app;

import okhttp3.OkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build();

    public static class Result {
        public final int code;
        public final String body;
        public Result(int c, String b) { this.code = c; this.body = b; }
    }

    public static Result shareWithCode(String cookie, String link, String limit) throws IOException {
        HttpUrl url = HttpUrl.parse("https://vern-rest-api.vercel.app/api/share")
            .newBuilder()
            .addQueryParameter("cookie", cookie == null ? "" : cookie)
            .addQueryParameter("link", link == null ? "" : link)
            .addQueryParameter("limit", limit == null ? "" : limit)
            .build();
        Request req = new Request.Builder()
            .url(url)
            .header("User-Agent", "FBShareBX/1.0 Android")
            .get()
            .build();
        try (Response resp = client.newCall(req).execute()) {
            String body = resp.body() != null ? resp.body().string() : "";
            return new Result(resp.code(), body);
        }
    }

    public static String fetchUrl(String urlStr) throws IOException {
        Request req = new Request.Builder().url(urlStr).get().build();
        try (Response resp = client.newCall(req).execute()) {
            return resp.body() != null ? resp.body().string() : "";
        }
    }
}
