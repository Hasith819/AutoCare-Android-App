package com.nibm.autocare.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nibm.autocare.R

class UploadedPhotosAdapter(
    private val uploadedPhotos: List<Uri>,
    private val onRemoveClickListener: (Int) -> Unit
) : RecyclerView.Adapter<UploadedPhotosAdapter.UploadedPhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadedPhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_uploaded_photo, parent, false)
        return UploadedPhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: UploadedPhotoViewHolder, position: Int) {
        val photoUri = uploadedPhotos[position]
        Glide.with(holder.itemView.context)
            .load(photoUri)
            .into(holder.ivUploadedPhoto)

        holder.btnRemovePhoto.setOnClickListener {
            onRemoveClickListener(position)
        }
    }

    override fun getItemCount(): Int {
        return uploadedPhotos.size
    }

    class UploadedPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUploadedPhoto: ImageView = itemView.findViewById(R.id.ivUploadedPhoto)
        val btnRemovePhoto: Button = itemView.findViewById(R.id.btnRemovePhoto)
    }
}