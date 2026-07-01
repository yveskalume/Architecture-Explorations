package dev.yveskalume.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.yveskalume.newsapp.ui.navigation.NewsNavHost
import dev.yveskalume.newsapp.ui.theme.NewsAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleApp()
        }
    }
}

@Composable
private fun SampleApp() {
    NewsAppTheme {
        NewsNavHost()
    }
}

@Preview
@Composable
private fun SampleAppPreview() {
    SampleApp()
}
