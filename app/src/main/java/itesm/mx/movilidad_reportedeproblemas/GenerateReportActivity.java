package itesm.mx.movilidad_reportedeproblemas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import itesm.mx.movilidad_reportedeproblemas.Adapters.CategoryAdapter;
import itesm.mx.movilidad_reportedeproblemas.Models.Category;
import itesm.mx.movilidad_reportedeproblemas.Services.DummyLocationService;
import itesm.mx.movilidad_reportedeproblemas.Services.IBitmapManager.HashByteArrayManager;
import itesm.mx.movilidad_reportedeproblemas.Services.IBitmapManager.IByteArrayManager;
import itesm.mx.movilidad_reportedeproblemas.Services.ICommentManager.HashStringManager;
import itesm.mx.movilidad_reportedeproblemas.Services.ICommentManager.IStringManager;
import itesm.mx.movilidad_reportedeproblemas.Services.IContainer;
import itesm.mx.movilidad_reportedeproblemas.Services.IDatabaseProvider;
import itesm.mx.movilidad_reportedeproblemas.Services.ILocationService;
import itesm.mx.movilidad_reportedeproblemas.Services.ListDatabaseProvider;

public class GenerateReportActivity extends AppCompatActivity implements View.OnClickListener, IContainer{

    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int BITMAP_CONTAINER = 1;
    public static final int AUDIO_CONTAINER = 2;

    private ILocationService _locationService = new DummyLocationService();
    private IDatabaseProvider _db = new ListDatabaseProvider();
    private IStringManager _commentManager = new HashStringManager();
    private IByteArrayManager _bitmapManager = new HashByteArrayManager();
    private IByteArrayManager _soundManager = new HashByteArrayManager();

    private Spinner spinner;
    private ViewGroup vgExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        spinner = (Spinner) findViewById(R.id.spinner_generateReport);
        CategoryAdapter adapter = new CategoryAdapter(this, (Category[])_db.getCategories().toArray());
        spinner.setAdapter(adapter);

        Button btnGenerate = (Button) findViewById(R.id.button_generateReport);
        btnGenerate.setOnClickListener(this);

        ImageButton btnTakePhoto = (ImageButton) findViewById(R.id.button_generateReport_photo);
        btnTakePhoto.setOnClickListener(this);

        ImageButton btnRecordAudio = (ImageButton) findViewById(R.id.button_generateReport_recordAudio);
        btnRecordAudio.setOnClickListener(this);

        ImageButton btnAddComment = (ImageButton) findViewById(R.id.button_generateReport_addComment);
        btnAddComment.setOnClickListener(this);

        vgExtras = (LinearLayout) findViewById(R.id.layout_generateReport_extras);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_generateReport:
                generateReport();
                break;
            case R.id.button_generateReport_photo:
                takePicture();
                break;
            case R.id.button_generateReport_recordAudio:
                generateRecordAudioFragment();
                break;
            case R.id.button_generateReport_addComment:
                generateCommentFragment();
                break;
        }
    }

    private void generateReport() {
        ILocationService.Location location = _locationService.getLocation();

        Category category = (Category) spinner.getSelectedItem();

        Log.i("GenerateReport", String.format("(%f, %f) %s", location.Longitude, location.Latitude, category.getName()));
        for (String comment : _commentManager.getStrings()) {
            Log.i("GenerateReport", comment);
        }
        Log.i("GenerateReport", "Bitmaps: " + _bitmapManager.getByteArrays().size());
        Log.i("GenerateReport", "Audios: " + _soundManager.getByteArrays().size());
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            generatePhotoFragment(bitmap);
        }
    }

    private void generatePhotoFragment(Bitmap bitmap){
        android.app.FragmentManager manager = getFragmentManager();
        manager.beginTransaction().add(vgExtras.getId(), TakePhotoFragment.newInstance(bitmap), "photo").commit();
    }

    private void generateRecordAudioFragment() {
        android.app.FragmentManager manager = getFragmentManager();
        manager.beginTransaction().add(vgExtras.getId(), AudioRecordFragment.newInstance(), "audio").commit();
    }

    private void generateCommentFragment() {
        android.app.FragmentManager manager = getFragmentManager();
        manager.beginTransaction().add(vgExtras.getId(), AddCommentFragment.newInstance(), "comment").commit();
    }

    @Override
    public Object getComponent(Class<?> $class, int code) {
        if ($class == IStringManager.class) return _commentManager;
        if ($class == IByteArrayManager.class) {
            switch(code){
                case BITMAP_CONTAINER:
                    return _bitmapManager;
                case AUDIO_CONTAINER:
                    return  _soundManager;
            }
            return null;
        }
        return null;
    }
}
