package com.example.eventjoy.services;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.eventjoy.callbacks.ValorationsCallback;
import com.example.eventjoy.models.Valoration;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
public class ValorationServiceTest {

    @Mock
    FirebaseDatabase mockFirebaseDatabase;

    @Mock
    DatabaseReference mockBaseReference;

    @Mock
    DatabaseReference mockValorationsReference; // referencia al nodo "valorations"

    // Para insertValoration(): referencia que se obtiene al llamar a push()
    @Mock
    DatabaseReference mockNewValorationReference;

    ValorationService valorationService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configuramos el FirebaseDatabase mockeado para devolver la referencia base y luego "valorations"
        when(mockFirebaseDatabase.getReference()).thenReturn(mockBaseReference);
        when(mockBaseReference.child("valorations")).thenReturn(mockValorationsReference);

        // Stub: push() sobre la referencia "valorations" debe retornar el mock de la nueva referencia
        when(mockValorationsReference.push()).thenReturn(mockNewValorationReference);

        // Usamos el constructor que recibe FirebaseDatabase
        valorationService = new ValorationService(mockFirebaseDatabase);
    }

    @Test
    public void testInsertValoration() {
        // Creamos un objeto Valoration y asignamos algunos valores
        Valoration v = new Valoration();
        v.setTitle("Gran Trabajo");
        v.setDescription("Excelente colaboración");
        v.setRating(4.5);
        v.setRatedUserId("userRated");
        v.setRaterUserId("userRater");

        // Stub: la nueva referencia retornará la key "val1"
        when(mockNewValorationReference.getKey()).thenReturn("val1");

        String returnedId = valorationService.insertValoration(v);

        // Verifica que se haya llamado a setValue(v) sobre la nueva referencia
        verify(mockNewValorationReference).setValue(v);
        // Verifica que se retorne la key y que ésta se asigne en el objeto
        assertEquals("val1", returnedId);
        assertEquals("val1", v.getId());
    }

    @Test
    public void testGetByRatedUserId_Success() {
        String ratedUserId = "userRated";
        ValorationsCallback callback = mock(ValorationsCallback.class);

        // Simulamos la consulta filtrada por "ratedUserId"
        Query mockQuery = mock(Query.class);
        when(mockValorationsReference.orderByChild("ratedUserId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(ratedUserId)).thenReturn(mockQuery);

        valorationService.getByRatedUserId(ratedUserId, callback);

        // Capturamos el ValueEventListener agregado a la query
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Creamos un DataSnapshot simulado con dos valoraciones
        DataSnapshot snapshot = mock(DataSnapshot.class);
        Valoration val1 = new Valoration();
        val1.setId("v1");
        val1.setRatedUserId(ratedUserId);
        Valoration val2 = new Valoration();
        val2.setId("v2");
        val2.setRatedUserId(ratedUserId);

        DataSnapshot snap1 = mock(DataSnapshot.class);
        DataSnapshot snap2 = mock(DataSnapshot.class);
        when(snap1.getValue(Valoration.class)).thenReturn(val1);
        when(snap2.getValue(Valoration.class)).thenReturn(val2);
        List<DataSnapshot> children = new ArrayList<>();
        children.add(snap1);
        children.add(snap2);
        when(snapshot.getChildren()).thenReturn(children);
        when(snapshot.exists()).thenReturn(true);

        // Invoca onDataChange simulando la consulta exitosa
        listener.onDataChange(snapshot);

        // Capturamos la lista resultante enviada al callback
        ArgumentCaptor<List<Valoration>> captor = ArgumentCaptor.forClass(List.class);
        verify(callback).onSuccess(captor.capture());
        List<Valoration> result = captor.getValue();

        assertEquals(2, result.size());
        assertEquals("v1", result.get(0).getId());
        assertEquals("v2", result.get(1).getId());
    }

    @Test
    public void testGetByRatedUserId_Failure() {
        String ratedUserId = "userRated";
        ValorationsCallback callback = mock(ValorationsCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockValorationsReference.orderByChild("ratedUserId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(ratedUserId)).thenReturn(mockQuery);

        valorationService.getByRatedUserId(ratedUserId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simula un error en la consulta (onCancelled)
        DatabaseError error = DatabaseError.fromException(new Exception("Test error"));
        listener.onCancelled(error);

        verify(callback).onFailure(argThat(e -> e.getMessage().contains("Test error")));
    }

    @Test
    public void testStopListening() throws Exception {
        // Inyectamos un listener dummy en el campo valorationsListener
        ValueEventListener dummyListener = mock(ValueEventListener.class);
        java.lang.reflect.Field field = ValorationService.class.getDeclaredField("valorationsListener");
        field.setAccessible(true);
        field.set(valorationService, dummyListener);

        valorationService.stopListening();

        verify(mockValorationsReference).removeEventListener(dummyListener);
        assertNull(field.get(valorationService));
    }
}
