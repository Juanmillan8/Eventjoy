package com.example.eventjoy.services;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.models.UserEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UserEventServiceTest {

    @Mock
    FirebaseDatabase mockFirebaseDatabase;

    @Mock
    DatabaseReference mockBaseReference;

    @Mock
    DatabaseReference mockUserEventsReference;

    @Mock
    DatabaseReference mockNewUserEventReference;

    UserEventService userEventService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configuramos la referencia base y obtenemos la rama "userEvents"
        when(mockFirebaseDatabase.getReference()).thenReturn(mockBaseReference);
        when(mockBaseReference.child("userEvents")).thenReturn(mockUserEventsReference);
        // Para joinTheEvent, stubear push() de userEvents:
        when(mockUserEventsReference.push()).thenReturn(mockNewUserEventReference);

        userEventService = new UserEventService(mockFirebaseDatabase);
    }

    // 1. deleteUserEvent: se debe llamar a removeValue() sobre la referencia hija a partir del id del UserEvent.
    @Test
    public void testDeleteUserEvent() {
        UserEvent ue = new UserEvent();
        ue.setId("ue1");

        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockUserEventsReference.child("ue1")).thenReturn(mockChildRef);

        userEventService.deleteUserEvent(ue);

        verify(mockChildRef).removeValue();
    }

    // 2. checkMemberIsParticipant: Caso en que el usuario es participante (se encuentra el UserEvent con el userId indicado).
    @Test
    public void testCheckMemberIsParticipant_Participant() {
        String eventId = "e1";
        String userId = "user1";
        SimpleCallback callback = mock(SimpleCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("eventId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(eventId)).thenReturn(mockQuery);

        userEventService.checkMemberIsParticipant(eventId, userId, callback);

        // Capturamos el listener asignado a la consulta.
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos un DataSnapshot que contiene un UserEvent con el mismo userId y eventId.
        DataSnapshot snapshot = mock(DataSnapshot.class);
        UserEvent userEvent = new UserEvent();
        userEvent.setId("ue2");
        userEvent.setUserId(userId);      // El usuario coincide.
        userEvent.setEventId(eventId);    // Ahora también se asigna el eventId correcto.

        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(UserEvent.class)).thenReturn(userEvent);

        List<DataSnapshot> children = new ArrayList<>();
        children.add(childSnapshot);
        when(snapshot.getChildren()).thenReturn(children);
        when(snapshot.exists()).thenReturn(true);

        listener.onDataChange(snapshot);

        // Se espera que se invoque callback.onSuccess("Participant")
        verify(callback).onSuccess(eq("Participant"));
    }


    // 3. checkMemberIsParticipant: Caso en que el usuario no es participante.
    @Test
    public void testCheckMemberIsParticipant_NoParticipant() {
        String eventId = "e1";
        String userId = "user1";
        SimpleCallback callback = mock(SimpleCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("eventId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(eventId)).thenReturn(mockQuery);

        userEventService.checkMemberIsParticipant(eventId, userId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos un DataSnapshot con un UserEvent cuyo userId es diferente.
        DataSnapshot snapshot = mock(DataSnapshot.class);
        UserEvent userEvent = new UserEvent();
        userEvent.setId("ue3");
        userEvent.setUserId("anotherUser");
        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(UserEvent.class)).thenReturn(userEvent);

        List<DataSnapshot> children = new ArrayList<>();
        children.add(childSnapshot);
        when(snapshot.getChildren()).thenReturn(children);
        when(snapshot.exists()).thenReturn(true);

        listener.onDataChange(snapshot);

        // Se espera que se invoque callback.onSuccess("NoParticipant")
        verify(callback).onSuccess(eq("NoParticipant"));
    }

    // 4. checkMemberIsParticipant: Caso en que ocurre error en la consulta (onCancelled).
    @Test
    public void testCheckMemberIsParticipant_onCancelled() {
        String eventId = "e1";
        String userId = "user1";
        SimpleCallback callback = mock(SimpleCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("eventId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(eventId)).thenReturn(mockQuery);

        userEventService.checkMemberIsParticipant(eventId, userId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        DatabaseError error = DatabaseError.fromException(new Exception("Test error"));
        listener.onCancelled(error);

        // Se espera que se invoque callback.onCancelled(...) con un mensaje que contiene "Test error"
        verify(callback).onCancelled(contains("Test error"));
    }

    // 5. getByEventId: Caso en que se usa listenForChanges = false y se llama a addListenerForSingleValueEvent.
    @Test
    public void testGetByEventId_singleValue() {
        String eventId = "e2";
        ValueEventListener dummyListener = mock(ValueEventListener.class);

        Query mockQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("eventId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(eventId)).thenReturn(mockQuery);

        userEventService.getByEventId(eventId, false, dummyListener);

        verify(mockQuery).addListenerForSingleValueEvent(dummyListener);
    }

    // 6. getByEventId: Caso en que se usa listenForChanges = true y se llama a addValueEventListener.
    @Test
    public void testGetByEventId_listenForChanges() {
        String eventId = "e2";
        ValueEventListener dummyListener = mock(ValueEventListener.class);

        Query mockQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("eventId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(eventId)).thenReturn(mockQuery);

        userEventService.getByEventId(eventId, true, dummyListener);

        verify(mockQuery).addValueEventListener(dummyListener);
    }

    // 7. leaveEvent: Se simula que se encuentra el UserEvent adecuado y se llama a removeValue() sobre su referencia.
    @Test
    public void testLeaveEvent() {
        String eventId = "e3";
        String userId = "userX";
        SimpleCallback callback = mock(SimpleCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("eventId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(eventId)).thenReturn(mockQuery);

        // Preconfiguramos la referencia hija para "ue4" antes de ejecutar el onDataChange:
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockUserEventsReference.child("ue4")).thenReturn(mockChildRef);

        userEventService.leaveEvent(eventId, userId, callback);

        // Capturamos el ValueEventListener asignado.
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos un DataSnapshot con un UserEvent que coincide en userId.
        DataSnapshot snapshot = mock(DataSnapshot.class);
        UserEvent foundUE = new UserEvent();
        foundUE.setId("ue4");
        foundUE.setUserId(userId);
        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(UserEvent.class)).thenReturn(foundUE);
        List<DataSnapshot> children = new ArrayList<>();
        children.add(childSnapshot);
        when(snapshot.getChildren()).thenReturn(children);
        when(snapshot.exists()).thenReturn(true);

        // Ahora, al ejecutar el onDataChange, el service llamará a removeValue()
        listener.onDataChange(snapshot);

        // Se espera que se invoque removeValue() sobre la referencia hija y luego callback.onSuccess.
        verify(mockChildRef).removeValue();
        verify(callback).onSuccess(contains("no longer part"));
    }


    // 8. joinTheEvent: Se prueba que se use push(), se asigne el id y se invoque setValue().
    @Test
    public void testJoinTheEvent() {
        UserEvent ue = new UserEvent();
        // Simulamos que el push() retorna una referencia con key "ue5"
        when(mockNewUserEventReference.getKey()).thenReturn("ue5");

        String returnedId = userEventService.joinTheEvent(ue);

        verify(mockNewUserEventReference).setValue(ue);
        assertEquals("ue5", returnedId);
        assertEquals("ue5", ue.getId());
    }
}
