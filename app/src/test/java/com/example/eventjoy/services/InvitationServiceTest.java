package com.example.eventjoy.services;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.eventjoy.callbacks.InvitationsCallback;
import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.models.Invitation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Collections;

public class InvitationServiceTest {

    @Mock
    FirebaseDatabase mockFirebaseDatabase;

    @Mock
    DatabaseReference mockBaseReference;

    @Mock
    DatabaseReference mockInvitationReference;

    @Mock
    DatabaseReference mockNewInvitationReference;

    InvitationService invitationService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configuramos la referencia base y la rama "invitations"
        when(mockFirebaseDatabase.getReference()).thenReturn(mockBaseReference);
        when(mockBaseReference.child("invitations")).thenReturn(mockInvitationReference);
        // Stub para push() en insertInvitation
        when(mockInvitationReference.push()).thenReturn(mockNewInvitationReference);

        invitationService = new InvitationService(mockFirebaseDatabase);
    }

    // 1. deleteInvitation: Se debe llamar a removeValue() en la referencia hija.
    @Test
    public void testDeleteInvitation() {
        Invitation invitation = new Invitation();
        invitation.setId("inv1");

        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockInvitationReference.child("inv1")).thenReturn(mockChildRef);

        invitationService.deleteInvitation(invitation);

        verify(mockChildRef).removeValue();
    }

    // 2. getInvitations: Escenario con invitaciones encontradas.
    @Test
    public void testGetInvitations_withInvitations() {
        String memberId = "member1";
        InvitationsCallback callback = mock(InvitationsCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockInvitationReference.orderByChild("invitedUserId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(memberId)).thenReturn(mockQuery);

        invitationService.getInvitations(memberId, callback);

        // Capturamos el ValueEventListener asignado a la query.
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos un DataSnapshot con un invitation.
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        Invitation invitation = new Invitation();
        invitation.setId("inv1");

        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(Invitation.class)).thenReturn(invitation);
        when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(childSnapshot));
        when(mockSnapshot.exists()).thenReturn(true);

        listener.onDataChange(mockSnapshot);

        // Se debe llamar a callback.onSuccess() con una lista que contenga el invitation.
        verify(callback).onSuccess(argThat(list -> list.size() == 1 && list.get(0).getId().equals("inv1")));
    }

    // 3. getInvitations: Escenario con lista vacía.
    @Test
    public void testGetInvitations_emptyList() {
        String memberId = "member1";
        InvitationsCallback callback = mock(InvitationsCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockInvitationReference.orderByChild("invitedUserId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(memberId)).thenReturn(mockQuery);

        invitationService.getInvitations(memberId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos un DataSnapshot sin hijos (lista vacía).
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.getChildren()).thenReturn(Collections.emptyList());
        when(mockSnapshot.exists()).thenReturn(false);
        listener.onDataChange(mockSnapshot);

        verify(callback).onSuccess(argThat(list -> list.isEmpty()));
    }

    // 4. hasAlreadyInvited: Caso en que ya se ha invitado (existe una invitación que coincide).
    @Test
    public void testHasAlreadyInvited_invited() {
        String memberId = "member1";
        String groupId = "g1";
        SimpleCallback callback = mock(SimpleCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockInvitationReference.orderByChild("invitedUserId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(memberId)).thenReturn(mockQuery);

        invitationService.hasAlreadyInvited(memberId, groupId, callback);

        // Capturamos el ValueEventListener asignado.
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos un DataSnapshot con una invitación que coincide en groupId.
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        Invitation invitation = new Invitation();
        invitation.setId("inv1");
        invitation.setGroupId(groupId);
        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(Invitation.class)).thenReturn(invitation);
        when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(childSnapshot));
        when(mockSnapshot.exists()).thenReturn(true);

        listener.onDataChange(mockSnapshot);

        verify(callback).onSuccess("true");
    }

    // 5. hasAlreadyInvited: Caso cuando no existe invitación para ese grupo.
    @Test
    public void testHasAlreadyInvited_notInvited() {
        String memberId = "member1";
        String groupId = "g1";
        SimpleCallback callback = mock(SimpleCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockInvitationReference.orderByChild("invitedUserId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(memberId)).thenReturn(mockQuery);

        invitationService.hasAlreadyInvited(memberId, groupId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos un DataSnapshot con una invitación que tenga groupId distinto.
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        Invitation invitation = new Invitation();
        invitation.setId("inv1");
        invitation.setGroupId("otherGroup");
        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(Invitation.class)).thenReturn(invitation);
        when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(childSnapshot));
        when(mockSnapshot.exists()).thenReturn(true);

        listener.onDataChange(mockSnapshot);

        verify(callback).onSuccess("false");
    }

    // 6. insertInvitation: Se asigna el id obtenido del push y se llama a setValue().
    @Test
    public void testInsertInvitation() {
        Invitation invitation = new Invitation();
        // Stub para que push() retorne un key "inv_new"
        when(mockNewInvitationReference.getKey()).thenReturn("inv_new");

        String returnedId = invitationService.insertInvitation(invitation);

        verify(mockNewInvitationReference).setValue(invitation);
        assertEquals("inv_new", returnedId);
        assertEquals("inv_new", invitation.getId());
    }

    // 7. stopListening: Se debe remover el listener y establecerlo a null.
    @Test
    public void testStopListening() throws Exception {
        // Creamos un listener dummy.
        ValueEventListener dummyListener = mock(ValueEventListener.class);
        // Inyectamos el listener en el private field usando reflexión.
        Field listenerField = InvitationService.class.getDeclaredField("invitationsListener");
        listenerField.setAccessible(true);
        listenerField.set(invitationService, dummyListener);

        invitationService.stopListening();

        verify(mockInvitationReference).removeEventListener(dummyListener);
        assertNull(listenerField.get(invitationService));
    }
}
