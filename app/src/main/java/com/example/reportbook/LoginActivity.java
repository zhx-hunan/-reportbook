package com.example.reportbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reportbook.util.EdgeToEdgeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * 登录页：演示 Activity 跳转、表单校验与 SharedPreferences。
 */
public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "report_book_prefs";
    public static final String KEY_USERNAME = "username";

    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private CheckBox cbRemember;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeUtils.setup(this);
        setContentView(R.layout.activity_login);

        EdgeToEdgeUtils.applyTopInset(findViewById(R.id.headerContent));
        EdgeToEdgeUtils.applyImeAndNavBottomInset(findViewById(R.id.loginRoot));

        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        cbRemember = findViewById(R.id.cbRemember);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String rememberedName = preferences.getString(KEY_USERNAME, "");
        if (!TextUtils.isEmpty(rememberedName)) {
            etUsername.setText(rememberedName);
            etUsername.setSelection(rememberedName.length());
        }

        btnLogin.setOnClickListener(v -> attemptLogin());
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    private void attemptLogin() {
        usernameLayout.setError(null);
        passwordLayout.setError(null);

        String username = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError(getString(R.string.login_username_empty));
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.login_password_empty));
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 4) {
            passwordLayout.setError(getString(R.string.login_password_short));
            etPassword.requestFocus();
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        if (cbRemember.isChecked()) {
            editor.putString(KEY_USERNAME, username);
        } else {
            editor.remove(KEY_USERNAME);
        }
        editor.apply();

        Toast.makeText(this, getString(R.string.login_success, username), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_USERNAME, username);
        startActivity(intent);
        finish();
    }
}
