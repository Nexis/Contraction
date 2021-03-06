package pl.mylittleworld.contraction.database;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import pl.mylittleworld.contraction.DataAccessor;

public class DataBaseAccessor implements DataAccessor {

    private static DataBase dataBase;
    private static final ArrayList<AsyncTask<Void, Void, Void>> queue = new ArrayList<>();
    private static boolean running = false;

    public DataBaseAccessor(@NonNull Context context) {
        dataBase = Room.databaseBuilder(context, DataBase.class, "ContractionDatabase").build();
    }

    private void addToQueue(AsyncTask<Void, Void, Void> asyncTask) {
        queue.add(asyncTask);
        doTasks();
    }

    private static void taskDone(AsyncTask asyncTask) {
        queue.remove(asyncTask);
        running = false;
        doTasks();
    }


    private static void doTasks() {
        if (queue.size() > 0 && !running) {
            running = true;
            queue.get(0).execute();
        }
    }

    @Override
    public void addContraction(Contraction contraction) {
        addToQueue(new AddTask(contraction));
    }

    @Override
    public void deleteContraction(Contraction contraction) {
        addToQueue(new DeleteTask(contraction));
    }

    @Override
    public void updateContraction(Contraction contraction) {
        addToQueue(new UpdateTask(contraction));
    }

    @Override
    public void getAllContractions(DataAccessListener dataAccessListener) {
        addToQueue(new GetAllContractionsTask(dataAccessListener));
    }

    private static class AddTask extends AsyncTask<Void, Void, Void> {

        private final Contraction contraction;

        AddTask(Contraction contraction) {
            this.contraction = contraction;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dataBase.getDao().insert(contraction);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            taskDone(this);
        }
    }

    private static class UpdateTask extends AsyncTask<Void, Void, Void> {

        private final Contraction contraction;

        UpdateTask(Contraction contraction) {
            this.contraction = contraction;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dataBase.getDao().update(contraction);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            taskDone(this);
        }
    }

    private static class DeleteTask extends AsyncTask<Void, Void, Void> {


        private final Contraction contraction;

        DeleteTask(Contraction contraction) {
            this.contraction = contraction;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dataBase.getDao().delete(contraction);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            taskDone(this);
        }
    }

    private static class GetAllContractionsTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<DataAccessListener> dataAccessListenerW;
        private ArrayList<Contraction> contractions;

        GetAllContractionsTask(DataAccessListener dataAccessListener) {
            dataAccessListenerW = new WeakReference<>(dataAccessListener);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            contractions = new ArrayList<>(dataBase.getDao().getAllContractions());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            DataAccessListener dataAccessListener=dataAccessListenerW.get();
            if(dataAccessListener!=null) {
                dataAccessListener.onActionDone(contractions);
            }
            taskDone(this);

        }
    }
}
