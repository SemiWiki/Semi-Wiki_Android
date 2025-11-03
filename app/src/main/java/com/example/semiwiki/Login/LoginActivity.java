package com.example.semiwiki.Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.semiwiki.Board.BoardActivity;
import com.example.semiwiki.databinding.ActivityLoginBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private static final String PREF = "semiwiki_prefs";
    private static final String KEY_AT = "access_token";
    private static final String KEY_RT = "refresh_token";
    private static final String KEY_ACC_ID = "account_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String existingToken = getSharedPreferences(PREF, MODE_PRIVATE).getString(KEY_AT, null);
        if (existingToken != null && !existingToken.isEmpty()) {
            RetrofitInstance.setAccessToken(existingToken);
            moveToBoard();
            return;
        }

        binding.loginButton.setOnClickListener(v -> {
            String id = binding.loginInputId.getText().toString().trim();
            String pw = binding.loginInputPw.getText().toString().trim();
            if (id.isEmpty()) { binding.loginInputId.setError("아이디를 입력해주세요"); return; }
            if (pw.isEmpty()) { binding.loginInputPw.setError("비밀번호를 입력해주세요"); return; }
            login(id, pw);
        });
    }

    private void moveToBoard() {
        Intent intent = new Intent(LoginActivity.this, BoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void login(String id, String pw) {
        binding.loginButton.setEnabled(false);

        LoginRequest request = new LoginRequest(id, pw);
        AuthService service = RetrofitInstance.getAuthService();
        Call<LoginResponse> call = service.signIn(request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                binding.loginButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    String accessToken  = response.body().getAccessToken();
                    String refreshToken = response.body().getRefreshToken();

                    if (accessToken == null || accessToken.isEmpty()) {
                        Toast.makeText(LoginActivity.this, "로그인 실패: 토큰이 비어 있습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String accountIdFromServer = response.body().getAccountId();
                    String accountIdToSave = (accountIdFromServer != null && !accountIdFromServer.isEmpty())
                            ? accountIdFromServer : id;

                    getSharedPreferences(PREF, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_AT, accessToken)
                            .putString(KEY_RT, refreshToken)
                            .putString(KEY_ACC_ID, accountIdToSave)
                            .putString("user_id", id)
                            .apply();

                    RetrofitInstance.setAccessToken(accessToken);

                    Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                    moveToBoard();
                } else {
                    int code = response.code();
                    String msg;
                    if (code == 403) msg = "비밀번호가 올바르지 않습니다. (403)";
                    else if (code == 404) msg = "존재하지 않는 아이디입니다. (404)";
                    else if (code == 400) msg = "요청 형식 오류입니다. (400)";
                    else msg = "로그인 실패 (" + code + ")";
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                binding.loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
