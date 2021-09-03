package com.example.crbug1242167

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.appcompat.app.AppCompatActivity

class KillProcessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pid = intent.getIntExtra(EXTRA_PID, 0)
        Process.killProcess(pid)
        val restartIntent = Intent(Intent.ACTION_MAIN)
        restartIntent.setPackage(packageName)
        restartIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        applicationContext.startActivity(restartIntent)
        finish()
        Process.killProcess(Process.myPid())
    }

    companion object {

        private const val EXTRA_PID = "KillProcessActivity.extra.PID"

        fun createIntent(context: Context) =
            Intent(context, KillProcessActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(EXTRA_PID, Process.myPid())
            }
    }
}
