package com.applimatix.mytriviaquiz.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.applimatix.mytriviaquiz.R
import com.applimatix.mytriviaquiz.databinding.ActivityMainBinding
import com.applimatix.mytriviaquiz.model.TriviaGame
import com.applimatix.mytriviaquiz.util.observe
import kotlinx.android.synthetic.main.activity_main.numQuestionsPkr
import kotlinx.android.synthetic.main.activity_main.questionList
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private var questionAdapter: QuestionAdapter? = null

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.model = mainViewModel

        val layoutManager = LinearLayoutManager(this)
        questionList.layoutManager = layoutManager

        // TODO see if can be changed to BindingAdapter
        numQuestionsPkr.maxValue = mainViewModel.numQuestionsMaxValue
        numQuestionsPkr.minValue = mainViewModel.numQuestionsMinValue
        numQuestionsPkr.value = mainViewModel.numQuestions.value ?: 0
        mainViewModel.numQuestions.observe(this) {
            numQuestionsPkr.value = it
        }

        mainViewModel.questions.observe(this) { questions ->
            questionAdapter = QuestionAdapter(questions)
            questionList.adapter = questionAdapter
        }

        mainViewModel.currentState.observe(this) { state ->
            onStateChanged(state)
        }
    }

    private fun onStateChanged(newState: TriviaGame.State) {
        when (newState) {
            TriviaGame.State.FETCH_ERROR -> showFetchError()
            TriviaGame.State.GAME_FINISHED -> questionAdapter?.showAnswers()
            else -> {}
        }
    }

    private fun showFetchError() {
        CustomDialogFragment.show(getString(R.string.fetch_error_title),
            getString(R.string.fetch_error_message),
            getString(android.R.string.ok),
            null,
            supportFragmentManager,
            mainViewModel::fetchErrorDialogOnClick)
    }
}
