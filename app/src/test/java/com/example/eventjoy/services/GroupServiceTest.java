package com.example.eventjoy.services;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Report;
import com.example.eventjoy.models.UserEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

public class GroupServiceTest {

    // Mocks para FirebaseDatabase y cada una de las ramas (child) que utiliza el service
    @Mock
    FirebaseDatabase mockFirebaseDatabase;
    @Mock
    DatabaseReference mockBaseReference;
    @Mock
    DatabaseReference mockGroupsReference;
    @Mock
    DatabaseReference mockUserGroupsReference;
    @Mock
    DatabaseReference mockMessageReference;
    @Mock
    DatabaseReference mockInvitationReference;
    @Mock
    DatabaseReference mockReportReference;
    @Mock
    DatabaseReference mockEventReference;
    @Mock
    DatabaseReference mockUserEventsReference;
    @Mock
    DatabaseReference mockNewGroupReference; // Para simular push() de groups

    GroupService groupService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configuramos la referencia base y las ramas de Firebase
        when(mockFirebaseDatabase.getReference()).thenReturn(mockBaseReference);
        when(mockBaseReference.child("groups")).thenReturn(mockGroupsReference);
        when(mockBaseReference.child("userGroups")).thenReturn(mockUserGroupsReference);
        when(mockBaseReference.child("messages")).thenReturn(mockMessageReference);
        when(mockBaseReference.child("invitations")).thenReturn(mockInvitationReference);
        when(mockBaseReference.child("reports")).thenReturn(mockReportReference);
        when(mockBaseReference.child("events")).thenReturn(mockEventReference);
        when(mockBaseReference.child("userEvents")).thenReturn(mockUserEventsReference);

        // Cuando se invoca push() en la rama de groups, devuelvo esta referencia simulada.
        when(mockGroupsReference.push()).thenReturn(mockNewGroupReference);

        groupService = new GroupService(mockFirebaseDatabase);
    }

    @Test
    public void testInsertGroup() {
        Group group = new Group();
        // Simulamos que la nueva referencia devuelve la clave "mockGroupId"
        when(mockNewGroupReference.getKey()).thenReturn("mockGroupId");

        String generatedId = groupService.insertGroup(group);

        // Se debe invocar setValue(group) sobre la nueva referencia
        verify(mockNewGroupReference).setValue(group);
        // El id generado debe coincidir y asignarse en el modelo
        assertEquals("mockGroupId", generatedId);
        assertEquals("mockGroupId", group.getId());
    }

    @Test
    public void testGetGroupById() {
        // Creamos un listener simulado
        ValueEventListener listener = mock(ValueEventListener.class);
        // Configuramos la query: orderByChild("id") y equalTo()
        Query mockQuery = mock(Query.class);
        when(mockGroupsReference.orderByChild("id")).thenReturn(mockQuery);
        when(mockQuery.equalTo("groupId123")).thenReturn(mockQuery);

        groupService.getGroupById("groupId123", listener);

        // Verificamos que se llame a addListenerForSingleValueEvent en la query
        verify(mockQuery).addListenerForSingleValueEvent(listener);
    }

    @Test
    public void testUpdateGroup() {
        Group group = new Group();
        group.setId("groupId456");

        // Simulamos la referencia hija para el grupo a actualizar
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockGroupsReference.child("groupId456")).thenReturn(mockChildRef);

        groupService.updateGroup(group);

        // Se debe invocar setValue(group) en la referencia hija
        verify(mockChildRef).setValue(group);
    }

    @Test
    public void testDeleteGroup() {
        // Preparamos el grupo a eliminar.
        Group group = new Group();
        group.setId("groupDelete");

        // 1. Eliminación directa en la rama "groups"
        DatabaseReference mockGroupChildRef = mock(DatabaseReference.class);
        when(mockGroupsReference.child("groupDelete")).thenReturn(mockGroupChildRef);

        // 2. Configuramos las queries para las ramas asociadas:
        Query userGroupsQuery = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("groupId")).thenReturn(userGroupsQuery);
        when(userGroupsQuery.equalTo("groupDelete")).thenReturn(userGroupsQuery);

        Query messagesQuery = mock(Query.class);
        when(mockMessageReference.orderByChild("groupId")).thenReturn(messagesQuery);
        when(messagesQuery.equalTo("groupDelete")).thenReturn(messagesQuery);

        Query invitationsQuery = mock(Query.class);
        when(mockInvitationReference.orderByChild("groupId")).thenReturn(invitationsQuery);
        when(invitationsQuery.equalTo("groupDelete")).thenReturn(invitationsQuery);

        Query reportsQuery = mock(Query.class);
        when(mockReportReference.orderByChild("groupId")).thenReturn(reportsQuery);
        when(reportsQuery.equalTo("groupDelete")).thenReturn(reportsQuery);

        Query eventsQuery = mock(Query.class);
        when(mockEventReference.orderByChild("groupId")).thenReturn(eventsQuery);
        when(eventsQuery.equalTo("groupDelete")).thenReturn(eventsQuery);

        // Stub para userEvents: se utiliza un stub genérico para cualquier llamada.
        Query userEventsQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild(anyString())).thenReturn(userEventsQuery);
        when(userEventsQuery.equalTo(anyString())).thenReturn(userEventsQuery);

        // Llamamos al método deleteGroup()
        groupService.deleteGroup(group);

        // Verificamos la eliminación en "groups"
        verify(mockGroupChildRef).removeValue();

        // 3. Para "userGroups": simulamos onDataChange
        ArgumentCaptor<ValueEventListener> userGroupsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(userGroupsQuery).addListenerForSingleValueEvent(userGroupsCaptor.capture());
        ValueEventListener userGroupsListener = userGroupsCaptor.getValue();
        DataSnapshot userGroupsSnapshot = mock(DataSnapshot.class);
        when(userGroupsSnapshot.exists()).thenReturn(true);
        DataSnapshot userGroupChild = mock(DataSnapshot.class);
        DatabaseReference userGroupChildRef = mock(DatabaseReference.class);
        when(userGroupChild.getRef()).thenReturn(userGroupChildRef);
        when(userGroupsSnapshot.getChildren()).thenReturn(Collections.singletonList(userGroupChild));
        userGroupsListener.onDataChange(userGroupsSnapshot);
        verify(userGroupChildRef).removeValue();

        // 4. Para "messages":
        ArgumentCaptor<ValueEventListener> messagesCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(messagesQuery).addListenerForSingleValueEvent(messagesCaptor.capture());
        ValueEventListener messagesListener = messagesCaptor.getValue();
        DataSnapshot messagesSnapshot = mock(DataSnapshot.class);
        when(messagesSnapshot.exists()).thenReturn(true);
        DataSnapshot messageChild = mock(DataSnapshot.class);
        DatabaseReference messageChildRef = mock(DatabaseReference.class);
        when(messageChild.getRef()).thenReturn(messageChildRef);
        when(messagesSnapshot.getChildren()).thenReturn(Collections.singletonList(messageChild));
        messagesListener.onDataChange(messagesSnapshot);
        verify(messageChildRef).removeValue();

        // 5. Para "invitations":
        ArgumentCaptor<ValueEventListener> invitationsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(invitationsQuery).addListenerForSingleValueEvent(invitationsCaptor.capture());
        ValueEventListener invitationsListener = invitationsCaptor.getValue();
        DataSnapshot invitationsSnapshot = mock(DataSnapshot.class);
        when(invitationsSnapshot.exists()).thenReturn(true);
        DataSnapshot invitationChild = mock(DataSnapshot.class);
        DatabaseReference invitationChildRef = mock(DatabaseReference.class);
        when(invitationChild.getRef()).thenReturn(invitationChildRef);
        when(invitationsSnapshot.getChildren()).thenReturn(Collections.singletonList(invitationChild));
        invitationsListener.onDataChange(invitationsSnapshot);
        verify(invitationChildRef).removeValue();

        // 6. Para "reports": actualizamos los reportes.
        ArgumentCaptor<ValueEventListener> reportsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(reportsQuery).addListenerForSingleValueEvent(reportsCaptor.capture());
        ValueEventListener reportsListener = reportsCaptor.getValue();
        DataSnapshot reportsSnapshot = mock(DataSnapshot.class);
        when(reportsSnapshot.exists()).thenReturn(true);
        DataSnapshot reportChildSnapshot = mock(DataSnapshot.class);
        Report report = new Report();
        report.setId("report1");
        report.setGroupId("groupDelete");
        when(reportChildSnapshot.getValue(Report.class)).thenReturn(report);
        when(reportsSnapshot.getChildren()).thenReturn(Collections.singletonList(reportChildSnapshot));
        // Stub para que al llamar a child("report1") se devuelva un ref simulado.
        DatabaseReference reportChildRef = mock(DatabaseReference.class);
        when(mockReportReference.child("report1")).thenReturn(reportChildRef);
        reportsListener.onDataChange(reportsSnapshot);
        // Se espera que el report pierda su groupId y se actualice.
        assertNull(report.getGroupId());
        verify(reportChildRef).setValue(report);

        // 7. Para "events": procesamos eventos pasados y futuros.
        ArgumentCaptor<ValueEventListener> eventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(eventsQuery).addListenerForSingleValueEvent(eventsCaptor.capture());
        ValueEventListener eventsListener = eventsCaptor.getValue();
        DataSnapshot eventsSnapshot = mock(DataSnapshot.class);
        when(eventsSnapshot.exists()).thenReturn(true);

        // Definimos dos eventos: uno pasado y uno futuro.
        Event eventPast = new Event();
        eventPast.setId("eventPast");
        eventPast.setStartDateAndTime("2000-01-01T00:00:00Z"); // Evento pasado

        Event eventFuture = new Event();
        eventFuture.setId("eventFuture");
        eventFuture.setStartDateAndTime("3000-01-01T00:00:00Z"); // Evento futuro

        DataSnapshot eventPastSnapshot = mock(DataSnapshot.class);
        when(eventPastSnapshot.getValue(Event.class)).thenReturn(eventPast);
        when(eventPastSnapshot.getKey()).thenReturn("eventPast");

        DataSnapshot eventFutureSnapshot = mock(DataSnapshot.class);
        when(eventFutureSnapshot.getValue(Event.class)).thenReturn(eventFuture);
        when(eventFutureSnapshot.getKey()).thenReturn("eventFuture");

        when(eventsSnapshot.getChildren()).thenReturn(Arrays.asList(eventPastSnapshot, eventFutureSnapshot));

        // Stub para el evento pasado
        DatabaseReference eventPastChildRef = mock(DatabaseReference.class);
        when(mockEventReference.child("eventPast")).thenReturn(eventPastChildRef);

        // Antes de llamar al callback de eventos futuros, stubear la referencia para "eventFuture"
        DatabaseReference eventFutureChildRef = mock(DatabaseReference.class);
        when(mockEventReference.child("eventFuture")).thenReturn(eventFutureChildRef);

        // Llamamos al callback de events.
        eventsListener.onDataChange(eventsSnapshot);

        // Verificamos el procesamiento del evento pasado.
        verify(eventPastChildRef).setValue(eventPast);
        assertNull(eventPast.getGroupId());

        // Para el evento futuro, se usa la query de userEvents.
        // Capturamos el listener de userEvents.
        ArgumentCaptor<ValueEventListener> userEventsCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(userEventsQuery).addListenerForSingleValueEvent(userEventsCaptor.capture());
        ValueEventListener userEventsListener = userEventsCaptor.getValue();

        // Simulamos un DataSnapshot para userEvents (snapshot vacío)
        DataSnapshot userEventsSnapshot = mock(DataSnapshot.class);
        when(userEventsSnapshot.exists()).thenReturn(false);

        // Ahora, ya con la referencia stubeada, disparamos el callback
        userEventsListener.onDataChange(userEventsSnapshot);

        // Verificamos que se invoque removeValue() sobre el event futuro.
        verify(eventFutureChildRef).removeValue();
    }

}
