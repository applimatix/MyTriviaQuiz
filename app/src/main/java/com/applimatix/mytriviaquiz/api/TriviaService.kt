package com.applimatix.mytriviaquiz.api

import com.squareup.moshi.Json
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

data class Question(
    val category: String,
    val type: Type,
    val difficulty: Difficulty,
    val question: String,
    @Json(name="correct_answer") val correctAnswer: String,
    @Json(name="incorrect_answers") private val incorrectAnswers: List<String>
) {

    @Transient
    var answer: String? = null

    val isCorrect: Boolean
        get() = correctAnswer == answer

    val answers = mutableListOf<String>().apply {
        addAll(incorrectAnswers)
        add(correctAnswer)
    }
        get() = field.apply { shuffle() }

    enum class Type {
        @Json(name="boolean")
        BOOLEAN,
        @Json(name="multiple")
        MULTIPLE
    }

    enum class Difficulty {
        @Json(name="easy")
        EASY,
        @Json(name="medium")
        MEDIUM,
        @Json(name="hard")
        HARD;
    }
}

data class Trivia(@Json(name="response_code") private val responseCode: Int = 0,
                  @Json(name="results") val questions: List<Question>)

interface TriviaService {

    @GET("api.php")
    fun fetchQuestions(@Query("amount") numQuestions: Int): Observable<Trivia>

    companion object {
        fun create(retrofit: Retrofit): TriviaService {
            return retrofit.create(TriviaService::class.java)
        }
    }
}
