package com.example.eventjoy.services;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.eventjoy.callbacks.ReportsCallback;
import com.example.eventjoy.enums.ReportStatus;
import com.example.eventjoy.models.Report;
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
public class ReportServiceTest {

    @Mock
    FirebaseDatabase mockFirebaseDatabase;

    @Mock
    DatabaseReference mockBaseReference;

    @Mock
    DatabaseReference mockReportsReference;

    @Mock
    DatabaseReference mockNewReportReference;

    ReportService reportService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Simulamos la referencia base y asignamos la rama "reports"
        when(mockFirebaseDatabase.getReference()).thenReturn(mockBaseReference);
        when(mockBaseReference.child("reports")).thenReturn(mockReportsReference);
        // Stub para insertReport: simulamos que push() retorna un objeto (DatabaseReference) en el que getKey() dará la ID
        when(mockReportsReference.push()).thenReturn(mockNewReportReference);

        reportService = new ReportService(mockFirebaseDatabase);
    }

    // 1. insertReport: Verifica que se le asigne el id obtenido del push() y se invoque setValue, devolviendo el id asignado.
    @Test
    public void testInsertReport() {
        Report report = new Report();
        report.setReportDescription("Test report");
        report.setReportedUserId("userA");
        report.setReporterUserId("userB");
        // Simulamos que el push() asigna el id "rep1"
        when(mockNewReportReference.getKey()).thenReturn("rep1");

        String returnedId = reportService.insertReport(report);

        verify(mockNewReportReference).setValue(report);
        assertEquals("rep1", returnedId);
        assertEquals("rep1", report.getId());
    }

    // 2. updateReport: Se verifica que se invoque setValue() sobre la referencia hija correspondiente al id del reporte.
    @Test
    public void testUpdateReport() {
        Report report = new Report();
        report.setId("rep2");
        report.setReportDescription("Updated report");

        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        when(mockReportsReference.child("rep2")).thenReturn(mockChildRef);

        reportService.updateReport(report);

        verify(mockChildRef).setValue(report);
    }

    // 3. getPendingReports: Caso en que la consulta retorna reportes PENDING.
    @Test
    public void testGetPendingReports_withReports() {
        ReportsCallback callback = mock(ReportsCallback.class);
        // Se simula la query: orderByChild("reportStatus").equalTo("PENDING")
        Query mockQuery = mock(Query.class);
        when(mockReportsReference.orderByChild("reportStatus")).thenReturn(mockQuery);
        when(mockQuery.equalTo("PENDING")).thenReturn(mockQuery);

        reportService.getPendingReports(callback);

        // Capturamos el ValueEventListener asignado a la query.
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        // Simulamos un DataSnapshot con un reporte
        DataSnapshot snapshot = mock(DataSnapshot.class);
        Report rep = new Report();
        rep.setId("rep3");
        rep.setReportStatus(ReportStatus.PENDING);

        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(Report.class)).thenReturn(rep);

        List<DataSnapshot> children = new ArrayList<>();
        children.add(childSnapshot);
        when(snapshot.getChildren()).thenReturn(children);
        when(snapshot.exists()).thenReturn(true);

        listener.onDataChange(snapshot);

        verify(callback).onSuccess(argThat(list -> list.size() == 1 && list.get(0).getId().equals("rep3")));
    }

    // 4. getPendingReports: Caso en que la consulta retorna una lista vacía.
    @Test
    public void testGetPendingReports_empty() {
        ReportsCallback callback = mock(ReportsCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockReportsReference.orderByChild("reportStatus")).thenReturn(mockQuery);
        when(mockQuery.equalTo("PENDING")).thenReturn(mockQuery);

        reportService.getPendingReports(callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(snapshot.getChildren()).thenReturn(Collections.emptyList());
        when(snapshot.exists()).thenReturn(false);

        listener.onDataChange(snapshot);

        verify(callback).onSuccess(argThat(list -> list.isEmpty()));
    }

    // 5. getPendingReports: Caso en que ocurre un error en la consulta (onCancelled)
    @Test
    public void testGetPendingReports_onCancelled() {
        ReportsCallback callback = mock(ReportsCallback.class);

        Query mockQuery = mock(Query.class);
        when(mockReportsReference.orderByChild("reportStatus")).thenReturn(mockQuery);
        when(mockQuery.equalTo("PENDING")).thenReturn(mockQuery);

        reportService.getPendingReports(callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        DatabaseError error = DatabaseError.fromException(new Exception("Test error"));
        listener.onCancelled(error);

        verify(callback).onFailure(any(Exception.class));
    }

    // 6. getByUserId: Caso en que la consulta retorna reportes filtrados por reportedUserId.
    @Test
    public void testGetByUserId_withReports() {
        ReportsCallback callback = mock(ReportsCallback.class);
        String userId = "userA";

        Query mockQuery = mock(Query.class);
        when(mockReportsReference.orderByChild("reportedUserId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(userId)).thenReturn(mockQuery);

        reportService.getByUserId(userId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        DataSnapshot snapshot = mock(DataSnapshot.class);
        Report rep = new Report();
        rep.setId("rep4");
        rep.setReportedUserId(userId);

        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(childSnapshot.getValue(Report.class)).thenReturn(rep);
        List<DataSnapshot> children = new ArrayList<>();
        children.add(childSnapshot);
        when(snapshot.getChildren()).thenReturn(children);
        when(snapshot.exists()).thenReturn(true);

        listener.onDataChange(snapshot);

        verify(callback).onSuccess(argThat(list -> list.size() == 1 && list.get(0).getId().equals("rep4")));
    }

    // 7. getByUserId: Caso en que la consulta retorna una lista vacía.
    @Test
    public void testGetByUserId_empty() {
        ReportsCallback callback = mock(ReportsCallback.class);
        String userId = "userA";

        Query mockQuery = mock(Query.class);
        when(mockReportsReference.orderByChild("reportedUserId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(userId)).thenReturn(mockQuery);

        reportService.getByUserId(userId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(snapshot.getChildren()).thenReturn(Collections.emptyList());
        when(snapshot.exists()).thenReturn(false);

        listener.onDataChange(snapshot);

        verify(callback).onSuccess(argThat(list -> list.isEmpty()));
    }

    // 8. getByUserId: Caso en que ocurre un error (onCancelled)
    @Test
    public void testGetByUserId_onCancelled() {
        ReportsCallback callback = mock(ReportsCallback.class);
        String userId = "userA";

        Query mockQuery = mock(Query.class);
        when(mockReportsReference.orderByChild("reportedUserId")).thenReturn(mockQuery);
        when(mockQuery.equalTo(userId)).thenReturn(mockQuery);

        reportService.getByUserId(userId, callback);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockQuery).addValueEventListener(listenerCaptor.capture());
        ValueEventListener listener = listenerCaptor.getValue();

        DatabaseError error = DatabaseError.fromException(new Exception("Test error"));
        listener.onCancelled(error);

        verify(callback).onFailure(any(Exception.class));
    }

    // 9. stopListening: Verifica que se remuevan los listeners y se establezca a null el campo interno.
    @Test
    public void testStopListening() throws Exception {
        ValueEventListener dummyListener = mock(ValueEventListener.class);
        Field reportsListenerField = ReportService.class.getDeclaredField("reportsListener");
        reportsListenerField.setAccessible(true);
        reportsListenerField.set(reportService, dummyListener);

        reportService.stopListening();

        verify(mockReportsReference).removeEventListener(dummyListener);
        assertNull(reportsListenerField.get(reportService));
    }
}
