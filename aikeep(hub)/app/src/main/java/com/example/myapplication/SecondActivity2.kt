package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SecondActivity2 : AppCompatActivity() {
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second) // 设置布局为 activity_second.xml

        // 找到返回按钮
        val backButton = findViewById<Button>(R.id.btn_back) // 假设你的返回按钮 ID 为 btn_back
        val submitButton = findViewById<Button>(R.id.submit_button)
        val questionInput = findViewById<EditText>(R.id.question_input)
        val resultText = findViewById<TextView>(R.id.result_text)

        // 设置按钮点击监听
        backButton.setOnClickListener {
            finish() // 结束当前活动，返回到 MainActivity
        }
    }
}