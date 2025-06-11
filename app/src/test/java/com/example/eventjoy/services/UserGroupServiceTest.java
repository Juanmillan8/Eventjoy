package com.example.eventjoy.services;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.eventjoy.callbacks.GroupsCallback;
import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.callbacks.UserGroupRoleCallback;
import com.example.eventjoy.enums.UserGroupRole;
import com.example.eventjoy.enums.Visibility;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.UserGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserGroupServiceTest {

    @Mock
    FirebaseDatabase mockFirebaseDatabase;

    @Mock
    DatabaseReference mockBaseReference;

    @Mock
    DatabaseReference mockUserGroupsReference;

    @Mock
    DatabaseReference mockGroupsReference;

    @Mock
    DatabaseReference mockMembersReference;

    // Para insertUserGroup() (push)
    @Mock
    DatabaseReference mockNewUserGroupReference;

    UserGroupService userGroupService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configuramos la referencia base y las ramas usadas
        when(mockFirebaseDatabase.getReference()).thenReturn(mockBaseReference);
        when(mockBaseReference.child("userGroups")).thenReturn(mockUserGroupsReference);
        when(mockBaseReference.child("groups")).thenReturn(mockGroupsReference);
        when(mockBaseReference.child("members")).thenReturn(mockMembersReference);

        // Stub para el push() en insertUserGroup
        when(mockUserGroupsReference.push()).thenReturn(mockNewUserGroupReference);

        userGroupService = new UserGroupService(mockFirebaseDatabase);
    }

    @Test
    public void testInsertUserGroup() {
        // Creamos una instancia de UserGroup y asignamos algunos valores
        UserGroup ug = new UserGroup();
        ug.setUserId("user1");
        ug.setGroupId("g1");

        // Stub: simulamos que el push() devuelve una referencia con key "ug1"
        when(mockNewUserGroupReference.getKey()).thenReturn("ug1");

        // Llamamos al méto_do insertUserGroup
        String returnedKey = userGroupService.insertUserGroup(ug);

        // Verificamos que se llamó a setValue con el objeto ug
        verify(mockNewUserGroupReference).setValue(ug);

        // Verificamos que el méto_do devolvió la clave esperada y que se asignó al userGroup
        assertEquals("ug1", returnedKey);
        assertEquals("ug1", ug.getId());
    }

    @Test
    public void testCheckUserGroupRole_Admin() {
        String groupId = "g1";
        String userId = "user1";
        UserGroupRoleCallback callback = mock(UserGroupRoleCallback.class);

        // Preparamos la query simulando que se filtra por userId.
        Query mockQuery = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("userId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(userId)).thenReturn(mockQuery);

        userGroupService.checkUserGroupRole(groupId, userId, callback);

        // Capturamos el ValueEventListener configurado en addListenerForSingleValueEvent.
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos un DataSnapshot que contiene una entrada
        DataSnapshot snapshot = mock(DataSnapshot.class);

        // Creamos un UserGroup para el caso ADMIN.
        UserGroup ug = new UserGroup();
        ug.setId("ug1");
        ug.setUserId(userId);
        ug.setGroupId(groupId);
        ug.setAdmin(true);  // Es administrador

        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(UserGroup.class)).thenReturn(ug);

        List<DataSnapshot> children = new ArrayList<>();
        children.add(childSnapshot);
        when(snapshot.getChildren()).thenReturn(children);
        when(snapshot.exists()).thenReturn(true);

        // Disparamos el onDataChange
        listener.onDataChange(snapshot);

        // Verificamos que se invoque el callback con ADMIN
        verify(callback).onSuccess(UserGroupRole.ADMIN);
    }

    @Test
    public void testCheckUserGroupRole_Participant() {
        String groupId = "g1";
        String userId = "user1";
        UserGroupRoleCallback callback = mock(UserGroupRoleCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("userId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(userId)).thenReturn(mockQuery);

        userGroupService.checkUserGroupRole(groupId, userId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        DataSnapshot snapshot = mock(DataSnapshot.class);

        // Creamos un UserGroup para el caso PARTICIPANT (no admin)
        UserGroup ug = new UserGroup();
        ug.setId("ug2");
        ug.setUserId(userId);
        ug.setGroupId(groupId);
        ug.setAdmin(false);

        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(UserGroup.class)).thenReturn(ug);

        List<DataSnapshot> children = new ArrayList<>();
        children.add(childSnapshot);
        when(snapshot.getChildren()).thenReturn(children);
        when(snapshot.exists()).thenReturn(true);

        listener.onDataChange(snapshot);

        // Se espera el rol PARTICIPANT
        verify(callback).onSuccess(UserGroupRole.PARTICIPANT);
    }

    @Test
    public void testCheckUserGroupRole_NoParticipant() {
        String groupId = "g1";
        String userId = "user1";
        UserGroupRoleCallback callback = mock(UserGroupRoleCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("userId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(userId)).thenReturn(mockQuery);

        userGroupService.checkUserGroupRole(groupId, userId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos un DataSnapshot vacío o sin coincidencias
        DataSnapshot snapshot = mock(DataSnapshot.class);
        // Hacemos que no existan entradas o que ninguna tenga el groupId buscado.
        when(snapshot.exists()).thenReturn(true);
        List<DataSnapshot> children = new ArrayList<>();
        // Por ejemplo, se simula una entrada con groupId distinto:
        UserGroup ug = new UserGroup();
        ug.setId("ug3");
        ug.setUserId(userId);
        ug.setGroupId("otroGrupo");
        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(UserGroup.class)).thenReturn(ug);
        children.add(childSnapshot);
        when(snapshot.getChildren()).thenReturn(children);

        listener.onDataChange(snapshot);

        // Se espera que se invoque callback con NO_PARTICIPANT
        verify(callback).onSuccess(UserGroupRole.NO_PARTICIPANT);
    }


    @Test
    public void testAsignAdmin_AssignsAdmin() {
        String groupId = "g1";
        String userId = "user1";
        SimpleCallback callback = mock(SimpleCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("userId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(userId)).thenReturn(mockQuery);

        userGroupService.asignAdmin(groupId, userId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        DataSnapshot snapshot = mock(DataSnapshot.class);
        UserGroup userGroup = new UserGroup();
        userGroup.setId("ug2");
        userGroup.setUserId(userId);
        userGroup.setGroupId(groupId);
        userGroup.setAdmin(false);

        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(UserGroup.class)).thenReturn(userGroup);
        List<DataSnapshot> children = new ArrayList<>();
        children.add(childSnapshot);
        when(snapshot.getChildren()).thenReturn(children);
        when(snapshot.exists()).thenReturn(true);

        // Stubear la llamada a child("ug2") para que no retorne null
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockUserGroupsReference.child("ug2")).thenReturn(mockChildRef);

        listener.onDataChange(snapshot);

        verify(mockUserGroupsReference).child("ug2");
        verify(mockChildRef).setValue(userGroup);
        verify(callback).onSuccess(contains("assigned as administrator"));
        assertTrue(userGroup.getAdmin());
    }

    @Test
    public void testAsignAdmin_RemovesAdmin() {
        String groupId = "g1";
        String userId = "user1";
        SimpleCallback callback = mock(SimpleCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("userId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(userId)).thenReturn(mockQuery);

        userGroupService.asignAdmin(groupId, userId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        DataSnapshot snapshot = mock(DataSnapshot.class);
        UserGroup userGroup = new UserGroup();
        userGroup.setId("ug3");
        userGroup.setUserId(userId);
        userGroup.setGroupId(groupId);
        userGroup.setAdmin(true);

        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(UserGroup.class)).thenReturn(userGroup);
        List<DataSnapshot> children = new ArrayList<>();
        children.add(childSnapshot);
        when(snapshot.getChildren()).thenReturn(children);
        when(snapshot.exists()).thenReturn(true);

        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockUserGroupsReference.child("ug3")).thenReturn(mockChildRef);

        listener.onDataChange(snapshot);

        verify(mockUserGroupsReference).child("ug3");
        verify(mockChildRef).setValue(userGroup);
        verify(callback).onSuccess(contains("removed successfully"));
        assertFalse(userGroup.getAdmin());
    }

    @Test
    public void testDeleteUserGroup() {
        String groupId = "g1";
        String userId = "user1";
        SimpleCallback callback = mock(SimpleCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("userId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(userId)).thenReturn(mockQuery);

        userGroupService.deleteUserGroup(groupId, userId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addListenerForSingleValueEvent(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        DataSnapshot snapshot = mock(DataSnapshot.class);
        UserGroup userGroup = new UserGroup();
        userGroup.setId("ug4");
        userGroup.setUserId(userId);
        userGroup.setGroupId(groupId);

        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(UserGroup.class)).thenReturn(userGroup);
        List<DataSnapshot> children = new ArrayList<>();
        children.add(childSnapshot);
        when(snapshot.getChildren()).thenReturn(children);
        when(snapshot.exists()).thenReturn(true);

        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockUserGroupsReference.child("ug4")).thenReturn(mockChildRef);

        listener.onDataChange(snapshot);

        verify(mockChildRef).removeValue();
        verify(callback).onSuccess(contains("successfully left"));
    }

    @Test
    public void testGetMembersByGroupId() {
        String groupId = "g1";
        String excludeUserId = "user1";
        MembersCallback callback = mock(MembersCallback.class);

        // Simular query para userGroups.
        Query mockQueryUserGroups = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("groupId")).thenReturn(mockQueryUserGroups);
        when(mockQueryUserGroups.equalTo(groupId)).thenReturn(mockQueryUserGroups);

        userGroupService.getMembersByGroupId(groupId, excludeUserId, callback);

        // Capturar el primer listener
        ArgumentCaptor<ValueEventListener> ugListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQueryUserGroups).addValueEventListener(ugListenerCaptor.capture());
        ValueEventListener ugListener = ugListenerCaptor.getValue();

        DataSnapshot snapshotUG = mock(DataSnapshot.class);
        UserGroup ug1 = new UserGroup();
        ug1.setUserId("user1");
        ug1.setGroupId(groupId);
        UserGroup ug2 = new UserGroup();
        ug2.setUserId("user2");
        ug2.setGroupId(groupId);

        DataSnapshot ugSnap1 = mock(DataSnapshot.class);
        DataSnapshot ugSnap2 = mock(DataSnapshot.class);
        when(ugSnap1.getValue(UserGroup.class)).thenReturn(ug1);
        when(ugSnap2.getValue(UserGroup.class)).thenReturn(ug2);

        List<DataSnapshot> ugChildren = new ArrayList<>();
        ugChildren.add(ugSnap1);
        ugChildren.add(ugSnap2);
        when(snapshotUG.getChildren()).thenReturn(ugChildren);
        when(snapshotUG.exists()).thenReturn(true);

        ugListener.onDataChange(snapshotUG);

        // Capturar el listener para members.
        ArgumentCaptor<ValueEventListener> memberListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockMembersReference).addValueEventListener(memberListenerCaptor.capture());
        ValueEventListener memberListener = memberListenerCaptor.getValue();

        DataSnapshot snapshotMembers = mock(DataSnapshot.class);
        // Crear miembros usando mock para stubear getId()
        Member member1 = mock(Member.class);
        Member member2 = mock(Member.class);
        when(member1.getId()).thenReturn("user2");
        when(member2.getId()).thenReturn("user3");

        DataSnapshot mSnap1 = mock(DataSnapshot.class);
        DataSnapshot mSnap2 = mock(DataSnapshot.class);
        when(mSnap1.getValue(Member.class)).thenReturn(member1);
        when(mSnap2.getValue(Member.class)).thenReturn(member2);

        List<DataSnapshot> memberChildren = new ArrayList<>();
        memberChildren.add(mSnap1);
        memberChildren.add(mSnap2);
        when(snapshotMembers.getChildren()).thenReturn(memberChildren);

        memberListener.onDataChange(snapshotMembers);

        ArgumentCaptor<List<Member>> membersCaptor = ArgumentCaptor.forClass(List.class);
        verify(callback).onSuccess(membersCaptor.capture());
        List<Member> returnedMembers = membersCaptor.getValue();
        // Solo se espera incluir member1 (id "user2")
        assertEquals(1, returnedMembers.size());
        assertEquals("user2", returnedMembers.get(0).getId());
    }

    @Test
    public void testGetOtherGroups() {
        String userId = "user1";
        GroupsCallback callback = mock(GroupsCallback.class);

        Query mockQueryUG = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("userId")).thenReturn(mockQueryUG);
        when(mockQueryUG.equalTo(userId)).thenReturn(mockQueryUG);

        userGroupService.getOtherGroups(userId, callback);

        ArgumentCaptor<ValueEventListener> ugListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQueryUG).addValueEventListener(ugListenerCaptor.capture());
        ValueEventListener ugListener = ugListenerCaptor.getValue();

        DataSnapshot snapshotUG = mock(DataSnapshot.class);
        UserGroup ug1 = new UserGroup();
        ug1.setGroupId("g1");
        UserGroup ug2 = new UserGroup();
        ug2.setGroupId("g2");
        DataSnapshot ugSnap1 = mock(DataSnapshot.class);
        DataSnapshot ugSnap2 = mock(DataSnapshot.class);
        when(ugSnap1.getValue(UserGroup.class)).thenReturn(ug1);
        when(ugSnap2.getValue(UserGroup.class)).thenReturn(ug2);
        List<DataSnapshot> ugChildren = new ArrayList<>();
        ugChildren.add(ugSnap1);
        ugChildren.add(ugSnap2);
        when(snapshotUG.getChildren()).thenReturn(ugChildren);
        when(snapshotUG.exists()).thenReturn(true);

        ugListener.onDataChange(snapshotUG);

        ArgumentCaptor<ValueEventListener> groupsListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockGroupsReference).addValueEventListener(groupsListenerCaptor.capture());
        ValueEventListener groupsListener = groupsListenerCaptor.getValue();

        DataSnapshot snapshotGroups = mock(DataSnapshot.class);
        Group group1 = mock(Group.class);
        Group group2 = mock(Group.class);
        Group group3 = mock(Group.class);
        when(group1.getId()).thenReturn("g1");
        when(group2.getId()).thenReturn("g2");
        when(group3.getId()).thenReturn("g3");
        when(group1.getVisibility()).thenReturn(Visibility.PRIVATE);
        when(group2.getVisibility()).thenReturn(Visibility.PUBLIC);
        when(group3.getVisibility()).thenReturn(Visibility.PUBLIC);

        DataSnapshot gSnap1 = mock(DataSnapshot.class);
        DataSnapshot gSnap2 = mock(DataSnapshot.class);
        DataSnapshot gSnap3 = mock(DataSnapshot.class);
        when(gSnap1.getValue(Group.class)).thenReturn(group1);
        when(gSnap2.getValue(Group.class)).thenReturn(group2);
        when(gSnap3.getValue(Group.class)).thenReturn(group3);

        List<DataSnapshot> groupChildren = new ArrayList<>();
        groupChildren.add(gSnap1);
        groupChildren.add(gSnap2);
        groupChildren.add(gSnap3);
        when(snapshotGroups.getChildren()).thenReturn(groupChildren);

        groupsListener.onDataChange(snapshotGroups);

        ArgumentCaptor<List<Group>> groupsCaptor = ArgumentCaptor.forClass(List.class);
        verify(callback).onSuccess(groupsCaptor.capture());
        List<Group> result = groupsCaptor.getValue();
        assertEquals(1, result.size());
        List<String> resultIds = new ArrayList<>();
        for (Group g : result) {
            resultIds.add(g.getId());
        }
        assertTrue(resultIds.contains("g3"));
    }

    @Test
    public void testGetAllGroups() {
        String userId = "user1";
        GroupsCallback callback = mock(GroupsCallback.class);

        Query mockQueryUG = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("userId")).thenReturn(mockQueryUG);
        when(mockQueryUG.equalTo(userId)).thenReturn(mockQueryUG);

        userGroupService.getAllGroups(userId, callback);

        // Capturar el primer listener (userGroupsListener)
        ArgumentCaptor<ValueEventListener> ugListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQueryUG).addValueEventListener(ugListenerCaptor.capture());
        ValueEventListener ugListener = ugListenerCaptor.getValue();

        DataSnapshot snapshotUG = mock(DataSnapshot.class);
        // Simular que el usuario pertenece a "g1" y "g2"
        UserGroup ug1 = new UserGroup();
        ug1.setGroupId("g1");
        UserGroup ug2 = new UserGroup();
        ug2.setGroupId("g2");
        DataSnapshot ugSnap1 = mock(DataSnapshot.class);
        DataSnapshot ugSnap2 = mock(DataSnapshot.class);
        when(ugSnap1.getValue(UserGroup.class)).thenReturn(ug1);
        when(ugSnap2.getValue(UserGroup.class)).thenReturn(ug2);
        List<DataSnapshot> ugChildren = new ArrayList<>();
        ugChildren.add(ugSnap1);
        ugChildren.add(ugSnap2);
        when(snapshotUG.getChildren()).thenReturn(ugChildren);
        when(snapshotUG.exists()).thenReturn(true);

        ugListener.onDataChange(snapshotUG);

        // Capturar el listener en groups (getAllGroups)
        ArgumentCaptor<ValueEventListener> groupsListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockGroupsReference).addValueEventListener(groupsListenerCaptor.capture());
        ValueEventListener groupsListener = groupsListenerCaptor.getValue();

        DataSnapshot snapshotGroups = mock(DataSnapshot.class);
        // Crear grupos como mocks.
        Group group1 = mock(Group.class);
        Group group2 = mock(Group.class);
        Group group3 = mock(Group.class);
        when(group1.getId()).thenReturn("g1");
        when(group2.getId()).thenReturn("g2");
        when(group3.getId()).thenReturn("g3");
        when(group1.getVisibility()).thenReturn(Visibility.PRIVATE);
        when(group2.getVisibility()).thenReturn(Visibility.PUBLIC);
        when(group3.getVisibility()).thenReturn(Visibility.PUBLIC);

        DataSnapshot gSnap1 = mock(DataSnapshot.class);
        DataSnapshot gSnap2 = mock(DataSnapshot.class);
        DataSnapshot gSnap3 = mock(DataSnapshot.class);
        when(gSnap1.getValue(Group.class)).thenReturn(group1);
        when(gSnap2.getValue(Group.class)).thenReturn(group2);
        when(gSnap3.getValue(Group.class)).thenReturn(group3);

        List<DataSnapshot> groupChildren = new ArrayList<>();
        groupChildren.add(gSnap1);
        groupChildren.add(gSnap2);
        groupChildren.add(gSnap3);
        when(snapshotGroups.getChildren()).thenReturn(groupChildren);

        groupsListener.onDataChange(snapshotGroups);

        // Según la lógica, en getAllGroups:
        // Si group es PRIVATE, se añade solo si su id está en userGroupIds (en este caso, "g1" y "g2").
        // Si es PUBLIC, siempre se añade.
        // Así, se esperan: group1, group2 y group3.
        ArgumentCaptor<List<Group>> groupsCaptor = ArgumentCaptor.forClass(List.class);
        verify(callback).onSuccess(groupsCaptor.capture());
        List<Group> result = groupsCaptor.getValue();
        assertEquals(3, result.size());
        List<String> resultIds = new ArrayList<>();
        for (Group g : result) {
            resultIds.add(g.getId());
        }
        assertTrue(resultIds.contains("g1"));
        assertTrue(resultIds.contains("g2"));
        assertTrue(resultIds.contains("g3"));
    }

    @Test
    public void testGetByMemberId() {
        String userId = "user1";
        GroupsCallback callback = mock(GroupsCallback.class);

        Query mockQueryUG = mock(Query.class);
        when(mockUserGroupsReference.orderByChild("userId")).thenReturn(mockQueryUG);
        when(mockQueryUG.equalTo(userId)).thenReturn(mockQueryUG);

        userGroupService.getByMemberId(userId, callback);

        ArgumentCaptor<ValueEventListener> ugListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQueryUG).addValueEventListener(ugListenerCaptor.capture());
        ValueEventListener ugListener = ugListenerCaptor.getValue();

        DataSnapshot snapshotUG = mock(DataSnapshot.class);
        // Simular que el usuario pertenece a grupos "g1" y "g5"
        UserGroup ug1 = new UserGroup();
        ug1.setGroupId("g1");
        UserGroup ug2 = new UserGroup();
        ug2.setGroupId("g5");
        DataSnapshot ugSnap1 = mock(DataSnapshot.class);
        DataSnapshot ugSnap2 = mock(DataSnapshot.class);
        when(ugSnap1.getValue(UserGroup.class)).thenReturn(ug1);
        when(ugSnap2.getValue(UserGroup.class)).thenReturn(ug2);
        List<DataSnapshot> ugChildren = new ArrayList<>();
        ugChildren.add(ugSnap1);
        ugChildren.add(ugSnap2);
        when(snapshotUG.getChildren()).thenReturn(ugChildren);
        when(snapshotUG.exists()).thenReturn(true);

        ugListener.onDataChange(snapshotUG);

        // Capturar el listener para groups
        ArgumentCaptor<ValueEventListener> groupsListenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockGroupsReference).addValueEventListener(groupsListenerCaptor.capture());
        ValueEventListener groupsListener = groupsListenerCaptor.getValue();

        DataSnapshot snapshotGroups = mock(DataSnapshot.class);
        // Simular tres grupos: g1, g2, g5.
        Group group1 = mock(Group.class);
        Group group2 = mock(Group.class);
        Group group3 = mock(Group.class);
        when(group1.getId()).thenReturn("g1");
        when(group2.getId()).thenReturn("g2");
        when(group3.getId()).thenReturn("g5");

        DataSnapshot gSnap1 = mock(DataSnapshot.class);
        DataSnapshot gSnap2 = mock(DataSnapshot.class);
        DataSnapshot gSnap3 = mock(DataSnapshot.class);
        when(gSnap1.getValue(Group.class)).thenReturn(group1);
        when(gSnap2.getValue(Group.class)).thenReturn(group2);
        when(gSnap3.getValue(Group.class)).thenReturn(group3);

        List<DataSnapshot> groupChildren = new ArrayList<>();
        groupChildren.add(gSnap1);
        groupChildren.add(gSnap2);
        groupChildren.add(gSnap3);
        when(snapshotGroups.getChildren()).thenReturn(groupChildren);

        groupsListener.onDataChange(snapshotGroups);

        ArgumentCaptor<List<Group>> groupsCaptor = ArgumentCaptor.forClass(List.class);
        verify(callback).onSuccess(groupsCaptor.capture());
        List<Group> result = groupsCaptor.getValue();
        // Se esperan solo grupos con id "g1" y "g5"
        assertEquals(2, result.size());
        List<String> resultIds = new ArrayList<>();
        for (Group g : result) {
            resultIds.add(g.getId());
        }
        assertTrue(resultIds.contains("g1"));
        assertTrue(resultIds.contains("g5"));
    }

    @Test
    public void testStopListening() throws Exception {
        ValueEventListener dummyUGListener = mock(ValueEventListener.class);
        ValueEventListener dummyGroupsListener = mock(ValueEventListener.class);
        ValueEventListener dummyMemberListener = mock(ValueEventListener.class);

        Field ugField = UserGroupService.class.getDeclaredField("userGroupsListener");
        Field groupsField = UserGroupService.class.getDeclaredField("groupsListener");
        Field memberField = UserGroupService.class.getDeclaredField("memberListener");
        ugField.setAccessible(true);
        groupsField.setAccessible(true);
        memberField.setAccessible(true);

        ugField.set(userGroupService, dummyUGListener);
        groupsField.set(userGroupService, dummyGroupsListener);
        memberField.set(userGroupService, dummyMemberListener);

        userGroupService.stopListening();

        verify(mockUserGroupsReference).removeEventListener(dummyUGListener);
        verify(mockGroupsReference).removeEventListener(dummyGroupsListener);
        verify(mockMembersReference).removeEventListener(dummyMemberListener);

        assertNull(ugField.get(userGroupService));
        assertNull(groupsField.get(userGroupService));
        assertNull(memberField.get(userGroupService));
    }
}
