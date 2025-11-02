package com.example.notifyclient

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notifyclient.databinding.ActivityTopicsBinding
import kotlinx.coroutines.launch
import java.io.IOException

class TopicsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicsBinding

    private val MY_PHONE_NUMBER = "+40785910180"

    private val apiService = RetrofitClient.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchTopics()

        binding.listViewTopics.setOnItemClickListener { parent, view, position, id ->
            val topicName = parent.getItemAtPosition(position) as String
            subscribeToTopic(topicName)
        }
    }

    private fun fetchTopics() {
        lifecycleScope.launch {
            try {
                val response = apiService.getTopics()

                val adapter = ArrayAdapter(
                    this@TopicsActivity,
                    android.R.layout.simple_list_item_1,
                    response.topics
                )
                binding.listViewTopics.adapter = adapter
            } catch (e: IOException) {
                Log.e("TopicsActivity", "Network error", e)
                Toast.makeText(this@TopicsActivity, "Network error", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("TopicsActivity", "Error fetching topics", e)
            }
        }
    }

    private fun subscribeToTopic(topicName: String) {
        lifecycleScope.launch {
            val requestBody = SubscribeRequest(phone = MY_PHONE_NUMBER)
            try {
                val response = apiService.subscribe(topicName, requestBody)
                Log.i("TopicsActivity", "Subscribed to $topicName")
                Toast.makeText(
                    this@TopicsActivity,
                    "Subscribed to $topicName",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("TopicsActivity", "Error subscribing", e)
                Toast.makeText(this@TopicsActivity, "Subscription failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}