import java.io.File;
import java.io.FileFilter;

public class Searcher implements Runnable {

    private int m_id;
    private String m_extension;
    private SynchronizedQueue<File> m_directoryQueue;
    private SynchronizedQueue<File> m_resultsQueue;
    private SynchronizedQueue<String> m_milestonesQueue;
    boolean m_isMilestones;

    public Searcher(int id, java.lang.String extension, SynchronizedQueue<java.io.File> directoryQueue, SynchronizedQueue<java.io.File> resultsQueue, SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        m_id = id;
        m_extension = extension;
        m_directoryQueue = directoryQueue;
        m_resultsQueue = resultsQueue;
        m_milestonesQueue = milestonesQueue;
        m_isMilestones = isMilestones;
    }

    public void run() {
        m_resultsQueue.registerProducer();
        if (m_isMilestones) {
            m_milestonesQueue.registerProducer();
        }
        File dirToSearch = null;
        dirToSearch = m_directoryQueue.dequeue();
        while (dirToSearch != null) {
            File[] allFiles = dirToSearch.listFiles(File::isFile);
            if (allFiles != null) {
                for (File file : allFiles) {
                    if (file.getName().endsWith(m_extension)) {
                        m_resultsQueue.enqueue(file);
                        if (m_isMilestones) {
                            m_milestonesQueue.enqueue(String.format("Searcher on thread id %d: directory named \"%s\" was found", m_id, file.getName()));
                        }
                    }
                }
            }
            dirToSearch = m_directoryQueue.dequeue();
        }
        if (m_isMilestones) {
            m_milestonesQueue.unregisterProducer();
        }
        m_resultsQueue.unregisterProducer();
    }
}
