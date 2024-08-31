package com.yr.restfulapidemo

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.yr.restfulapidemo.databinding.ActivityMainBinding
import com.yr.restfulapidemo.network.TodoItemApi
import com.yr.restfulapidemo.network.TodoPostItem
import com.yr.restfulapidemo.view.base.BaseActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 儲存使用者輸入的基礎 URL
        val defaultBaseUrl = "http://192.168.0.12:8080/"
        var newBaseUrl = sharedPreferences.getString("base_url", defaultBaseUrl) ?: defaultBaseUrl
        sharedPreferences.edit().putString("base_url", newBaseUrl).apply()

        // 取得 Retrofit 服務，使用新的基礎 URL
        val todoItemApiService = TodoItemApi.getRetrofitService(newBaseUrl)

        binding.apply {
            etBaseUrl.setText(newBaseUrl)
            etBaseUrl.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    newBaseUrl = s.toString()
                    sharedPreferences.edit().putString("base_url", newBaseUrl).apply()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // GET
            btnGet.setOnClickListener {
                lifecycleScope.launch {
                    getInputId()?.let { id ->
                        val todoItem = todoItemApiService.getTodoItemById(id)
                        val resultMsg = todoItem?.let {
                            """
                                ID: ${todoItem.id}
                                Name: ${todoItem.name}
                                Task: ${todoItem.task}
                                Status: ${todoItem.status}
                                Update Time: ${todoItem.updateTime}
                            """.trimIndent()
                        } ?: "null"

                        showResultAlert(resultMsg)
                    }
                }
            }

            // POST
            btnPost.setOnClickListener {
                lifecycleScope.launch {
                    val todoItem = todoItemApiService.addTodoItem(
                        TodoPostItem(
                            name = "Android add",
                            task = "POST POST",
                            status = 0,
                            updateTime = getCurrentTime()
                        )
                    )
                    val resultMsg = todoItem?.let {
                        """
                                ID: ${todoItem.id}
                                Name: ${todoItem.name}
                                Task: ${todoItem.task}
                                Status: ${todoItem.status}
                                Update Time: ${todoItem.updateTime}
                            """.trimIndent()
                    } ?: "null"

                    showResultAlert(resultMsg)
                }
            }

            // Put
            btnPut.setOnClickListener {
                lifecycleScope.launch {
                    getInputId()?.let { id ->
                        todoItemApiService.updateTodoItem(
                            id = id,
                            todoItem = TodoPostItem(
                                name = "Android update",
                                task = "Put Put",
                                status = 1,
                                updateTime = getCurrentTime()
                            )
                        )

                        showResultAlert("Put $id 已送出")
                    }
                }
            }

            // Delete
            btnDelete.setOnClickListener {
                lifecycleScope.launch {
                    getInputId()?.let { id ->
                        todoItemApiService.deleteTodoItem(id)

                        showResultAlert("Delete $id 已送出")
                    }
                }
            }
        }
    }

    private fun showResultAlert(msg: String) {
        AlertDialog.Builder(this@MainActivity)
            .setTitle("Todo Item Details")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = formatter.format(Date())
        return currentTime //輸出範例：2023-11-16 15:30:45
    }

    private fun getInputId(): Int? {
        val inputId = binding.etItemId.text.toString().toIntOrNull()

        if (inputId == null) {
            showResultAlert("輸入框(ID) 不可為空")
        }

        return inputId
    }
}