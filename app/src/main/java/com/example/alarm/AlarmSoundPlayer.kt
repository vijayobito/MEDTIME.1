package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.InputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class AlarmSoundPlayer(private val context: Context) {

    companion object {
        private const val TAG = "AlarmSoundPlayer"
        private const val SAMPLE_RATE = 44100
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null
    private var synthLoopJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    @Volatile
    private var isPlaying = false

    fun isCurrentlyPlaying(): Boolean = isPlaying

    /**
     * Plays the given sound name or custom URI.
     * @param soundName One of AlarmSettingsManager.PRESET_SOUNDS
     * @param looping Whether to continuously loop (for 60s active alarm) or play once (for preview)
     * @param onFallback Callback invoked if a custom sound fails and falls back to default
     */
    fun playSound(
        soundName: String,
        looping: Boolean = true,
        onFallback: ((String) -> Unit)? = null
    ) {
        stop()
        isPlaying = true

        if (!AlarmSettingsManager.isSoundEnabled(context)) {
            Log.d(TAG, "Reminder sound is disabled in settings. Skipping audible playback.")
            return
        }

        try {
            when (soundName) {
                AlarmSettingsManager.SOUND_CUSTOM -> {
                    val customUri = AlarmSettingsManager.getCustomSoundUri(context)
                    if (customUri != null) {
                        playCustomUri(customUri, looping, onFallback)
                    } else {
                        Log.w(TAG, "No custom sound URI set; falling back to MedTime Default.")
                        onFallback?.invoke("Custom audio file not set. Using MedTime Default.")
                        playSystemDefaultAlarm(looping)
                    }
                }
                AlarmSettingsManager.SOUND_DEFAULT -> {
                    playSystemDefaultAlarm(looping)
                }
                AlarmSettingsManager.SOUND_GENTLE_CHIME -> {
                    playSynthesizedChime(SoundPreset.GENTLE_CHIME, looping)
                }
                AlarmSettingsManager.SOUND_SOFT_BELL -> {
                    playSynthesizedChime(SoundPreset.SOFT_BELL, looping)
                }
                AlarmSettingsManager.SOUND_MEDICATION_ALERT -> {
                    playSynthesizedChime(SoundPreset.MEDICATION_ALERT, looping)
                }
                AlarmSettingsManager.SOUND_DIGITAL_ALARM -> {
                    playSynthesizedChime(SoundPreset.DIGITAL_ALARM, looping)
                }
                AlarmSettingsManager.SOUND_CALM_REMINDER -> {
                    playSynthesizedChime(SoundPreset.CALM_REMINDER, looping)
                }
                else -> {
                    playSystemDefaultAlarm(looping)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound $soundName: ${e.message}", e)
            playSystemDefaultAlarm(looping)
        }
    }

    private fun playCustomUri(uri: Uri, looping: Boolean, onFallback: ((String) -> Unit)?) {
        try {
            // Verify URI readability before initializing MediaPlayer
            var stream: InputStream? = null
            try {
                stream = context.contentResolver.openInputStream(uri)
                if (stream == null) {
                    throw IllegalStateException("Cannot open input stream for URI: $uri")
                }
            } finally {
                stream?.close()
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = looping
                setOnCompletionListener {
                    if (!looping) {
                        stop()
                    }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra. Falling back to default alarm.")
                    onFallback?.invoke("Could not play selected audio file. Using MedTime Default.")
                    playSystemDefaultAlarm(looping)
                    true
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play custom URI $uri: ${e.message}. Falling back to default.", e)
            onFallback?.invoke("Selected audio file is unavailable. Using MedTime Default.")
            playSystemDefaultAlarm(looping)
        }
    }

    private fun playSystemDefaultAlarm(looping: Boolean) {
        try {
            var alarmUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            if (alarmUri != null) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, alarmUri)
                    isLooping = looping
                    setOnCompletionListener {
                        if (!looping) stop()
                    }
                    prepare()
                    start()
                }
            } else {
                // Fallback to high-quality synthetic chime if system returns null
                playSynthesizedChime(SoundPreset.MEDICATION_ALERT, looping)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play system default alarm: ${e.message}. Using synthetic tone.", e)
            playSynthesizedChime(SoundPreset.MEDICATION_ALERT, looping)
        }
    }

    private enum class SoundPreset {
        GENTLE_CHIME,
        SOFT_BELL,
        MEDICATION_ALERT,
        DIGITAL_ALARM,
        CALM_REMINDER
    }

    private fun playSynthesizedChime(preset: SoundPreset, looping: Boolean) {
        synthLoopJob?.cancel()
        synthLoopJob = scope.launch {
            try {
                val samples = generateSamplesForPreset(preset)
                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(samples.size * 2)

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()

                do {
                    if (!isActive || !isPlaying) break
                    audioTrack?.write(samples, 0, samples.size)
                    if (looping) {
                        delay(600) // Pause between chime repeats
                    }
                } while (looping && isActive && isPlaying)

            } catch (e: Exception) {
                Log.e(TAG, "Synthesized chime playback error: ${e.message}", e)
            } finally {
                if (!looping) {
                    stop()
                }
            }
        }
    }

    private fun generateSamplesForPreset(preset: SoundPreset): ShortArray {
        return when (preset) {
            SoundPreset.GENTLE_CHIME -> generateChimeTones(
                frequencies = doubleArrayOf(659.25, 830.61, 987.77, 1318.51), // E5, G#5, B5, E6
                durations = doubleArrayOf(0.25, 0.25, 0.25, 0.65),
                decayRate = 3.5
            )
            SoundPreset.SOFT_BELL -> generateHarmonicBell(
                fundamentalFreq = 523.25, // C5
                durationSeconds = 1.8,
                decayRate = 2.0
            )
            SoundPreset.MEDICATION_ALERT -> generatePulsedAlert(
                freq1 = 880.0, // A5
                freq2 = 1760.0, // A6
                pulses = 3
            )
            SoundPreset.DIGITAL_ALARM -> generateDigitalBeeps(
                freq = 2093.0, // C7
                beeps = 4,
                beepDuration = 0.09,
                pauseDuration = 0.06
            )
            SoundPreset.CALM_REMINDER -> generateArpeggio(
                frequencies = doubleArrayOf(523.25, 659.25, 783.99, 1046.50), // C5, E5, G5, C6
                noteDuration = 0.3
            )
        }
    }

    private fun generateChimeTones(frequencies: DoubleArray, durations: DoubleArray, decayRate: Double): ShortArray {
        val totalDuration = durations.sum()
        val totalSamples = (totalDuration * SAMPLE_RATE).toInt()
        val out = ShortArray(totalSamples)
        var offset = 0

        for (i in frequencies.indices) {
            val freq = frequencies[i]
            val dur = durations[i]
            val count = (dur * SAMPLE_RATE).toInt()

            for (s in 0 until count) {
                if (offset + s >= totalSamples) break
                val t = s.toDouble() / SAMPLE_RATE
                val envelope = exp(-decayRate * t)
                // Fundamental + rich 2nd harmonic
                val wave = sin(2.0 * PI * freq * t) * 0.7 + sin(2.0 * PI * freq * 2.0 * t) * 0.3
                val sampleVal = (wave * envelope * 24000).toInt().coerceIn(-32767, 32767)
                out[offset + s] = sampleVal.toShort()
            }
            offset += count
        }
        return out
    }

    private fun generateHarmonicBell(fundamentalFreq: Double, durationSeconds: Double, decayRate: Double): ShortArray {
        val totalSamples = (durationSeconds * SAMPLE_RATE).toInt()
        val out = ShortArray(totalSamples)

        for (s in 0 until totalSamples) {
            val t = s.toDouble() / SAMPLE_RATE
            val env = exp(-decayRate * t)
            val wave = sin(2.0 * PI * fundamentalFreq * t) * 0.5 +
                    sin(2.0 * PI * fundamentalFreq * 2.76 * t) * 0.3 +
                    sin(2.0 * PI * fundamentalFreq * 5.4 * t) * 0.2
            val sampleVal = (wave * env * 26000).toInt().coerceIn(-32767, 32767)
            out[s] = sampleVal.toShort()
        }
        return out
    }

    private fun generatePulsedAlert(freq1: Double, freq2: Double, pulses: Int): ShortArray {
        val pulseLen = 0.12
        val gapLen = 0.08
        val singlePulseTotal = (pulseLen + gapLen) * SAMPLE_RATE
        val totalSamples = (pulses * singlePulseTotal).toInt()
        val out = ShortArray(totalSamples)

        for (p in 0 until pulses) {
            val base = (p * singlePulseTotal).toInt()
            val pulseSamples = (pulseLen * SAMPLE_RATE).toInt()
            val freq = if (p % 2 == 0) freq1 else freq2

            for (s in 0 until pulseSamples) {
                val t = s.toDouble() / SAMPLE_RATE
                val wave = sin(2.0 * PI * freq * t)
                val sampleVal = (wave * 28000).toInt().coerceIn(-32767, 32767)
                if (base + s < totalSamples) {
                    out[base + s] = sampleVal.toShort()
                }
            }
        }
        return out
    }

    private fun generateDigitalBeeps(freq: Double, beeps: Int, beepDuration: Double, pauseDuration: Double): ShortArray {
        val totalDuration = beeps * (beepDuration + pauseDuration)
        val totalSamples = (totalDuration * SAMPLE_RATE).toInt()
        val out = ShortArray(totalSamples)
        var offset = 0

        for (b in 0 until beeps) {
            val beepCount = (beepDuration * SAMPLE_RATE).toInt()
            val pauseCount = (pauseDuration * SAMPLE_RATE).toInt()

            for (s in 0 until beepCount) {
                if (offset + s < totalSamples) {
                    val t = s.toDouble() / SAMPLE_RATE
                    val wave = if (sin(2.0 * PI * freq * t) >= 0) 0.85 else -0.85 // Square wave
                    out[offset + s] = (wave * 24000).toInt().toShort()
                }
            }
            offset += beepCount + pauseCount
        }
        return out
    }

    private fun generateArpeggio(frequencies: DoubleArray, noteDuration: Double): ShortArray {
        val totalSamples = ((frequencies.size * noteDuration + 0.5) * SAMPLE_RATE).toInt()
        val out = ShortArray(totalSamples)
        var offset = 0

        for (freq in frequencies) {
            val noteSamples = (noteDuration * SAMPLE_RATE).toInt()
            for (s in 0 until noteSamples) {
                val t = s.toDouble() / SAMPLE_RATE
                val env = exp(-2.5 * t)
                val wave = sin(2.0 * PI * freq * t) * 0.8 + sin(2.0 * PI * freq * 2.0 * t) * 0.2
                if (offset + s < totalSamples) {
                    out[offset + s] = (wave * env * 25000).toInt().toShort()
                }
            }
            offset += (noteDuration * SAMPLE_RATE * 0.75).toInt()
        }
        return out
    }

    fun stop() {
        isPlaying = false
        synthLoopJob?.cancel()
        synthLoopJob = null

        try {
            audioTrack?.apply {
                pause()
                flush()
                stop()
                release()
            }
        } catch (ignored: Exception) {}
        audioTrack = null

        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (ignored: Exception) {}
        mediaPlayer = null
    }
}
