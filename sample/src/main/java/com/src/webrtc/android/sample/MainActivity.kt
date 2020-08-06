package com.src.webrtc.android.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.src.webrtc.android.LocalVideoTrack
import com.src.webrtc.android.RemoteVideoTrack
import com.src.webrtc.android.VideoRenderer
import com.src.webrtc.android.sample.databinding.ActivityMainBinding
import org.webrtc.RendererCommon

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private val mainViewRenderer = VideoRenderer()
    private val subViewRenderer = VideoRenderer()

    private var localVideoTrack: LocalVideoTrack? = null
    private var remoteVideoTrack: RemoteVideoTrack? = null

    private var isSwapped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory.getInstance(application)
        ).get(MainViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this

        binding.apply {
            options.visibility = View.GONE

            connectRoom.setOnClickListener {
                Log.d(TAG, "on createRoom click")
                if (roomName.text.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.connect_room_name_needed,
                        Toast.LENGTH_SHORT
                    )
                } else {
                    if (isPermissionsGranted()) {
                        connectRoom.isEnabled = false
                        roomName.isEnabled = false
                        this@MainActivity.viewModel.connectToRoom(binding.roomName.text.toString())
                    } else {
                        requestPermissions()
                    }
                }
            }

            hangUp.setOnClickListener {
                Log.d(TAG, "on hangUp click")
                this@MainActivity.viewModel.leaveRoom()
                connectRoom.isEnabled = true
                roomName.isEnabled = true
            }

//            micToggle.setOnClickListener {
//                Log.d(TAG, "on micToggle click")
//                viewModel.toggleMic()
//            }
//
//            cameraToggle.setOnClickListener {
//                Log.d(TAG, "on cameraToggle click")
//                viewModel.toggleCamera()
//            }
//
//            cameraSwitch.setOnClickListener {
//                Log.d(TAG, "on cameraSwitch click")
//                viewModel.switchCamera()
//            }
        }

        viewModel.room.observe(this, Observer {
            Log.d(TAG, "observe room $it")
            if (it != null) {
                binding.options.visibility = View.VISIBLE
                binding.mainView.apply {
                    init(it.eglBase.eglBaseContext, null)
                    setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                    setEnableHardwareScaler(false)
                    mainViewRenderer.setTarget(this)
                }
                binding.subView.apply {
                    init(it.eglBase.eglBaseContext, null)
                    setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                    setZOrderMediaOverlay(true)
                    setEnableHardwareScaler(true)
                    subViewRenderer.setTarget(this)
                    binding.subView.visibility = View.GONE
                }
            } else {
                isSwapped = false
                binding.options.visibility = View.GONE
                mainViewRenderer.setTarget(null)
                subViewRenderer.setTarget(null)
                binding.mainView.clearImage()
                binding.subView.clearImage()
                Handler().postDelayed({
                    binding.mainView.release()
                    binding.subView.release()
                }, 100)
            }
        })
        viewModel.localPeer.observe(this, Observer {
            Log.d(TAG, "observe localPeer $it")
            if (it != null) {
                localVideoTrack = it.getVideoTracks()["camera"]
                localVideoTrack?.addRenderer(mainViewRenderer)
            } else {
                localVideoTrack?.removeRenderer(mainViewRenderer)
            }
        })
        viewModel.remotePeerEvent.observe(this, EventObserver {
            if (it.second) {
                Toast.makeText(this, getString(R.string.remote_peer_connected, it.first), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.remote_peer_disconnected, it.first), Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.remoteVideoTrack.observe(this, Observer {
            if (it != null) {
                remoteVideoTrack = it
                swapRenderers(true)
            } else {
                swapRenderers(false)
            }
        })

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        localVideoTrack?.removeRenderer(mainViewRenderer)
        remoteVideoTrack?.removeRenderer(subViewRenderer)

        mainViewRenderer.setTarget(null)
        subViewRenderer.setTarget(null)
        binding.mainView.release()
        binding.subView.release()

        super.onDestroy()
    }

    private fun swapRenderers(isSwapped: Boolean) {
        if (this.isSwapped != isSwapped) {
            this.isSwapped = isSwapped
            if (isSwapped) {
                localVideoTrack?.removeRenderer(mainViewRenderer)
                localVideoTrack?.addRenderer(subViewRenderer)
                remoteVideoTrack?.addRenderer(mainViewRenderer)
                binding.subView.setMirror(true)
                binding.mainView.setMirror(false)
                binding.subView.visibility = View.VISIBLE
            } else {
                localVideoTrack?.removeRenderer(subViewRenderer)
                localVideoTrack?.addRenderer(mainViewRenderer)
                remoteVideoTrack?.removeRenderer(mainViewRenderer)
                binding.subView.setMirror(false)
                binding.mainView.setMirror(true)
                binding.subView.visibility = View.GONE
            }
        }
    }


    //region Runtime Permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (verifyPermissions(grantResults)) {
                binding.connectRoom.isEnabled = false
                binding.roomName.isEnabled = false
                viewModel.connectToRoom(binding.roomName.text.toString())
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun isPermissionsGranted(): Boolean {
        val result = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "isPermissionsGranted: $result")
        return result
    }

    private fun requestPermissions() {
        Log.d(TAG, "requestPermissions")
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
            || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            val builder = AlertDialog.Builder(this)
                .setMessage(R.string.connect_permissions_needed)
            val dialog = builder.create()
            dialog.show()
        } else {
            ActivityCompat.requestPermissions(
                this,
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
    //endregion

    companion object {
        private const val TAG = "[rtc]MainActivity"

        private const val PERMISSION_REQUEST_CODE = 100
        private val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}