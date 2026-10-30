package com.nmplayer.video.ui.adapter.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.nmplayer.video.R
import com.nmplayer.video.databinding.FolderItemBinding
import com.nmplayer.video.model.Folder
import com.nmplayer.video.ui.adapter.FolderListener

class FolderViewHolder(
    private val binding: FolderItemBinding,
    private val listener: FolderListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(folder: Folder) {
        binding.txtName.text = folder.folderName
        binding.txtTotalVideos.text =
            binding.root.context.getString(R.string.total_videos, folder.totalVideos.toString())

        binding.root.setOnClickListener {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onFolderClicked(position, folder)
            }
        }
    }
}

