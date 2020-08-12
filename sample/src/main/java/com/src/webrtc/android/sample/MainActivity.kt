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
import com.src.webrtc.android.*
import com.src.webrtc.android.sample.databinding.ActivityMainBinding
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private val curPeers = mutableListOf<Peer>()
    private val peerViews = mutableListOf<SurfaceViewRenderer>()
    private val viewRenderers =
        listOf(VideoRenderer(), VideoRenderer(), VideoRenderer(), VideoRenderer())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory.getInstance(application)
        ).get(MainViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this

        binding.apply {
            peerViews.add(mainView)
            peerViews.add(subView1)
            peerViews.add(subView2)
            peerViews.add(subView3)
            for (i in viewRenderers.indices) {
                viewRenderers[i].setTarget(peerViews[i])
            }
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
        }

        viewModel.room.observe(this, Observer {
            Log.d(TAG, "observe room $it")
            if (it != null) {
                binding.options.visibility = View.VISIBLE
                binding.mainView.apply {
                    init(it.eglBase.eglBaseContext, null)
                    setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                    setEnableHardwareScaler(false)
                }
                initSubView(it, binding.subView1)
                initSubView(it, binding.subView2)
                initSubView(it, binding.subView3)
            } else {
                binding.options.visibility = View.GONE
                binding.mainView.clearImage()
                binding.subView1.clearImage()
                binding.subView2.clearImage()
                binding.subView3.clearImage()
                Handler().postDelayed({
                    binding.mainView.release()
                    binding.subView1.release()
                    binding.subView2.release()
                    binding.subView3.release()
                }, 100)
            }
        })
        viewModel.peers.observe(this, Observer {
            Log.d(TAG, "observe peers $it")
            handlePeers(it)
        })

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)
    }

    override fun onDestroy() {
        binding.mainView.release()
        binding.subView1.release()
        binding.subView2.release()
        binding.subView3.release()

        super.onDestroy()
    }

    private fun initSubView(room: Room, view: SurfaceViewRenderer) {
        view.apply {
            init(room.eglBase.eglBaseContext, null)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            setZOrderMediaOverlay(true)
            setEnableHardwareScaler(true)
        }
    }

    private fun handlePeers(peers: List<Peer>) {
        for (i in 0 until curPeers.size) {
            if (curPeers[i] != peers.getOrNull(i)) {
                when (val peer = curPeers[i]) {
                    is LocalPeer -> peer.getVideoTracks()["camera"]?.removeRenderer(viewRenderers[i])
                    is RemotePeer -> peer.getVideoTracks()["camera"]?.removeRenderer(viewRenderers[i])
                    else -> {
                        Log.e(TAG, "MUST not happen.")
                    }
                }
            }
        }

        for (i in peers.indices) {
            if (peers[i] != curPeers.getOrNull(i)) {
                when (val peer = peers[i]) {
                    is LocalPeer -> {
                        peer.getVideoTracks()["camera"]?.addRenderer(viewRenderers[i])
                        peerViews[i].setMirror(true)
                    }
                    is RemotePeer -> {
                        peer.getVideoTracks()["camera"]?.addRenderer(viewRenderers[i])
                        peerViews[i].setMirror(false)
                    }
                    else -> {
                        Log.e(TAG, "MUST not happen.")
                    }
                }
            }
        }

        for (i in peerViews.indices) {
            if (i < peers.size) {
                peerViews[i].visibility = View.VISIBLE
            } else {
                peerViews[i].visibility = View.GONE
            }
        }

        curPeers.clear()
        curPeers.addAll(peers)
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