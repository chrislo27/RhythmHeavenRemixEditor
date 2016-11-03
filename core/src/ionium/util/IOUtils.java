package ionium.util;

import com.badlogic.gdx.files.FileHandle;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class IOUtils {

	public static void saveGzip(FileHandle handle, byte[] bytes) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(handle.file().getAbsolutePath());
		GZIPOutputStream gzipStream = new GZIPOutputStream(outputStream);

		try {
			gzipStream.write(bytes);
		} catch (IOException e) {
			gzipStream.close();

			throw e;
		}

		gzipStream.close();
	}

	public static byte[] loadGzip(FileHandle file) throws IOException {
		FileInputStream fis = new FileInputStream(file.file());
		GZIPInputStream gzipstream = new GZIPInputStream(fis);

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(2048);
		byte[] buffer = new byte[2048];
		int bytesRead;
		while ((bytesRead = gzipstream.read(buffer)) > 0) {
			byteStream.write(buffer, 0, bytesRead);
		}

		gzipstream.close();
		fis.close();

		return byteStream.toByteArray();
	}

}
