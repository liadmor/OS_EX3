import java.io.File;
import java.io.FileFilter;


public class Scouter implements Runnable {


    private int m_id;
    private SynchronizedQueue<File> m_directoryQeueu;
    private SynchronizedQueue<String> m_milestonesQueue;
    private File m_root;
    private boolean m_isMilestones;

    public Scouter(int id, SynchronizedQueue<java.io.File> directoryQueue, java.io.File root, SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        m_directoryQeueu = directoryQueue;
        m_milestonesQueue = milestonesQueue;
        m_root = root;
        m_isMilestones = isMilestones;
        m_id = id;
    }

    public void run() {
        m_directoryQeueu.registerProducer();
        try {
            run(m_root, m_isMilestones);
            m_directoryQeueu.unregisterProducer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run(File m_root, boolean isMilestones) throws InterruptedException {
        if (m_root.isFile()) {
            return;
        } else {
            File[] allDirs = m_root.listFiles(File::isDirectory);
            for (File dir : allDirs) {
                m_directoryQeueu.registerProducer();
                m_directoryQeueu.enqueue(dir);
                m_directoryQeueu.unregisterProducer();
                run(dir, m_isMilestones);
                if (isMilestones) {
                    m_milestonesQueue.enqueue(dir.getName());
                }
            }
        }
    }
}

