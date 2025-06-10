package com.example.eventjoy.tests.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.util.Log;

import com.example.eventjoy.enums.Provider;
import com.example.eventjoy.enums.Role;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.MemberService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class MainActivityTest {

    private static MemberService memberService;
    private static String idMemberTest;
    private DatabaseReference databaseReferenceMembers;

    @Before
    public void setUp() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceMembers = firebaseDatabase.getReference().child("members");
        memberService = new MemberService();
    }

    @Test
    public void testInsertMember_returnsValidId() {
        Member member = new Member();
        member.setUsername("test_user");
        member.setDni("test_DNI");
        member.setUserAccountId("uid_test");
        member.setProvider(Provider.EMAIL);
        member.setPhoto(null);
        member.setName("name_test_user");
        member.setRole(Role.MEMBER);
        member.setSurname("test_surname");
        member.setPhone("test_phone");
        member.setBirthdate("1990-03-02");
        member.setLevel(0);

        String generatedId = memberService.insertMember(member);
        Log.i("GeneratedID", generatedId);
        assertNotNull("Generated ID should not be null", generatedId);
        assertEquals(generatedId, member.getId());
        idMemberTest = generatedId;
    }

    /*@AfterClass
    public static void deleteData() {
        if (idMemberTest != null) {
            memberService.deleteMemberById(idMemberTest);
        }
    }*/
}
