package com.example.newgoodbooks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.newgoodbooks.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class Principal extends AppCompatActivity {
    LibroFragment libroFragment=new LibroFragment();
    ExplorarFragment explorarFragment=new ExplorarFragment();
    ListasFragment listasFragment=new ListasFragment();
    BottomNavigationView menuNavegacion;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        frameLayout=findViewById(R.id.frameContainer);
        menuNavegacion=findViewById(R.id.menuNavegacion);
        menuNavegacion.setOnItemSelectedListener(item -> {
           switch (item.getItemId()){
               case R.id.itemPrincipal:
                   reemplazarFragment(libroFragment);
                   break;
               case R.id.itemExplorar:
                   reemplazarFragment(explorarFragment);
                   break;
               case R.id.itemListas:
                   reemplazarFragment(listasFragment);
                   break;
           }
           return false;
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, libroFragment).commit();
    }
    private void reemplazarFragment(Fragment fragment){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameContainer, fragment);
        fragmentTransaction.commit();
    }
}