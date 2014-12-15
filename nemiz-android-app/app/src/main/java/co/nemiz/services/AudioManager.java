package co.nemiz.services;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

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
    private Gson gson = new Gson();

    private AudioDefinition definition;
    private Context context;

    public AudioManager(Context context) {
        this.context = context;

        // attempt to load the definition from cache.
        attemptDefinitionLoad();
    }

    private File getRoot() {
        return Environment.getExternalStoragePublicDirectory("Nemiz");
    }

    private File getAudioFile(Audio audio) {
        return new File(getRoot(), FilenameUtils.getName(audio.getName()));
    }

    private File getAudioCacheFile(Audio audio) {
        return new File(getRoot(), FilenameUtils.getName(audio.getName()) + ".md5");
    }

    public void setDefinition(AudioDefinition definition) {
        this.definition = definition;

        // store the definition on filesystem
        // otherwise when app is killed it doesn't know anything about
        // the audio files
        Gson gson = new Gson();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new File(getRoot(), "audio.json"));
            IOUtils.write(gson.toJson(definition), outputStream);
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * Loads audio definition from external storage.
     */
    private void attemptDefinitionLoad() {
        if (definition == null) {
            File definitionFile = new File(getRoot(), "audio.json");
            if (definitionFile.exists()) {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(new File(getRoot(), "audio.json"));
                    definition = gson.fromJson(IOUtils.toString(inputStream), AudioDefinition.class);
                } catch (IOException e) {
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
        }
    }

    public int getCount() {
        if (definition != null) {
            return definition.getCount();
        }
        return 0;
    }

    /**
     * Check if the audio file exists in external storage.
     * The file name and the MD5 checksum must match.
     *
     * @param audio audio
     * @return status
     */
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

    /**
     * Generate MD5 checksum for a file.
     *
     * @param file file
     * @return checksum
     */
    private String generateChecksum(File file) {
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

    /**
     * Returns random audio file.
     *
     * @return uri to audio file.
     */
    public Uri getRandomAudio() {
        if (definition == null) {
            // returns default notification sound if no info about the audio is present.
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        Audio audio = definition.getFiles().get(random.nextInt(getCount()));
        if (!audioExists(audio)) {
            File cache = getAudioFile(audio);
            try {
                URL url = new URL(audio.getUrl());
                FileOutputStream outputStream = null;
                try {
                    if (cache.getParentFile() != null) {
                        cache.getParentFile().mkdirs();
                    }
                    outputStream = new FileOutputStream(cache);
                    IOUtils.copy(url.openStream(), outputStream);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open output stream for cache file", e);
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }

                String md5 = generateChecksum(cache);
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
