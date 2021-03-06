package org.duniter.app.view.connection;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.view.InitActivity;
import org.duniter.app.view.connection.pin.PinFragment;


public class ConnectionActivity extends Activity {

    int etapeCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        etapeCurrent = getIntent().getIntExtra(Application.ETAPE,InitActivity.ETAPE_0);

        Fragment fragment;

        switch (etapeCurrent) {
            case InitActivity.ETAPE_1_1:
                fragment = ChooseInitFragment.newInstance();
                break;
            case InitActivity.ETAPE_3_1:
                fragment = InscriptionFragment.newInstance();
                break;
            case InitActivity.ETAPE_3_2:
                fragment = ConnectionFragment.newInstance();
                break;
            default:
                fragment = null;
                break;
        }
        displayFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void nextEtape(int etape){
        Intent intent = new Intent(this,InitActivity.class);
        intent.putExtra(InitActivity.FUTUR_ETAPE,etape);
        intent.putExtra(Application.ETAPE,etapeCurrent);
        setResult(RESULT_OK,intent);
        finish();
    }

    public void setCurrency(long currencyId){
        Intent intent = new Intent(ConnectionActivity.this, InitActivity.class);
        intent.putExtra(Application.CURRENCY_ID,currencyId);
        intent.putExtra(Application.ETAPE,etapeCurrent);
        setResult(RESULT_OK,intent);
        finish();
    }

    private void displayFragment(Fragment fragment){
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_fade_in,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.fade_out)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

}