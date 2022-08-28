package com.bluethunder.tar2.ui.case_details.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bluethunder.tar2.R;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

/**
 * Dialog Fragment containing rating form.
 */
public class ShareCaseQRDialogFragment extends DialogFragment {

    private static final String TAG = "ShareCaseQRDialogFragme";
    private static final String CASE_URL_QR_CODE = "CASE_URL_QR_CODE";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_share_case, container, false);

        ImageView qrImage = v.findViewById(R.id.qr_img_view);
        try {
            String qrData = getArguments().getString(CASE_URL_QR_CODE);
            Log.d(TAG, "onCreateView: " + qrData);
            // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
            QRGEncoder qrgEncoder = new QRGEncoder(qrData, null, QRGContents.Type.TEXT, 2000);
//            qrgEncoder.setColorBlack(Color.RED);
//            qrgEncoder.setColorWhite(Color.BLUE);

            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.getBitmap();
            // Setting Bitmap to ImageView
            qrImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        return v;
    }


}
