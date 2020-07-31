package com.src.webrtc.android.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.src.webrtc.android.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding.apply {
            createRoom.setOnClickListener {
                Log.d(TAG, "on createRoom click")
                joinRoom.isEnabled = false
                if (isPermissionsGranted()) {
                    viewModel.createRoom()
                } else {
                    requestPermissions()
                }
            }

            joinRoom.setOnClickListener {
                Log.d(TAG, "on joinRoom click")
                createRoom.isEnabled = false
                if (isPermissionsGranted()) {
                    viewModel.joinRoom()
                } else {
                    requestPermissions()
                }
            }

            leaveRoom.setOnClickListener {
                Log.d(TAG, "on leaveRoom click")
                viewModel.leaveRoom()
            }
        }

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (verifyPermissions(grantResults)) {
                if (binding.createRoom.isEnabled) {
                    viewModel.createRoom()
                } else {
                    viewModel.joinRoom()
                }
            } else {
                binding.createRoom.isEnabled = true
                binding.joinRoom.isEnabled = true
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun isPermissionsGranted(): Boolean {
        val result = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "isPermissionsGranted: $result")
        return result
    }

    private fun requestPermissions() {
        Log.d(TAG, "requestPermissions")
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            val builder = AlertDialog.Builder(this)
                .setMessage(R.string.connect_permissions_needed)
            val dialog = builder.create()
            dialog.show()
        } else {
            ActivityCompat.requestPermissions(this,
                PERMISSIONS,
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun verifyPermissions(grantResults: IntArray): Boolean {
        if (grantResults.isEmpty()) {
            return false
        }

        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    companion object {
        private const val TAG = "[wa]MainActivity"

        private const val PERMISSION_REQUEST_CODE = 100
        private val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}