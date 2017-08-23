package com.github.romanarranz.androidfileuploaderexample.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase auxiliar para manejar los permisos de Android 6.0+
 *
 * @author romanarranzguerrero
 */

public class AndroidPermissions {

    private static final String LOG_TAG = AndroidPermissions.class.getSimpleName();

    private Activity mContext;
    private String[] mRequiredPermissions;
    private List<String> mPermissionsToRequest = new ArrayList<>();

    public AndroidPermissions(Activity context, String... requiredPermissions) {
        mContext = context;
        mRequiredPermissions = requiredPermissions;
    }

    /**
     * Comprueba si todos los permisos requeridos son concedidos.
     *
     * @return true si todos los permisos son concedidos, false en otro caso
     */
    public boolean checkPermissions() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return true;
        }

        for (String permission : mRequiredPermissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionsToRequest.add(permission);
            }
        }

        if (mPermissionsToRequest.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * Solicita un permiso
     *
     * @param requestCode codigo de solicitud de la Activity
     */
    public void requestPermissions(int requestCode) {
        String[] request = mPermissionsToRequest.toArray(new String[mPermissionsToRequest.size()]);

        StringBuilder log = new StringBuilder();
        log.append("Requesting Permissions:\n");

        for (String permission : request) {
            log.append(permission).append("\n");
        }

        Log.i(LOG_TAG, log.toString());

        ActivityCompat.requestPermissions(mContext, request, requestCode);
    }

    /**
     * Metodo que comprueba si todos los permisos solicitados son concedidos
     *
     * @param permissions permisos solicitados
     * @param grantResults resultados
     * @return true si todos los permisos son concedidos, false en otro caso
     */
    public boolean areAllRequiredPermissionsGranted(String[] permissions, int[] grantResults) {
        if (permissions == null || permissions.length == 0 ||
                grantResults == null ||grantResults.length == 0) {
            return false;
        }

        Map<String, Integer> perms = new LinkedHashMap<>();

        for (int i = 0; i < permissions.length; i++) {
            if (!perms.containsKey(permissions[i])
                    || (perms.containsKey(permissions[i]) && perms.get(permissions[i]) == PackageManager.PERMISSION_DENIED))
                perms.put(permissions[i], grantResults[i]);
        }

        for (Map.Entry<String, Integer> entry : perms.entrySet()) {
            if (entry.getValue() != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
