package gal.cifpacarballeira.unidad4_tarea7gestordeberes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

public class HomeworkViewModel extends ViewModel {
    private MutableLiveData<List<Homework>> list = new MutableLiveData<>(null);

    public LiveData<List<Homework>> getList(){
        return this.list;
    }

    public void setList(List<Homework> updatedList){
        this.list.setValue(updatedList);
    }

    public void setHomeworkInList(int index, Homework homework){
        List<Homework> tempList = list.getValue();
        if (tempList != null && index >= 0 && index < tempList.size()) {
            tempList.set(index, homework);
            list.setValue(tempList);
        }
    }

    public void addHomeworkToList(Homework newHomework){
        this.list.getValue().add(newHomework);
    }

    public void removeHomeworkFromList(Homework thisHomework){
        this.list.getValue().remove(thisHomework);
    }

    public void updateHomeworkInList(Homework currHomework){
        this.list.getValue().set(this.list.getValue().indexOf(currHomework), currHomework);
    }
}
