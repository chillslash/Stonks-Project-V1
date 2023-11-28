package stonksproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser currentUser = mAuth.getCurrentUser();
    EditText email;
    EditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        email = (EditText) findViewById(R.id.enter_email);
        password = (EditText) findViewById(R.id.enter_password);

    }

    public void clickSignup(View v) {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();
        if (userEmail.length() != 0 && userPassword.length() != 0) {
            // Signup an account using email and password from EditTexts
            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign up success
                                Toast.makeText(getApplicationContext(), "Signup Successful.",
                                        Toast.LENGTH_SHORT).show();


                                startActivity(new Intent(getApplicationContext(), StockNews.class));

                            } else {
                                // If sign up fails, display a message to the user.
                                Toast.makeText(getApplicationContext(), "Signup failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Signup Failed. Try Again Later.",
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });
        } else {
            Toast.makeText(this, "ERROR: Email and Password cannot be empty.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void clickForgot(View v) {
        // Directs the user to the ForgotPassword activity
        Intent forgotActivity = new Intent(this, ForgotPasswordActivity.class);
        startActivity(forgotActivity);
    }

    public void clickLogin(View v) {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();
        if (userEmail.length() != 0 && userPassword.length() != 0) {

            mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign up success
                                Toast.makeText(getApplicationContext(), "Login Successful.",
                                        Toast.LENGTH_SHORT).show();
                                // Bring user to success activvity
                                Intent successActivity = new Intent(getApplicationContext(),
                                        SuccessActivity.class);
                                startActivity(successActivity);
                                startActivity(new Intent(getApplicationContext(), StockNews.class));
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(getApplicationContext(), "Login Failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Login Failed, Try Again Later.",
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });
        } else {
            Toast.makeText(this, "ERROR: Email and Password cannot be empty.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}


