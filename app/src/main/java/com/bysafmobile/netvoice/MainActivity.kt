package com.bysafmobile.netvoice

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SimpleAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    val TAG: String = "MainActivity"

    // переменная для текстового поля
    lateinit var requestInput: TextInputEditText

    lateinit var podsAdapter:SimpleAdapter

    lateinit var progress_bar: ProgressBar

    // список с данными для адаптера
    val pods = mutableListOf<HashMap<String, String>>(
        HashMap<String, String>().apply {
            put("title", "title1")
            put("context", "context1")
        },
        HashMap<String, String>().apply {
            put("title", "title2")
            put("context", "context2")
        },
        HashMap<String, String>().apply {
            put("title", "title3")
            put("context", "context3")
        },
        HashMap<String, String>().apply {
            put("title", "title4")
            put("context", "context4")
        }
    )
        @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }
    // подключаем свой созданный toolbar
    fun initViews(){
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        //Установим toolbar в качестве ActionBar  для этого окна действия.
        setSupportActionBar(toolbar)

        requestInput = findViewById(R.id.text_input_edit)

        val podsList: ListView = findViewById(R.id.pods_list)
        podsAdapter = SimpleAdapter(
            applicationContext,
            pods,
            R.layout.item_pod,
            arrayOf("title", "context"),
            intArrayOf(R.id.title, R.id.context)
        )
        podsList.adapter = podsAdapter

        val voiceInputButton: FloatingActionButton = findViewById(R.id.voice_input_button)

        voiceInputButton.setOnClickListener {
            Log.d(TAG, "FAB")
        }
        progress_bar = findViewById(R.id.progress_bar)
    }

    //для отображения меню переопределим методы:
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //будем отрисовывать свой toolbar_menu
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    // при нажатии на элементы toolbar_menu добавим выполнение действий
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_stop ->{
                Log.d(TAG, "action stop")
                return true
            }
            R.id.action_clear ->{
                Log.d(TAG, "action clear")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}