package gal.cifpacarballeira.unidad4_tarea7gestordeberes;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HomeworkAdapter adapter;
    private List<Homework> homeworkList;
    private SQLiteDatabase hwDBWrite;
    private SQLiteDatabase hwDBRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización de los objetos de BBDD
        hwDBWrite = new HomeworkDB(this).getWritableDatabase();
        hwDBRead = new HomeworkDB(this).getReadableDatabase();

        // Inicialización de componentes
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);
        homeworkList = new ArrayList<>();

        // Cargar datos desde la base de datos
        readAllHw(hwDBRead,homeworkList);

        // Crear y configurar el adaptador
        adapter = new HomeworkAdapter(homeworkList, homework -> showBottomSheet(homework));

        // Este código sería lo mismo que la anterior línea
        // adapter = new HomeworkAdapter(homeworkList, this::showBottomSheet);
        // ¿Por qué le paso ese segundo parámetro?
        // Porque le estoy pasando la función que quiero que se lance al hacer click en un elemento
        // Investiga sobre "operador de referencia de método en Java"


        // Configuración del RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Configuración del botón flotante
        fab.setOnClickListener(v -> showAddHomeworkDialog(null));
    }

    private void showAddHomeworkDialog(Homework homeworkToEdit) {

        NewHomeworkDialogFragment dialog = new NewHomeworkDialogFragment();
        // Pasarle el objeto Homework al diálogo si se está editando
        if (homeworkToEdit != null) {
            Bundle args = new Bundle();
            args.putParcelable("homework", homeworkToEdit);
            dialog.setArguments(args);
        }
        dialog.setOnHomeworkSavedListener(homework -> {
                    if (homeworkToEdit == null) {
                        homeworkList.add(homework);
                        insertHW(hwDBWrite, hwDBRead, homework);
                    } else {
                        homeworkList.set(homeworkList.indexOf(homeworkToEdit), homework);
                        updateHw(hwDBWrite,homeworkToEdit,homework);
                    }
            adapter.notifyDataSetChanged();
                });
        dialog.show(getSupportFragmentManager(), "AddHomeworkDialog");
//
//        AddHomeworkDialogFragment dialog = AddHomeworkDialogFragment.newInstance(homeworkToEdit);
//        dialog.setOnHomeworkSavedListener(homework -> {
//            if (homeworkToEdit == null) {
//                homeworkList.add(homework);
//            } else {
//                homeworkList.set(homeworkList.indexOf(homeworkToEdit), homework);
//            }
//            adapter.notifyDataSetChanged();
//        });
//        dialog.show(getSupportFragmentManager(), "AddHomeworkDialog");
    }

    private void showBottomSheet(Homework homework) {
        // Creación del diálogo
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        // Inflar el layout del diálogo
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_homework_options, null);

        // Asignar acciones a los botones

        // Opción de editar
        view.findViewById(R.id.editOption).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showAddHomeworkDialog(homework);
        });

        // Opción de eliminar
        view.findViewById(R.id.deleteOption).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showDeleteConfirmation(homework);
        });


        // Opción de marcar como completada
        view.findViewById(R.id.completeOption).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            homework.setCompleted(true);
            updateHw(hwDBWrite,homework,homework);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Tarea marcada como completada", Toast.LENGTH_SHORT).show();
        });

        // Mostrar el diálogo
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void showDeleteConfirmation(Homework homework) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar este deber?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    homeworkList.remove(homework);
                    deleteHW(hwDBWrite,homework);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Metodos CRUD
    public void insertHW(SQLiteDatabase dbWrite, SQLiteDatabase dbRead, Homework hw){
        ContentValues contentValues = new ContentValues();
        contentValues.put("subject",hw.getSubject());
        contentValues.put("description",hw.getDescription());
        contentValues.put("duedate",hw.getDueDate());
        contentValues.put("completed",hw.getCompletedInt());

        dbWrite.insert("homework",null,contentValues);
        dbWrite.close();

        String sql = "SELECT id FROM homework WHERE description = " + hw.getDescription() + "AND subject = " + hw.getSubject();
        Cursor cursor = dbRead.rawQuery(sql, null);
        if(cursor.moveToFirst()){
            do{
                hw.setId(cursor.getInt(0));
            } while(cursor.moveToNext());
            cursor.close();
        }
    }

    public void deleteHW(SQLiteDatabase dbWrite, Homework hw){
        String id = String.valueOf(hw.getId());

        dbWrite.delete("homework","id = "+id,null);
        dbWrite.close();
    }

    public void readAllHw(SQLiteDatabase hwDBRead, List<Homework> hwList){
        String sql = "SELECT id, subject, description, duedate, completed FROM homework";
        Cursor cursor = hwDBRead.rawQuery(sql, null);
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(0);
                String subject = cursor.getString(1);
                String description = cursor.getString(2);
                String duedate = cursor.getString(3);
                boolean completed = cursor.getInt(4) != 0;
                hwList.add(new Homework(id,subject,description,duedate,completed));
            } while(cursor.moveToNext());
            cursor.close();
        }
    }

    public void updateHw(SQLiteDatabase dbWrite, Homework hw, Homework hwToEdit){
        String id = String.valueOf(hw.getId());
        ContentValues contentValues = new ContentValues();
        contentValues.put("subject",hwToEdit.getSubject());
        contentValues.put("description",hwToEdit.getDescription());
        contentValues.put("duedate",hwToEdit.getDueDate());
        contentValues.put("completed",hwToEdit.getCompletedInt());

        dbWrite.update("homework",contentValues,"id=?",new String[]{id});
        dbWrite.close();
    }
}
