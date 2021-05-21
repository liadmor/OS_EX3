import java.io.*;
import java.io.File;

public class Copier implements Runnable {

    public static final int COPY_BUFFER_SIZE = 4096;
    int m_id;
    File m_destination;
    SynchronizedQueue<File> m_resultsQueue;
    SynchronizedQueue<String> m_milestonesQueue;
    boolean m_isMilestones;

    public Copier(int id, java.io.File destination, SynchronizedQueue<java.io.File> resultsQueue, SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        m_id = id;
        m_destination = destination;
        m_resultsQueue = resultsQueue;
        m_milestonesQueue = milestonesQueue;
        m_isMilestones = isMilestones;
    }

    public void run() {

        File fileToCopy = null;
        InputStream is = null;
        OutputStream os = null;

        try {
            fileToCopy = m_resultsQueue.dequeue();
            while (fileToCopy != null) {
                is = new FileInputStream((fileToCopy));
                os = new FileOutputStream(m_destination);
                byte[] buf = new byte[COPY_BUFFER_SIZE];
                int bytesRead = is.read(buf);
                while (bytesRead > 0) {
                    os.write(buf, 0, bytesRead);
                }
                is.close();
                os.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
}
