package com.example.hack.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.compose.runtime.MutableState
import com.example.hack.AppState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.core.content.edit

private val auth = Firebase.auth

fun registeredUser(email: String, passwd: String,
                   context: Context, appState: MutableState<AppState>) {
    auth.createUserWithEmailAndPassword(email, passwd)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                context.getSharedPreferences("User", MODE_PRIVATE).edit() {
                    this
                        .putBoolean("Registered", true)
                        .putString("Email", email)
                        .putString("Passwd", passwd)
                }
                appState.value = AppState.MENU
            }
            else
                Log.e("Auth", "Login failed", task.exception)
        }
}

fun signInUser(email: String, passwd: String,
               context: Context, appState: MutableState<AppState>) {
    auth.signInWithEmailAndPassword(email, passwd)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                context.getSharedPreferences("User", MODE_PRIVATE).edit() {
                    this
                        .putBoolean("Registered", true)
                        .putString("Email", email)
                        .putString("Passwd", passwd)
                }
                appState.value = AppState.MENU
            }
            else
                Log.e("Auth", "Login failed", task.exception)
        }
}