package me.darthwithap.android.sketchaholic.ui.setup.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withContext
import me.darthwithap.android.sketchaholic.R
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Announcement
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Announcement.Companion.TYPE_ALL_PLAYERS_GUESSED_WORD
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Announcement.Companion.TYPE_PLAYER_DISCONNECTED
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Announcement.Companion.TYPE_PLAYER_GUESSED_WORD
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Announcement.Companion.TYPE_PLAYER_JOINED
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.Announcement.Companion.TYPE_PLAYER_LEFT
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.BaseModel
import me.darthwithap.android.sketchaholic.data.remote.websockets.models.ChatMessage
import me.darthwithap.android.sketchaholic.databinding.ListItemAnnouncementBinding
import me.darthwithap.android.sketchaholic.databinding.ListItemChatMessageIncomingBinding
import me.darthwithap.android.sketchaholic.databinding.ListItemChatMessageOutgoingBinding
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import java.text.SimpleDateFormat
import java.util.Locale

private const val DRAWING_VIEW_ANNOUNCEMENT = 0
private const val DRAWING_VIEW_INCOMING_MESSAGE = 1
private const val DRAWING_VIEW_OUTGOING_MESSAGE = 2
private const val DRAWING_VIEW_MESSAGE_NONE = -1

class ChatMessageAdapter @AssistedInject constructor(
  private val username: String,
  @Assisted private val dispatchers: DispatcherProvider
) : RecyclerView.Adapter<ViewHolder>() {

  var chats = listOf<BaseModel>()
    private set

  suspend fun updateData(newData: List<BaseModel>) {
    val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
      override fun getOldListSize(): Int {
        return chats.size
      }

      override fun getNewListSize(): Int {
        return newData.size
      }

      override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return chats[oldItemPosition] == newData[newItemPosition]
      }

      override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
      ): Boolean {
        return chats[oldItemPosition].toString() == newData[newItemPosition].toString()
      }

    })
    withContext(dispatchers.main) {
      chats = newData
      diff.dispatchUpdatesTo(this@ChatMessageAdapter)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val inflate: (Int) -> View = { layoutRes ->
      LayoutInflater.from(parent.context).inflate(layoutRes, parent, false).rootView
    }

    return when (viewType) {
      DRAWING_VIEW_ANNOUNCEMENT -> {
        AnnouncementViewHolder(inflate(R.layout.list_item_announcement))
      }

      DRAWING_VIEW_INCOMING_MESSAGE -> {
        IncomingChatMessageViewHolder(inflate(R.layout.list_item_chat_message_incoming))
      }

      DRAWING_VIEW_OUTGOING_MESSAGE -> {
        OutgoingChatMessageViewHolder(inflate(R.layout.list_item_chat_message_outgoing))
      }

      else -> {
        EmptyViewHolder(inflate(R.layout.list_item_empty_view))
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (val chat = chats[position]) {
      is Announcement -> DRAWING_VIEW_ANNOUNCEMENT
      is ChatMessage -> {
        if (chat.from == username) DRAWING_VIEW_OUTGOING_MESSAGE
        else DRAWING_VIEW_INCOMING_MESSAGE
      }

      else -> DRAWING_VIEW_MESSAGE_NONE
    }
  }

  override fun getItemCount(): Int {
    return chats.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    when (val chat = chats[position]) {
      is Announcement -> {
        val viewHolder = holder as AnnouncementViewHolder
        viewHolder.bind(chat)
      }

      is ChatMessage -> {
        if (chat.from == username) {
          val viewHolder = holder as OutgoingChatMessageViewHolder
          viewHolder.bind(chat)
        } else {
          val viewHolder = holder as IncomingChatMessageViewHolder
          viewHolder.bind(chat)
        }
      }
    }
  }

  inner class AnnouncementViewHolder(itemView: View) : ViewHolder(itemView) {
    fun bind(announcement: Announcement) {
      ListItemAnnouncementBinding.bind(itemView).apply {
        tvAnnouncement.text = announcement.message

        tvTime.text = getFormattedString(announcement.timestamp)

        when (announcement.announcementType) {
          TYPE_PLAYER_JOINED -> {
            root.setBackgroundColor(Color.GREEN)
            tvAnnouncement.setTextColor(Color.BLACK)
            tvTime.setTextColor(Color.BLACK)
          }

          TYPE_PLAYER_GUESSED_WORD -> {
            root.setBackgroundColor(Color.YELLOW)
            tvAnnouncement.setTextColor(Color.BLACK)
            tvTime.setTextColor(Color.BLACK)
          }

          TYPE_PLAYER_LEFT -> {
            root.setBackgroundColor(Color.LTGRAY)
            tvAnnouncement.setTextColor(Color.WHITE)
            tvTime.setTextColor(Color.WHITE)
          }

          TYPE_PLAYER_DISCONNECTED -> {
            root.setBackgroundColor(Color.RED)
            tvAnnouncement.setTextColor(Color.WHITE)
            tvTime.setTextColor(Color.WHITE)
          }

          TYPE_ALL_PLAYERS_GUESSED_WORD -> {
            root.setBackgroundColor(Color.CYAN)
            tvAnnouncement.setTextColor(Color.BLACK)
            tvTime.setTextColor(Color.BLACK)
          }
        }
      }
    }
  }

  inner class IncomingChatMessageViewHolder(itemView: View) : ViewHolder(itemView) {
    fun bind(message: ChatMessage) {
      ListItemChatMessageIncomingBinding.bind(itemView).apply {
        tvMessage.text = message.message
        tvUsername.text = message.from
        tvTimestamp.text = getFormattedString(message.timestamp)
      }
    }
  }

  inner class OutgoingChatMessageViewHolder(itemView: View) : ViewHolder(itemView) {
    fun bind(message: ChatMessage) {
      ListItemChatMessageOutgoingBinding.bind(itemView).apply {
        tvMessage.text = message.message
        tvUsername.text = message.from
        tvTimestamp.text = getFormattedString(message.timestamp)
      }
    }
  }

  inner class EmptyViewHolder(view: View) : ViewHolder(view)

  private fun getFormattedString(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("kk:mm:ss", Locale.getDefault())
    return dateFormat.format(timestamp)
  }
}
