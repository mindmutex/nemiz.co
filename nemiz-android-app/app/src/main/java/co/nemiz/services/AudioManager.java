package co.nemiz.services;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import co.nemiz.domain.Audio;
import co.nemiz.domain.AudioDefinition;


public class AudioManager {
    private final static String TAG = AudioManager.class.getSimpleName();

    private static AudioManager singletonInstance;

    public static AudioManager get(Context context) {
        if (singletonInstance == null) {
            singletonInstance = new AudioManager(context);
        }
        return singletonInstance;
    }

    private Random random = new Random();
    private Context context;
    private AudioDefinition definition;

    public AudioManager(Context context) {
        this.context = context;
    }

    public void setDefinition(AudioDefinition definition) {
        this.definition = definition;
    }

    public int getCount() {
        if (definition != null) {
            return definition.getCount();
        }
        return 0;
    }

    private File getAudioFile(Audio audio) {
        return context.getFileStreamPath(FilenameUtils.getName(audio.getName()));
    }

    private File getAudioCacheFile(Audio audio) {
        return context.getFileStreamPath(FilenameUtils.getName(audio.getName()) + ".md5");
    }

    private boolean audioExists(Audio audio) {
        File file = getAudioFile(audio);
        if (file.exists()) {
            File hash = getAudioCacheFile(audio);
            if (!hash.exists()) {
                return false;
            }

            FileInputStream stream = null;
            try {
                stream = new FileInputStream(hash);

                String md5 = IOUtils.toString(stream);
                if (audio.getMd5().equals(md5)) {
                    return true;
                }
                file.delete();
            } catch (IOException e) {
                return false;
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        return false;
    }

    private String getCacheMD5(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            inputStream = new FileInputStream(file);
            byte[] dataBytes = new byte[1024];

            int read;
            while ((read = inputStream.read(dataBytes)) != -1) {
                messageDigest.update(dataBytes, 0, read);
            }

            StringBuilder buffer = new StringBuilder();
            for (byte b : messageDigest.digest()) {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return "";
    }

    private void writeCacheMD5(File cache, String md5) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(cache);
            IOUtils.write(md5, outputStream);
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    public Uri getRandomAudio() {
        if (definition == null) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        Audio audio = definition.getFiles().get(random.nextInt(getCount()));
        if (!audioExists(audio)) {
            File cache = getAudioFile(audio);
            cache.setReadable(true, false);

            try {
                URL url = new URL(audio.getUrl());
                FileOutputStream outputStream = null;
                try {
                    outputStream = context.openFileOutput(FilenameUtils.getName(audio.getName()), Context.MODE_WORLD_READABLE);
                    IOUtils.copy(url.openStream(), outputStream);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open output stream for cache file", e);
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }

                String md5 = getCacheMD5(getAudioFile(audio));
                if (md5.equals(audio.getMd5())) {
                    writeCacheMD5(getAudioCacheFile(audio), md5);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Uri.fromFile(getAudioFile(audio));
    }
}
