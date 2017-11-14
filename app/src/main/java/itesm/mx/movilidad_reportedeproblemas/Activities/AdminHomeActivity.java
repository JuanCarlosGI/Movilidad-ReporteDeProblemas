package itesm.mx.movilidad_reportedeproblemas.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import itesm.mx.movilidad_reportedeproblemas.R;
import itesm.mx.movilidad_reportedeproblemas.Services.ILoginProvider.DummyLoginProvider;
import itesm.mx.movilidad_reportedeproblemas.Services.ILoginProvider.ILoginProvider;

public class AdminHomeActivity extends AppCompatActivity {
    private ILoginProvider _loginProvider = DummyLoginProvider.getInstance();

    TextView tvName;
    Button btnAdminister;
    Button btnHistory;
    Button btnManageAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        tvName = (TextView) findViewById(R.id.text_adminHome_userName);
        btnAdminister = (Button) findViewById(R.id.button_adminHome_administer);
        btnHistory = (Button) findViewById(R.id.button_adminHome_history);
        btnManageAdmin = (Button) findViewById(R.id.button_adminHome_addAdmin);

        tvName.setText(_loginProvider.getCurrentUser().getName());

        btnAdminister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AdminReportListActivity.class);
                startActivity(intent);
            }
        });

        btnManageAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ManageAdminsActivity.class);
                startActivity(intent);
            }
        });
    }
}
