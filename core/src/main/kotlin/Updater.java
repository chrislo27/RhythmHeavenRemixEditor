import java.io.File;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Atomically moves the file denoted by the first argument to the file denoted by the second.
 *
 */
public class Updater {
    public static void main(String[] args) throws InterruptedException {
        final Path file1 = new File(args[0]).toPath();
        final Path file2 = new File(args[1]).toPath();
        final int maxAttempts = 40;
        final long waitBetween = 250L;
        int attemptNum = 0;
        boolean atomic = true;
        while (attemptNum < maxAttempts) {
            try {
                if (atomic) {
                    Files.move(file1, file2, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.move(file1, file2, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof AtomicMoveNotSupportedException) {
                    atomic = false;
                }
                attemptNum++;
                Thread.sleep(waitBetween);
            }
        }
    }
}
