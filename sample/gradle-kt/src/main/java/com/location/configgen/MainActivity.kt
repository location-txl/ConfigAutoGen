package com.location.configgen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.location.configgen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.appLog.text = getString(R.string.show_app_log, FeatureConfig.enableLog)
        binding.showPayPage.text = getString(R.string.show_pay_page, FeatureConfig.showPayPage)
        binding.baseUrl.text = getString(R.string.base_url, NetworkConfig.baseUrl)
        binding.networkDebugLog.text =
            getString(R.string.show_network_debug_log, NetworkConfig.debugLog)
        binding.timeout.text = getString(
            R.string.net_work_timeout,
            NetworkConfig.Timeout.connect,
            NetworkConfig.Timeout.read,
            NetworkConfig.Timeout.write
        )
        FeatureConfig.enableLog
//        SampleConfig.testList
    }
}