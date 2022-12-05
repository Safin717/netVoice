package com.bysafmobile.netvoice

import android.annotation.SuppressLint
import android.app.ProgressDialog.show
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SimpleAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.wolfram.alpha.WAEngine
import com.wolfram.alpha.WAPlainText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {
    val TAG: String = "MainActivity"
    lateinit var waEngine: WAEngine

    // переменная для текстового поля
    lateinit var requestInput: TextInputEditText

    lateinit var podsAdapter:SimpleAdapter

    lateinit var progress_bar: ProgressBar

    // список с данными для адаптера
    val pods = mutableListOf<HashMap<String, String>>()

    lateinit var textToSpeech: TextToSpeech

    var isTtsReady: Boolean = false

    val VOICE_RECOGNITION_REQUEST_CODE: Int = 777

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        wolframEngine()
        initTts()
    }
    // подключаем свой созданный toolbar
    fun initViews(){
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        //Установим toolbar в качестве ActionBar  для этого окна действия.
        setSupportActionBar(toolbar)

        requestInput = findViewById(R.id.text_input_edit)
        requestInput.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE){
                pods.clear()
                podsAdapter.notifyDataSetChanged()

                val question = requestInput.text.toString()
                askWolfram(question)
            }
            return@setOnEditorActionListener false
        }

        val podsList: ListView = findViewById(R.id.pods_list)
        podsAdapter = SimpleAdapter(
            applicationContext,
            pods,
            R.layout.item_pod,
            arrayOf("Title", "Content"),
            intArrayOf(R.id.title, R.id.context)
        )
        podsList.adapter = podsAdapter

        podsList.setOnItemClickListener { parent, view, position, id ->
            if(isTtsReady){
                val title = pods[position]["Title"]
                val content = pods[position]["Content"]
                textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, title)
            }
        }

        val voiceInputButton: FloatingActionButton = findViewById(R.id.voice_input_button)

        voiceInputButton.setOnClickListener {
            pods.clear()
            podsAdapter.notifyDataSetChanged()
            if(isTtsReady){
                textToSpeech.stop()
            }
            showVoiceInputDialog()
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
                if(isTtsReady){
                    textToSpeech.stop()
                }
                return true
            }
            R.id.action_clear ->{
                requestInput.text?.clear()
                pods.clear()
                podsAdapter.notifyDataSetChanged()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun wolframEngine(){
        waEngine = WAEngine().apply {
            appID = "KWPK6P-XPKWLP6VP8"
            addFormat("plaintext")
        }
    }
    fun showSnackBar(message:String){
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(android.R.string.ok){
                dismiss()
            }
            show()
        }
    }
    fun askWolfram(request:String){
        //отображаем progress_bar
        progress_bar.visibility = View.VISIBLE
        // запускаем корутину на второстепенном потоке
        CoroutineScope(Dispatchers.IO).launch {
            // создаем запрос
            val query = waEngine.createQuery().apply { input = request }
            runCatching {
                waEngine.performQuery(query)
            }.onSuccess { result ->
                // возвращаем результат на основной поток
                withContext(Dispatchers.Main){
                    progress_bar.visibility = View.GONE
                    // если результат с ошибкой, то выведем в сообщении SnackBar
                    if (result.isError){
                        showSnackBar(result.errorMessage)
                        return@withContext
                    }
                    // неизвестные ошибки: некорректный ввод пользователя
                    if(!result.isSuccess){
                        requestInput.error = getString(R.string.error_do_not_understand)
                        return@withContext
                    }

                    for (pod in result.pods){
                        if(pod.isError) continue
                        val content = StringBuilder()
                        for (subpod in pod.subpods){
                            for(element in subpod.contents){
                                if(element is WAPlainText){
                                    content.append(element.text)
                                }
                            }
                        }
                        pods.add(0, HashMap<String, String>().apply {
                            put("Title", pod.title)
                            put("Content", content.toString())
                        })
                    }
                    podsAdapter.notifyDataSetChanged()
                }
            }.onFailure { t ->
                // возвращаем результат на основной поток
                withContext(Dispatchers.Main){
                    progress_bar.visibility = View.GONE
                    // выводим сообщение SnackBar об ошибке
                    showSnackBar(t.message ?: getString(R.string.error_something_went_wrong))
                }
            }
        }
    }

    fun initTts(){
        textToSpeech = TextToSpeech(this) {code ->
            if(code != TextToSpeech.SUCCESS){
                Log.d(TAG, "TTS error message: $code")
                showSnackBar(getString(R.string.error_tts_is_not_ready))
            }
            else{
                isTtsReady = true
            }
        }
        textToSpeech.language = Locale.US
    }
    fun showVoiceInputDialog(){
        // намерение для вызова формы обработки речи
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // сюда он слушает и запоминает
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.request_hint))
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
        }
        runCatching {
            // вызываем активность 
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)

        }.onFailure { t ->
            showSnackBar(t.message ?: getString(R.string.error_voice_recognition_unavailable))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK){
            data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)?.let { question ->
                requestInput.setText(question)
                askWolfram(question)
            }
        }
    }
}