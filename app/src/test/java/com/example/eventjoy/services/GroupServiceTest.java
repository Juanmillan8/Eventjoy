package com.example.eventjoy.services;

import static org.mockito.Mockito.*;

import com.example.eventjoy.models.Group;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GroupServiceTest {

    @Mock
    FirebaseDatabase mockFirebaseDatabase;

    @Mock
    DatabaseReference mockGroupsReference;

    @Mock
    DatabaseReference mockNewGroupReference;

    GroupService groupService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mockear la referencia a "groups"
        when(mockFirebaseDatabase.getReference()).thenReturn(mockGroupsReference);
        when(mockGroupsReference.child("groups")).thenReturn(mockGroupsReference);

        // Mockear push() para devolver una nueva referencia simulada
        when(mockGroupsReference.push()).thenReturn(mockNewGroupReference);

        groupService = new GroupService(mockFirebaseDatabase);
    }

    @Test
    public void insertGroup_shouldSetIdAndCallSetValue() {
        Group group = new Group();
        // No tiene id todavía

        // Mock que devuelve la clave cuando se llama a getKey() en la nueva referencia
        when(mockNewGroupReference.getKey()).thenReturn("mockGroupId");

        // Ejecutamos método a testear
        String generatedId = groupService.insertGroup(group);

        // Verificamos que se ha llamado a setValue en la nueva referencia
        verify(mockNewGroupReference).setValue(group);

        // Verificamos que el id ha sido asignado al grupo
        assert(generatedId.equals("mockGroupId"));
        assert(group.getId().equals("mockGroupId"));
    }



}