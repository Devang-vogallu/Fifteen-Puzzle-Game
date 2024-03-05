package com.ttt.fifteenpuzzlegame

import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.fifteenpuzzlegame.ui.theme.FifteenPuzzleGameTheme
import kotlinx.coroutines.delay

private var buttonState by mutableStateOf(true)

class MainActivity : ComponentActivity() {



    private val viewModel by viewModels<Puzzle15ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.isGameReset.observe(this) { isGameReset ->
            isGameReset?.let {
                setContent {
                    PuzzleApp(viewModel)
                }
            }
        }

        setContent {
            PuzzleApp(viewModel = viewModel)
        }
    }
}

@Composable
fun PuzzleApp(viewModel: Puzzle15ViewModel) {

    var result by remember { mutableStateOf(false) }
    var isGameRunning by remember { mutableStateOf(false) }

    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }

    LaunchedEffect(isGameRunning) {
        while (isGameRunning) {
            if (seconds == 59) {
                minutes++
                seconds = 0
            } else {
                seconds++
            }
            delay(1000)

        }

    }

    FifteenPuzzleGameTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "\uD835\uDFD9\uD835\uDFDD ℙ\uD835\uDD66\uD835\uDD6B\uD835\uDD6B\uD835\uDD5D\uD835\uDD56 ",
                fontSize = 48.sp,
                modifier = Modifier.padding(80.dp, 48.dp, 0.dp, 24.dp),
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(8.dp, 30.dp, 8.dp, 8.dp)
            ) {
                Puzzle15Board(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    cellInfos = viewModel.boardData,
                    shakeState = viewModel.shakeFlow
                ) { cellInfo ->
                    run {
                        result = viewModel.onCellClicked(cellInfo)
                    }
                }

                if (result) {
                    PlayAgainExitPopup(viewModel) {
                        minutes = 0
                        seconds = 0
                        isGameRunning = false
                        buttonState = true
                    }
                }
            }

            println("TTTT TIME PASSED = $minutes   $seconds")

            Column(
                modifier = Modifier
                    .padding(135.dp,20.dp,0.dp,0.dp),
                verticalArrangement = Arrangement.Center,
//                HorizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Time :- ${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
                    style = TextStyle(fontSize = 24.sp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Button(
                    onClick = {
                        minutes = 0
                        seconds = 0
                        isGameRunning = false
                        viewModel.resetGame()
                        buttonState = true

                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Reset")
                }

                Button(
                    enabled = buttonState,
                    onClick = {
                        isGameRunning = true
                        viewModel.startGame()
                        buttonState = false
                    },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text(text = "Start")
                }
            }
        }
    }
}


@Composable
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun BackInvokeHandler(
    handleBackHandler: Boolean,
    priority : Int = OnBackInvokedDispatcher.PRIORITY_DEFAULT,
    callback : () -> Unit = {}
) {
    val backInvokedCallback = remember {
        OnBackInvokedCallback {
            callback()
        }
    }

    val activity = when(LocalLifecycleOwner.current) {
        is MainActivity -> LocalLifecycleOwner.current as MainActivity
//        is Fragment -> (LocalLifecycleOwner.current as Fragment).requireActivity() as MainActivity
        else -> {
            val context = LocalContext.current
            if (context is MainActivity) {
                context
            } else {
                throw IllegalStateException("LocalLifecycleOwner is not MainActivity or Fragment")
            }
        }
    }

    if (handleBackHandler) {
        activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(priority, backInvokedCallback)
    }

    LaunchedEffect(handleBackHandler) {
        if (!handleBackHandler) {
            activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(backInvokedCallback)
        }
    }

    DisposableEffect(activity.lifecycle, activity.onBackInvokedDispatcher) {
        onDispose {
            activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(backInvokedCallback)
        }
    }
}

data class CellInfo(
    val number: Int,
    val row: Int,
    val column: Int,
    var size: Int,
) {
    var offsetState by mutableStateOf(IntOffset.Zero)

    val actualRow: Int
        get() = offsetState.y / size + row

    val actualColumn: Int
        get() = offsetState.x / size + column
}