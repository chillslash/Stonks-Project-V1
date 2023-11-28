package stonksproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        email = (EditText) findViewById(R.id.editText3);
    }

    public void clickCancel(View v) {
    // Go to the previous page
        onBackPressed();
    }

    public void clickSend(View v) {
        String userEmail = email.getText().toString();

        if (userEmail.length() != 0) {
            mAuth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Email send success
                                Toast.makeText(getApplicationContext(), "Email sent successfully.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Email send fails
                                Toast.makeText(getApplicationContext(), "Error sending email.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "ERROR: Email and Password cannot be empty.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}