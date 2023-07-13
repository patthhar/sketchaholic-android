package me.darthwithap.android.sketchaholic.data.remote.websockets

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.flow.Flow
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.BaseModel


interface DrawingApi {
  @Receive
  fun observeConnectionEvents(): Flow<WebSocket.Event>

  @Send
  fun sendBaseModel(baseModel: BaseModel): Boolean

  @Receive
  fun observeBaseModels(): Flow<BaseModel>
}