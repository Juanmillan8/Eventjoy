package com.example.eventjoy.services;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.eventjoy.callbacks.EventsCallback;
import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.callbacks.SimpleCallbackOnError;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.UserEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EventServiceTest {

    @Mock
    FirebaseDatabase mockFirebaseDatabase;
    @Mock
    DatabaseReference mockBaseReference;
    @Mock
    DatabaseReference mockEventReference;
    @Mock
    DatabaseReference mockUserEventsReference;
    @Mock
    DatabaseReference mockNewEventReference;

    EventService eventService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configuramos la referencia base y sus ramas
        when(mockFirebaseDatabase.getReference()).thenReturn(mockBaseReference);
        when(mockBaseReference.child("events")).thenReturn(mockEventReference);
        when(mockBaseReference.child("userEvents")).thenReturn(mockUserEventsReference);
        // Para insertEvent, stubear push() en "events"
        when(mockEventReference.push()).thenReturn(mockNewEventReference);
        eventService = new EventService(mockFirebaseDatabase);
    }

    // 1. deleteEvent: debe llamar a removeValue() sobre la referencia hija del evento.
    @Test
    public void testDeleteEvent() {
        Event event = new Event();
        event.setId("e1");

        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockEventReference.child("e1")).thenReturn(mockChildRef);

        eventService.deleteEvent(event);

        verify(mockChildRef).removeValue();
    }

    // 2. updateEvent: se debe llamar a setValue() sobre la referencia hija respectiva.
    @Test
    public void testUpdateEvent() {
        Event event = new Event();
        event.setId("e2");

        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockEventReference.child("e2")).thenReturn(mockChildRef);

        eventService.updateEvent(event);

        verify(mockChildRef).setValue(event);
    }

    // 3. insertEvent: se asigna el id obtenido del push y se llama a setValue().
    @Test
    public void testInsertEvent() {
        Event event = new Event();
        when(mockNewEventReference.getKey()).thenReturn("e3");

        String returnedId = eventService.insertEvent(event);

        verify(mockNewEventReference).setValue(event);
        assertEquals("e3", returnedId);
        assertEquals("e3", event.getId());
    }

    // 4. isUserRegisteredInOngoingEvent: Se simula un escenario en el que existe un evento vigente
    // y en la consulta interna de userEvents se encuentra un UserEvent cuyo userId coincide.
    @Test
    public void testIsUserRegisteredInOngoingEvent_UserRegistered() {
        String groupId = "g1";
        String userId = "user1";
        SimpleCallback callback = mock(SimpleCallback.class);

        // Stub para la query de events: filtering por "groupId"
        Query mockEventsQuery = mock(Query.class);
        when(mockEventReference.orderByChild("groupId")).thenReturn(mockEventsQuery);
        when(mockEventsQuery.equalTo(groupId)).thenReturn(mockEventsQuery);

        // *** Configuramos ya el stub para la query en userEvents ***
        Query mockUserEventsQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("eventId")).thenReturn(mockUserEventsQuery);
        // Usamos el id del evento que simularemos ("e1")
        when(mockUserEventsQuery.equalTo("e1")).thenReturn(mockUserEventsQuery);

        // Llamamos al méto_do a testear.
        eventService.isUserRegisteredInOngoingEvent(groupId, userId, callback);

        // Capturamos el listener del outer query (eventos).
        ArgumentCaptor<ValueEventListener> eventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockEventsQuery).addListenerForSingleValueEvent(eventsCaptor.capture());
        ValueEventListener outerListener = eventsCaptor.getValue();

        // Simulamos un DataSnapshot que contiene un único evento "ongoing".
        DataSnapshot outerSnapshot = mock(DataSnapshot.class);
        Event ongoingEvent = new Event();
        ongoingEvent.setId("e1");
        // Fechas que aseguran que el evento está vigente (por ejemplo, inicio en 2000 y fin en 3000)
        ongoingEvent.setStartDateAndTime("2000-01-01T00:00:00Z");
        ongoingEvent.setEndDateAndTime("3000-01-01T00:00:00Z");
        ongoingEvent.setGroupId(groupId);

        DataSnapshot eventChildSnapshot = mock(DataSnapshot.class);
        when(eventChildSnapshot.getValue(Event.class)).thenReturn(ongoingEvent);
        when(outerSnapshot.getChildren()).thenReturn(Collections.singletonList(eventChildSnapshot));
        when(outerSnapshot.exists()).thenReturn(true);

        // Disparamos el callback del outer query.
        outerListener.onDataChange(outerSnapshot);

        // Capturamos el listener asignado a la query en userEvents.
        ArgumentCaptor<ValueEventListener> userEventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockUserEventsQuery).addListenerForSingleValueEvent(userEventsCaptor.capture());
        ValueEventListener innerListener = userEventsCaptor.getValue();

        // Simulamos un DataSnapshot para userEvents que contiene un UserEvent con userId "user1"
        DataSnapshot innerSnapshot = mock(DataSnapshot.class);
        UserEvent userEvent = new UserEvent();
        userEvent.setUserId(userId);
        userEvent.setEventId("e1");
        DataSnapshot userEventSnapshot = mock(DataSnapshot.class);
        when(userEventSnapshot.getValue(UserEvent.class)).thenReturn(userEvent);
        when(innerSnapshot.getChildren()).thenReturn(Collections.singletonList(userEventSnapshot));
        when(innerSnapshot.exists()).thenReturn(true);

        // Disparamos el callback del inner listener.
        innerListener.onDataChange(innerSnapshot);

        // Verificamos que se invoque callback.onSuccess("true")
        verify(callback).onSuccess("true");
    }


    // 5. checkOngoingEvent: Se simula la existencia de un evento vigente en un grupo.
    @Test
    public void testCheckOngoingEvent() {
        String groupId = "g2";
        SimpleCallback callback = mock(SimpleCallback.class);

        Query mockEventsQuery = mock(Query.class);
        when(mockEventReference.orderByChild("groupId")).thenReturn(mockEventsQuery);
        when(mockEventsQuery.equalTo(groupId)).thenReturn(mockEventsQuery);

        eventService.checkOngoingEvent(groupId, callback);

        ArgumentCaptor<ValueEventListener> eventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockEventsQuery).addListenerForSingleValueEvent(eventsCaptor.capture());
        ValueEventListener listener = eventsCaptor.getValue();

        DataSnapshot snapshot = mock(DataSnapshot.class);
        Event event = new Event();
        event.setStartDateAndTime("2000-01-01T00:00:00Z");
        event.setEndDateAndTime("3000-01-01T00:00:00Z");
        DataSnapshot eventSnap = mock(DataSnapshot.class);
        when(eventSnap.getValue(Event.class)).thenReturn(event);
        when(snapshot.getChildren()).thenReturn(Collections.singletonList(eventSnap));
        when(snapshot.exists()).thenReturn(true);

        listener.onDataChange(snapshot);

        verify(callback).onSuccess("true");
    }

    // 6. getByMemberId: Se simula la cadena de consultas para obtener eventos a partir de los userEvents.
    @Test
    public void testGetByMemberId() {
        String memberId = "m1";
        EventsCallback callback = mock(EventsCallback.class);

        // Stub para userEvents query filtrado por "userId"
        Query mockUserEventsQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("userId")).thenReturn(mockUserEventsQuery);
        when(mockUserEventsQuery.equalTo(memberId)).thenReturn(mockUserEventsQuery);

        eventService.getByMemberId(memberId, callback);

        // Capturamos el listener asignado a userEvents
        ArgumentCaptor<ValueEventListener> userEventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockUserEventsQuery).addValueEventListener(userEventsCaptor.capture());
        ValueEventListener userEventsListener = userEventsCaptor.getValue();

        DataSnapshot userEventsSnapshot = mock(DataSnapshot.class);
        UserEvent ue = new UserEvent();
        ue.setEventId("e100");
        DataSnapshot ueSnapshot = mock(DataSnapshot.class);
        when(ueSnapshot.getValue(UserEvent.class)).thenReturn(ue);
        when(userEventsSnapshot.getChildren()).thenReturn(Collections.singletonList(ueSnapshot));
        when(userEventsSnapshot.exists()).thenReturn(true);

        userEventsListener.onDataChange(userEventsSnapshot);

        ArgumentCaptor<ValueEventListener> eventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockEventReference).addValueEventListener(eventsCaptor.capture());
        ValueEventListener eventsListener = eventsCaptor.getValue();

        DataSnapshot eventsSnapshot = mock(DataSnapshot.class);
        Event event = new Event();
        event.setId("e100");
        DataSnapshot eventSnap = mock(DataSnapshot.class);
        when(eventSnap.getValue(Event.class)).thenReturn(event);
        when(eventSnap.getKey()).thenReturn("e100");
        when(eventsSnapshot.getChildren()).thenReturn(Collections.singletonList(eventSnap));

        eventsListener.onDataChange(eventsSnapshot);

        verify(callback).onSuccess(argThat(list -> list.size() == 1 && list.get(0).getId().equals("e100")));
    }

    // 7. getByGroupId: Se simula la consulta de eventos filtrados por groupId
    @Test
    public void testGetByGroupId() {
        String groupId = "g3";
        EventsCallback callback = mock(EventsCallback.class);

        Query mockEventsQuery = mock(Query.class);
        when(mockEventReference.orderByChild("groupId")).thenReturn(mockEventsQuery);
        when(mockEventsQuery.equalTo(groupId)).thenReturn(mockEventsQuery);

        eventService.getByGroupId(groupId, callback);

        ArgumentCaptor<ValueEventListener> eventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockEventsQuery).addValueEventListener(eventsCaptor.capture());
        ValueEventListener listener = eventsCaptor.getValue();

        DataSnapshot snapshot = mock(DataSnapshot.class);
        Event event = new Event();
        event.setId("e200");
        DataSnapshot eventSnap = mock(DataSnapshot.class);
        when(eventSnap.getValue(Event.class)).thenReturn(event);
        when(snapshot.getChildren()).thenReturn(Collections.singletonList(eventSnap));
        when(snapshot.exists()).thenReturn(true);

        listener.onDataChange(snapshot);

        verify(callback).onSuccess(argThat(list -> list.size() == 1 && list.get(0).getId().equals("e200")));
    }

    // 8. checkOverlapingEvents: Cuando existe superposición (overlap) entre el eventCheck y un evento registrado.
    @Test
    public void testCheckOverlapingEvents_WithOverlap() {
        String memberId = "m1";
        // eventCheck: 2023-10-10T10:00:00Z a 2023-10-10T12:00:00Z
        Event eventCheck = new Event();
        eventCheck.setId("e_override");
        eventCheck.setStartDateAndTime("2023-10-10T10:00:00Z");
        eventCheck.setEndDateAndTime("2023-10-10T12:00:00Z");

        SimpleCallbackOnError callback = mock(SimpleCallbackOnError.class);

        Query mockUserEventsQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("userId")).thenReturn(mockUserEventsQuery);
        when(mockUserEventsQuery.equalTo(memberId)).thenReturn(mockUserEventsQuery);

        eventService.checkOverlapingEvents(memberId, eventCheck, callback);

        ArgumentCaptor<ValueEventListener> userEventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockUserEventsQuery).addListenerForSingleValueEvent(userEventsCaptor.capture());
        ValueEventListener userEventsListener = userEventsCaptor.getValue();

        DataSnapshot userEventsSnapshot = mock(DataSnapshot.class);
        UserEvent userEvent = new UserEvent();
        userEvent.setEventId("e1");
        DataSnapshot ueSnapshot = mock(DataSnapshot.class);
        when(ueSnapshot.getValue(UserEvent.class)).thenReturn(userEvent);
        when(userEventsSnapshot.getChildren()).thenReturn(Collections.singletonList(ueSnapshot));
        when(userEventsSnapshot.exists()).thenReturn(true);

        userEventsListener.onDataChange(userEventsSnapshot);

        ArgumentCaptor<ValueEventListener> eventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockEventReference).addListenerForSingleValueEvent(eventsCaptor.capture());
        ValueEventListener eventsListener = eventsCaptor.getValue();

        // Simulamos un evento existente que se superpone: de 2023-10-10T09:00:00Z a 2023-10-10T11:00:00Z
        Event existingEvent = new Event();
        existingEvent.setId("e1");
        existingEvent.setGroupId("someGroup");
        existingEvent.setStartDateAndTime("2023-10-10T09:00:00Z");
        existingEvent.setEndDateAndTime("2023-10-10T11:00:00Z");

        DataSnapshot eventsSnapshot = mock(DataSnapshot.class);
        DataSnapshot eventSnapshot = mock(DataSnapshot.class);
        when(eventSnapshot.getValue(Event.class)).thenReturn(existingEvent);
        when(eventSnapshot.getKey()).thenReturn("e1");
        when(eventsSnapshot.getChildren()).thenReturn(Collections.singletonList(eventSnapshot));
        when(eventsSnapshot.exists()).thenReturn(true);

        eventsListener.onDataChange(eventsSnapshot);

        verify(callback).onError("This event overlaps with another event you have registered for");
    }

    // 9. checkOverlapingEvents: No hay solapamiento.
    @Test
    public void testCheckOverlapingEvents_NoOverlap() {
        String memberId = "m1";
        // eventCheck: 2023-10-10T10:00:00Z a 2023-10-10T12:00:00Z
        Event eventCheck = new Event();
        eventCheck.setId("e_override");
        eventCheck.setStartDateAndTime("2023-10-10T10:00:00Z");
        eventCheck.setEndDateAndTime("2023-10-10T12:00:00Z");

        SimpleCallbackOnError callback = mock(SimpleCallbackOnError.class);

        Query mockUserEventsQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("userId")).thenReturn(mockUserEventsQuery);
        when(mockUserEventsQuery.equalTo(memberId)).thenReturn(mockUserEventsQuery);

        eventService.checkOverlapingEvents(memberId, eventCheck, callback);

        ArgumentCaptor<ValueEventListener> userEventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockUserEventsQuery).addListenerForSingleValueEvent(userEventsCaptor.capture());
        ValueEventListener userEventsListener = userEventsCaptor.getValue();

        DataSnapshot userEventsSnapshot = mock(DataSnapshot.class);
        UserEvent userEvent = new UserEvent();
        userEvent.setEventId("e1");
        DataSnapshot ueSnapshot = mock(DataSnapshot.class);
        when(ueSnapshot.getValue(UserEvent.class)).thenReturn(userEvent);
        when(userEventsSnapshot.getChildren()).thenReturn(Collections.singletonList(ueSnapshot));
        when(userEventsSnapshot.exists()).thenReturn(true);

        userEventsListener.onDataChange(userEventsSnapshot);

        ArgumentCaptor<ValueEventListener> eventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockEventReference).addListenerForSingleValueEvent(eventsCaptor.capture());
        ValueEventListener eventsListener = eventsCaptor.getValue();

        // Evento existente que NO se superpone: de 2023-10-10T12:30:00Z a 2023-10-10T14:00:00Z
        Event existingEvent = new Event();
        existingEvent.setId("e1");
        existingEvent.setGroupId("someGroup");
        existingEvent.setStartDateAndTime("2023-10-10T12:30:00Z");
        existingEvent.setEndDateAndTime("2023-10-10T14:00:00Z");

        DataSnapshot eventsSnapshot = mock(DataSnapshot.class);
        DataSnapshot eventSnapshot = mock(DataSnapshot.class);
        when(eventSnapshot.getValue(Event.class)).thenReturn(existingEvent);
        when(eventSnapshot.getKey()).thenReturn("e1");
        when(eventsSnapshot.getChildren()).thenReturn(Collections.singletonList(eventSnapshot));
        when(eventsSnapshot.exists()).thenReturn(true);

        eventsListener.onDataChange(eventsSnapshot);

        verify(callback).onError(null);
    }

    // 10. checkOverlapingEvents: Cuando no hay userEvents registrados para el usuario.
    @Test
    public void testCheckOverlapingEvents_NoUserEvents() {
        String memberId = "m1";
        Event eventCheck = new Event();
        eventCheck.setId("e_override");
        eventCheck.setStartDateAndTime("2023-10-10T10:00:00Z");
        eventCheck.setEndDateAndTime("2023-10-10T12:00:00Z");

        SimpleCallbackOnError callback = mock(SimpleCallbackOnError.class);

        Query mockUserEventsQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("userId")).thenReturn(mockUserEventsQuery);
        when(mockUserEventsQuery.equalTo(memberId)).thenReturn(mockUserEventsQuery);

        eventService.checkOverlapingEvents(memberId, eventCheck, callback);

        ArgumentCaptor<ValueEventListener> userEventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockUserEventsQuery).addListenerForSingleValueEvent(userEventsCaptor.capture());
        ValueEventListener userEventsListener = userEventsCaptor.getValue();

        DataSnapshot userEventsSnapshot = mock(DataSnapshot.class);
        when(userEventsSnapshot.exists()).thenReturn(false);

        userEventsListener.onDataChange(userEventsSnapshot);

        verify(callback).onError(null);
    }

    // 11. stopListening: Verifica que se remueven los listeners (si existen) y se establecen a null.
    @Test
    public void testStopListening() throws Exception {
        // Creamos listeners dummy simulados.
        ValueEventListener dummyEventsListener = mock(ValueEventListener.class);
        ValueEventListener dummyUserEventsListener = mock(ValueEventListener.class);

        // Inyectamos los listeners en el service mediante reflexión.
        Field eventsListenerField = EventService.class.getDeclaredField("eventsListener");
        eventsListenerField.setAccessible(true);
        eventsListenerField.set(eventService, dummyEventsListener);

        Field userEventsListenerField = EventService.class.getDeclaredField("userEventsListener");
        userEventsListenerField.setAccessible(true);
        userEventsListenerField.set(eventService, dummyUserEventsListener);

        eventService.stopListening();

        verify(mockEventReference).removeEventListener(dummyEventsListener);
        verify(mockUserEventsReference).removeEventListener(dummyUserEventsListener);

        assertNull(eventsListenerField.get(eventService));
        assertNull(userEventsListenerField.get(eventService));
    }
}
