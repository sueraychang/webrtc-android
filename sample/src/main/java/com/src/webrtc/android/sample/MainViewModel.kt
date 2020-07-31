package com.src.webrtc.android.sample

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel : ViewModel() {

    init {
        Log.d(TAG, "init")
    }

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
    }

    companion object {
        private const val TAG = "[wa]MainViewModel"
    }
}