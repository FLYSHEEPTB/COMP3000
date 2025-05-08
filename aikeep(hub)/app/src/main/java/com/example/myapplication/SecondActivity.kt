package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import android.content.res.Configuration
import java.util.Locale

class SecondActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private lateinit var languageToggleButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val backButton = findViewById<Button>(R.id.btn_back)
        val submitButton = findViewById<Button>(R.id.submit_button)
        val questionInput = findViewById<EditText>(R.id.question_input)
        val resultText = findViewById<TextView>(R.id.result_text)
        // 初始化语言切换按钮
        languageToggleButton = findViewById(R.id.btn_language_toggle)

        // 设置点击事件
        languageToggleButton.setOnClickListener {
            toggleLanguage()
        }

        backButton.setOnClickListener {
            finish()
        }

        submitButton.setOnClickListener {
            val inputText = questionInput.text.toString()
            if (inputText.isNotEmpty()) {
                sendRequest(inputText, resultText)
            }
        }
    }

    private fun toggleLanguage() {
        val currentLocale = resources.configuration.locale
        val newLocale = if (currentLocale.language == "zh") Locale.ENGLISH else Locale.SIMPLIFIED_CHINESE

        // 更新语言设置
        val config = Configuration(resources.configuration)
        config.setLocale(newLocale)

        // 应用新语言并刷新界面
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }

    private fun sendRequest(text: String, resultText: TextView) {
        val url = "http://72eg212952.vicp.fun/predict"
        val json = JSONObject()
        json.put("text", text)

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    resultText.text = getString(R.string.language_request_failed)
                    resultText.visibility = TextView.VISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            resultText.text = getString(R.string.language_request_failed) + ": ${response.message}"
                            resultText.visibility = TextView.VISIBLE
                        }
                        return
                    }

                    val responseData = response.body?.string()
                    val jsonResponse = JSONObject(responseData)
                    val predictedClass = jsonResponse.getString("predicted_class")

                    runOnUiThread {
                        // 动态加载字符串资源
                        val resultPrefix = getString(R.string.language_result)
                        resultText.text = "$resultPrefix $predictedClass"
                        resultText.visibility = TextView.VISIBLE
                    }
                }
            }
        })
    }
}
