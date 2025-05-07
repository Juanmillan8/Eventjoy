package com.example.eventjoy.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.eventjoy.R;

public class ProgressDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Crea un builder para construir el cuadro de diálogo usando un estilo personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialogStyle);

        //Infla el layout del fragmento, que contiene el diseño del indicador de progreso
        View rootView = requireActivity().getLayoutInflater().inflate(R.layout.fragment_progress_dialog, null);

        //Establece la vista del diálogo con el layout inflado
        builder.setView(rootView);

        //Crea el cuadro de diálogo a partir del builder
        AlertDialog dialog = builder.create();

        //Impide que el diálogo se cierre si se toca fuera de él
        dialog.setCanceledOnTouchOutside(false);

        //Retorna el cuadro de diálogo para que sea mostrado
        return dialog;
    }

}
