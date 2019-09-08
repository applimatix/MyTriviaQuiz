package com.applimatix.mytriviaquiz.view

import android.content.Context
import android.view.View
import android.widget.NumberPicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.applimatix.mytriviaquiz.R
import com.applimatix.mytriviaquiz.api.Question
import com.applimatix.mytriviaquiz.model.TriviaGame

class MainViewModel(private val triviaGame: TriviaGame) : ViewModel() {

    val questions: LiveData<List<Question>> = triviaGame.questions

    val currentState: LiveData<TriviaGame.State> = triviaGame.currentState

    val numQuestions = MutableLiveData<Int>().apply { value = NUM_QUESTIONS_INITAL }
    val numQuestionsMaxValue = NUM_QUESTIONS_MAX
    val numQuestionsMinValue = NUM_QUESTIONS_MIN

    fun numQuestionsPkrOnClick(view: View) {
        val numberPkr: NumberPicker? = view as? NumberPicker
        numberPkr?.let {
            numQuestions.value = it.value
        }
    }

    val numQuestionsGroupVisibility: LiveData<Int> = Transformations.map(currentState) { state ->
        when (state) {
            TriviaGame.State.START_UP,
            TriviaGame.State.PRE_GAME -> View.VISIBLE
            else -> View.GONE
        }
    }

    val scoreTextVisibility: LiveData<Int> = Transformations.map(currentState) { state ->
        when (state) {
            TriviaGame.State.GAME_FINISHED -> View.VISIBLE
            else -> View.GONE
        }
    }

    fun scoreText(context: Context): LiveData<String> = Transformations.map(currentState) { state ->
        if (state == TriviaGame.State.GAME_FINISHED) {
            context.getString(R.string.score, triviaGame.score.value, numQuestions.value)
        } else {
            ""
        }
    }

    val checkAnswersRestartGameBtnVisibility: LiveData<Int> =
        Transformations.map(currentState) { state ->
            when (state) {
                TriviaGame.State.IN_GAME,
                TriviaGame.State.GAME_FINISHED -> View.VISIBLE
                else -> View.GONE
            }
        }

    fun checkAnswersRestartGameBtnLabel(context: Context): LiveData<String> =
        Transformations.map(currentState) { state ->
            when (state) {
                TriviaGame.State.IN_GAME -> context.getString(R.string.check_answers)
                TriviaGame.State.GAME_FINISHED -> context.getString(R.string.restart_game)
                else -> ""
            }
        }

    @Suppress("UNUSED_PARAMETER")
    fun checkAnswersRestartGameBtnOnClick(view: View) = when (currentState.value) {
        TriviaGame.State.IN_GAME -> triviaGame.checkAnswers()
        TriviaGame.State.GAME_FINISHED -> triviaGame.prepareGame()
        else -> {
        }
    }

    val questionListVisibility: LiveData<Int> = Transformations.map(currentState) { state ->
        when (state) {
            TriviaGame.State.IN_GAME,
            TriviaGame.State.GAME_FINISHED -> View.VISIBLE
            else -> View.GONE
        }
    }

    val busyProgressVisibility: LiveData<Int> = Transformations.map(currentState) { state ->
        when (state) {
            TriviaGame.State.FETCHING,
            TriviaGame.State.TALLYING_SCORE -> View.VISIBLE
            else -> View.GONE
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun fetchErrorDialogOnClick(which: Int) {
        triviaGame.prepareGame()
    }

    @Suppress("UNUSED_PARAMETER")
    fun fetchQuestionsOnClick(view: View) {
        triviaGame.fetchQuestions(numQuestions.value ?: NUM_QUESTIONS_INITAL)
    }

    companion object {
        val NUM_QUESTIONS_INITAL = 10
        val NUM_QUESTIONS_MIN = 5
        val NUM_QUESTIONS_MAX = 20
    }
}
