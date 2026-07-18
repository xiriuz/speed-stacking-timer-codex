package com.xiriuz.speedstackingtimer

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiriuz.speedstackingtimer.core.DualTouchTracker
import com.xiriuz.speedstackingtimer.core.HandPad
import com.xiriuz.speedstackingtimer.core.StackingTimerController
import com.xiriuz.speedstackingtimer.core.SystemNanoClock
import com.xiriuz.speedstackingtimer.core.TimerPhase
import com.xiriuz.speedstackingtimer.core.TimerSnapshot
import com.xiriuz.speedstackingtimer.presentation.TimerFormatter
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            SpeedStackingTheme {
                TimerApp()
            }
        }
    }
}

@Composable
private fun TimerApp() {
    val controller = remember { StackingTimerController(SystemNanoClock) }
    var snapshot by remember { mutableStateOf(controller.snapshot()) }

    LaunchedEffect(controller) {
        while (isActive) {
            withFrameNanos {
                controller.tick()
                snapshot = controller.snapshot()
            }
        }
    }

    TimerScreen(
        snapshot = snapshot,
        onHandsChanged = { left, right ->
            controller.onHandsChanged(left, right)
            snapshot = controller.snapshot()
        },
    )
}

@Composable
private fun TimerScreen(
    snapshot: TimerSnapshot,
    onHandsChanged: (leftPressed: Boolean, rightPressed: Boolean) -> Unit,
) {
    val signal = signalFor(snapshot.phase)
    val animatedSignal by animateColorAsState(
        targetValue = signal,
        animationSpec = tween(durationMillis = 120),
        label = "signal",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "스피드-스태깅-타이머 (codex)",
            color = MutedText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(animatedSignal.copy(alpha = 0.18f))
                .border(5.dp, animatedSignal, CircleShape)
                .semantics { contentDescription = instructionFor(snapshot.phase) },
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = TimerFormatter.format(snapshot.elapsedMillis),
            color = PrimaryText,
            fontSize = if (snapshot.elapsedMillis < 60_000L) 64.sp else 52.sp,
            lineHeight = 68.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )

        Text(
            text = instructionFor(snapshot.phase),
            modifier = Modifier.padding(top = 12.dp),
            color = animatedSignal,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = "두 패드를 동시에 터치하세요",
            modifier = Modifier.padding(bottom = 12.dp),
            color = MutedText,
            fontSize = 14.sp,
        )

        HandTouchArea(
            leftPressed = snapshot.leftPressed,
            rightPressed = snapshot.rightPressed,
            onHandsChanged = onHandsChanged,
        )
    }
}

@Composable
private fun HandTouchArea(
    leftPressed: Boolean,
    rightPressed: Boolean,
    onHandsChanged: (Boolean, Boolean) -> Unit,
) {
    val tracker = remember { DualTouchTracker() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .pointerInput(Unit) {
                try {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            event.changes.forEach { change ->
                                if (!change.pressed) {
                                    tracker.removePointer(change.id.value)
                                } else {
                                    val isInside = change.position.x >= 0f &&
                                        change.position.x <= size.width.toFloat() &&
                                        change.position.y >= 0f &&
                                        change.position.y <= size.height.toFloat()
                                    val pad = if (!isInside) {
                                        null
                                    } else if (change.position.x < size.width / 2f) {
                                        HandPad.LEFT
                                    } else {
                                        HandPad.RIGHT
                                    }
                                    tracker.updatePointer(change.id.value, pad)
                                }
                                change.consume()
                            }
                            onHandsChanged(tracker.leftPressed, tracker.rightPressed)
                        }
                    }
                } finally {
                    tracker.clear()
                    onHandsChanged(false, false)
                }
            },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HandPadView(
            label = "왼손",
            pressed = leftPressed,
            modifier = Modifier.weight(1f),
        )
        HandPadView(
            label = "오른손",
            pressed = rightPressed,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HandPadView(label: String, pressed: Boolean, modifier: Modifier = Modifier) {
    val padColor by animateColorAsState(
        targetValue = if (pressed) GreenSignal else PadBackground,
        animationSpec = tween(90),
        label = "$label pad",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(padColor.copy(alpha = if (pressed) 0.34f else 1f))
            .border(
                width = if (pressed) 4.dp else 2.dp,
                color = if (pressed) GreenSignal else PadBorder,
                shape = RoundedCornerShape(28.dp),
            )
            .semantics { contentDescription = "$label 터치 패드" },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (pressed) "●" else "○",
                color = if (pressed) GreenSignal else MutedText,
                fontSize = 42.sp,
            )
            Text(
                text = label,
                color = if (pressed) PrimaryText else MutedText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun signalFor(phase: TimerPhase): Color = when (phase) {
    TimerPhase.HOLDING_GREEN -> GreenSignal
    TimerPhase.ARMED_RED -> RedSignal
    TimerPhase.RUNNING -> RunningSignal
    TimerPhase.STOPPED -> GreenSignal
    TimerPhase.IDLE -> NeutralSignal
}

private fun instructionFor(phase: TimerPhase): String = when (phase) {
    TimerPhase.IDLE -> "양손을 패드에 올리세요"
    TimerPhase.HOLDING_GREEN -> "초록불 · 그대로 유지하세요"
    TimerPhase.ARMED_RED -> "빨간불 · 양손을 떼세요"
    TimerPhase.RUNNING -> "측정 중 · 양손으로 멈추세요"
    TimerPhase.STOPPED -> "기록 완료 · 양손을 떼세요"
}

@Composable
private fun SpeedStackingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = AppBackground,
            surface = AppBackground,
            primary = GreenSignal,
        ),
        content = content,
    )
}

private val AppBackground = Color(0xFF090B10)
private val PadBackground = Color(0xFF151922)
private val PadBorder = Color(0xFF303746)
private val PrimaryText = Color(0xFFF7F8FA)
private val MutedText = Color(0xFF99A1B3)
private val NeutralSignal = Color(0xFF616A7C)
private val GreenSignal = Color(0xFF36E58A)
private val RedSignal = Color(0xFFFF4D61)
private val RunningSignal = Color(0xFFFFC857)
