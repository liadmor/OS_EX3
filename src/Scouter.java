import java.io.File;
import java.io.FileFilter;


public class Scouter implements Runnable {

    public static final int	MAX_DIRECTORY = 10;
    public static int counter = 1;
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
        if (m_isMilestones) {
            m_milestonesQueue.registerProducer();
        }
        run(m_root, m_isMilestones);
        m_directoryQeueu.unregisterProducer();
        if (m_isMilestones) {
            m_milestonesQueue.unregisterProducer();
        }
    }

    public void run(File m_root, boolean isMilestones) {

        if (!m_root.isDirectory() || counter > MAX_DIRECTORY) {
            return;
        } else {
            counter++;
            m_directoryQeueu.enqueue(m_root);
            if (isMilestones) {
                synchronized (this) {
                    m_milestonesQueue.enqueue(String.format("Scouter on thread id %d: directory named \"%s\" was scouted", m_id, m_root.getName()));
                }
            }
            File[] allDirs = m_root.listFiles(File::isDirectory);
            if (allDirs == null){
                return;
            }
            for (File dir : allDirs) {
                run(dir, m_isMilestones);
            }
        }
    }
}

