package syscall.zixuan.com.flutter_sys_call.video.takevideo.camera
import android.app.Application
import android.media.MediaPlayer
import android.view.Surface
import syscall.zixuan.com.flutter_sys_call.video.takevideo.utils.LogUtils


/**
 * 由于拍摄跟播放都关联TextureView,停止播放时要释放mediaplayer
 */

class MediaPlayerManager private constructor(private val app: Application) {

    private var mPlayer: MediaPlayer? = null

    /**
     * 播放Media
     */
    fun playMedia(surface: Surface, mediaPath: String) {
        try {
            if (mPlayer == null) {
                mPlayer = MediaPlayer()
                mPlayer!!.setDataSource(mediaPath)
            } else {
                if (mPlayer!!.isPlaying) {
                    mPlayer!!.stop()
                }
                mPlayer!!.reset()
                mPlayer!!.setDataSource(mediaPath)
            }
            mPlayer!!.setSurface(surface)
            mPlayer!!.isLooping = true
            mPlayer!!.prepareAsync()
            mPlayer!!.setOnPreparedListener { mp -> mp.start() }
        } catch (e: Exception) {
            LogUtils.i(e)
        }

    }

    /**
     * 停止播放Media
     */
    fun stopMedia() {
        try {
            if (mPlayer != null) {
                if (mPlayer!!.isPlaying) {
                    mPlayer!!.stop()
                }
                mPlayer!!.release()
                mPlayer = null
            }
        } catch (e: Exception) {
            LogUtils.i(e)
        }

    }

    companion object {

        private var INSTANCE: MediaPlayerManager? = null

        fun getInstance(app: Application): MediaPlayerManager {
            if (INSTANCE == null) {
                synchronized(CameraManager::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = MediaPlayerManager(app)
                    }
                }
            }
            return INSTANCE!!
        }
    }

}
