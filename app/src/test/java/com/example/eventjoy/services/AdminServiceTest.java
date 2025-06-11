package com.example.eventjoy.services;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.eventjoy.models.Admin;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AdminServiceTest {

    @Mock
    FirebaseDatabase mockFirebaseDatabase;

    @Mock
    DatabaseReference mockBaseReference;

    @Mock
    DatabaseReference mockAdminsReference;

    AdminService adminService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Stub a la referencia base y la rama "admins"
        when(mockFirebaseDatabase.getReference()).thenReturn(mockBaseReference);
        when(mockBaseReference.child("admins")).thenReturn(mockAdminsReference);

        // Inyectamos el FirebaseDatabase mockeado en el service
        adminService = new AdminService(mockFirebaseDatabase);
    }

    @Test
    public void testGetAdminById() {
        // Preparamos un listener simulado
        ValueEventListener listener = mock(ValueEventListener.class);
        // La consulta se construye sobre la referencia admins con orderByChild("id") y equalTo(...)
        Query mockQuery = mock(Query.class);
        when(mockAdminsReference.orderByChild("id")).thenReturn(mockQuery);
        when(mockQuery.equalTo("adminId123")).thenReturn(mockQuery);

        // Ejecutamos el méto_do
        adminService.getAdminById("adminId123", listener);

        // Verificamos que la consulta se haya configurado y se haya registrado el listener
        verify(mockQuery).addListenerForSingleValueEvent(listener);
    }

    @Test
    public void testGetAdminByUid() {
        // Preparamos un listener simulado
        ValueEventListener listener = mock(ValueEventListener.class);
        // La consulta se hace por "userAccountId" en vez de "id"
        Query mockQuery = mock(Query.class);
        when(mockAdminsReference.orderByChild("userAccountId")).thenReturn(mockQuery);
        when(mockQuery.equalTo("adminUid123")).thenReturn(mockQuery);

        // Ejecutamos el método
        adminService.getAdminByUid("adminUid123", listener);

        // Verificamos que se haya agregado el listener a la query construida
        verify(mockQuery).addListenerForSingleValueEvent(listener);
    }

    @Test
    public void testUpdateAdmin() {
        // Creamos un Admin y asignamos un id
        Admin admin = new Admin();
        admin.setId("adminId456");

        // Stub para que al llamar a child(id) en la referencia admins se obtenga un objeto simulado
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockAdminsReference.child("adminId456")).thenReturn(mockChildRef);

        // Ejecutamos el método a testear
        adminService.updateAdmin(admin);

        // Verificamos que se llame a setValue(admin) en la referencia correspondiente
        verify(mockChildRef).setValue(admin);
    }
}
