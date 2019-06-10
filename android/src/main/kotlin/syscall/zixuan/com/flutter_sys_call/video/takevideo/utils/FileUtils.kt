package syscall.zixuan.com.flutter_sys_call.video.takevideo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.TextUtils

import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random


object FileUtils {
    /**
     * 随机命名
     */
    private val RANDOM_STRING = "abcdefghijklmnopqrstuvwxyz0123456789"
    /**
     * 时间命名
     */
    private val TIME_STRING = "yyyyMMdd_HHmmss"
    /**
     * 限制图片最大宽度进行压缩
     */
    private val MAX_WIDTH = 720
    /**
     * 限制图片最大高度进行压缩
     */
    private val MAX_HEIGHT = 1280
    /**
     * 上传最大图片限制
     */
    private val MAX_UPLOAD_PHOTO_SIZE = 300 * 1024

    /**
     * 缓存文件根目录名
     */
    private val FILE_DIR = "you"
    /**
     * 上传的照片文件路径
     */
    private val UPLOAD_FILE = "upload"

    /**
     * SD卡是否存在
     */
    val isSDCardExist: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    /**
     * sd卡容量
     *
     * @return
     */
    val availableStorage: Long
        get() {
            var availableSize: Long = 0
            if (isSDCardExist) {
                val sdFile = Environment.getExternalStorageDirectory()
                val stat = StatFs(sdFile.path)
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    availableSize = stat.availableBytes
                } else {
                    availableSize = stat.availableBlocks.toLong() * stat.blockSize
                }
            }
            return availableSize
        }

    val timeString: String
        get() = SimpleDateFormat(TIME_STRING).format(Date())

    /**
     * 获取缓存目录路径
     *
     * @return
     */
    fun getCacheDirPath(context: Context): String {
        if (isSDCardExist) {
            val path = Environment.getExternalStorageDirectory().toString() + File.separator + FILE_DIR + File.separator
            val directory = File(path)
            if (!directory.exists()) directory.mkdirs()
            return path
        } else {
            val directory = File(context.cacheDir, FileUtils.FILE_DIR)
            if (!directory.exists()) directory.mkdirs()
            return directory.absolutePath
        }
    }

    /**
     * 获取缓存目录
     *
     * @return
     */
    fun getCacheDir(context: Context): File {
        if (isSDCardExist) {
            val path = Environment.getExternalStorageDirectory().toString() + File.separator + FILE_DIR + File.separator
            val directory = File(path)
            if (!directory.exists()) directory.mkdirs()
            return directory
        } else {
            val directory = File(context.cacheDir, FileUtils.FILE_DIR)
            if (!directory.exists()) directory.mkdirs()
            return directory
        }
    }

    /**
     * 获取上传的路径
     *
     * @return
     */
    fun getUploadCachePath(context: Context): String {
        if (isSDCardExist) {
            val path =
                Environment.getExternalStorageDirectory().toString() + File.separator + FILE_DIR + File.separator + UPLOAD_FILE + File.separator
            val directory = File(path)
            if (!directory.exists()) directory.mkdirs()
            return path
        } else {
            val directory = File(context.cacheDir, FileUtils.FILE_DIR + File.separator + UPLOAD_FILE)
            if (!directory.exists()) directory.mkdirs()
            return directory.absolutePath
        }
    }

    /**
     * jpg文件名
     * @param context
     * @return
     */
    fun getUploadPhotoFile(context: Context): String {
        return getUploadCachePath(context) + timeString + ".jpg"
    }

    /**
     * mp4文件名
     * @param context
     * @return
     */
    fun getUploadVideoFile(context: Context): String {
        return getUploadCachePath(context) + timeString + ".mp4"
    }

    /**
     * 保存拍摄图片
     * @param photoPath
     * @param data
     * @param isFrontFacing 是否为前置拍摄
     * @return
     */
    fun savePhoto(photoPath: String?, data: ByteArray?, isFrontFacing: Boolean): Boolean {
        if (photoPath != null && data != null) {
            var fos: FileOutputStream? = null
            try {
                var preBitmap = compressBitmap(data, MAX_WIDTH, MAX_HEIGHT)
                if (isFrontFacing) {
                    val matrix = Matrix()
                    matrix.postScale(1f, -1f)
                    val newBitmap =
                        Bitmap.createBitmap(preBitmap!!, 0, 0, preBitmap.width, preBitmap.height, matrix, true)
                    preBitmap.recycle()
                    preBitmap = newBitmap
                }
                val newDatas = compressBitmapToBytes(preBitmap, MAX_UPLOAD_PHOTO_SIZE)
                fos = FileOutputStream(photoPath)
                fos.write(newDatas)
                LogUtils.i("compress over ")
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtils.i(e)
            } finally {
                closeCloseable(fos)
            }
        }
        return false
    }

    /**
     * 把字节流按照图片方式大小进行压缩
     * @param datas
     * @param w
     * @param h
     * @return
     */
    fun compressBitmap(datas: ByteArray?, w: Int, h: Int): Bitmap? {
        if (datas != null) {
            val opts = BitmapFactory.Options()
            opts.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(datas, 0, datas.size, opts)
            if (opts.outWidth != 0 && opts.outHeight != 0) {
                LogUtils.i(opts.outWidth.toString() + " " + opts.outHeight)
                val scaleX = opts.outWidth / w
                val scaleY = opts.outHeight / h
                var scale = 1
                if (scaleX >= scaleY && scaleX >= 1) {
                    scale = scaleX
                }
                if (scaleX < scaleY && scaleY >= 1) {
                    scale = scaleY
                }
                opts.inJustDecodeBounds = false
                opts.inSampleSize = scale
                LogUtils.i("compressBitmap inSampleSize " + datas.size + " " + scale)
                return BitmapFactory.decodeByteArray(datas, 0, datas.size, opts)
            }
        }
        return null
    }

    /**
     * 质量压缩图片
     * @param bitmap
     * @param maxSize
     * @return
     */
    fun compressBitmapToBytes(bitmap: Bitmap?, maxSize: Int): ByteArray? {
        if (bitmap == null) return null
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        var datas = baos.toByteArray()
        var options = 80
        var longs = datas.size
        while (longs > maxSize && options > 0) {
            LogUtils.i("compressBitmapToBytes $longs  $options")
            baos.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)
            datas = baos.toByteArray()
            longs = datas.size
            options -= 20
        }
        return datas
    }

    /**
     * 获取文件路径下所有文件大小, 适当放到子线程中执行
     *
     * @param file
     * @return
     */
    fun getFileSize(file: File?): Long {
        if (file == null || !file.exists()) return 0
        var totalSize: Long = 0
        if (file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                totalSize += getFileSize(f)
            }
            return totalSize
        } else {
            return file.length()
        }
    }

    /**
     * 删除文件夹下所有文件,适当放到子线程中执行
     *
     * @param file
     */
    fun delteFiles(file: File?) {
        if (file == null || !file.exists()) return
        if (file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                if (!f.isDirectory) {
                    f.delete()
                }
            }
        } else {
            file.delete()
        }
    }

    /**
     * 关闭资源
     *
     * @param close
     */
    fun closeCloseable(close: Closeable?) {
        if (close != null) {
            try {
                close.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 文件大小
     *
     * @param fileS
     * @return
     */
    fun formetFileSize(fileS: Long): String {
        if (fileS <= 0) return "0B"
        return if (fileS < 1024) {
            DecimalFormat("#.00").format(fileS.toDouble()) + "B"
        } else if (fileS < 1048576) {
            DecimalFormat("#.00").format(fileS.toDouble() / 1024) + "KB"
        } else if (fileS < 1073741824) {
            DecimalFormat("#.00").format(fileS.toDouble() / 1048576) + "MB"
        } else {
            DecimalFormat("#.00").format(fileS.toDouble() / 1073741824) + "GB"
        }
    }

    /**
     * 获取随机文件名称字符串
     *
     * @param length 生成字符串的长度
     * @return
     */
    fun getRandomString(length: Int): String {
        val random = Random()
        val sb = StringBuffer()
        for (i in 0 until length) {
            val number = random.nextInt(RANDOM_STRING.length)
            sb.append(RANDOM_STRING[number])
        }
        return sb.toString()
    }

    /**
     * 将字符串写入文件中
     *
     * @param str    需要写入的字符串
     * @param file   写入文件的路径
     * @param append true 追加写入 false 覆盖写入
     * @return
     */
    fun writeFile(str: String, file: File?, append: Boolean): Boolean {
        if (TextUtils.isEmpty(str) || file == null) return false
        var fw: FileWriter? = null
        try {
            fw = FileWriter(file, append)
            fw.write(str)
            fw.flush()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            closeCloseable(fw)
        }
        return false
    }

}
