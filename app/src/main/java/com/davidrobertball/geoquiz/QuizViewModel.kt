package com.davidrobertball.geoquiz

import android.util.Log
import androidx.lifecycle.ViewModel

private const val TAG = "QuizViewModel"

// ViewModel will save the current state on rotation
class QuizViewModel : ViewModel() {
    init {
        Log.i(TAG, "init - QuizViewModel instance created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "onCleared - QuizViewModel instance about to be destroyed")
    }

    private val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true)
    )
    private var questionIndex = 0
    private var questionAnswers = arrayOfNulls<Boolean?>(questionBank.size)
    private var score = 0
    private var cheatingRecord = BooleanArray(questionBank.size) {
        false
    }
    private var cheatTokens = 3

    val currentCheatTokens: Int
        get() = cheatTokens

    val isCheater: Boolean
        get() = cheatingRecord[questionIndex]

    val currentQuestionIndex: Int
        get() = questionIndex

    val currentQuestionNumber: Int
        get() = questionIndex + 1

    val currentQuestionCorrectAnswer: Boolean
        get() = questionBank[questionIndex].answer

    val currentQuestionMyAnswer: Boolean?
        get() = questionAnswers[questionIndex]

    val currentQuestionText: Int
        get() = questionBank[questionIndex].textResId

    val currentScore: Int
        get() = score

    val count: Int
        get() = questionBank.size

    fun prev() {
        questionIndex = if (questionIndex - 1 >= 0) questionIndex - 1 else questionBank.size - 1
    }

    fun next() {
        questionIndex = (questionIndex + 1) % questionBank.size
    }

    fun answerQuestion(answer: Boolean): Boolean {
        questionAnswers[questionIndex] = answer
        return answer == questionBank[questionIndex].answer
    }

    fun incrementScore() {
        score++
    }

    fun getQuestionAnswers(): Array<Boolean?> {
        return questionAnswers
    }

    fun setState(prevIndex: Int, prevAnswers: Array<Boolean?>, prevScore: Int) {
        questionIndex = prevIndex
        questionAnswers = prevAnswers
        score = prevScore
    }

    fun cheated() {
        cheatingRecord[questionIndex] = true
        cheatTokens--
    }
}
