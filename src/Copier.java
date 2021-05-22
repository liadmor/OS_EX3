import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import java.io.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        byte[] buf = new byte[COPY_BUFFER_SIZE];
        String dest = null;
        if (m_isMilestones) {
            m_milestonesQueue.registerProducer();
        }
        try {
            fileToCopy = m_resultsQueue.dequeue();
            while (fileToCopy != null) {
                dest = m_destination.getAbsolutePath() + File.separator + fileToCopy.getName();
                is = new FileInputStream((fileToCopy));
                os = new FileOutputStream(dest);
                int bytesRead = is.read(buf);
                while (bytesRead > 0) {
                    os.write(buf, 0, bytesRead);
                    bytesRead = is.read(buf);
                }
                if (m_isMilestones) {
                    m_milestonesQueue.enqueue(String.format("Copier on thread id %d: directory named \"%s\" was copied", m_id, fileToCopy.getName()));
                }
                is.close();
                os.close();
                fileToCopy = m_resultsQueue.dequeue();
            }
            if (m_isMilestones) {
                m_milestonesQueue.unregisterProducer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*public void run()
    {
        System.out.println("gal is the king!!!!!!!!!!!!!!!!!!!!!!");

        if(this.m_isMilestones)
        {
            m_milestonesQueue.registerProducer();
        }
        File file;
        while((file = m_resultsQueue.dequeue()) != null)
        {
            try {
                Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(m_destination.getAbsolutePath() + "\\" + file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(m_isMilestones)
            {
                m_milestonesQueue.enqueue(" Copier from thread id " +  m_id + " : file named " +  file.getName() +  " was copied");
            }
        }
        if(m_isMilestones)
        {
            m_milestonesQueue.unregisterProducer();
        }
    }*/
}
