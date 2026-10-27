package com.semenenko.healthdiary.util

/** When to ask the user to rate the app. */
object RatingPolicy {
    const val FIRST_ASK = 10
    const val REPEAT_EVERY = 50

    /** Developer mailbox for ratings 1-3. */
    const val DEV_EMAIL = "vl.profits@gmail.com"

    /** Opens 10, 60, 110, ... (the dialog is shown only until the user has rated). */
    fun isDue(launches: Int): Boolean =
        launches >= FIRST_ASK && (launches - FIRST_ASK) % REPEAT_EVERY == 0

    /** 4 and 5 stars go to the store page, 1-3 stars become a letter to the developer. */
    fun goesToStore(stars: Int): Boolean = stars >= 4
}
