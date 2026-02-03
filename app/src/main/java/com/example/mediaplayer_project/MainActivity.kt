package com.example.mediaplayer_project

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var mediaPlayer: MediaPlayer

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

        permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                arrayOfSongs = fetchSongs(this@MainActivity)
                val adapter = MyAdapter(this@MainActivity, arrayOfSongs)
                mediaRecyclerView.adapter = adapter
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