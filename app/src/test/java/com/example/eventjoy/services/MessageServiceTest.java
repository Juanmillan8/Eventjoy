package com.example.eventjoy.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.eventjoy.callbacks.MessagesCallback;
import com.example.eventjoy.models.Message;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MessageServiceTest {

    @Mock
    FirebaseDatabase mockFirebaseDatabase;
    @Mock
    DatabaseReference mockBaseReference;
    @Mock
    DatabaseReference mockMessagesReference;
    @Mock
    DatabaseReference mockNewMessageReference;

    MessageService messageService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configuramos la referencia base y la rama "messages"
        when(mockFirebaseDatabase.getReference()).thenReturn(mockBaseReference);
        when(mockBaseReference.child("messages")).thenReturn(mockMessagesReference);
        // Para insertMessage: stub de push()
        when(mockMessagesReference.push()).thenReturn(mockNewMessageReference);

        messageService = new MessageService(mockFirebaseDatabase);
    }

    // 1. insertMessage: Se debe asignar el id obtenido del push() y llamar setValue().
    @Test
    public void testInsertMessage() {
        Message message = new Message();
        // Suponemos que el push retorna un id "msg1"
        when(mockNewMessageReference.getKey()).thenReturn("msg1");

        String returnedId = messageService.insertMessage(message);

        verify(mockNewMessageReference).setValue(message);
        assertEquals("msg1", returnedId);
        assertEquals("msg1", message.getId());
    }

    // 2. getByGroupId: Escenario en el que la consulta retorna mensajes
    @Test
    public void testGetByGroupId_withMessages() {
        String groupId = "group1";
        MessagesCallback callback = mock(MessagesCallback.class);

        // Stub de la query: databaseReferenceMessages.orderByChild("groupId").equalTo(groupId)
        Query mockQuery = mock(Query.class);
        when(mockMessagesReference.orderByChild("groupId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(groupId)).thenReturn(mockQuery);

        messageService.getByGroupId(groupId, callback);

        // Capturamos el ValueEventListener asignado a la query
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos un DataSnapshot con un hijo que represente un mensaje
        DataSnapshot snapshot = mock(DataSnapshot.class);
        Message msg = new Message();
        msg.setId("msg10");
        msg.setGroupId(groupId);  // Se asigna el groupId para que el mensaje corresponda a la consulta.

        // Creamos un child snapshot simulado
        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(Message.class)).thenReturn(msg);
        when(snapshot.getChildren()).thenReturn(Collections.singletonList(childSnapshot));
        when(snapshot.exists()).thenReturn(true);

        // Ejecutamos el callback del listener
        listener.onDataChange(snapshot);

        // Verificamos que se invoque callback.onSuccess con una lista que contenga el mensaje "msg10"
        verify(callback).onSuccess(argThat(list -> list.size() == 1 && list.get(0).getId().equals("msg10")));
    }


    // 3. getByGroupId: Escenario en el que no hay mensajes (lista vacía)
    @Test
    public void testGetByGroupId_emptyList() {
        String groupId = "group1";
        MessagesCallback callback = mock(MessagesCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockMessagesReference.orderByChild("groupId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(groupId)).thenReturn(mockQuery);

        messageService.getByGroupId(groupId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos lo que haría Firebase: si no hay mensajes de group1, el snapshot es vacío.
        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(snapshot.getChildren()).thenReturn(Collections.emptyList());
        when(snapshot.exists()).thenReturn(false);

        listener.onDataChange(snapshot);

        verify(callback).onSuccess(argThat(list -> list.isEmpty()));
    }


    // 4. getByGroupId: Escenario en que hay error (onCancelled)
    @Test
    public void testGetByGroupId_onCancelled() {
        String groupId = "group1";
        MessagesCallback callback = mock(MessagesCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockMessagesReference.orderByChild("groupId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(groupId)).thenReturn(mockQuery);

        messageService.getByGroupId(groupId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos error: creamos un DatabaseError usando fromException
        DatabaseError error = DatabaseError.fromException(new Exception("Test error"));
        listener.onCancelled(error);

        verify(callback).onFailure(any(Exception.class));
    }

    // 5. stopListening: Se debe remover el listener y establecerlo a null.
    @Test
    public void testStopListening() {
        String groupId = "group1";
        MessagesCallback callback = mock(MessagesCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockMessagesReference.orderByChild("groupId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(groupId)).thenReturn(mockQuery);
        messageService.getByGroupId(groupId, callback);

        // Capturamos el listener asignado a la query.
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Llamamos a stopListening() en el service.
        messageService.stopListening();

        // Verificamos que se haya llamado a removeEventListener con el listener capturado,
        // y que el campo interno se haya establecido a null.
        verify(mockMessagesReference).removeEventListener(listener);
        try {
            Field field = MessageService.class.getDeclaredField("messageListener");
            field.setAccessible(true);
            Object value = field.get(messageService);
            assertNull(value);
        } catch (Exception e) {
            fail("Error en la reflexión: " + e.getMessage());
        }
    }
}
