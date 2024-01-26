package com.example.android.politicalpreparedness.utils

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.android.politicalpreparedness.R
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("dateText")
fun TextView.bindDateText(date: Date?) {
    val format = SimpleDateFormat("EEEE, MMM. dd, yyyy • HH:mm z", Locale.US)
    text = date?.let { format.format(it) } ?: ""
}

@BindingAdapter("isVisible")
fun View.bindContentVisibility(content: String?) {
    visibility = if (!content.isNullOrEmpty()) View.VISIBLE else View.GONE
}


@BindingAdapter("isVoteText")
fun Button.bindFollowText(isFollow: Boolean) {
    text = context.getString(if (isFollow) R.string.unfollow_button else R.string.follow_button)
}

