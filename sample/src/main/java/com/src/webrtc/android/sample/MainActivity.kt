package com.src.webrtc.android.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
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

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(application)).get(MainViewModel::class.java)

        binding.apply {
            connectRoom.setOnClickListener {
                Log.d(TAG, "on createRoom click")
                if (roomName.text.isEmpty()) {
                    Toast.makeText(this@MainActivity, R.string.connect_room_name_needed, Toast.LENGTH_SHORT)
                } else {
                    if (isPermissionsGranted()) {
                        connectRoom.isEnabled = false
                        roomName.isEnabled = false
                        leaveRoom.isEnabled = true
                        viewModel.connectToRoom(binding.roomName.text.toString())
                    } else {
                        requestPermissions()
                    }
                }
            }

            leaveRoom.isEnabled = false
            leaveRoom.setOnClickListener {
                Log.d(TAG, "on leaveRoom click")
                viewModel.leaveRoom()
                connectRoom.isEnabled = true
                roomName.isEnabled = true
                leaveRoom.isEnabled = false
            }

            viewModel.setMainView(mainView)
            viewModel.setSubView(subView)
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
                binding.connectRoom.isEnabled = false
                binding.roomName.isEnabled = false
                binding.leaveRoom.isEnabled = true
                viewModel.connectToRoom(binding.roomName.text.toString())
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
        private const val TAG = "[rtc]MainActivity"

        private const val ROOM_NAME = "1d426c42"

        private const val PERMISSION_REQUEST_CODE = 100
        private val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}