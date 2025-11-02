package com.example.notifyclient // Make sure this package name matches yours

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notifyclient.databinding.ActivitySubscriptionsBinding
import kotlinx.coroutines.launch

class SubscriptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionsBinding

    private val MY_PHONE_NUMBER = "+40785910180"

    private val apiService = RetrofitClient.instance
    private lateinit var adapter: ArrayAdapter<String>
    private var mySubsList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mySubsList)
        binding.listViewMySubscriptions.adapter = adapter

        fetchMySubscriptions()

        binding.listViewMySubscriptions.setOnItemClickListener { parent, view, position, id ->
            val topicName = parent.getItemAtPosition(position) as String
            unsubscribeFromTopic(topicName)
        }
    }

    private fun fetchMySubscriptions() {
        lifecycleScope.launch {
            try {
                val response = apiService.getMySubscriptions(MY_PHONE_NUMBER)
                mySubsList.clear()
                mySubsList.addAll(response.subscriptions)
                adapter.notifyDataSetChanged() // Tell the list to refresh
            } catch (e: Exception) {
                Log.e("SubscriptionsActivity", "Error fetching subs", e)
                Toast.makeText(this@SubscriptionsActivity, "Error fetching subs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun unsubscribeFromTopic(topicName: String) {
        lifecycleScope.launch {
            val requestBody = SubscribeRequest(phone = MY_PHONE_NUMBER)
            try {
                apiService.unsubscribe(topicName, requestBody)
                Toast.makeText(this@SubscriptionsActivity, "Unsubscribed from $topicName", Toast.LENGTH_SHORT).show()

                fetchMySubscriptions()

            } catch (e: Exception) {
                Log.e("SubscriptionsActivity", "Error unsubscribing", e)
                Toast.makeText(this@SubscriptionsActivity, "Failed to unsubscribe", Toast.LENGTH_SHORT).show()
            }
        }
    }
}