package com.example.torch.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("NewApi")
@HiltViewModel
class TorchViewModel @Inject constructor(
    @ApplicationContext private val context: Context
): ViewModel() {
    private val _isTorchOn = MutableStateFlow<Boolean>(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn

    private val _brightness = MutableStateFlow<Float>(1f)
    val brightness: StateFlow<Float> = _brightness

    private val _isSosOn = MutableStateFlow<Boolean>(false)
    val isSosOn: StateFlow<Boolean> = _isSosOn

    private val _isStrobeOn = MutableStateFlow<Boolean>(false)
    val isStrobeOn: StateFlow<Boolean> = _isStrobeOn

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId = cameraManager.cameraIdList.firstOrNull()

    private var maxBrightness: Float = 1.0f
    private var job: Job?=null

    init {
        cameraId?.let{ id->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            maxBrightness = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL)?.toFloat() ?: 1.0f
        }
    }

    fun toggleTorch(){
        viewModelScope.launch {
            _isTorchOn.value = !_isTorchOn.value
            updateTorchState()
        }
    }

    fun updateTorchBrightness(value: Float){
        viewModelScope.launch {
            _brightness.value = value
            if (_isTorchOn.value) {
                updateTorchState()
            }
        }
    }

    fun toggleSosMode(enable: Boolean){
        viewModelScope.launch {
            _isSosOn.value = enable
            if (enable) {
//                _isTorchOn.value = true
                startSOSSignal()
            } else {
                stopSignal()
                updateTorchState()
            }
        }
    }

    fun toggleStrobeMode(enable: Boolean){
        viewModelScope.launch {
            _isStrobeOn.value = enable
            if (enable) {
//                _isTorchOn.value = true
                startStrobeSignal()
            } else {
                stopSignal()
                updateTorchState()
            }
        }
    }

    private fun startSOSSignal() {
        job?.cancel()
        job = viewModelScope.launch {
            while (isActive && _isSosOn.value){
                for (i in 1..3) {
                    flashLight(250) // Dot
                    delay(250) // Gap between dots
                }
                delay(500)

                for (i in 1..3) {
                    flashLight(750) // Dash
                    delay(250) // Gap between dashes
                }
                delay(500) // Gap between letters

                // Short signals (S)
                for (i in 1..3) {
                    flashLight(250) // Dot
                    delay(250) // Gap between dots
                }
                delay(1500)
            }
        }
    }



    private fun startStrobeSignal() {
        job?.cancel()
        job = viewModelScope.launch {
            while (isActive && _isStrobeOn.value){
                for (i in 1..3) {
                    flashLight(70) // Dot
                    delay(70) // Gap between dots
                }
            }
        }
    }

    private fun stopSignal() {
        job?.cancel()
        job = null
//        _isTorchOn.value = false
    }

    private suspend fun flashLight(duration: Long) {
        try {
            cameraId?.let { id ->
                cameraManager.turnOnTorchWithStrengthLevel(id, maxBrightness.toInt())
                delay(duration)
                cameraManager.setTorchMode(id, false)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun updateTorchState(){
        try {
            cameraId?.let { id->
                if(_isTorchOn.value){
                    val actualBrightness = (_brightness.value * maxBrightness).toInt()
                    Log.d("TAG", "updateTorchState: $actualBrightness")
                    if(actualBrightness!=0) {
                        try {
                            cameraManager.turnOnTorchWithStrengthLevel(id, actualBrightness)
                        } catch (e: CameraAccessException) {
                            cameraManager.setTorchMode(id, true)
                        }
                    }
                    else{
                        cameraManager.turnOnTorchWithStrengthLevel(id, 1)
                    }
                }else{
                    cameraManager.setTorchMode(id, false)
                }
            }
        } catch (e: CameraAccessException){
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSignal()
        viewModelScope.launch {
            try {
                cameraId?.let { id ->
                    cameraManager.setTorchMode(id, false)
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }
}