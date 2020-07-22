package com.davidrobertball.geoquiz

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

// val is immutable
// var is mutable

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val KEY_ANSWERS = "answers"
private const val KEY_SCORE = "score"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {
    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var prevButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var questionTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var resultImageView: ImageView
    private lateinit var cheatButton: Button
    private lateinit var cheatText: TextView
    private lateinit var infoButton: ImageButton
    private lateinit var snackBar: Snackbar

    // store all presentation logic in a VM
    // calculation and assignment will not happen until first use
    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    // this is where we will get data returned from a child activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult() called")
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHEAT) {
            val isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
            if (isCheater) quizViewModel.cheated()
            updateCheatTokens()
        }
    }

    /* Lifecycle Methods - in the order that they are called */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate() called")
        setContentView(R.layout.activity_main)

        savedInstanceState?.let { bundle ->
            val prevIndex = bundle.getInt(KEY_INDEX, 0)
            val prevScore = bundle.getInt(KEY_SCORE, 0)
            val prevAnswers = arrayOfNulls<Boolean?>(quizViewModel.count)
            val str = bundle.getString(KEY_ANSWERS, "")
            Log.d(TAG, "onCreate() - $str")
            var ind = 0
            str.split(",").forEach {
                when (it) {
                    "true" -> {
                        prevAnswers[ind] = true
                    }
                    "false" -> {
                        prevAnswers[ind] = false
                    }
                    "null" -> {
                        prevAnswers[ind] = null
                    }
                }
                ind++
            }
            quizViewModel.setState(prevIndex, prevAnswers, prevScore)
        }

        trueButton = findViewById(R.id.trueButton)
        falseButton = findViewById(R.id.falseButton)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)
        questionTextView = findViewById(R.id.questionText)
        scoreTextView = findViewById(R.id.scoreText)
        resultImageView = findViewById(R.id.resultImage)
        cheatButton = findViewById(R.id.cheatButton)
        cheatText = findViewById(R.id.cheatText)
        infoButton = findViewById(R.id.infoButton)

        quizViewModel.currentQuestionMyAnswer?.let {
            disableButtons(it)
        } ?: run {
            defaultButtonStyles()
        }
        updateQuestion()
        updateScore(false)
        updateCheatTokens()

        trueButton.setOnClickListener {
            checkAnswer(true)
        }

        falseButton.setOnClickListener {
            checkAnswer(false)
        }

        prevButton.setOnClickListener {
            quizViewModel.prev()
            updateQuestion()
        }

        nextButton.setOnClickListener {
            quizViewModel.next()
            updateQuestion()
        }

        cheatButton.setOnClickListener {
            val intent = CheatActivity.newIntent(
                this@MainActivity,
                quizViewModel.currentQuestionCorrectAnswer
            )
            // primitive way to target features not supported in all versions of android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val options = ActivityOptions.makeClipRevealAnimation(it, 0, 0, it.width, it.height)
                startActivityForResult(intent, REQUEST_CODE_CHEAT, options.toBundle())
            } else {
                startActivityForResult(intent, REQUEST_CODE_CHEAT)
            }
        }

        snackBar = Snackbar.make(
            findViewById(R.id.mainContainer),
            R.string.info,
            Snackbar.LENGTH_INDEFINITE
        )
        snackBar.setAction(R.string.dismiss) {
            snackBar.dismiss()
        }

        infoButton.setOnClickListener {
            if (snackBar.isShown) {
                snackBar.dismiss()
            } else {
                snackBar.show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart() called")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // could put logic for restoring previous state here or in the onCreate function
        super.onRestoreInstanceState(savedInstanceState)
        Log.i(TAG, "onRestoreInstanceState() called")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume() called")
    }

    override fun onPause() {
        Log.i(TAG, "onPause() called")
        super.onPause()
    }

    override fun onStop() {
        Log.i(TAG, "onStop() called")
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.i(TAG, "onSaveInstanceState() called")
        outState.putInt(KEY_INDEX, quizViewModel.currentQuestionIndex)
        outState.putInt(KEY_SCORE, quizViewModel.currentScore)
        val arr = quizViewModel.getQuestionAnswers()
        var str = ""
        arr.forEach {
            str += "${it.toString()},"
        }
        outState.putString(KEY_ANSWERS, str)
        Log.d(TAG, "onSaveInstanceState() - $str")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy() called")
        super.onDestroy()
    }

    /* Functions */
    private fun checkAnswer(userAnswer: Boolean) {
        disableButtons(userAnswer)
        val correct = quizViewModel.answerQuestion(userAnswer)
        updateScore(correct)
        updateBackground(correct)
        // if the user cheats, we will judge them
        val messageResId =
            when {
                quizViewModel.isCheater -> R.string.judgment
                correct -> R.string.correct
                else -> R.string.incorrect
            }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
    }

    private fun updateQuestion() {
        val questionText =
            resources.getString(
                quizViewModel.currentQuestionText,
                quizViewModel.currentQuestionNumber
            )
        questionTextView.text = questionText
        val answer = quizViewModel.currentQuestionMyAnswer
        answer?.let {
            disableButtons(it)
            updateBackground(it == quizViewModel.currentQuestionCorrectAnswer)
        } ?: run {
            enableButtons()
            updateBackground()
        }
    }

    private fun updateScore(increment: Boolean) {
        if (increment) quizViewModel.incrementScore()
        val scoreText =
            resources.getString(
                R.string.score_text,
                quizViewModel.currentScore,
                quizViewModel.count
            )
        scoreTextView.text = scoreText
    }

    private fun updateCheatTokens() {
        val ct =
            resources.getString(
                R.string.cheat_text,
                quizViewModel.currentCheatTokens
            )
        cheatText.text = ct
        if (quizViewModel.currentCheatTokens == 0) cheatButton.isEnabled = false
    }

    private fun disableButtons(userAnswer: Boolean) {
        trueButton.isEnabled = false
        trueButton.isClickable = false
        falseButton.isEnabled = false
        falseButton.isClickable = false
        // style buttons based on user answer
        if (userAnswer) {
            trueButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            trueButton.setBackgroundColor(ContextCompat.getColor(this, R.color.trueColour))
            falseButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            falseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        } else {
            trueButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            trueButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            falseButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            falseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.falseColour))
        }
    }

    private fun enableButtons() {
        trueButton.isEnabled = true
        trueButton.isClickable = true
        falseButton.isEnabled = true
        falseButton.isClickable = true
        defaultButtonStyles()
    }

    private fun defaultButtonStyles() {
        // default style for buttons
        trueButton.setTextColor(ContextCompat.getColor(this, R.color.trueColour))
        trueButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        falseButton.setTextColor(ContextCompat.getColor(this, R.color.falseColour))
        falseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun updateBackground(correct: Boolean? = null) {
        // set background image based on whether or not the users answer was correct
        correct?.let {
            if (it) {
                resultImageView.setImageResource(R.drawable.ic_check_circle_green_24dp)
                resultImageView.contentDescription = resources.getString(R.string.correct)
            } else {
                resultImageView.setImageResource(R.drawable.ic_cancel_red_24dp)
                resultImageView.contentDescription = resources.getString(R.string.incorrect)
            }
        } ?: run {
            resultImageView.setImageDrawable(null)
            resultImageView.contentDescription = resources.getString(R.string.result_image)
        }
    }
}
