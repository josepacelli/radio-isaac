package com.radioisaac.viewmodel

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.extractor.metadata.icy.IcyInfo
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.radioisaac.PlaybackService
import com.radioisaac.data.BrazilRegion
import com.radioisaac.data.RadioRepository
import com.radioisaac.data.RadioStation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class RadioUiState(
    val stations: List<RadioStation> = emptyList(),
    val currentStation: RadioStation? = null,
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val signalLevel: Float = 0f,
    val qualityLevel: Float = 0f,
    val isStereo: Boolean = false,
    val isStreamActive: Boolean = false,
    val nowPlaying: String = "",
    val rtArtist: String = "",
    val rtTitle: String = "",
    val hasRdsData: Boolean = false,
    val errorMessage: String? = null,
    val showStationList: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<RadioStation> = emptyList(),
    val isLoading: Boolean = false,
    val selectedCategory: String = "BR",
    val emptyStates: Set<String> = emptySet()
)

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RadioRepository()

    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var player: MediaController? = null
    private var signalJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startSignalSimulation() else stopSignalSimulation()
        }

        override fun onPlaybackStateChanged(state: Int) {
            _uiState.update {
                it.copy(
                    isBuffering = state == Player.STATE_BUFFERING,
                    isStreamActive = state == Player.STATE_READY
                )
            }
            if (state == Player.STATE_IDLE) {
                _uiState.update { it.copy(signalLevel = 0f, qualityLevel = 0f, isStereo = false) }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _uiState.update {
                it.copy(
                    errorMessage = "Erro no stream: ${error.localizedMessage}",
                    isPlaying = false,
                    isBuffering = false,
                    signalLevel = 0f,
                    qualityLevel = 0f
                )
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            val artist = mediaMetadata.artist?.toString()?.trim() ?: ""
            val title = mediaMetadata.title?.toString()?.trim() ?: ""
            when {
                artist.isNotBlank() && title.isNotBlank() ->
                    _uiState.update { it.copy(rtArtist = artist, rtTitle = title, nowPlaying = "$artist - $title", hasRdsData = true) }
                title.isNotBlank() -> parseAndUpdateRt(title)
                artist.isNotBlank() -> parseAndUpdateRt(artist)
            }
        }

        override fun onMetadata(metadata: Metadata) {
            for (i in 0 until metadata.length()) {
                val entry = metadata[i]
                if (entry is IcyInfo) {
                    val raw = entry.title?.trim() ?: continue
                    if (raw.isNotBlank()) parseAndUpdateRt(raw)
                }
            }
        }
    }

    init {
        val token = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        val future = MediaController.Builder(application, token).buildAsync()
        controllerFuture = future
        future.addListener({
            player = future.get()
            player?.addListener(playerListener)
            loadTopStations()
        }, MoreExecutors.directExecutor())
    }

    private fun parseAndUpdateRt(raw: String) {
        val dashIdx = raw.indexOf(" - ")
        if (dashIdx > 0) {
            _uiState.update {
                it.copy(
                    rtArtist = raw.substring(0, dashIdx).trim(),
                    rtTitle = raw.substring(dashIdx + 3).trim(),
                    nowPlaying = raw,
                    hasRdsData = true
                )
            }
        } else {
            _uiState.update { it.copy(rtArtist = "", rtTitle = raw, nowPlaying = raw, hasRdsData = true) }
        }
    }

    private fun startSignalSimulation() {
        signalJob?.cancel()
        signalJob = viewModelScope.launch {
            var noise = 0f
            while (true) {
                val p = player ?: break
                val buffered = p.bufferedPercentage / 100f
                val state = p.playbackState

                noise = noise * 0.6f + (Random.nextFloat() - 0.5f) * 0.08f
                noise = noise.coerceIn(-0.1f, 0.1f)

                val target = when (state) {
                    Player.STATE_READY -> (0.70f + buffered * 0.22f + noise).coerceIn(0.55f, 1f)
                    Player.STATE_BUFFERING -> (0.15f + Random.nextFloat() * 0.35f)
                    else -> 0f
                }

                val current = _uiState.value.signalLevel
                val smoothed = current + (target - current) * 0.25f

                _uiState.update {
                    it.copy(
                        signalLevel = smoothed,
                        qualityLevel = ((smoothed + buffered) / 2f).coerceIn(0f, 1f),
                        isStereo = state == Player.STATE_READY
                    )
                }
                delay(500)
            }
        }
    }

    private fun stopSignalSimulation() {
        signalJob?.cancel()
        viewModelScope.launch {
            var level = _uiState.value.signalLevel
            while (level > 0.02f) {
                level *= 0.65f
                _uiState.update { it.copy(signalLevel = level, qualityLevel = level * 0.8f, isStereo = false) }
                delay(80)
            }
            _uiState.update { it.copy(signalLevel = 0f, qualityLevel = 0f, isStereo = false) }
        }
    }

    fun loadTopStations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getByCountry("BR")
                .onSuccess { stations ->
                    _uiState.update { it.copy(stations = stations, isLoading = false, selectedCategory = "BR") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Falha ao carregar: ${e.message}") }
                }
        }
    }

    fun loadByRegion(region: BrazilRegion) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getByRegion(region)
                .onSuccess { stations ->
                    val empty = if (stations.isEmpty())
                        _uiState.value.emptyStates + region.display
                    else
                        _uiState.value.emptyStates - region.display
                    _uiState.update {
                        it.copy(
                            stations = stations,
                            isLoading = false,
                            selectedCategory = region.display,
                            emptyStates = empty
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun loadByCountry(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getByCountry(code)
                .onSuccess { stations ->
                    _uiState.update { it.copy(stations = stations, isLoading = false, selectedCategory = code) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.length < 2) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            repository.searchStations(query)
                .onSuccess { results ->
                    _uiState.update { it.copy(searchResults = results) }
                }
        }
    }

    fun selectStation(station: RadioStation) {
        val idx = _uiState.value.stations.indexOfFirst { it.uuid == station.uuid }
        _uiState.update {
            it.copy(
                currentStation = station,
                currentIndex = if (idx >= 0) idx else it.currentIndex,
                nowPlaying = "",
                rtArtist = "",
                rtTitle = "",
                hasRdsData = false,
                errorMessage = null,
                showStationList = false
            )
        }
        playStream(station.effectiveStreamUrl)
    }

    private fun playStream(url: String) {
        if (url.isBlank()) return
        player?.apply {
            stop()
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            play()
        }
    }

    fun togglePlayback() {
        val p = player ?: return
        val station = _uiState.value.currentStation ?: return
        when {
            p.isPlaying -> p.pause()
            p.playbackState == Player.STATE_IDLE || p.playbackState == Player.STATE_ENDED -> playStream(station.effectiveStreamUrl)
            else -> p.play()
        }
    }

    fun nextStation() {
        val state = _uiState.value
        val stations = state.stations.ifEmpty { return }
        val idx = if (state.currentIndex < 0) 0 else (state.currentIndex + 1) % stations.size
        selectStation(stations[idx])
    }

    fun prevStation() {
        val state = _uiState.value
        val stations = state.stations.ifEmpty { return }
        val idx = when {
            state.currentIndex <= 0 -> stations.size - 1
            else -> state.currentIndex - 1
        }
        selectStation(stations[idx])
    }

    fun openStationList() = _uiState.update { it.copy(showStationList = true) }
    fun closeStationList() = _uiState.update { it.copy(showStationList = false) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    override fun onCleared() {
        super.onCleared()
        signalJob?.cancel()
        player?.removeListener(playerListener)
        player = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }
}
