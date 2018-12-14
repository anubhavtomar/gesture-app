package com.example.anubhav.gesture_app

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.provider.MediaStore
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.save_file.*
import java.io.File
import java.util.*
import kotlin.concurrent.schedule

class Dashboard : AppCompatActivity() {

    var FILE_RECORDING:File? = null
    var mediaRecorder: MediaRecorder? = null
    val PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED
    val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    val PERMISSION_REQUEST_CODE = 100

    var chirpMedia: MediaPlayer? = null

    var chirpTimer: Timer? = null
    var recorderTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        requestAudioPermission()

        chirpMedia = MediaPlayer.create(this, R.raw.chirp)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        recordBtn.setOnClickListener {
            inputDialog.setVisibility(View.VISIBLE)
        }

        startRcrd.setOnClickListener{
            if(sampleName.text.toString().length != 0)  {
                val snack = Snackbar.make(findViewById(R.id.root),"Stage 1",Snackbar.LENGTH_LONG).setDuration(80)
                snack.show()
                playChirp()
                FILE_RECORDING = File(filesDir, "${sampleName.text}.aac")
//                FILE_RECORDING = File("${externalCacheDir.absolutePath}/${sampleName.text}.aac")
                startRecording()
                Timer("SettingUp", false).schedule(5000) {
                    stopRecording()
                    stopChirp()
//                    inputDialog.setVisibility(View.INVISIBLE)
                }
            } else {

            }
        }

        stopRcrd.setOnClickListener{
            inputDialog.setVisibility(View.GONE)
        }

    }

    fun isPermissionGranted(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) checkSelfPermission(AUDIO_PERMISSION) == PERMISSION_GRANTED
        else return true
    }

    fun requestAudioPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(arrayOf(AUDIO_PERMISSION), PERMISSION_REQUEST_CODE)
        }
    }

    fun playChirp () {
        chirpTimer = Timer()
        chirpTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                chirpMedia?.start()
            }
        }, 0, 100)
        val snack = Snackbar.make(findViewById(R.id.root) , "Playing Chirp" , Snackbar.LENGTH_LONG).setDuration(80)
        snack.show()
    }

    fun stopChirp () {
        chirpTimer!!.cancel()
        chirpTimer!!.purge()
        chirpTimer = null
        val snack = Snackbar.make(findViewById(R.id.root) , "Stop Chirp" , Snackbar.LENGTH_LONG).setDuration(80)
        snack.show()
    }

    fun startRecording () {
        if(!isPermissionGranted()) {
            requestAudioPermission()
            return
        }
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        mediaRecorder!!.setOutputFile(FILE_RECORDING!!.path)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder!!.prepare()
        mediaRecorder!!.start()
        val snack = Snackbar.make(findViewById(R.id.root) , "Start Recorder on ${FILE_RECORDING!!.path}" , Snackbar.LENGTH_LONG).setDuration(2800)
        snack.show()
    }

    fun stopRecording(){
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        val snack = Snackbar.make(findViewById(R.id.root) , "Stop Recorder" , Snackbar.LENGTH_LONG).setDuration(80)
        snack.show()
    }
}