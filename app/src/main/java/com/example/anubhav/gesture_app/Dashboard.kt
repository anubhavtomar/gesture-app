package com.example.anubhav.gesture_app

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log.d
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.save_file.*
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

import java.io.*


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

        collectBtn.setOnClickListener {
            outputText.setText("")
            inputDialog.setVisibility(View.VISIBLE)
        }

        startRcrd.setOnClickListener{
            outputText.setText("")
            if(sampleName.text.toString().length != 0)  {
                val snack = Snackbar.make(findViewById(R.id.root),"Stage 1",Snackbar.LENGTH_LONG).setDuration(80)
                snack.show()
                playChirp()
                val fileName = "${sampleName.text}.aac$"
                FILE_RECORDING = File("${externalCacheDir.absolutePath}/$fileName")
                startRecording()
                inputDialog.setVisibility(View.INVISIBLE)
                loader.setVisibility(View.VISIBLE)
                Timer("SettingUp", false).schedule(5000) {
                    stopRecording()
                    stopChirp()
                    val snack = Snackbar.make(findViewById(R.id.root) , "Upload Start" , Snackbar.LENGTH_LONG).setDuration(80)
                    snack.show()
                    fileUploader(findViewById(R.id.loader), findViewById(R.id.root), fileName, File("${externalCacheDir.absolutePath}/${fileName}")).execute(); }
                    sampleName.setText("")
            } else {
                val snack = Snackbar.make(findViewById(R.id.root) , "Sample name is important" , Snackbar.LENGTH_LONG).setDuration(80)
                snack.show()
            }
        }

        detectBtn.setOnClickListener{
            outputText.setText("")
            val snack = Snackbar.make(findViewById(R.id.root),"Stage 1",Snackbar.LENGTH_LONG).setDuration(80)
            snack.show()
            playChirp()
            val fileName = "detect.aac$"
            FILE_RECORDING = File("${externalCacheDir.absolutePath}/${fileName}")
            startRecording()
            loader.setVisibility(View.VISIBLE)
            Timer("SettingUp", false).schedule(5000) {
                stopRecording()
                stopChirp()
                val snack = Snackbar.make(findViewById(R.id.root) , "Detection Start" , Snackbar.LENGTH_LONG).setDuration(80)
                snack.show()
                detectGesture(outputText, findViewById(R.id.loader), findViewById(R.id.root), fileName , File("${externalCacheDir.absolutePath}/${fileName}")).execute(); }
        }

        stopRcrd.setOnClickListener{
            inputDialog.setVisibility(View.INVISIBLE)
        }

    }

    open class fileUploader(viewForloader: View, viewForSnackbar: View, sampleName: String, audioFile: File) : AsyncTask<Unit, Unit, String>() {

        var sampleName = sampleName
        var audioFile = audioFile
        var viewForSnackbar = viewForSnackbar
        var viewForloader = viewForloader

        override fun doInBackground(vararg params: Unit?): String {
            d("testing","----------------caughtDebugger1------------")
            val client = networkClient()
            var response = client.postApi("http://172.24.21.173:5000/audio-uploader", "$sampleName" , audioFile)
            viewForloader.setVisibility(View.INVISIBLE)
            response = BufferedInputStream(response)
            return readStream(response)
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            d("testing","----------------caughtDebugger2------------")
            val response = JSONObject(result)
            if(response["success"] == true) {
                val snack = Snackbar.make(viewForSnackbar , "Upload Successful" , Snackbar.LENGTH_LONG).setDuration(80)
                snack.show()
            } else {
                val snack = Snackbar.make(viewForSnackbar , "Something went wrong in Uploader" , Snackbar.LENGTH_LONG).setDuration(80)
                snack.show()
            }
        }

        fun readStream(inputStream: BufferedInputStream): String {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            bufferedReader.forEachLine { stringBuilder.append(it) }
            return stringBuilder.toString()
        }
    }

    open class detectGesture(viewForOutput: TextView, viewForloader: View, viewForSnackbar: View, sampleName: String, audioFile: File) : AsyncTask<Unit, Unit, String>() {

        var sampleName = sampleName
        var audioFile = audioFile
        var viewForSnackbar = viewForSnackbar
        var viewForloader = viewForloader
        var viewForOutput = viewForOutput

        override fun doInBackground(vararg params: Unit?): String {
            d("testing","----------------caughtDebugger1------------")
            val client = networkClient()
            var response = client.postApi("http://172.24.21.173:5000/predict", "$sampleName" , audioFile)
            viewForloader.setVisibility(View.INVISIBLE)
            response = BufferedInputStream(response)
            return readStream(response)
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            d("testing","----------------caughtDebugger2------------")
            val response = JSONObject(result)
            if(response["success"] == true) {
                viewForOutput.setText("${response["class"]}")
                val snack = Snackbar.make(viewForSnackbar , "Successful" , Snackbar.LENGTH_LONG).setDuration(80)
                snack.show()
            } else {
                val snack = Snackbar.make(viewForSnackbar , "Something went wrong in Detection" , Snackbar.LENGTH_LONG).setDuration(80)
                snack.show()
            }


        }

        fun readStream(inputStream: BufferedInputStream): String {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            bufferedReader.forEachLine { stringBuilder.append(it) }
            return stringBuilder.toString()
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
        val snack = Snackbar.make(findViewById(R.id.root) , "Start Recorder" , Snackbar.LENGTH_LONG).setDuration(80)
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