package com.github.romanarranz.androidfileuploaderexample;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.romanarranz.androidfileuploaderexample.network.FileUploaderLoader;
import com.github.romanarranz.androidfileuploaderexample.utils.AndroidPermissions;
import com.github.romanarranz.androidfileuploaderexample.utils.FilePath;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<Integer> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int PICK_FILE_REQUEST = 1;
    private static final String SERVER_REQ_URL = "http://192.168.1.38:3000/upload/multipart";
    private static final int LOADER_ID = 1;

    @BindView(R.id.img_attachment) ImageView mPreview;
    @BindView(R.id.filename) TextView mFilename;

    private String mSelectedFilePath;
    private AndroidPermissions mPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        mPermissions = new AndroidPermissions(this, permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (!mPermissions.areAllRequiredPermissionsGranted(permissions, grantResults)) {
            Toast.makeText(this, "Confirma los permisos!", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.img_attachment)
    void selectImage() {
        Intent intent = new Intent();
        intent.setType("*/*"); // seleccionamos todos los tipos de archivos
        intent.setAction(Intent.ACTION_GET_CONTENT); // permite seleccionar datos y devolverlos

        startActivityForResult(Intent.createChooser(intent, "Elige el archivo a subir.."), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data == null) {
                    return;
                }

                Uri selectedFileUri = data.getData();
                mSelectedFilePath = FilePath.getPath(this, selectedFileUri);
                Log.i(LOG_TAG, "Path seleccionado: " + mSelectedFilePath);

                if (mSelectedFilePath != null && !mSelectedFilePath.equals("")) {
                    mFilename.setText(mSelectedFilePath);
                    mPreview.setImageURI(selectedFileUri);
                } else {
                    Toast.makeText(this, "No se puede subir ese archivo", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @OnClick(R.id.upload_button)
    void onUpload() {

        if (mSelectedFilePath != null) {
            // Comprobar que tenemos salida a internet
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            // Obtener detalles de la red actual
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                LoaderManager loaderManager = getLoaderManager();

                if (loaderManager.getLoader(LOADER_ID) != null && loaderManager.getLoader(LOADER_ID).isStarted()) {
                    loaderManager.restartLoader(LOADER_ID, null, this);
                } else {
                    loaderManager.initLoader(LOADER_ID, null, this);
                }
            } else {
                Toast.makeText(this, "No hay conexion de red disponible", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Integer> onCreateLoader(int id, Bundle args) {

        Uri baseUri = Uri.parse(SERVER_REQ_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        mFilename.setText("Subiendo...");

        // Create a new loader for the given URL
        return new FileUploaderLoader(this, uriBuilder.toString(), mSelectedFilePath);
    }

    @Override
    public void onLoadFinished(Loader<Integer> loader, Integer serverResponseCode) {
        if (serverResponseCode == 200) {
            mFilename.setText("Se ha subido correctamente");
        } else {
            mFilename.setText("Ha ocurrido un error");
        }
    }

    @Override
    public void onLoaderReset(Loader<Integer> loader) {
        mFilename.setText("Subiendo...");
    }
}
