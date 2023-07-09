package me.darthwithap.android.sketchaholic.ui.setup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.withContext
import me.darthwithap.android.sketchaholic.data.remote.websockets.Room
import me.darthwithap.android.sketchaholic.databinding.ListItemRoomBinding
import me.darthwithap.android.sketchaholic.util.DispatcherProvider
import javax.inject.Inject

class RoomAdapter @Inject constructor(
    private val dispatchers: DispatcherProvider
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    var rooms: List<Room> = listOf()
        private set

    interface RoomClickListener {
        fun onClick(room: Room)
    }

    private var clickListener: RoomClickListener? = null

    fun setOnRoomClickListener(roomClickListener: RoomClickListener) {
        clickListener = roomClickListener
    }

    suspend fun updateData(newData: List<Room>) {
        withContext(dispatchers.io) {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return rooms.size
                }

                override fun getNewListSize(): Int {
                    return newData.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return rooms[oldItemPosition] == newData[newItemPosition]
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return rooms[oldItemPosition].toString() == newData[newItemPosition].toString()
                }

            })
            withContext(dispatchers.main) {
                rooms = newData
                diff.dispatchUpdatesTo(this@RoomAdapter)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        return RoomViewHolder(
            ListItemRoomBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root
        )
    }

    override fun getItemCount(): Int {
        return rooms.size
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(room: Room) {
            ListItemRoomBinding.bind(itemView).apply {
                tvRoomName.text = room.name
                val personCount = "${room.playerCount}/${room.maxPlayers}"
                tvRoomPersonCount.text = personCount

                root.setOnClickListener {
                    clickListener?.onClick(room)
                }
            }
        }
    }
}
