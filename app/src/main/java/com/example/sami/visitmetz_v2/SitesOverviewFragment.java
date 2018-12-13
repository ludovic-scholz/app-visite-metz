package com.example.sami.visitmetz_v2;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sami.visitmetz_v2.models.SiteCard;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;

import static android.support.v4.content.ContextCompat.getDrawable;


public class SitesOverviewFragment extends Fragment {

    ArrayList<SiteCard> listitems = new ArrayList<>();
    RecyclerView MyRecyclerView;
    DatabaseHelper databaseHelper;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(getContext());

        byte[] img1=getByteFromDrawable(Objects.requireNonNull(getDrawable(Objects.requireNonNull(getContext()), R.drawable.cathedrale_st_etienne)));

        byte[] img2=getByteFromDrawable(Objects.requireNonNull(getDrawable(getContext(), R.drawable.centre_pompidou)));

        byte[] img3=getByteFromDrawable(Objects.requireNonNull(getDrawable(getContext(), R.drawable.stade_st_symphorien)));

        databaseHelper.addData("Cathédrale Saint-Étienne", 49.120484, 6.176334,"Place d'Armes, 57000 Metz, France", "Sites historiques, monuments, musées et statues", "La cathédrale Saint-Étienne de Metz est la cathédrale catholique du diocèse de Metz, dans le département français de la Moselle en région Grand Est.",  img1);
        databaseHelper.addData("Centre Pompidou-Metz", 49.108465, 6.181730, "1 Parvis des Droits de l'Homme, 57020 Metz, France","Sites historiques, monuments, musées et statues", "Le centre Pompidou-Metz est un établissement public de coopération culturelle d’art situé à Metz, entre le parc de la Seille et la gare. Sa construction est réalisée dans le cadre de l’opération d’aménagement du quartier de l’Amphithéâtre.", img2);
        databaseHelper.addData("Stade St Symphorien", 49.109968, 6.159747, "3 Boulevard Saint-Symphorien, 57050 Longeville-lès-Metz, France", "Jeux et divertissements", "Le stade Saint-Symphorien est l'enceinte sportive principale de l'agglomération messine. C'est un stade consacré au football qui est utilisé par le Football Club de Metz.", img3);
        initializeList();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card, container, false);
        MyRecyclerView = view.findViewById(R.id.cardView);
        MyRecyclerView.setHasFixedSize(true);
        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(getActivity());
        MyLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        if (listitems.size() > 0 & MyRecyclerView != null) {
            MyRecyclerView.setAdapter(new MyAdapter(listitems));
        }
        MyRecyclerView.setLayoutManager(MyLayoutManager);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

    public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        public ArrayList<SiteCard> list;

        MyAdapter(ArrayList<SiteCard> Data) {
            list = Data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // create a new view
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycle_items, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

            byte[] img = list.get(position).getImage();
            Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);

            holder.titleTextView.setText(list.get(position).getNomCard());
            holder.coverImageView.setImageBitmap(bitmap);
            holder.coverImageView.setTag(bitmap);
            holder.likeImageView.setTag(R.drawable.ic_thumb_up_black_24dp);
            holder.editImageView.setTag(R.drawable.edit_black_24dp);
            holder.deleteImageView.setTag(R.drawable.ic_delete_black_24dp);

        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        ImageView coverImageView;
        ImageView likeImageView;
        ImageView shareImageView;
        ImageView editImageView;
        ImageView deleteImageView;

        MyViewHolder(View v) {
            super(v);
            titleTextView = v.findViewById(R.id.titleTextView);
            coverImageView = v.findViewById(R.id.coverImageView);
            likeImageView = v.findViewById(R.id.likeImageView);
            shareImageView = v.findViewById(R.id.shareImageView);
            editImageView = v.findViewById(R.id.editImageView);
            deleteImageView = v.findViewById(R.id.deleteImageView);

            editImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Cursor currentData  = databaseHelper.getItem(titleTextView.getText().toString());
                    currentData.moveToFirst();
                    if(currentData.moveToFirst()) {
                        SiteData currentSite = new SiteData(currentData.getString(2),
                                Double.valueOf(currentData.getString(3)), Double.valueOf(currentData.getString(4)),
                                currentData.getString(5), currentData.getString(6), currentData.getString(7),
                                currentData.getBlob(8));

                        // Create new fragment, give it an object and start transaction
                        Fragment newFragment = new AjouterSiteDetailsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("site", currentSite);
                        newFragment.setArguments(bundle);

                        // consider using Java coding conventions (upper first char class names!!!)
                        FragmentTransaction transaction;
                        if (getFragmentManager() != null) {
                            transaction = getFragmentManager().beginTransaction();
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                            // Replace whatever is in the fragment_container view with this fragment,
                            // and add the transaction to the back stack
                            transaction.replace(R.id.fragment_container, newFragment);
                            transaction.addToBackStack(null);

                            // Commit the transaction
                            transaction.commit();
                        }
                    }
                }
            });

            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(false);
                    builder.setTitle("Supprimer le site " + titleTextView.getText().toString());
                    builder.setMessage("Êtes-vous sûr?");
                    builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getActivity(), titleTextView.getText() + " a été supprimé!", Toast.LENGTH_SHORT).show();
                            databaseHelper.DeleteData(titleTextView.getText().toString());
                            databaseHelper.getItem(titleTextView.getText().toString());
                        }
                    });
                    builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.create().show();
                }
            });

            /*likeImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                int id = (int)likeImageView.getTag();
                if( id == R.drawable.ic_thumb_up_black_24dp){

                    likeImageView.setTag(R.drawable.ic_like_colored); //ic_liked
                    likeImageView.setImageResource(R.drawable.ic_like_colored); //ic_liked

                    Toast.makeText(getActivity(),titleTextView.getText()+" wurde erfolgreich zu 'Favoriten' hinzugefügt",Toast.LENGTH_SHORT).show();

                } else{

                    likeImageView.setTag(R.drawable.ic_thumb_up_black_24dp);
                    likeImageView.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                    Toast.makeText(getActivity(),titleTextView.getText()+" wurde von 'Favoriten' entfernt",Toast.LENGTH_SHORT).show();
                }
               }
            });*/

            shareImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + getResources().getResourcePackageName(coverImageView.getId())
                        + '/' + "drawable" + '/' + getResources().getResourceEntryName((int) coverImageView.getTag()));

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM,imageUri);
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));

                }
            });
        }
    }

    public void initializeList() {
        listitems.clear();

        databaseHelper = new DatabaseHelper(getActivity());
        Cursor data = databaseHelper.getData();
        while(data.moveToNext())
        {
            SiteCard item = new SiteCard();
            item.setNomCard(data.getString(2));
            item.setLatitude(Double.valueOf(data.getString(3)));
            item.setLongitude(Double.valueOf(data.getString(4)));
            item.setAdresse(data.getString(5));
            item.setCategorie(data.getString(6));
            item.setResume(data.getString(7));
            item.setImage(data.getBlob(8));

            listitems.add(item);
        }
    }
}
