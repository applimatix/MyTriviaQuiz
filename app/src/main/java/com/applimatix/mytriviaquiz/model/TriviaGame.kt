package com.applimatix.mytriviaquiz.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.applimatix.mytriviaquiz.api.Question
import com.applimatix.mytriviaquiz.api.TriviaService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

interface TriviaGame {

    val score: LiveData<Int>

    val questions: LiveData<List<Question>>

    val currentState: LiveData<State>

    fun prepareGame()

    fun fetchQuestions(num: Int)

    fun cancelFetch()

    fun checkAnswers()

    enum class State {
        START_UP, PRE_GAME, FETCHING, FETCH_ERROR, IN_GAME, TALLYING_SCORE, GAME_FINISHED
    }
}

class TriviaGameImpl(private val service: TriviaService) : TriviaGame {

    private val _currentState =
        MutableLiveData<TriviaGame.State>().apply { value = TriviaGame.State.START_UP }
    override val currentState: LiveData<TriviaGame.State> = _currentState

    private val _questions = MutableLiveData<List<Question>>()
    override val questions: LiveData<List<Question>> = _questions

    private val _score = MutableLiveData<Int>().apply { value = 0 }
    override val score: LiveData<Int> = _score

    private val transitions: MutableMap<RequestedTransition, AllowedTransition>

    private var disposable: Disposable? = null

    init {

        transitions = hashMapOf()
        transitions[RequestedTransition(TriviaGame.State.START_UP, Command.INIT_GAME)] =
            AllowedTransition(TriviaGame.State.PRE_GAME)
        transitions[RequestedTransition(TriviaGame.State.PRE_GAME, Command.FETCH_DATA)] =
            AllowedTransition(TriviaGame.State.FETCHING)
        transitions[RequestedTransition(TriviaGame.State.FETCHING, Command.SHOW_FETCH_ERROR)] =
            AllowedTransition(TriviaGame.State.FETCH_ERROR)
        transitions[RequestedTransition(TriviaGame.State.FETCHING, Command.START_GAME)] =
            AllowedTransition(TriviaGame.State.IN_GAME)
        transitions[RequestedTransition(TriviaGame.State.FETCH_ERROR, Command.INIT_GAME)] =
            AllowedTransition(TriviaGame.State.PRE_GAME)
        transitions[RequestedTransition(TriviaGame.State.IN_GAME, Command.CHECK_SCORE)] =
            AllowedTransition(TriviaGame.State.TALLYING_SCORE)
        transitions[RequestedTransition(TriviaGame.State.TALLYING_SCORE, Command.END_GAME)] =
            AllowedTransition(TriviaGame.State.GAME_FINISHED)
        transitions[RequestedTransition(TriviaGame.State.GAME_FINISHED, Command.INIT_GAME)] =
            AllowedTransition(TriviaGame.State.PRE_GAME)
    }

    override fun prepareGame() {
        updateState(Command.INIT_GAME)
    }

    override fun fetchQuestions(num: Int) {

        updateState(Command.FETCH_DATA)

        disposable =
            service.fetchQuestions(num)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { trivia ->
                        _questions.value = trivia.questions
                        updateState(Command.START_GAME)
                    },
                    { throwable ->
                        Log.w("fetchQuestions", throwable)
                        updateState(Command.SHOW_FETCH_ERROR)
                    }
                )
    }

    override fun cancelFetch() {
        disposable?.dispose()
    }

    override fun checkAnswers() {
        updateState(Command.CHECK_SCORE)
        _score.value = tallyScore()
        updateState(Command.END_GAME)
    }

    private fun updateState(command: Command) {

        currentState.value?.let { state ->

            val requestedTransition = RequestedTransition(state, command)

            check(transitions.containsKey(requestedTransition)) {
                String.format(
                    "Cannot perform a %s command when in the %s state!!",
                    command.name,
                    state.name
                )
            }

            val allowedTransition = transitions[requestedTransition]

            allowedTransition?.let {
                if (it.newState != state) {
                    _currentState.value = it.newState
                }
            }
        }
    }

    private fun tallyScore(): Int {

        var interimScore = 0

        questions.value?.forEach { question ->
            if (question.isCorrect) {
                interimScore++
            }
        }

        return interimScore
    }

    internal enum class Command {
        FETCH_DATA, INIT_GAME, SHOW_FETCH_ERROR, START_GAME, CHECK_SCORE, END_GAME
    }

    internal data class AllowedTransition constructor(val newState: TriviaGame.State)

    internal data class RequestedTransition(
        val currentState: TriviaGame.State,
        val command: Command
    )
}
