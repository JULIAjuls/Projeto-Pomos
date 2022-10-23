package com.example.pomos.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pomos.R
import com.example.pomos.database.AppDatabase
import com.example.pomos.databinding.ActivityMainBinding
import com.example.pomos.model.TimerService
import com.example.pomos.viewmodel.MainActivityRecyclerViewAdapter
import com.google.android.material.button.MaterialButton
import kotlin.math.roundToInt

private lateinit var binding: ActivityMainBinding
private var timerStarted = false
private lateinit var serviceIntent: Intent
private var time = 1500.0
private var timeLimit = 1500.0

class MainActivity : AppCompatActivity() {
    var currentDialog = DialogFragment()
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        iniciaDialogMaterialButton(binding.activityMainMaterialbutton1,AddTarefaDialog())
        iniciaDialogImageButton(binding.activityMainImageview2, AddTarefaDialog())
        configuraRecyclerView()

        configuraSetas(binding.activityMainImageview3,true,binding.activityMainTextView1)
        configuraSetas(binding.activityMainImageview4,false,binding.activityMainTextView1)

        binding.activityMainMaterialbutton2.setOnClickListener{
            startStopTimer()
        }
        binding.activityMainMaterialbutton3.setOnClickListener{
            resetTimer()
        }
        serviceIntent = Intent(applicationContext,TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
    }


    private fun startStopTimer() {
        if (timerStarted){
            stopTimer()
        }else{
            startTimer()
        }
    }

    private fun startTimer() {
        serviceIntent.putExtra(TimerService.TIMER_EXTRA, time)
        startService(serviceIntent)
        binding.activityMainMaterialbutton2.text = "Pausar"
        timerStarted = true
        binding.activityMainImageview3.drawable.setTint(getColor(R.color.grey))
        binding.activityMainImageview4.drawable.setTint(getColor(R.color.grey))
    }

    private fun stopTimer() {
        serviceIntent.putExtra(TimerService.TIMER_EXTRA, time)
        stopService(serviceIntent)
        binding.activityMainMaterialbutton2.text = "Iniciar"
        timerStarted = false
        binding.activityMainImageview3.drawable.setTint(getColor(R.color.dark_grey))
        binding.activityMainImageview4.drawable.setTint(getColor(R.color.dark_grey))
        timeLimit = time
    }

    private fun resetTimer() {
        stopTimer()
        time = 1500.0
        timeLimit = time
        binding.activityMainTextView1.text = getTimeStringFromDouble(time)
    }

    private val updateTime : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIMER_EXTRA,1500.0)
            binding.activityMainTextView1.text = getTimeStringFromDouble(time)
        }
    }

    private fun raiseTimer(){
        if (!timerStarted){
            val text = binding.activityMainTextView1.text
            timeLimit++
            time = timeLimit
            binding.activityMainTextView1.text = getTimeStringFromDouble(timeLimit)
        }
    }
    private fun decreaseTimer() {
        if (!timerStarted){
            val text = binding.activityMainTextView1.text
            timeLimit--
            time = timeLimit
            binding.activityMainTextView1.text = getTimeStringFromDouble(timeLimit)
        }
    }

    private fun getTimeStringFromDouble(time: Double): String? {
        val resultInt = time.roundToInt()
        val minutes = resultInt % 3600 / 60
        val seconds = resultInt % 3600 % 60
        return makeTimeString(minutes,seconds)
    }

    private fun makeTimeString(minutes: Int, seconds: Int): String = String.format("%02d:%02d", minutes, seconds)


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        configuraRecyclerView()
    }
    fun configuraRecyclerView(){
        val adapter = MainActivityRecyclerViewAdapter(context = this)
        val db = AppDatabase.instancia(this)
        adapter.refresh(db.funDao().queryAllTarefa())
        val recyclerView = findViewById<RecyclerView>(R.id.activity_main_recyclerview_1 )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    fun iniciaDialogMaterialButton(
        materialButton: MaterialButton,
        dialogFragment: DialogFragment
    ){
        materialButton.setOnClickListener(){
            dialogFragment.show(supportFragmentManager, "CustomFragment")
        }
        currentDialog = dialogFragment
    }
    fun iniciaDialogImageButton(
        imageView: ImageView,
        dialogFragment: DialogFragment
    ){
        imageView.setOnClickListener(){
            dialogFragment.show(supportFragmentManager, "CustomFragment")
        }
        currentDialog = dialogFragment
    }
    private fun configuraSetas(imageView: ImageView, boolean: Boolean, textView: TextView){
        imageView.setOnClickListener{
            if(boolean){
                raiseTimer()
            }else{
                decreaseTimer()
            }
        }
        imageView.setOnLongClickListener(object :
            OnLongClickListener {
            private val mHandler = Handler()
            private val incrementRunnable: Runnable = object : Runnable {
                override fun run() {
                    mHandler.removeCallbacks(this)
                    if (imageView.isPressed()) {

                        if (!timerStarted){
                            if(boolean){
                                timeLimit += 20
                            }else{
                                timeLimit -= 20
                            }
                            time = timeLimit
                            textView.text = getTimeStringFromDouble(timeLimit)
                        }
                        mHandler.postDelayed(this, 100)
                    }
                }
            }
            override fun onLongClick(view: View): Boolean {
                mHandler.postDelayed(incrementRunnable, 0)
                return true
            }
        })
    }
}
