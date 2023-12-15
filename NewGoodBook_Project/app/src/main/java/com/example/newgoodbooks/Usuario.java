package com.example.newgoodbooks;

public class Usuario {
    String nombre;
    String nombreUser;
    String email;
    String password;
    public Usuario(){

    }
    public Usuario(String nombre, String nombreUser, String email, String password){
        this.nombre = nombre;
        this.nombreUser = nombreUser;
        this.email = email;
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreUser() {
        return nombreUser;
    }

    public void setNombreUser(String nombreUser) {
        this.nombreUser = nombreUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
