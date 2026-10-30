package com.nmplayer.video.ui.adapter.viewholder

import android.text.format.DateUtils
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nmplayer.video.R
import com.nmplayer.video.databinding.VideoItemBinding
import com.nmplayer.video.model.Video
import com.nmplayer.video.ui.adapter.VideoListener
import com.nmplayer.video.util.AppConstant

class VideoViewHolder(
    private val binding: VideoItemBinding,
    private val videoListener: VideoListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(video: Video) {
        binding.txtName.text = video.name
        binding.txtDuration.text = DateUtils.formatElapsedTime(video.duration / 1000)

        // Subtitle text: Size + Resolution
        val sizeStr: String = if (video.size > 1000 * 1000 * 1000) {
            String.format(AppConstant.SIZE_FLOAT_FORMAT, (video.size / (1000.0 * 1000 * 1000))) + AppConstant.GB_SIZE
        } else {
            String.format(AppConstant.SIZE_FLOAT_FORMAT, (video.size / (1000.0 * 1000))) + AppConstant.MB_SIZE
        }
        val resStr = if (!video.resolution.isNullOrBlank()) " • ${video.resolution}" else if (video.width > 0) " • ${video.width}x${video.height}" else ""
        binding.txtSubtitle.text = "$sizeStr$resStr"

        // Thumbnail loading
        Glide.with(binding.root.context)
            .load(video.path)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_background)
            .into(binding.imgVideo)

        // Favorite icon state
        updateFavoriteIcon(video.isFavorite)

        binding.root.setOnClickListener {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                videoListener.onItemClicked(position, video)
            }
        }

        binding.btnFavorite.setOnClickListener {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                video.isFavorite = !video.isFavorite
                updateFavoriteIcon(video.isFavorite)
                videoListener.onFavoriteClicked(position, video)
            }
        }

        binding.btnMoreOption.setOnClickListener { v ->
            val context = binding.root.context
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val popupMenu = PopupMenu(context, v)
                popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
                val favItem = popupMenu.menu.findItem(R.id.favorite)
                favItem?.title = if (video.isFavorite) context.getString(R.string.unfavorite) else context.getString(R.string.favorite)
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.favorite -> {
                            video.isFavorite = !video.isFavorite
                            updateFavoriteIcon(video.isFavorite)
                            videoListener.onFavoriteClicked(position, video)
                        }
                        R.id.details -> videoListener.showDetailsClicked(video)
                        R.id.share -> videoListener.shareVideoClicked(video)
                        R.id.delete -> videoListener.onItemDeleteClicked(position)
                    }
                    true
                }
            }
        }
    }

    private fun updateFavoriteIcon(isFav: Boolean) {
        if (isFav) {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
        } else {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
        }
    }
}

