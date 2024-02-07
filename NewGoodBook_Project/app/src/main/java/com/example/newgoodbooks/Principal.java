package com.example.newgoodbooks;

import android.os.Bundle;

import com.example.newgoodbooks.Fragments.Explorar;
import com.example.newgoodbooks.Fragments.Home;
import com.example.newgoodbooks.Fragments.HomeIU.HomeFragment;
import com.example.newgoodbooks.Fragments.Listas;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.newgoodbooks.databinding.ActivityPrincipalBinding;

public class Principal extends AppCompatActivity {

    private ActivityPrincipalBinding binding;
    private HomeFragment home;
    private Explorar explorar;
    private Listas listas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.explorar, R.id.listas)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_principal);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
}