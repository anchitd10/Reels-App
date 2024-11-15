package com.example.reelapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.reelapp.databinding.FragmentReelBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.R
import com.google.firebase.database.ValueEventListener

class ReelFragment : Fragment() {
    private var _binding: FragmentReelBinding? = null
    private val binding get() = _binding!!
    private var player: ExoPlayer? = null
    private var videoItem: VideoItem? = null
    private var isLiked = false
    private lateinit var database: DatabaseReference

    companion object {
        private const val ARG_VIDEO_ITEM = "video_item"

        fun newInstance(videoItem: VideoItem): ReelFragment {
            val fragment = ReelFragment()
            val args = Bundle()
            args.putParcelable(ARG_VIDEO_ITEM, videoItem)
            fragment.arguments = args
            return fragment
        }
    }

    // Get instance of firebase real time database
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoItem = it.getParcelable(ARG_VIDEO_ITEM)
        }
        database = FirebaseDatabase.getInstance("https://reels-app-f31c9-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("videos")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPlayer()
        setupLikeButton()
        updateLikeCountDisplay()
    }

    // Setup  the Exoplayer instance
    private fun setupPlayer() {
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player

        videoItem?.url?.let { url ->
            val mediaItem = MediaItem.fromUri(url)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.playWhenReady = true
        }
    }

    private fun setupLikeButton() {
        binding.likeButton.setOnClickListener {
            isLiked = !isLiked

            // Use getIdentifier to dynamically fetch the drawable resource ID
            val drawableResId = if (isLiked) {
                requireContext().resources.getIdentifier("ic_favorite", "drawable", requireContext().packageName)
            } else {
                requireContext().resources.getIdentifier("ic_favorite_border", "drawable", requireContext().packageName)
            }

            // Set the drawable resource ID to the like button's image
            binding.likeButton.setImageResource(drawableResId)

            /*
            binding.likeButton.setImageResource(
                if (isLiked) R.drawable.ic_favorite
                else R.drawable.ic_favorite_border
            )
            */

            updateLikeCount(isLiked)
        }
    }

    private fun updateLikeCount(isLiked: Boolean) {
        videoItem?.let { item ->
            val newLikeCount = if (isLiked) item.likeCount + 1 else maxOf(0, item.likeCount - 1)
            videoItem = item.copy(likeCount = newLikeCount)
            updateLikeCountDisplay()
            updateLikeCountInDatabase(newLikeCount)
        }
    }

    private fun updateLikeCountInDatabase(newLikeCount: Int) {
        videoItem?.url?.let { url ->
            database.child("videos").orderByChild("url").equalTo(url)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val videoRef = snapshot.children.first().ref
                            videoRef.child("likeCount").setValue(newLikeCount)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error updating like count: ${error.message}")
                    }
                })
        }
    }

    private fun updateLikeCountDisplay() {
        binding.likeCountText.text = videoItem?.likeCount?.toString() ?: "0"
    }

    fun pauseVideo() {
        player?.pause()
    }

    fun resumeVideo() {
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        pauseVideo()
    }

    override fun onResume() {
        super.onResume()
        resumeVideo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
        _binding = null
    }
}