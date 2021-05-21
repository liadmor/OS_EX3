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
        File dirToSearch = null;
        try {
            dirToSearch = m_directoryQueue.dequeue();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (dirToSearch != null) {
            File[] allFiles = dirToSearch.listFiles(File::isFile);
            for (File file : allFiles) {
                if (file.getName().toLowerCase().endsWith(m_extension)) {
                    m_resultsQueue.registerProducer();
                    try {
                        m_resultsQueue.enqueue(file);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    m_resultsQueue.unregisterProducer();
                    if (m_isMilestones) {
                        try {
                            m_milestonesQueue.enqueue(file.getName());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            try {
                dirToSearch = m_directoryQueue.dequeue();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        System.out.println(test.m_resultsQueue.getSize());
    }
}
