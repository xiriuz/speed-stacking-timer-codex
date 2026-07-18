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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiriuz.speedstackingtimer.core.BestRecordTracker
import com.xiriuz.speedstackingtimer.core.DualTouchTracker
import com.xiriuz.speedstackingtimer.core.HandPad
import com.xiriuz.speedstackingtimer.core.StackingTimerController
import com.xiriuz.speedstackingtimer.core.SystemNanoClock
import com.xiriuz.speedstackingtimer.core.TimerPhase
import com.xiriuz.speedstackingtimer.core.TimerSnapshot
import com.xiriuz.speedstackingtimer.data.SharedPreferencesBestRecordStore
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
    val context = LocalContext.current
    val controller = remember { StackingTimerController(SystemNanoClock) }
    val bestRecordTracker = remember {
        BestRecordTracker(SharedPreferencesBestRecordStore(context.applicationContext))
    }
    var snapshot by remember { mutableStateOf(controller.snapshot()) }
    var bestMillis by remember { mutableStateOf(bestRecordTracker.bestMillis) }

    LaunchedEffect(controller) {
        while (isActive) {
            withFrameNanos {
                controller.tick()
                snapshot = controller.snapshot()
            }
        }
    }

    LandscapeTimerScreen(
        snapshot = snapshot,
        bestMillis = bestMillis,
        onHandsChanged = { left, right ->
            val previousPhase = snapshot.phase
            controller.onHandsChanged(left, right)
            val nextSnapshot = controller.snapshot()
            if (previousPhase != TimerPhase.STOPPED && nextSnapshot.phase == TimerPhase.STOPPED) {
                bestRecordTracker.record(nextSnapshot.elapsedMillis)
                bestMillis = bestRecordTracker.bestMillis
            }
            snapshot = nextSnapshot
        },
        onReset = {
            controller.reset()
            bestRecordTracker.reset()
            bestMillis = null
            snapshot = controller.snapshot()
        },
    )
}

@Composable
private fun LandscapeTimerScreen(
    snapshot: TimerSnapshot,
    bestMillis: Long?,
    onHandsChanged: (leftPressed: Boolean, rightPressed: Boolean) -> Unit,
    onReset: () -> Unit,
) {
    val signalColor = signalFor(snapshot.phase)
    val animatedSignal by animateColorAsState(
        targetValue = signalColor,
        animationSpec = tween(durationMillis = 140),
        label = "signal color",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundTop, BackgroundBottom),
                ),
            )
            .drawBehind {
                drawCircle(
                    color = animatedSignal.copy(alpha = 0.07f),
                    radius = size.minDimension * 0.72f,
                    center = Offset(size.width / 2f, size.height / 2f),
                )
                drawCircle(
                    color = GreenSignal.copy(alpha = 0.035f),
                    radius = size.height * 0.65f,
                    center = Offset(0f, size.height / 2f),
                )
                drawCircle(
                    color = GreenSignal.copy(alpha = 0.035f),
                    radius = size.height * 0.65f,
                    center = Offset(size.width, size.height / 2f),
                )
            },
    ) {
        LandscapeTouchLayout(
            snapshot = snapshot,
            onHandsChanged = onHandsChanged,
        ) {
            CenterConsole(
                snapshot = snapshot,
                bestMillis = bestMillis,
                signalColor = animatedSignal,
                onReset = onReset,
            )
        }
    }
}

@Composable
private fun LandscapeTouchLayout(
    snapshot: TimerSnapshot,
    onHandsChanged: (Boolean, Boolean) -> Unit,
    centerContent: @Composable () -> Unit,
) {
    val tracker = remember { DualTouchTracker() }
    val padWidth = 176.dp

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .edgePadPointerInput(padWidth, tracker, onHandsChanged),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EdgeHandPad(
            label = "LEFT",
            koreanLabel = "왼손",
            pressed = snapshot.leftPressed,
            modifier = Modifier.width(padWidth),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center,
        ) {
            centerContent()
        }

        EdgeHandPad(
            label = "RIGHT",
            koreanLabel = "오른손",
            pressed = snapshot.rightPressed,
            modifier = Modifier.width(padWidth),
        )
    }
}

private fun Modifier.edgePadPointerInput(
    padWidth: Dp,
    tracker: DualTouchTracker,
    onHandsChanged: (Boolean, Boolean) -> Unit,
): Modifier = pointerInput(padWidth) {
    val padWidthPixels = padWidth.toPx()
    try {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                event.changes.forEach { change ->
                    if (!change.pressed) {
                        tracker.removePointer(change.id.value)
                    } else {
                        val pad = when {
                            change.position.x in 0f..padWidthPixels -> HandPad.LEFT
                            change.position.x in (size.width - padWidthPixels)..size.width.toFloat() -> {
                                HandPad.RIGHT
                            }
                            else -> null
                        }
                        tracker.updatePointer(change.id.value, pad)
                    }
                    if (change.position.x <= padWidthPixels ||
                        change.position.x >= size.width - padWidthPixels
                    ) {
                        change.consume()
                    }
                }
                onHandsChanged(tracker.leftPressed, tracker.rightPressed)
            }
        }
    } finally {
        tracker.clear()
        onHandsChanged(false, false)
    }
}

@Composable
private fun EdgeHandPad(
    label: String,
    koreanLabel: String,
    pressed: Boolean,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (pressed) GreenSignal else PadBorder,
        animationSpec = tween(100),
        label = "$label border",
    )
    val fillColor by animateColorAsState(
        targetValue = if (pressed) GreenSignal.copy(alpha = 0.22f) else PadSurface,
        animationSpec = tween(100),
        label = "$label fill",
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(34.dp))
            .background(
                Brush.verticalGradient(
                    listOf(fillColor.copy(alpha = 0.92f), fillColor),
                ),
            )
            .border(
                width = if (pressed) 4.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(34.dp),
            )
            .drawBehind {
                if (pressed) {
                    drawCircle(
                        color = GreenSignal.copy(alpha = 0.13f),
                        radius = size.width * 0.72f,
                        center = center,
                    )
                }
            }
            .semantics { contentDescription = "$koreanLabel 터치 패드" },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(if (pressed) GreenSignal.copy(alpha = 0.24f) else InnerPad)
                    .border(2.dp, borderColor.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (pressed) "●" else "◎",
                    color = if (pressed) GreenSignal else MutedText,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = koreanLabel,
                color = if (pressed) PrimaryText else SoftText,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = label,
                modifier = Modifier.padding(top = 5.dp),
                color = if (pressed) GreenSignal else MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
        }
    }
}

@Composable
private fun CenterConsole(
    snapshot: TimerSnapshot,
    bestMillis: Long?,
    signalColor: Color,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "SPEED STACKING TIMER",
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.4.sp,
        )

        Spacer(Modifier.height(9.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(signalColor)
                    .drawBehind {
                        drawCircle(signalColor.copy(alpha = 0.25f), radius = size.width * 1.3f)
                    },
            )
            Text(
                text = instructionFor(snapshot.phase),
                color = signalColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }

        Text(
            text = TimerFormatter.format(snapshot.elapsedMillis),
            modifier = Modifier.padding(top = 2.dp),
            color = PrimaryText,
            fontSize = if (snapshot.elapsedMillis < 60_000L) 66.sp else 52.sp,
            lineHeight = 68.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )

        Row(
            modifier = Modifier.padding(top = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = GoldSignal.copy(alpha = 0.10f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    GoldSignal.copy(alpha = 0.42f),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "★", color = GoldSignal, fontSize = 15.sp)
                    Column {
                        Text(
                            text = "최고기록",
                            color = MutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = bestMillis?.let(TimerFormatter::format) ?: "--.---",
                            color = GoldSignal,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onReset,
                enabled = snapshot.phase == TimerPhase.IDLE || snapshot.phase == TimerPhase.STOPPED,
                shape = RoundedCornerShape(14.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = Brush.linearGradient(listOf(PadBorder, SoftText)),
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SoftText,
                    disabledContentColor = MutedText.copy(alpha = 0.35f),
                ),
            ) {
                Text(
                    text = "기록 리셋",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun signalFor(phase: TimerPhase): Color = when (phase) {
    TimerPhase.HOLDING_GREEN -> GreenSignal
    TimerPhase.ARMED_RED -> RedSignal
    TimerPhase.RUNNING -> GoldSignal
    TimerPhase.STOPPED -> GreenSignal
    TimerPhase.IDLE -> NeutralSignal
}

private fun instructionFor(phase: TimerPhase): String = when (phase) {
    TimerPhase.IDLE -> "양 끝 패드에 손을 올리세요"
    TimerPhase.HOLDING_GREEN -> "그대로 유지하세요"
    TimerPhase.ARMED_RED -> "양손을 떼세요"
    TimerPhase.RUNNING -> "측정 중 · 양손으로 멈추세요"
    TimerPhase.STOPPED -> "기록 완료 · 양손을 떼세요"
}

@Composable
private fun SpeedStackingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = BackgroundBottom,
            surface = PadSurface,
            primary = GreenSignal,
        ),
        content = content,
    )
}

private val BackgroundTop = Color(0xFF111620)
private val BackgroundBottom = Color(0xFF07090E)
private val PadSurface = Color(0xFF141A24)
private val InnerPad = Color(0xFF202937)
private val PadBorder = Color(0xFF344052)
private val PrimaryText = Color(0xFFF7F9FC)
private val SoftText = Color(0xFFCBD3DF)
private val MutedText = Color(0xFF7D899C)
private val NeutralSignal = Color(0xFF738096)
private val GreenSignal = Color(0xFF3BE58C)
private val RedSignal = Color(0xFFFF4F68)
private val GoldSignal = Color(0xFFFFC85A)
