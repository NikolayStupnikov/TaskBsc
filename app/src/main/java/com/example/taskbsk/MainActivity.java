package com.example.taskbsk;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taskbsk.model.Page;
import com.example.taskbsk.recycle.ItemDecoration;
import com.example.taskbsk.recycle.ListAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.cert.CertificateException;
import java.util.Objects;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private Toolbar toolbar;

    private TextView invite;
    private ImageView logo;
    private ImageView avatar;

    private RecyclerView body;
    private ListAdapter listAdapter;

    private DisplayMetrics displayMetrics;

    private int size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());

        logo = (ImageView) findViewById(R.id.logo);
        avatar = (ImageView) findViewById(R.id.avatar);
        invite = (TextView) findViewById(R.id.invite);

        body = (RecyclerView) findViewById(R.id.list);
        body.setNestedScrollingEnabled(true);
        body.addItemDecoration(new ItemDecoration(size / 5));
        listAdapter = new ListAdapter(this);
        body.setAdapter(listAdapter);

        new Thread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                String jsonString = getJsonStringFromResource();
                if (jsonString != null) {
                    StringReader reader = new StringReader(jsonString);
                    ObjectMapper mapper = new ObjectMapper();

                    try {
                        final Page page = mapper.readValue(reader, Page.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAdapter.addItem(page.getList());
                                invite.setText(getResources().getString(R.string.invite) + " " + page.getName());
                                Picasso.get()
                                        .load(page.getAvatar())
                                        .resize(size, size)
                                        .transform(new CircleTransform())
                                        .into(avatar);

                                Picasso picasso;
                                // Примечание: выбранный мною логотип не прогружался на 19 версии апи (504 ошибка)
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                    picasso = new Picasso.Builder(MainActivity.this).downloader(new OkHttp3Downloader(getUnsafeOkHttpClient())).build();
                                } else {
                                    picasso = Picasso.get();
                                }
                                picasso.load(page.getLogo())
                                        .resize(displayMetrics.widthPixels, 5 * size)
                                        .into(logo);
                            }
                        });
                    } catch (IOException e) {
                        Log.e(MainActivity.class.getSimpleName(), e.getMessage() + "");
                    }
                }
            }
        }).start();

    }

    public String getJsonStringFromResource() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.data)))) {
            StringBuilder builder = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                builder.append(chars, 0, read);
            }

            return builder.toString();

        } catch (Exception e) {
            return null;
        }
    }

    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}