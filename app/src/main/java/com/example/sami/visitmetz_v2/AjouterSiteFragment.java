package com.example.sami.visitmetz_v2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sami.visitmetz_v2.ContentProvider.CategoriesProvider;
import com.example.sami.visitmetz_v2.ContentProvider.SitesProvider;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class AjouterSiteFragment extends Fragment {

    final int REQUEST_CODE_GALLERY = 42;
    DatabaseHelper mDatabaseHelper1;
    Button bouton_ajouter_site, btnAnnuler, bouton_ajouter_categorie;
    ImageButton bouton_choisir_image;
    EditText nom;
    EditText longitude;
    EditText latitude;
    EditText adresse_postale;
    EditText categorie;
    EditText resume;
    ImageView editImage;

    Spinner spinner;

    private String newCategorie = "";
    private EditText nCategorie;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.ajouter_site_activity, container, false);
        bouton_ajouter_site = v.findViewById(R.id.bouton_ajouter_site);
        bouton_choisir_image = v.findViewById(R.id.bouton_choisir_image);
        bouton_ajouter_categorie = v.findViewById(R.id.bouton_ajouter_categorie);
        editImage = v.findViewById(R.id.imageView4);
        nom = v.findViewById(R.id.nom);
        longitude = v.findViewById(R.id.longitude);
        latitude = v.findViewById(R.id.latitude);
        adresse_postale = v.findViewById(R.id.adresse_postale);
        categorie = v.findViewById(R.id.categorie);
        resume = v.findViewById(R.id.resume);
        spinner = v.findViewById(R.id.categorie_spinner);

        loadspinner();

        btnAnnuler = v.findViewById(R.id.bouton_annuler);
        btnAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });


        mDatabaseHelper1 = new DatabaseHelper(this.getActivity());

        editImage.setVisibility(View.GONE);

        bouton_choisir_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_CODE_GALLERY);
                bouton_choisir_image.setVisibility(View.GONE);
            }
        });
        bouton_choisir_image.setVisibility(View.VISIBLE);

        bouton_ajouter_categorie.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Ajouter une nouvelle catégorie");

                    // I'm using fragment here so I'm using getView() to provide ViewGroup
                    // but you can provide here any other instance of ViewGroup from your Fragment / Activity
                    View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_categorie, (ViewGroup) getView(), false);

                    // Inflate and set the layout for the dialog
                    // Pass null as the parent view because its going in the dialog layout
                    builder.setView(viewInflated);

                    // Set up the input
                    nCategorie = viewInflated.findViewById(R.id.newcategorie);

                    // Add action buttons
                    builder.setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            newCategorie = nCategorie.getText().toString();

                           if (newCategorie.trim().length() > 0) {
                                ContentValues content = new ContentValues();
                                content.put("nom", newCategorie);

                                Uri uri2 = getActivity().getContentResolver().insert(
                                        CategoriesProvider.CONTENT_URI, content);
                                loadspinner();
                                //this.notify();
                           } else {
                               Toast.makeText(getContext(), "Le champ est vide!", Toast.LENGTH_LONG)
                                       .show();
                               dialog.cancel();
                           }
                        }
                    });

                    builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                }
            }
        );

        //When the button is clicked, the button in the text field is added to the database
        bouton_ajouter_site.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomSite = nom.getText().toString();
                byte[] ImageSite = getByteFromDrawable(editImage.getDrawable());
                double longSite = Double.valueOf(longitude.getText().toString());
                double latSite = Double.valueOf(latitude.getText().toString());
                String adressSite = adresse_postale.getText().toString();
                String categorieSite = categorie.getText().toString();
                String resumeSite = resume.getText().toString();

                //Checks if it is not empty
                if (nomSite.trim().length() > 0) {

                    // Add a new site record
                    ContentValues sitesValues = contentValues(0, nomSite, latSite, longSite, adressSite, categorieSite, resumeSite, ImageSite);

                    Uri uri = getActivity().getContentResolver().insert(
                            SitesProvider.CONTENT_URI, sitesValues);

                    Toast.makeText(getContext(), "Le site "+ nomSite +" a été ajouté: " + latSite + ", " + longSite, Toast.LENGTH_LONG)
                            .show();

                    // Create new fragment and transaction
                    Fragment newFragment = new SitesOverviewFragment();
                    // consider using Java coding conventions (upper first char class names!!!)
                    FragmentTransaction transaction ;
                    if (getFragmentManager() != null) {
                        transaction = getFragmentManager().beginTransaction();

                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack
                        transaction.replace(R.id.fragment_container, newFragment);
                        transaction.addToBackStack(null);

                        // Commit the transaction
                        transaction.commit();
                    }
                } else {
                    Toast.makeText(getActivity(), "Le formulaire est vide !",Toast.LENGTH_SHORT).show();
                }
            }

        });
        return v;
    }

    public ContentValues contentValues(int id_ext, String nom, double latitude, double longitude, String adresse, String categorie, String resume, byte[] image)
    {
        //Permits to add new info in the table
        ContentValues values = new ContentValues();
        values.put("id_ext",id_ext);
        values.put("nom",nom);
        values.put("image",image);
        values.put("latitude",latitude);
        values.put("longitude",longitude);
        values.put("adresse_postale",adresse);
        values.put("categorie",categorie);
        values.put("resume",resume);
        return values;
    }

    @NonNull
    public byte[] getByteFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG,100,stream);
        return stream.toByteArray();
    }

    Intent intent;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                // Filter to show only images, using the image MIME data type.
                // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                // To search for all documents available via installed storage providers,
                // it would be "*/*".
                intent.setType("image/*");

                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(), "Vous ne disposez pas d'autorisation pour mener cette action !", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = data.getData();
            try {
                assert uri != null;
                InputStream inputStream = this.getActivity().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                editImage.setVisibility(View.VISIBLE);
                editImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadspinner() {
        String[] from = {
                "_id",
                "nom"
        };

        // View IDs to map the columns (fetched above) into
        int[] to = {
                android.R.id.text1
        };

        final int[] currentPos = {0};

        Cursor cursor = getContext().getContentResolver().query(
                CategoriesProvider.CONTENT_URI, null,null, null,
                null
        );

        final int[] finalCurrentPos = {currentPos[0]};
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, cursor, from, to, 0){
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == finalCurrentPos[0]) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        // Create the list view and bind the adapter
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //get value by name
                Cursor qc = adapter.getCursor();
                String nom = qc.getString(qc.getColumnIndex("nom"));
                categorie.setText(nom);
                finalCurrentPos[0] =position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
