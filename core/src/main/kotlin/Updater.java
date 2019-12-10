import javax.swing.*;
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
        final int maxAttempts = 20 * 4;
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
                break;
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof AtomicMoveNotSupportedException) {
                    atomic = false;
                }
                attemptNum++;
                Thread.sleep(waitBetween);
            }
        }
        JOptionPane.showMessageDialog(null, (attemptNum >= maxAttempts ? "Rhythm Heaven Remix Editor update failed. Please try again, or consider updating manually from https://github.com/chrislo27/RhythmHeavenRemixEditor/releases/latest." : "Rhythm Heaven Remix Editor updated successfully! You can relaunch the program now."), "RHRE Auto-Update Information", attemptNum >= maxAttempts ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }
}
