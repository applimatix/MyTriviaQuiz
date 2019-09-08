package com.applimatix.mytriviaquiz.view

import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.applimatix.mytriviaquiz.R
import com.applimatix.mytriviaquiz.api.Question
import com.applimatix.mytriviaquiz.databinding.ItemQuestionBinding


class QuestionAdapter(private val questions: List<Question>) :
    RecyclerView.Adapter<QuestionViewHolder>() {

    private var showAnswers = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemQuestionBinding.inflate(layoutInflater, parent, false)
        return QuestionViewHolder(binding);
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        holder.bind(question, showAnswers)
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    fun showAnswers() {
        showAnswers = true
        notifyDataSetChanged()
    }
}

class QuestionViewHolder(val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(question: Question, showAnswers: Boolean) {

        binding.model = question
        binding.executePendingBindings()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.questionTxt.text = Html.fromHtml(question.question, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            binding.questionTxt.text = Html.fromHtml(question.question)
        }

        binding.answersGrp.removeAllViews()

        val answers = question.answers

        var answerId = 0

        for (answer in answers) {

            if (TextUtils.isEmpty(answer)) {
                continue
            }

            val answerBtn = RadioButton(itemView.context)

            answerBtn.id = answerId++
            answerBtn.isEnabled = !showAnswers
            answerBtn.isChecked = answer == question.answer

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                answerBtn.text = Html.fromHtml(answer, Html.FROM_HTML_MODE_LEGACY)
            } else {
                @Suppress("DEPRECATION")
                answerBtn.text = Html.fromHtml(answer)
            }
            // Store original answer string as well.
            answerBtn.tag = answer

            if (showAnswers && answer == question.correctAnswer) {
                if (question.isCorrect) {
                    answerBtn.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorPositive))
                } else {
                    answerBtn.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorNegative))
                }
            }

            answerBtn.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    question.answer = compoundButton.tag.toString()
                }
            }

            binding.answersGrp.addView(answerBtn)
        }
    }
}
