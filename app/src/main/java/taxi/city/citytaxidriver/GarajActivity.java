package taxi.city.citytaxidriver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;


public class GarajActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garaj);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new GarajFragment())
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    public static class GarajFragment extends Fragment implements View.OnClickListener {

        public GarajFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_garaj, container, false);

            ImageButton ibPersonalInfo = (ImageButton)rootView.findViewById(R.id.imageButtonDriverInfo);
            ImageButton ibCarInfo = (ImageButton)rootView.findViewById(R.id.imageButtonCarInfo);
            ImageButton ibAccountInfo = (ImageButton)rootView.findViewById(R.id.imageButtonAccountNumber);
            ImageButton ibOrderList = (ImageButton)rootView.findViewById(R.id.imageButtonHistory);

            Button btnMain = (Button)rootView.findViewById(R.id.buttonMain);
            Button btnCancel = (Button)rootView.findViewById(R.id.buttonCancel);

            ibPersonalInfo.setOnClickListener(this);
            ibCarInfo.setOnClickListener(this);
            ibAccountInfo.setOnClickListener(this);
            ibOrderList.setOnClickListener(this);

            btnMain.setOnClickListener(this);
            btnCancel.setOnClickListener(this);

            return rootView;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imageButtonDriverInfo:
                    openDriverInfo();
                    break;
                case R.id.imageButtonCarInfo:
                    openCarInfo();
                    break;
                case R.id.imageButtonAccountNumber:
                    openAccountNumber();
                    break;
                case R.id.imageButtonHistory:
                    openOrderHistory();
                    break;
                case R.id.buttonMain:
                    goToMain();
                    break;
                case R.id.buttonCancel:
                    goToMain();
                    break;
            }
        }

        private void goToMain() {
            Intent intent = new Intent();
            getActivity().setResult(5, intent);
            getActivity().finish();
        }

        private void openOrderHistory() {
            Intent intent = new Intent(getActivity(), OrderActivity.class);
            intent.putExtra("NEW", false);
            startActivityForResult(intent, 5);
        }

        private void openAccountNumber() {
            Intent intent = new Intent(getActivity(), AccountActivity.class);
            startActivity(intent);
        }

        private void openCarInfo() {
            Intent intent = new Intent(getActivity(), CarDetailsActivity.class);
            intent.putExtra("NEW", false);
            startActivityForResult(intent, 5);
        }

        private void openDriverInfo() {
            Intent intent = new Intent(getActivity(), UserDetailsActivity.class);
            intent.putExtra("NEW", false);
            startActivity(intent);
        }
    }
}
