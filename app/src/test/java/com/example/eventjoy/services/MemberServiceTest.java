package com.example.eventjoy.services;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.models.UserGroup;
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

public class MemberServiceTest {

    @Mock
    FirebaseDatabase mockFirebaseDatabase;

    @Mock
    DatabaseReference mockBaseReference;

    @Mock
    DatabaseReference mockMembersReference;

    @Mock
    DatabaseReference mockUserEventsReference;

    @Mock
    DatabaseReference mockUserGroupsReference;

    @Mock
    DatabaseReference mockNewMemberReference;

    MemberService memberService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Stubear la referencia base y las ramas usadas en el MemberService.
        when(mockFirebaseDatabase.getReference()).thenReturn(mockBaseReference);
        when(mockBaseReference.child("members")).thenReturn(mockMembersReference);
        when(mockBaseReference.child("userEvents")).thenReturn(mockUserEventsReference);
        when(mockBaseReference.child("userGroups")).thenReturn(mockUserGroupsReference);

        // Para insertMember, stubear push() en members.
        when(mockMembersReference.push()).thenReturn(mockNewMemberReference);

        memberService = new MemberService(mockMembersReference, mockUserEventsReference, mockUserGroupsReference);
    }

    // 1. insertMember: Se asigna el id al miembro y se invoca setValue().
    @Test
    public void testInsertMember() {
        Member member = new Member();
        // Stub: simular el retorno de push() con key "m1"
        when(mockNewMemberReference.getKey()).thenReturn("m1");

        String returnedId = memberService.insertMember(member);

        verify(mockNewMemberReference).setValue(member);
        assertEquals("m1", returnedId);
        assertEquals("m1", member.getId());
    }

    // 2. getMembersNotInGroup: Se simula una consulta en userGroups para obtener los IDs ya asociados
    // y luego se consulta members para retornar solo los que NO están en ese grupo.
    @Test
    public void testGetMembersNotInGroup() {
        String groupId = "group1";
        MembersCallback callback = mock(MembersCallback.class);

        // Stub para la query en userGroups: orderByChild("groupId").equalTo(groupId)
        Query mockUserGroupsQuery = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("groupId")).thenReturn(mockUserGroupsQuery);
        when(mockUserGroupsQuery.equalTo(groupId)).thenReturn(mockUserGroupsQuery);

        // Llamada al méto_do.
        memberService.getMembersNotInGroup(groupId, callback);

        // Capturamos el listener asignado a la consulta en userGroups.
        ArgumentCaptor<ValueEventListener> ugListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockUserGroupsQuery).addValueEventListener(ugListenerCaptor.capture());
        ValueEventListener ugListener = ugListenerCaptor.getValue();

        // Simulamos un DataSnapshot en userGroups: por ejemplo, que ya está en el grupo un UserGroup con userId "m2".
        DataSnapshot ugSnapshot = mock(DataSnapshot.class);
        UserGroup ug = new UserGroup();
        ug.setUserId("m2");
        DataSnapshot ugChild = mock(DataSnapshot.class);
        when(ugChild.getValue(UserGroup.class)).thenReturn(ug);
        when(ugSnapshot.getChildren()).thenReturn(Collections.singletonList(ugChild));
        when(ugSnapshot.exists()).thenReturn(true);

        ugListener.onDataChange(ugSnapshot);

        // Luego se consulta en members. Capturamos el listener asignado a la consulta en members.
        ArgumentCaptor<ValueEventListener> mListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockMembersReference).addValueEventListener(mListenerCaptor.capture());
        ValueEventListener mListener = mListenerCaptor.getValue();

        // Simulamos un DataSnapshot de members: dos miembros, uno con id "m1" y otro con "m2".
        DataSnapshot membersSnapshot = mock(DataSnapshot.class);
        Member member1 = new Member();
        member1.setId("m1");
        Member member2 = new Member();
        member2.setId("m2");
        DataSnapshot mSnapshot1 = mock(DataSnapshot.class);
        DataSnapshot mSnapshot2 = mock(DataSnapshot.class);
        when(mSnapshot1.getValue(Member.class)).thenReturn(member1);
        when(mSnapshot2.getValue(Member.class)).thenReturn(member2);
        List<DataSnapshot> memberChildren = new ArrayList<>();
        memberChildren.add(mSnapshot1);
        memberChildren.add(mSnapshot2);
        when(membersSnapshot.getChildren()).thenReturn(memberChildren);

        mListener.onDataChange(membersSnapshot);

        // Se espera que retorne solo el miembro "m1", ya que "m2" ya está en el grupo.
        verify(callback).onSuccess(argThat(list -> list.size() == 1 && list.get(0).getId().equals("m1")));
    }

    // 3. getByEventId: Se simula la consulta en userEvents para obtener un userId y luego se consulta members para retornar los correspondientes.
    @Test
    public void testGetByEventId() {
        String eventId = "e10";
        MembersCallback callback = mock(MembersCallback.class);

        // Stub para la query en userEvents: orderByChild("eventId").equalTo(eventId)
        Query mockUserEventsQuery = mock(Query.class);
        when(mockUserEventsReference.orderByChild("eventId")).thenReturn(mockUserEventsQuery);
        when(mockUserEventsQuery.equalTo(eventId)).thenReturn(mockUserEventsQuery);

        memberService.getByEventId(eventId, callback);

        // Capturamos el listener asignado a la consulta en userEvents.
        ArgumentCaptor<ValueEventListener> ueListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockUserEventsQuery).addValueEventListener(ueListenerCaptor.capture());
        ValueEventListener ueListener = ueListenerCaptor.getValue();

        // Simulamos un DataSnapshot de userEvents que retorna un UserEvent con userId "m3".
        DataSnapshot ueSnapshot = mock(DataSnapshot.class);
        UserEvent ue = new UserEvent();
        ue.setUserId("m3");
        DataSnapshot ueChild = mock(DataSnapshot.class);
        when(ueChild.getValue(UserEvent.class)).thenReturn(ue);
        when(ueSnapshot.getChildren()).thenReturn(Collections.singletonList(ueChild));
        when(ueSnapshot.exists()).thenReturn(true);

        ueListener.onDataChange(ueSnapshot);

        // Ahora se consulta en la rama de members.
        ArgumentCaptor<ValueEventListener> mListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockMembersReference).addValueEventListener(mListenerCaptor.capture());
        ValueEventListener mListener = mListenerCaptor.getValue();

        // Simulamos un DataSnapshot de members que contiene dos miembros: uno con id "m3" y otro con "m4".
        DataSnapshot mSnapshot = mock(DataSnapshot.class);
        Member member3 = new Member();
        member3.setId("m3");
        Member member4 = new Member();
        member4.setId("m4");
        DataSnapshot mSnap1 = mock(DataSnapshot.class);
        DataSnapshot mSnap2 = mock(DataSnapshot.class);
        when(mSnap1.getValue(Member.class)).thenReturn(member3);
        when(mSnap2.getValue(Member.class)).thenReturn(member4);
        List<DataSnapshot> memberChildren = new ArrayList<>();
        memberChildren.add(mSnap1);
        memberChildren.add(mSnap2);
        when(mSnapshot.getChildren()).thenReturn(memberChildren);

        mListener.onDataChange(mSnapshot);

        // Se espera que retorne solo el miembro con id "m3"
        verify(callback).onSuccess(argThat(list -> list.size() == 1 && list.get(0).getId().equals("m3")));
    }

    // 4. getMemberById: Se verifica que se construya correctamente la consulta y se registre el listener.
    @Test
    public void testGetMemberById() {
        ValueEventListener listener = mock(ValueEventListener.class);
        String memberId = "m5";

        Query mockQuery = mock(Query.class);
        when(mockMembersReference.orderByChild("id")).thenReturn(mockQuery);
        when(mockQuery.equalTo(memberId)).thenReturn(mockQuery);

        memberService.getMemberById(memberId, listener);

        verify(mockQuery).addListenerForSingleValueEvent(listener);
    }

    // 5. getMemberByUid: Se verifica la construcción de consulta usando "userAccountId".
    @Test
    public void testGetMemberByUid() {
        ValueEventListener listener = mock(ValueEventListener.class);
        String memberUid = "uid123";

        Query mockQuery = mock(Query.class);
        when(mockMembersReference.orderByChild("userAccountId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(memberUid)).thenReturn(mockQuery);

        memberService.getMemberByUid(memberUid, listener);

        verify(mockQuery).addListenerForSingleValueEvent(listener);
    }

    // 6. checkRepeatedDNI: Se verifica que se construya la consulta correctamente.
    @Test
    public void testCheckRepeatedDNI() {
        String dni = "12345678A";
        ValueEventListener listener = mock(ValueEventListener.class);

        Query mockQuery = mock(Query.class);
        when(mockMembersReference.orderByChild("dni")).thenReturn(mockQuery);
        when(mockQuery.equalTo(dni)).thenReturn(mockQuery);

        memberService.checkRepeatedDNI(dni, listener);
        verify(mockQuery).addListenerForSingleValueEvent(listener);
    }

    // 7. checkRepeatedUsername: Se verifica la construcción de la consulta usando "username"
    @Test
    public void testCheckRepeatedUsername() {
        String username = "userTest";
        ValueEventListener listener = mock(ValueEventListener.class);

        Query mockQuery = mock(Query.class);
        when(mockMembersReference.orderByChild("username")).thenReturn(mockQuery);
        when(mockQuery.equalTo(username)).thenReturn(mockQuery);

        memberService.checkRepeatedUsername(username, listener);
        verify(mockQuery).addListenerForSingleValueEvent(listener);
    }

    // 8. deleteMemberById: Se verifica que se invoque removeValue() sobre la referencia hija correspondiente.
    @Test
    public void testDeleteMemberById() {
        String memberId = "m6";
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockMembersReference.child(memberId)).thenReturn(mockChildRef);

        memberService.deleteMemberById(memberId);
        verify(mockChildRef).removeValue();
    }

    // 9. updateMember: Se verifica que se invoque setValue() en la referencia hija identificada por el id.
    @Test
    public void testUpdateMember() {
        Member member = new Member();
        member.setId("m7");
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockMembersReference.child("m7")).thenReturn(mockChildRef);

        memberService.updateMember(member);
        verify(mockChildRef).setValue(member);
    }

    // 10. stopListening: Se verifica que se removieron los listeners y que los campos se establecieron a null.
    @Test
    public void testStopListening() throws Exception {
        // Creamos listeners dummy.
        ValueEventListener dummyUserEventListener = mock(ValueEventListener.class);
        ValueEventListener dummyMemberListener = mock(ValueEventListener.class);
        ValueEventListener dummyUserGroupsListener = mock(ValueEventListener.class);

        // Inyectamos estos listeners en los campos privados mediante reflexión.
        Field ueField = MemberService.class.getDeclaredField("userEventListener");
        Field mField = MemberService.class.getDeclaredField("memberListener");
        Field ugField = MemberService.class.getDeclaredField("userGroupsListener");
        ueField.setAccessible(true);
        mField.setAccessible(true);
        ugField.setAccessible(true);
        ueField.set(memberService, dummyUserEventListener);
        mField.set(memberService, dummyMemberListener);
        ugField.set(memberService, dummyUserGroupsListener);

        memberService.stopListening();

        verify(mockUserEventsReference).removeEventListener(dummyUserEventListener);
        verify(mockMembersReference).removeEventListener(dummyMemberListener);
        verify(mockUserGroupsReference).removeEventListener(dummyUserGroupsListener);

        assertNull(ueField.get(memberService));
        assertNull(mField.get(memberService));
        assertNull(ugField.get(memberService));
    }
}
