package itesm.mx.movilidad_reportedeproblemas.Activities;

import android.support.v4.app.FragmentManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import itesm.mx.movilidad_reportedeproblemas.Fragments.KmeansMapFragment;
import itesm.mx.movilidad_reportedeproblemas.Fragments.PieChartFragment;
import itesm.mx.movilidad_reportedeproblemas.Fragments.ScrollFriendlySupportMapFragment;
import itesm.mx.movilidad_reportedeproblemas.Models.Category;
import itesm.mx.movilidad_reportedeproblemas.Models.Report;
import itesm.mx.movilidad_reportedeproblemas.R;
import itesm.mx.movilidad_reportedeproblemas.Services.IDatabaseProvider.IDatabaseProvider;
import itesm.mx.movilidad_reportedeproblemas.Services.IDatabaseProvider.WebDatabaseProvider;
import itesm.mx.movilidad_reportedeproblemas.Services.StatusColorMatcher;
import itesm.mx.movilidad_reportedeproblemas.Services.StatusParser;
import itesm.mx.movilidad_reportedeproblemas.Services.Tuple;

//////////////////////////////////////////////////////////
//Clase: StatsActivity
// Descripción: Se muestra el status de los reportes.
// Autor: Armando Aguiar y Juan Carlos Guzman
// Fecha de creación: 03/11/2017
// Fecha de última modificación: 23/11/2017
//////////////////////////////////////////////////////////

public class StatsActivity extends AppCompatActivity {
    private WebDatabaseProvider _db = new WebDatabaseProvider();

    ScrollView layScrollView;
    ViewGroup vgFragments;
    TextView tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        layScrollView = findViewById(R.id.layout_stats_scrollView);
        vgFragments = findViewById(R.id.layout_stats_charts);
        tvTotal = findViewById(R.id.text_stats_total);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _db.getReports(new IDatabaseProvider.IDbHandler<ArrayList<Report>>() {
            @Override
            public void handle(final ArrayList<Report> result) {
                if (result == null) {
                    Log.e("StatsActivity", "Could not get reports.");
                    Toast.makeText(getApplicationContext(), "Hubo un problema con la conexion con el servidor. Intente de nuevo.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                tvTotal.setText("Total de reportes: " + result.size());
                Map<Integer, Integer> statusCounter = new HashMap<>();
                final Map<Long, Integer> categoryCounter = new HashMap<>();
                for (Report report : result) {
                    if (!statusCounter.containsKey(report.getStatus()))
                        statusCounter.put(report.getStatus(), 0);
                    if (!categoryCounter.containsKey(report.getCategoryId()))
                        categoryCounter.put(report.getCategoryId(), 0);

                    statusCounter.put(report.getStatus(), statusCounter.get(report.getStatus()) + 1);
                    categoryCounter.put(report.getCategoryId(), categoryCounter.get(report.getCategoryId()) + 1);
                }

                final List<Tuple<Tuple<String,Double>,Integer>> statuses = new ArrayList<>();
                for (int status : statusCounter.keySet()) {
                    statuses.add(new Tuple<>(new Tuple<>(StatusParser.Parse(status), (double) statusCounter.get(status)), StatusColorMatcher.getColor(status)));
                }

                _db.getCategories(new IDatabaseProvider.IDbHandler<ArrayList<Category>>() {
                    @Override
                    public void handle(ArrayList<Category> innerResult) {
                        if (innerResult == null) {
                            Log.e("StatsActivity", "Could not get categories.");
                            Toast.makeText(getApplicationContext(), "Hubo un problema con la conexion con el servidor. Intente de nuevo.", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        int i = 0;
                        List<Tuple<Tuple<String,Double>,Integer>> categories = new ArrayList<>();
                        int[] colors = new int[] {Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.GREEN};

                        for (long categoryId : categoryCounter.keySet()) {
                            String name = "";
                            for (Category category : innerResult) {
                                if (category.getId() == categoryId) {
                                    name = category.getName();
                                    break;
                                }
                            }
                            categories.add(new Tuple<>(new Tuple<>(name, (double) categoryCounter.get(categoryId)), colors[i++ % colors.length]));
                        }

                        FragmentManager manager = getSupportFragmentManager();
                        vgFragments.removeAllViews();
                        if (result.size() != 0) {
                            KmeansMapFragment kmeansFragment = KmeansMapFragment.newInstance(result);
                            kmeansFragment.setListener(new ScrollFriendlySupportMapFragment.OnTouchListener() {
                                @Override
                                public void onTouch() {
                                    layScrollView.requestDisallowInterceptTouchEvent(true);
                                }
                            });
                            manager.beginTransaction().add(vgFragments.getId(), kmeansFragment).commit();
                            manager.beginTransaction().add(vgFragments.getId(), PieChartFragment.newInstance(statuses, "Reportes por estatus")).commit();
                            manager.beginTransaction().add(vgFragments.getId(), PieChartFragment.newInstance(categories, "Reportes por categoria")).commit();
                        } else {
                            Toast.makeText(getApplicationContext(), "No hay reportes que mostrar; puede haber un problema de conexion con el servidor.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
