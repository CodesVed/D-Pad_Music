package com.example.mediaplayer_project

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer_project.databinding.ActivityMainBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionListener: PermissionListener
    private lateinit var arrayOfSongs: ArrayList<Song>
    private lateinit var mediaRecyclerView: RecyclerView
    private var adapter: MyAdapter ?= null
    private var mediaPlayer: MediaPlayer? = null
    private var current = 0
    private val handler = android.os.Handler(Looper.getMainLooper())

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mediaRecyclerView = findViewById(R.id.recyclerView)
        mediaRecyclerView.layoutManager = LinearLayoutManager(this)

        setupControlButtons()

        permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                arrayOfSongs = fetchSongs(this@MainActivity)
                adapter = MyAdapter(this@MainActivity, arrayOfSongs)
                mediaRecyclerView.adapter = adapter

                val txtCount = findViewById<TextView>(R.id.txtNumber)
                val txtTitle = findViewById<TextView>(R.id.txtTitle)
                val txtArtist = findViewById<TextView>(R.id.txtArtist)
                val txtDuration = findViewById<TextView>(R.id.txtDuration)

                adapter?.setOnItemClickListener(object : MyAdapter.OnItemClickListener{
                    override fun onItemClick(position: Int) {
                        adapter?.setSelected(position)
                        startMusic(position)
                    }
                })
            }

            override fun onPermissionDenied(p0: List<String?>?) {
                Toast.makeText(this@MainActivity, "Media Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getString(R.string.permission_rationale_message))
                .setDeniedMessage(R.string.permission_denial_message)
                .setGotoSettingButton(true)
                .setGotoSettingButtonText(R.string.goto_settings)
                .setPermissions(Manifest.permission.READ_MEDIA_AUDIO)
                .check()
        }
    }

    private fun setupControlButtons(){
        binding.btnPlay.setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying){
                    it.pause()
                    binding.btnPlay.setImageResource(R.drawable.baseline_play_arrow_24)
                } else {
                    it.start()
                    binding.btnPlay.setImageResource(R.drawable.baseline_pause_24)
                }
            }
        }

        binding.btnNext.setOnClickListener {
            if (arrayOfSongs.isNotEmpty()){
                current = (current+1) % arrayOfSongs.size
                startMusic(current)
                adapter?.setSelected(current)
                mediaRecyclerView.scrollToPosition(current)
            }
        }

        binding.btnPrevious.setOnClickListener {
            if (arrayOfSongs.isNotEmpty()){
                current = (current-1 + arrayOfSongs.size) % arrayOfSongs.size
                startMusic(current)
                adapter?.setSelected(current)
                mediaRecyclerView.scrollToPosition(current)
            }
        }

        binding.btnLoop.setOnClickListener {
            mediaPlayer?.isLooping = true
            Toast.makeText(this, "Loop ON", Toast.LENGTH_SHORT).show()
        }

        binding.btnLoopOff.setOnClickListener {
            mediaPlayer?.isLooping = false
            Toast.makeText(this, "Loop OFF", Toast.LENGTH_SHORT).show()
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser){
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun startMusic(position : Int){
        current = position

        mediaPlayer?.stop()
        mediaPlayer?.release()

        val uri = arrayOfSongs[position].path.toUri()
        mediaPlayer = MediaPlayer.create(this, uri)

        mediaPlayer?.start()
        binding.btnPlay.setImageResource(R.drawable.baseline_pause_24)

        binding.seekbar.max = mediaPlayer?.duration ?: 0
        updateSeekBar()

        mediaPlayer?.setOnCompletionListener {
            binding.btnNext.performClick()
        }
    }

    private fun updateSeekBar(){
        handler.postDelayed(object : Runnable{
            override fun run() {
                binding.seekbar.progress = mediaPlayer?.currentPosition!!
                handler.postDelayed(this, 1000)
            }
        },0)
    }

    private fun fetchSongs(context: Context): ArrayList<Song>{
        val songList = arrayListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
//            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
//            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()){
                songList.add(Song(
                    id = cursor.getInt(idCol),
//                    albumId = cursor.getInt(albumIdCol),
                    title = cursor.getString(titleCol),
                    artist = cursor.getString(artistCol),
                    path = cursor.getString(pathCol),
                    duration = cursor.getInt(durationCol)
                ))
            }
        }
        return songList
    }
}