package stonksproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PortfolioTracker extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser currentUser = mAuth.getCurrentUser();
    private final FirebaseFirestore firebase = FirebaseFirestore.getInstance();
    private final String uid = currentUser.getUid();
    private final HashMap<String, double[]> data = new HashMap<>();
    private boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio_tracker);
        callInfo();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.portfolio);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //NonNull = annotation which is processed compile time by the android studio to warn you that the particular function needs non null parameter.
                switch (item.getItemId()) {
                    case R.id.stocks:
                        running = false;
                        startActivity(new Intent(getApplicationContext(), StockPrices.class));
                        // context of current state of the application/object; lets newly-created objects understand what has been going on
                        overridePendingTransition(0, 0);
                        // After starting or closing an activity, Android will automatically play default transitions
                        return true;
                    case R.id.home:
                        running = false;
                        startActivity(new Intent(getApplicationContext(), StockNews.class));
                        overridePendingTransition(0, 0);

                        return true;

                    default:
                        return false;
                }

            }
        });

    }

    private void callInfo() {
        firebase.collection("portfolio").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        Set<Map.Entry<String, Object>> rawData = document.getData().entrySet();
                        for (Map.Entry<String, Object> item : rawData){
                            String tickerSymbol = item.getKey();
                            double shares = Double.parseDouble(item.getValue().toString().split(",")[0]);
                            double oldPrice = Double.parseDouble(item.getValue().toString().split(",")[1]);
                            data.put(tickerSymbol, new double[]{shares, oldPrice});
                        }
                        if (!data.isEmpty()){
                            watchInfo();
                        }
                    } else{
                        // Does not exist
                    }
                }
                else{
                    // Error handling
                }
            }
        });

    }

    private void watchInfo(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                boolean firstTime = true;
                try{
                    while (running) {
                        if ((LocalTime.now().getSecond() > 0) && (LocalTime.now().getSecond() < 59) && (!firstTime)) {
                            Thread.sleep(1000);

                        } else if ((LocalTime.now().getSecond() == 0) || (firstTime)) {
                            HashMap<String, double[]> displayData = new HashMap<>();

                            for (String tickerSymbol : data.keySet()) {
                                double[] oneData = callYahoo(tickerSymbol);
                                displayData.put(tickerSymbol, oneData);
                            }

                            displayInfo(displayData);

                            if (firstTime) {
                                Thread.sleep(1000);
                            } else {
                                Thread.sleep(45000);
                            }

                            firstTime = false;
                        }
                    }
                } catch (InterruptedException | IOException | JSONException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private double[] callYahoo(String tickerSymbol) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://query1.finance.yahoo.com/v8/finance/chart/" + tickerSymbol + "?range=6h&includePrePost=false&interval=1m")
                .build();

        Response response = client.newCall(request).execute();
        JSONObject jsonMsg = new JSONObject(response.body().string());

        double newPrice = Double.parseDouble(jsonMsg.getJSONObject("chart").getJSONArray("result").getJSONObject(0).getJSONObject("meta").getString("regularMarketPrice"));
        double shares = data.get(tickerSymbol)[0];
        double oldPrice = data.get(tickerSymbol)[1];

        double newValue = shares * newPrice;
        double oldValue = shares * oldPrice;
        double pnl = newValue - oldValue;

        return new double[]{shares, Math.round(newValue*100.0)/100.0, Math.round(pnl*100.0)/100.0, newPrice};
    }

    private void displayInfo(HashMap<String, double[]> displayData){
        this.runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                Integer[] tickerSymbolViews = new Integer[]{R.id.ticker1, R.id.ticker2, R.id.ticker3, R.id.ticker4, R.id.ticker5, R.id.ticker6};
                Integer[] valueViews = new Integer[]{R.id.value1, R.id.value2, R.id.value3, R.id.value4, R.id.value5, R.id.value6};
                Integer[] pnlViews = new Integer[]{R.id.pnl1, R.id.pnl2, R.id.pnl3, R.id.pnl4, R.id.pnl5, R.id.pnl6};
                Integer[] priceViews = new Integer[]{R.id.price1, R.id.price2, R.id.price3, R.id.price4, R.id.price5, R.id.price6};
                Integer[] holdingViews = new Integer[]{R.id.holdings1, R.id.holdings2, R.id.holdings3, R.id.holdings4, R.id.holdings5, R.id.holdings6};

                double total = 0.0;
                int i = 0;
                for (String tickerSymbol : displayData.keySet()){

                    TextView tickerSymbolView = findViewById(tickerSymbolViews[i]);
                    tickerSymbolView.setText(tickerSymbol);

                    TextView holdingsView = findViewById(holdingViews[i]);
                    holdingsView.setText(String.valueOf(displayData.get(tickerSymbol)[0]));

                    TextView valueView = findViewById(valueViews[i]);
                    valueView.setText("$" + String.valueOf(displayData.get(tickerSymbol)[1]));

                    TextView pnlView = findViewById(pnlViews[i]);
                    pnlView.setText("$" + String.valueOf(displayData.get(tickerSymbol)[2]));

                    TextView priceView = findViewById(priceViews[i]);
                    priceView.setText("$" + String.valueOf(Math.round(displayData.get(tickerSymbol)[3]*100.0)/100.0));

                    total += displayData.get(tickerSymbol)[1];

                    i += 1;
                }
                TextView totalView = findViewById(R.id.total);
                totalView.setText("$ " + Math.round(total * 100.0) / 100.0);
            }
        });
    }

    public void onAdd(View v){
        if (v.getId() == R.id.addSymbols) {
            startActivity(new Intent(getApplicationContext(), EnterHoldingsPage.class));
        }
    }
}