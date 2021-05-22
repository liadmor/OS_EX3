import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
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
        String dest = null;

        try {
            fileToCopy = m_resultsQueue.dequeue();
            dest = m_destination.getAbsolutePath() + File.separator + fileToCopy.getName();
            while (fileToCopy != null) {
                dest = m_destination.getAbsolutePath() + File.separator + fileToCopy.getName();
                is = new FileInputStream((fileToCopy));
                os = new FileOutputStream(dest);
                byte[] buf = new byte[COPY_BUFFER_SIZE];
                int bytesRead = is.read(buf);
                while (bytesRead > 0) {
                    os.write(buf, 0, bytesRead);
                }
                is.close();
                os.close();
                fileToCopy = m_resultsQueue.dequeue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String extension = ".docx";
        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>(20);
        String root = "C:\\Users\\97250\\OperSys\\EX3\\stest";
        SynchronizedQueue<File> resultsQueue = new SynchronizedQueue<>(20);
        SynchronizedQueue<String> milestonesQueue = new SynchronizedQueue<>(20);
        Scouter ans = new Scouter(1, directoryQueue, new File(root), milestonesQueue, true );
        ans.run();
        //System.out.println(directoryQueue.getSize());

        Searcher test = new Searcher(2, extension, directoryQueue, resultsQueue, milestonesQueue, true);
        test.run();
        String descopy = "C:\\Users\\97250\\Desktop\\descopy";
        File des = new File(descopy);

        Copier test2 = new Copier(5, des, resultsQueue, milestonesQueue, true);
        test2.run();

    }
}
