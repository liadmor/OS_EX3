import com.sun.jdi.PrimitiveValue;

import javax.print.DocFlavor;
import javax.swing.*;
import javax.swing.plaf.synth.SynthLookAndFeel;
import java.io.File;
import java.util.PrimitiveIterator;

public class DiskSearcher {
    public static final int	DIRECTORY_QUEUE_CAPACITY = 50;
    public static final int RESULTS_QUEUE_CAPACITY = 50;
    public static final int MILESTONES_QUEUE_CAPACITY = DIRECTORY_QUEUE_CAPACITY*RESULTS_QUEUE_CAPACITY;
    private static boolean m_mileFlag;
    private static String m_extention;
    private static File m_rootDir;
    private static File m_destDir;
    private static int m_numSearchers;
    private static int m_numCopiers;
    private static int m_id = 0;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        if (args.length != 6){
            return;
        }
        m_mileFlag = Boolean.parseBoolean(args[0]);
        m_extention = args[1];
        m_rootDir = new File(args[2]);
        m_destDir = new File(args[3]);
        m_numSearchers = Integer.parseInt(args[4]);
        m_numCopiers = Integer.parseInt(args[5]);

        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>(DIRECTORY_QUEUE_CAPACITY);
        SynchronizedQueue<String> milestonesQueue = new SynchronizedQueue<>(MILESTONES_QUEUE_CAPACITY);
        SynchronizedQueue<File> resultQueue = new SynchronizedQueue<>(RESULTS_QUEUE_CAPACITY);
        if (m_mileFlag){
            milestonesQueue.enqueue("General, program has start the search");
        }
        else{
            milestonesQueue = null;
        }

        //init thread scouter
        Scouter scouter = new Scouter(m_id, directoryQueue, m_rootDir, milestonesQueue, m_mileFlag);
        Thread scouterThread = new Thread(scouter);
        scouterThread.start();
        m_id++;

        //init thread search = m_numSearcher
        Thread[] searchersTheads = new Thread[m_numSearchers];
        for (int i = 0; i < m_numSearchers; i++){
            Searcher search = new Searcher(m_id, m_extention, directoryQueue, resultQueue, milestonesQueue, m_mileFlag);
            searchersTheads[i] = new Thread(search);
            searchersTheads[i].start();
            m_id++;
        }

        Thread[] copiersThreads = new Thread[m_numCopiers];
        for (int i = 0; i < m_numCopiers; i++){
            Copier copier = new Copier(m_id, m_destDir, resultQueue, milestonesQueue, m_mileFlag);
            copiersThreads[i] = new Thread(copier);
            copiersThreads[i].start();
            m_id++;
        }

        try {
            scouterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Thread searcherThead : searchersTheads) {
            try {
                if (searcherThead != null) {
                    searcherThead.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String milestone = milestonesQueue.dequeue();
        while ( milestone != null){
            System.out.println(milestone);
            milestone = milestonesQueue.dequeue();
        }

        //init thread copier = m_numCopier
        for(Thread copiersThread : copiersThreads)
        {
            try {
                if(copiersThread != null){
                    copiersThread.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long estimatedTime = System.currentTimeMillis() - startTime;

        milestone = milestonesQueue.dequeue();
        while ( milestone != null){
            System.out.println(milestone);
            milestone = milestonesQueue.dequeue();
        }

        System.out.println(estimatedTime);
        System.out.println(m_numCopiers +" "+ m_numSearchers);

    }
}
