package stonksproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class StockNews extends AppCompatActivity {

    private static final int MAX_BITMAP_SIZE = 100 * 1024 * 1024; // 100 MB
    private static final String[][] result = new String[5][5];
    private static final Bitmap[] bmp = new Bitmap[5];
    private int countBmp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_news);

        if (result[0][0] == null) {
            JSONAsyncTask task = new JSONAsyncTask();
            task.execute();
        }
        else{
            displayNews();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.home);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //NonNull = annotation which is processed compile time by the android studio to warn you that the particular function needs non null parameter.
                switch (item.getItemId()) {
                    case R.id.stocks:
                        startActivity(new Intent(getApplicationContext(), StockPrices.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.portfolio:
                        startActivity(new Intent(getApplicationContext(), PortfolioTracker.class));
                        overridePendingTransition(0, 0);
                    default:
                        return false;
                }

            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    class JSONAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(mediaType, "Pass in the value of uuids field returned right in this endpoint to load the next page, or leave empty to load first page");
                Request request = new Request.Builder()
                        .url("https://apidojo-yahoo-finance-v1.p.rapidapi.com/news/v2/list?region=SG&snippetCount=5")
                        .post(body)
                        .addHeader("content-type", "text/plain")
                        .addHeader("x-rapidapi-key", "83a03a9c84msh3234bf71e034e6ap13bbcajsn348eff77806e")
                        .addHeader("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com")
                        .build();

                Response response = client.newCall(request).execute();

                JSONObject jsonMsg = new JSONObject(response.body().string());

                for (int i = 0; i < 5; i++) {
                    String thumbnail = "";
                    String url = "";

                    JSONObject content = jsonMsg.getJSONObject("data").getJSONObject("main").getJSONArray("stream").getJSONObject(i).getJSONObject("content");
                    String title = content.getString("title");
                    if (title.length() > 30){
                        title = title.substring(0, 30) + "...";
                    }
                    String pubDate = content.getString("pubDate");
                    String provider = content.getJSONObject("provider").getString("displayName");

                    try {
                        thumbnail = content.getJSONObject("thumbnail").getJSONArray("resolutions").getJSONObject(0).getString("url");
                        url = content.getJSONObject("clickThroughUrl").getString("url");
                    } catch (org.json.JSONException ignored) {
                    }
                    result[i] = new String[]{title, pubDate, provider, thumbnail, url};

                    if (!thumbnail.equals("")){
                        getImages(thumbnail, i);
                    }
                    else{
                        countBmp += 1;
                    }
                }

                return null;

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                // DO ERROR HANDLING
                return null;
            }
        }

        protected void onPostExecute(String data) {
            while (countBmp != 5){
            }
            displayNews();
        }
    }
    private void openLink(String link) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    private void displayNews(){
        // These are lists of IDs from .xml file (e.g. titles has textView)
        int[] titles = new int[]{R.id.textView, R.id.textView2, R.id.textView3, R.id.textView4, R.id.textView5};
        int[] imgs = new int[]{R.id.imgCoverArt, R.id.imgCoverArt2, R.id.imgCoverArt3, R.id.imgCoverArt4, R.id.imgCoverArt5};
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        for (int i = 0 ; i < 5 ; i++){
            // All the values you need
            String title = result[i][0];
            String pubData = result[i][1];
            String provider = result[i][2];
            String url = result[i][4];

            // Example of inserting titles
            TextView titleTextBox = findViewById(titles[i]);
            titleTextBox.setText(title);
            LinkBuilder.on(titleTextBox).addLink(new Link(title).setOnClickListener(clickedText -> {
                openLink(url);
            })).build();

            // Image part, udrn to know ig
            Bitmap oneBmp = bmp[i];
            ImageView ivCoverArt = findViewById(imgs[i]);
            ivCoverArt.setImageBitmap(oneBmp);
        }
    }

    private void getImages(String thumbnail, int i){
        new Thread(() -> {
            try {
                URL thumbnail1Url = new URL(thumbnail);
                bmp[i] = BitmapFactory.decodeStream(thumbnail1Url.openConnection().getInputStream());
                if (bmp[i].getByteCount() > MAX_BITMAP_SIZE) {
                    bmp[i] = null;
                }
                countBmp += 1;
            } catch (IOException e){
                e.printStackTrace();
            }
        }).start();
    }
}

// TODO
// - Pull down to refresh
// - Able to load more when scrolling down
// - More details with publisher and date
// - StockLogo if there is no thumbnail


