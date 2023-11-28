package stonksproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EnterHoldingsPage extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser currentUser = mAuth.getCurrentUser();
    private final FirebaseFirestore firebase = FirebaseFirestore.getInstance();
    private final String uid = currentUser.getUid();

    EditText EtickerSymbol;
    EditText Eshares;
    EditText Eprice;
    String tickerSymbol;
    String shares;
    String price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_holdings_page);

        EtickerSymbol = (EditText) findViewById(R.id.enterTicker);
        Eshares = (EditText) findViewById(R.id.enterShares);
        Eprice = (EditText) findViewById(R.id.enterPriceBought);
    }

    public void onSubmit(View v) {
        if (v.getId() == R.id.submitShares) {
            checkInfo task = new checkInfo();
            task.execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class checkInfo extends AsyncTask<String, Void, String>{

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            try {
                tickerSymbol = EtickerSymbol.getText().toString().toUpperCase();
                shares = Eshares.getText().toString();
                price = Eprice.getText().toString();

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://query1.finance.yahoo.com/v8/finance/chart/" + tickerSymbol)
                        .build();

                Response response = client.newCall(request).execute();
                JSONObject jsonMsg = new JSONObject(response.body().string());
                return jsonMsg.getJSONObject("chart").getString("result");


            } catch (IOException | JSONException e){
                e.printStackTrace();
                return null;
            }
        }
        protected void onPostExecute(String result) {

            if (tickerSymbol.length() == 0 || shares.length() == 0 || price.length() == 0) {
                Toast.makeText(getApplicationContext(), "All fields must be filled",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try{
                Double.parseDouble(shares);
                Double.parseDouble(price);
            } catch (NumberFormatException e){
                Toast.makeText(getApplicationContext(), "Invalid Shares or Price input",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (result.equals("null")) {
                Toast.makeText(getApplicationContext(), "Check Ticker Symbol",
                        Toast.LENGTH_SHORT).show();

            } else{
                checkDuplicate();
            }
        }
    }

    private void checkDuplicate(){

        firebase.collection("portfolio").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()){
                        Set<Map.Entry<String, Object>> rawData = document.getData().entrySet();

                        for (Map.Entry<String, Object> item : rawData){
                            String existingTickerSymbol = item.getKey();
                            double existingShares = Double.parseDouble(item.getValue().toString().split(",")[0]);
                            double existingPrice = Double.parseDouble(item.getValue().toString().split(",")[1]);

                            if (existingTickerSymbol.equals(tickerSymbol)){
                                Double[] data = dollarCostAveraging(existingShares, existingPrice, Double.parseDouble(shares), Double.parseDouble(price));
                                shares = String.valueOf(data[0]);
                                price = String.valueOf(data[1]);
                            }
                        }
                    } else{
                        // Does not exist
                    }
                    save();
                }
                else{
                    // Error handling
                }
            }
        });
    }

    private Double[] dollarCostAveraging(double existingShares, double existingPrice, double shares, double price){
        double totalShare = existingShares + shares;
        double totalPrice = ((existingShares * existingPrice) + (shares * price)) / totalShare;
        return new Double[]{totalShare, totalPrice};
    }

    private void save(){
        Map<String, Object> data = new HashMap<>();
        data.put(tickerSymbol, shares + "," + price);

        firebase.collection("portfolio").document(uid).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Added Successfully",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), PortfolioTracker.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to add. Try again",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}