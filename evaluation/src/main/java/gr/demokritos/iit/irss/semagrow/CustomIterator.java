package gr.demokritos.iit.irss.semagrow;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by Nick on 10-Aug-14.
 */
public class CustomIterator<T> implements Iterator<T> {

    private ArrayList<File> files;
    private String poolPath;


    public CustomIterator(String poolPath) {
        this.poolPath = poolPath;
        files = new ArrayList<File>(Arrays.asList(new File(poolPath).listFiles()));
    }


    @Override
    public boolean hasNext() {
        return !files.isEmpty();
    }


    @Override
    public T next() {
        return readFromPool(poolPath, files.get(0).getName());
    }


    @Override
    public void remove() {
        files.remove(0);
    }


    private T readFromPool(String path, String filename) {
        T rdfQueryRecord = null;
        File file = new File(path + filename);
        ObjectInputStream ois;

        try {

            ois = new ObjectInputStream(new FileInputStream(file));

            rdfQueryRecord = (T)ois.readObject();

            ois.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return  rdfQueryRecord;
    }// readFromPool

}
