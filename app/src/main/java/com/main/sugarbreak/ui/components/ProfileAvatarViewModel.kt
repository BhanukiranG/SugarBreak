package com.main.sugarbreak.ui.components

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.sugarbreak.data.repository.AvatarThumbnail
import com.main.sugarbreak.data.repository.ProfileAvatar
import com.main.sugarbreak.data.repository.ProfileAvatarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AvatarUiState(
    val avatar: ProfileAvatar = ProfileAvatar(),
    val preview: ByteArray? = null,
    val busy: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileAvatarViewModel @Inject constructor(
    private val repository: ProfileAvatarRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(AvatarUiState())
    val state = _state.asStateFlow()
    private var pickerUid: String? = null
    private var operation: Job? = null

    init {
        viewModelScope.launch {
            repository.avatar.collect { avatar ->
                if (_state.value.avatar.uid != avatar.uid) {
                    operation?.cancel()
                    pickerUid = null
                    _state.value = AvatarUiState(avatar)
                } else {
                    _state.update { it.copy(avatar = avatar) }
                }
            }
        }
    }

    fun beginPicker(): Boolean {
        val current = _state.value
        val uid = current.avatar.uid ?: return false
        if (current.busy || current.avatar.pending || !repository.isCurrentAccount(uid)) return false
        pickerUid = uid
        return true
    }

    fun select(uri: Uri?) {
        val uid = pickerUid ?: return
        pickerUid = null
        if (uri == null) return
        runOperation(uid, "Could not open this photo. Choose another photo or try again.") {
            val jpeg = withContext(Dispatchers.IO) { AvatarThumbnail.read(context.contentResolver, uri) }
            updateAccount(uid) { it.copy(preview = jpeg) }
        }
    }

    fun cancelPreview() {
        _state.update { if (it.busy) it else it.copy(preview = null, error = null) }
    }

    fun savePreview() {
        val current = _state.value
        val jpeg = current.preview ?: return
        save(jpeg)
    }

    fun remove() = save(null)

    private fun save(jpeg: ByteArray?) {
        val uid = _state.value.avatar.uid ?: return
        runOperation(uid, "Could not update your photo. Please try again.") {
            repository.save(uid, jpeg)
            updateAccount(uid) { it.copy(preview = null) }
        }
    }

    private fun runOperation(uid: String, error: String, action: suspend () -> Unit) {
        if (_state.value.busy || _state.value.avatar.pending || !repository.isCurrentAccount(uid)) return
        updateAccount(uid) { it.copy(busy = true, error = null) }
        operation = viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateAccount(uid) { it.copy(error = error) }
            } finally {
                updateAccount(uid) { it.copy(busy = false) }
            }
        }
    }

    private fun updateAccount(uid: String, update: (AvatarUiState) -> AvatarUiState) {
        _state.update { if (it.avatar.uid == uid && repository.isCurrentAccount(uid)) update(it) else it }
    }
}
