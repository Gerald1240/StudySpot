package com.example.studyspotucsm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    TextView bienvenidoLabel, continuarLabel, nuevoUsuario;
    ImageView loginImageView;
    TextInputLayout usuarioTextField, contrasenaTextField;
    MaterialButton inicioSesion;
    TextInputEditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;

    SignInButton signInButton; // Asegúrate de tener la referencia correcta a este botón si es personalizado
    GoogleSignInClient mGoogleSignInClient;

    private PublicClientApplication msalApp;
    private final CountDownLatch msalInitializationLatch = new CountDownLatch(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginImageView = findViewById(R.id.loginImageView);
        bienvenidoLabel = findViewById(R.id.bienvenidoLabel);
        continuarLabel = findViewById(R.id.continuarLabel);
        usuarioTextField = findViewById(R.id.usuarioTextField);
        contrasenaTextField = findViewById(R.id.contrasenaTextField);
        inicioSesion = findViewById(R.id.inicioSesion);
        nuevoUsuario = findViewById(R.id.nuevoUsuario);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        mAuth = FirebaseAuth.getInstance();

        nuevoUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        inicioSesion.setOnClickListener(v -> validate());

        // Configurar el botón de inicio de sesión con Google
        signInButton = findViewById(R.id.loginGoogle);
        signInButton.setOnClickListener(v -> googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent()));

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Inicialización de MSAL en un hilo secundario
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                msalApp = (PublicClientApplication) PublicClientApplication.createMultipleAccountPublicClientApplication(
                        getApplicationContext(),
                        R.raw.auth_config);
                Log.d(TAG, "MSAL initialized successfully");
            } catch (MsalException | InterruptedException e) {
                Log.e(TAG, "Error initializing MSAL: " + e.getMessage(), e);
            } finally {
                msalInitializationLatch.countDown(); // Señala que MSAL ha sido inicializado incluso si hay un error
            }
        });

        MaterialButton microsoftSignInButton = findViewById(R.id.loginMicrosoft);
        microsoftSignInButton.setOnClickListener(v -> signInWithMicrosoft());
    }

    private void signInWithMicrosoft() {
        new Thread(() -> {
            try {
                msalInitializationLatch.await(); // Espera hasta que MSAL se haya inicializado
                if (msalApp != null) {
                    runOnUiThread(() -> msalApp.acquireToken(LoginActivity.this, new String[]{"User.Read"}, getAuthInteractiveCallback()));
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "MSAL not initialized yet", Toast.LENGTH_SHORT).show());
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Error waiting for MSAL initialization", e);
                runOnUiThread(() -> Toast.makeText(this, "Error waiting for MSAL initialization", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                String accessToken = authenticationResult.getAccessToken();
                Log.d(TAG, "MSAL authentication success, token: " + accessToken);
                firebaseAuthWithMicrosoft(accessToken);
            }

            @Override
            public void onError(MsalException exception) {
                Log.e(TAG, "Authentication failed: " + exception.toString(), exception);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User cancelled login.");
            }
        };
    }

    private void firebaseAuthWithMicrosoft(String accessToken) {
        AuthCredential credential = OAuthProvider.newCredentialBuilder("microsoft.com")
                .setAccessToken(accessToken)
                .build();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "Firebase authentication with Microsoft failed", task.getException());
                        Toast.makeText(LoginActivity.this, "Fallo en la autenticación con Microsoft", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.e(TAG, "Google sign-in failed", e);
                        Toast.makeText(LoginActivity.this, "Fallo con los Servicios de Google", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "Firebase authentication with Google failed", task.getException());
                        Toast.makeText(LoginActivity.this, "Fallo en iniciar sesión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void validate() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Correo inválido");
            return;
        }
        emailEditText.setError(null);

        if (password.isEmpty() || password.length() < 8) {
            passwordEditText.setError("Se necesitan más de 8 caracteres");
            return;
        } else if (!Pattern.compile("[0-9]").matcher(password).find()) {
            passwordEditText.setError("Use al menos un número");
            return;
        }
        passwordEditText.setError(null);
        iniciarSesion(email, password);
    }

    public void iniciarSesion(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "Firebase email/password authentication failed", task.getException());
                        Toast.makeText(LoginActivity.this, "Credenciales equivocadas, intenta de nuevo", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
