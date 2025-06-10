package com.example.tae_levision.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.tae_levision.R;
import com.example.tae_levision.utils.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Terapkan tema sebelum memuat tampilan
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Ambil NavHostFragment dan NavController
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Konfigurasi AppBar untuk menyertakan fragment utama
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.homeFragment,
                    R.id.searchFragment,
                    R.id.favoriteFragment
            ).build();

            // Setup NavigationUI dengan BottomNavigationView
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        } else {
            Log.e(TAG, "NavHostFragment is null");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        // Jika di home fragment dan user menekan back, keluar dari aplikasi
        if (navController != null && navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == R.id.homeFragment) {
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
