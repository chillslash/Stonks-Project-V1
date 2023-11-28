package stonksproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;

public class StockPrices extends AppCompatActivity {

    public final String[] tickerSymbols = new String[]{"TSLA", "AAPL", "FB", "AMZN", "BTC-USD", "ETH-USD", "SNP"};
    private boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_prices);
        prices();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.stocks);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //NonNull = annotation which is processed compile time by the android studio to warn you that the particular function needs non null parameter.
                switch (item.getItemId()) {
                    case R.id.home:
                        running = false;
                        startActivity(new Intent(getApplicationContext(), StockNews.class));
                        overridePendingTransition(0, 0);

                        return true;
                    case R.id.portfolio:
                        running = false;
                        startActivity(new Intent(getApplicationContext(), PortfolioTracker.class));
                        overridePendingTransition(0, 0);

                    default:
                        return false;
                }

            }
        });
    }

    private void prices(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                boolean firstTime = true;
                try {
                    while (running) {
                        if ((LocalTime.now().getSecond() > 0) && (LocalTime.now().getSecond() < 59) && (!firstTime)) {
                            Thread.sleep(1000);

                        } else if ((LocalTime.now().getSecond() == 0) || (firstTime)) {
                            String[][] data = new String[tickerSymbols.length][];

                            for (int i = 0; i < tickerSymbols.length; i++) {
                                data[i] = callYahoo(tickerSymbols[i]);
                            }

                            displayInfo(data);

                            if (firstTime) {
                                Thread.sleep(1000);
                            } else {
                                Thread.sleep(45000);
                            }

                            firstTime = false;
                        }
                    }
                } catch (InterruptedException e){
                    e.printStackTrace();
                    }
                }
        }).start();
    }

    private String[] callYahoo(String tickerSymbol){
        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://query1.finance.yahoo.com/v8/finance/chart/" + tickerSymbol + "?range=1D&includePrePost=false&interval=1m")
                    .build();

            Response response = client.newCall(request).execute();
            JSONObject jsonMsg = new JSONObject(response.body().string());

            double newPrice = Double.parseDouble(jsonMsg.getJSONObject("chart").getJSONArray("result").getJSONObject(0).getJSONObject("meta").getString("regularMarketPrice"));
            double oldPrice = Double.parseDouble(jsonMsg.getJSONObject("chart").getJSONArray("result").getJSONObject(0).getJSONObject("indicators").getJSONArray("quote").getJSONObject(0).getJSONArray("open").getString(0));
            double percentChange = Math.round(((newPrice - oldPrice) / oldPrice) * 100.0)/100.0;

            return new String[]{tickerSymbol, String.valueOf(Math.round(newPrice*100.0)/100.0), String.valueOf(percentChange)};

        } catch (IOException | JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    private void displayInfo(String[][] data){
        this.runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {

                Integer[] stockNameViews = new Integer[]{R.id.stockName, R.id.stockName2, R.id.stockName3, R.id.stockName4, R.id.stockName5, R.id.stockName6, R.id.stockName7};
                Integer[] stockPriceViews = new Integer[]{R.id.stockPrice1, R.id.stockPrice2, R.id.stockPrice3, R.id.stockPrice4, R.id.stockPrice5, R.id.stockPrice6, R.id.stockPrice7};
                Integer[] stockChangeViews = new Integer[]{R.id.stockChange, R.id.stockChange2, R.id.stockChange3, R.id.stockChange4, R.id.stockChange5, R.id.stockChange6, R.id.stockChange7};

                for (int i = 0; i < data.length; i++) {

                    TextView stockName = findViewById(stockNameViews[i]);
                    stockName.setText(data[i][0]);

                    TextView stockPrice = findViewById(stockPriceViews[i]);
                    stockPrice.setText("$" + data[i][1]);

                    TextView stockChange = findViewById(stockChangeViews[i]);
                    stockChange.setText(data[i][2] + "%");
                }
            }
        });
    }
}