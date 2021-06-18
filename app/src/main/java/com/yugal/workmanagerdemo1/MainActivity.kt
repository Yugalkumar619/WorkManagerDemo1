package com.yugal.workmanagerdemo1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.work.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object{
        const val KEY_COUNT_VALUE = "key_count"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener(){
            setOneTimeWorkRequest()
        }
    }

    private fun setOneTimeWorkRequest(){
        val workManager = WorkManager.getInstance(applicationContext)

        val data: Data = Data.Builder()
            .putInt(KEY_COUNT_VALUE,125)
            .build()
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
        .build()

        val filteringRequest = OneTimeWorkRequest.Builder(FilteringWorker::class.java)
            .build()
        val compressingResult = OneTimeWorkRequest.Builder(CompressingWorker::class.java)
            .build()
        val downloadingWorkers = OneTimeWorkRequest.Builder(DownloadingWorker::class.java)
            .build()

        val parallelWorkers = mutableListOf<OneTimeWorkRequest>()
        parallelWorkers.add(downloadingWorkers)
        parallelWorkers.add(filteringRequest)

        workManager
            .beginWith(parallelWorkers)
            .then(compressingResult)
            .then(uploadRequest)
        .enqueue()

        workManager.getWorkInfoByIdLiveData(uploadRequest.id)
            .observe(this, Observer {
                textView.text = it.state.name
                if(it.state.isFinished){
                    val data: Data = it.outputData
                    val message = data.getString(UploadWorker.KEY_WORK)
                    Toast.makeText(applicationContext, message.toString(),Toast.LENGTH_LONG).show()

                }
            })
    }
}