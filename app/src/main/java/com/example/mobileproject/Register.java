package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class Register extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonReg;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        // 获取当前用户
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // 如果当前用户不为空并且邮箱已经验证过，则直接跳转到主界面
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化 Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 初始化 UI 元素
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        // 点击“登录现在”文字跳转到登录页面
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        // 注册按钮的点击事件
        buttonReg.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Register.this, "put email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Register.this, "please put correct address", Toast.LENGTH_SHORT).show();
                return;
            }

            // 检查邮箱域名是否在允许列表
            List<String> allowedDomains = Arrays.asList("@gmail.com", "@yahoo.com", "@outlook.com","northeastern.edu");
            boolean isAllowedDomain = false;
            for (String domain : allowedDomains) {
                if (email.endsWith(domain)) {
                    isAllowedDomain = true;
                    break;
                }
            }

            if (!isAllowedDomain) {
                Toast.makeText(Register.this,
                        "email address not valid, please pick one address blow：" + allowedDomains.toString(),
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Register.this, "password is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(Register.this, "password should longer than 6", Toast.LENGTH_SHORT).show();
                return;
            }

            // 显示进度条
            progressBar.setVisibility(View.VISIBLE);

            // 使用Firebase创建用户
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // 创建完成后隐藏进度条
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                // 用户创建成功
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // send verification email
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(Register.this,
                                                                "Account created, please verify your account through email",
                                                                Toast.LENGTH_LONG).show();
                                                        // 发送成功后，回到登录界面让用户验证后再登录
                                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(Register.this,
                                                                "Failed to send verification, please try again later",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            } else {
                                // 创建用户失败，提示错误信息
                                Toast.makeText(Register.this,
                                        "Fail to register：" + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        // 为主布局设置系统窗口（状态栏、导航栏）边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
