package com.example.android.politicalpreparedness.election.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("dateText")
fun dateTimeToString(textView: TextView, date: Date?) {
    if (date != null) {
        textView.text = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).format(date)
    }
}
