package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import android.content.res.Configuration
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var questionInput: EditText
    private lateinit var submitButton: Button
    private lateinit var replyContent: TextView
    private lateinit var btnNavigate: Button
    private lateinit var clearHistoryButton: Button
    private lateinit var languageToggleButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        questionInput = findViewById(R.id.question_input)
        submitButton = findViewById(R.id.submit_button)
        replyContent = findViewById(R.id.reply_content)
        btnNavigate = findViewById(R.id.btn_navigate)
        clearHistoryButton = findViewById(R.id.btn_clear_history)
        // 初始化语言切换按钮
        languageToggleButton = findViewById(R.id.btn_language_toggle)

        // 设置点击事件
        languageToggleButton.setOnClickListener {
            toggleLanguage()
        }

        clearHistoryButton.setOnClickListener {
            clearHistory()
        }

        // Set up submit button click listener
        submitButton.setOnClickListener {
            val question = questionInput.text.toString()
            if (question.isNotEmpty()) {
                callApi(question)

                // 延迟 3 秒后清空输入框的内容
                val handler = android.os.Handler(mainLooper)
                handler.postDelayed({
                    questionInput.setText("")
                }, 3000) // 3000 毫秒 = 3 秒

            } else {
                replyContent.text = "请输入你的问题！"
                replyContent.visibility = TextView.VISIBLE
            }
        }

        // Set up navigation button click listener
        btnNavigate.setOnClickListener {
            Log.d("MainActivity", "Button clicked")
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
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

    // 定义清除历史记录的函数
    private fun clearHistory() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://72eg212952.vicp.fun/clear_history") // 替换为后端的清除历史记录接口
            .post(FormBody.Builder().build()) // POST 请求，但无内容
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    replyContent.text = "清除历史记录失败: ${e.message}"
                    replyContent.visibility = TextView.VISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        replyContent.text = "历史记录已清除！"
                        replyContent.visibility = TextView.VISIBLE
                    }
                } else {
                    runOnUiThread {
                        replyContent.text = "清除历史记录失败: ${response.message}"
                        replyContent.visibility = TextView.VISIBLE
                    }
                }
            }
        })
    }

    private fun callApi(question: String) {
        val client = OkHttpClient()

        // Create form body to send the question
        val formBody = FormBody.Builder()
            .add("question", question)
            .build()

        val request = Request.Builder()
            .url("http://72eg212952.vicp.fun/") // Use the correct address
            .post(formBody) // Use POST method
            .build()

        // Send request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MainActivity", "请求失败: ${e.message}")
                runOnUiThread {
                    replyContent.text = "请求失败: ${e.message}"
                    replyContent.visibility = TextView.VISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string() ?: "没有回复"
                    Log.d("API Response", responseData) // 输出HTML内容

                    // 使用Jsoup解析HTML
                    val document = Jsoup.parse(responseData)

                    // 找到所有问题和回复
                    val items = document.select("li") // 定位每个问题-回复块
                    val extractedData = StringBuilder()

                    for (item in items) {
                        val questionElement = item.selectFirst("strong:contains(问题)")
                        val replyElement = item.selectFirst("strong:contains(回复)")

                        val question = questionElement?.nextSibling()?.toString()?.trim() ?: "无问题内容"
                        val reply = replyElement?.nextSibling()?.toString()?.trim() ?: "无回复内容"

                        extractedData.append("问题：$question\n")
                        extractedData.append("回复：$reply\n\n")
                    }

                    // 更新UI线程中的内容
                    runOnUiThread {
                        // 确保文本不为空，避免空白显示
                        val content = extractedData.toString()
                        if (content.isNotEmpty()) {
                            replyContent.text = content
                            replyContent.visibility = TextView.VISIBLE
                        } else {
                            replyContent.text = "没有回复内容"
                            replyContent.visibility = TextView.VISIBLE
                        }

                        // 滚动到最底部
                        val scrollView = findViewById<ScrollView>(R.id.reply_scroll_view)
                        // 更新内容
                        replyContent.text = content
                        scrollView.post {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                        }
                    }
                } else {
                    runOnUiThread {
                        replyContent.text = "请求失败: ${response.message}"
                        replyContent.visibility = TextView.VISIBLE
                    }
                }
            }
        })
    }

}
