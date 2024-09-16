package com.parohy.outwo.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.parohy.outwo.R

@Composable
fun ScreenLoading(alpha: Float = 1f) {
  Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha)), contentAlignment = Alignment.Center) {
    CircularProgressIndicator()
  }
}

@Composable
fun ScreenError(t: Throwable) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Image(painter = painterResource(R.drawable.ic_error), contentDescription = t.message)
      Text(text = t.message ?: "Unknown error", style = MaterialTheme.typography.titleLarge)
    }
  }
}

@Composable
fun Alert(title: String, text: String? = null, onDismiss: () -> Unit = {}, confirm: (() -> Unit)? = null) {
  AlertDialog(
    title = { Text(title) },
    text = { Text(text ?: "") },
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(onClick = confirm ?: onDismiss) {
        Text("Yes")
      }
    },
    dismissButton = {
      if (confirm != null) {
        Button(onClick = onDismiss) {
          Text("No")
        }
      }
    }
  )
}

@Composable
fun alertDialog(
  onDismiss: () -> Unit = {},
  confirm: (() -> Unit)? = null
): (String, String?) -> Unit {
  val isShowing = remember { mutableStateOf(false) }
  val title = remember { mutableStateOf("") }
  val text = remember { mutableStateOf<String?>(null) }
  if (isShowing.value)
    Alert(title = title.value, text = text.value, onDismiss = { onDismiss(); isShowing.value = false }, confirm = confirm?.let { { it(); isShowing.value = false } })

  return { t, m ->
    title.value = t
    text.value = m
    isShowing.value = true
  }
}
