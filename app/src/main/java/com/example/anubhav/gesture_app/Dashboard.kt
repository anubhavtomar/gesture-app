package com.example.anubhav.gesture_app

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Network
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.provider.MediaStore
import android.util.Log
import android.util.Log.d
import android.view.View
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.save_file.*
import okhttp3.OkHttpClient
import java.util.*
import kotlin.concurrent.schedule

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import java.io.*


class Dashboard : AppCompatActivity() {

    var FILE_RECORDING:File? = null
    var mediaRecorder: MediaRecorder? = null
    val PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED
    val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    val PERMISSION_REQUEST_CODE = 100
    val internetPermission = Manifest.permission.INTERNET
    val internetPermissionCode = 101
    val networkStatePermission = Manifest.permission.ACCESS_NETWORK_STATE
    val networkStatePermissionCode = 102

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
//                FILE_RECORDING = File(filesDir, "${sampleName.text}.aac")
                FILE_RECORDING = File("${externalCacheDir.absolutePath}/${sampleName.text}.aac")
                startRecording()
                Timer("SettingUp", false).schedule(5000) {
                    stopRecording()
                    stopChirp()
                    val snack = Snackbar.make(findViewById(R.id.root) , "Upload Start" , Snackbar.LENGTH_LONG).setDuration(80)
                    snack.show()

                    GetJsonWithOkHttpClient(sampleName , File("${externalCacheDir.absolutePath}/${sampleName.text}.aac")).execute();
//                    val client = networkClient()
//                    client.postApi("${sampleName.text}.aac" , File("${externalCacheDir.absolutePath}/${sampleName.text}.aac"))
                }
            } else {

            }
        }

        stopRcrd.setOnClickListener{
            inputDialog.setVisibility(View.GONE)
        }

    }

    open class GetJsonWithOkHttpClient(sampleName: EditText, FILE_RECORDING: File) : AsyncTask<Unit, Unit, String>() {

        var sampleName = sampleName
        var FILE_RECORDING = FILE_RECORDING

        override fun doInBackground(vararg params: Unit?): String? {
            d("testing","----------------caughtDebugger1------------")
            val client = networkClient()
            client.postApi("${sampleName}.aac" , FILE_RECORDING)
            return "Success"
        }
    }

    fun success(response: Response<Cook>) {
        print(response)
    }

    fun failure(response: Throwable) {
        print(response)
    }

    fun isPermissionGranted(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) checkSelfPermission(AUDIO_PERMISSION) == PERMISSION_GRANTED
        else return true
    }

    fun requestAudioPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(arrayOf(AUDIO_PERMISSION), PERMISSION_REQUEST_CODE)
            requestPermissions(arrayOf(internetPermission), internetPermissionCode)
            requestPermissions(arrayOf(networkStatePermission), networkStatePermissionCode)
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
        val snack = Snackbar.make(findViewById(R.id.root) , "Start Recorder on ${FILE_RECORDING!!.path}" , Snackbar.LENGTH_LONG).setDuration(80)
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

class Cook(
    val name : String = "Bob",
    val job : String = "Cook",
    @Transient val age : Int = 44
) : Serializable