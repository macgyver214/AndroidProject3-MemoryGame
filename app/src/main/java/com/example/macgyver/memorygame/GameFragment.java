package com.example.macgyver.memorygame;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment {

    private static final int PICS_ON_BOARD = 8;
    private static final int NUMBER_OF_TILES = 16;

    private static int tileIds[] = {R.id.img0, R.id.img1, R.id.img2, R.id.img3, R.id.img4, R.id.img5, R.id.img6, R.id.img7, R.id.img8, R.id.img9, R.id.img10, R.id.img11, R.id.img12, R.id.img13, R.id.img14, R.id.img15};
    private static int imagesToChoseFrom[] = {R.drawable.pic1, R.drawable.pic2, R.drawable.pic3, R.drawable.pic4, R.drawable.pic5, R.drawable.pic6, R.drawable.pic7, R.drawable.pic8, R.drawable.pic9, R.drawable.pic10};
    List<Integer> pictureRef = Arrays.asList(0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7);
    private ArrayList<Integer> picIdsOnMap = new ArrayList<>();
    private Tile allTiles[] = new Tile[16];
    private int FLIPPED_TILES = 1;
    private int UNCOVERED_TILES = 0;
    private int clicks = 0;
    private ArrayList<Tile> comparedTiles = new ArrayList<>();
    private Handler h = new Handler();

    SoundPool soundPool = null;
    int hitSound=0, missSound=0;

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        this.soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        this.hitSound = this.soundPool.load(getContext(), R.raw.hit, 1);
        this.missSound = this.soundPool.load(getContext(), R.raw.miss, 1);

        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_game, container, true);
        initViews(rootView);
        updateAllTiles();
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        h.removeCallbacks(null);
    }

    private void initViews(View rootView) {
        final Resources res = getResources();

        for(int i = 0; i < NUMBER_OF_TILES; i++) {
            final int id = res.getIdentifier("img"+i, "id", getActivity().getPackageName());
            final Tile currentTile = allTiles[i];
            final int imgId = res.getIdentifier(picIdsOnMap.get(currentTile.getPicReference()).toString(), "drawable", getActivity().getPackageName());
            final Drawable d = res.getDrawable(imgId);
            final TextView counter = (TextView) rootView.findViewById(res.getIdentifier("count_num", "id", getActivity().getPackageName()));

            final ImageButton tileButton = (ImageButton) rootView.findViewById(id);
            tileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicks++;
                    String text = Integer.toString(clicks);
                    counter.setText(text);
                    tileButton.setImageDrawable(d);
                    currentTile.setFlipped(true);
                    comparedTiles.add(currentTile);
                    if (FLIPPED_TILES == 2)
                        think();
                    FLIPPED_TILES++;
                    if(getWinCondition()) {
                        Toast.makeText(getContext(), "You cleared the board!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), TitleActivity.class);
                        getActivity().startActivity(intent);
                    }
                }
            });
        }

    }

    /**
     * An initialization method to set up the game board
     */
    private void init() {
        //shuffle the references to
        Collections.shuffle(pictureRef);
        chose8Pics(imagesToChoseFrom);
        for(int i=0; i < 16; i++) {
            allTiles[i] = new Tile(this, pictureRef.get(i), tileIds[i]);
        }
    }

    private void think() {
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) return;
                ImageButton ib1 = (ImageButton) getActivity().findViewById(comparedTiles.get(0).getButtonID());
                ImageButton ib2 = (ImageButton) getActivity().findViewById(comparedTiles.get(1).getButtonID());
                if (!comparedTiles.isEmpty() && compare(ib1.getDrawable(), ib2.getDrawable())) {
                    ib1.setEnabled(false);
                    ib2.setEnabled(false);
                    comparedTiles.clear();
                    UNCOVERED_TILES += 2;
                    System.out.print(UNCOVERED_TILES);
                    soundPool.play(hitSound, 1f, 1f, 1, 0, 1);
                } else {
                    Resources res = getActivity().getResources();
                    int id = res.getIdentifier("unknown_tile", "drawable", getActivity().getPackageName());
                    Drawable d = res.getDrawable(id);
                    ib1.setImageDrawable(d);
                    ib2.setImageDrawable(d);
                    comparedTiles.get(0).setFlipped(false);
                    comparedTiles.get(1).setFlipped(false);
                    comparedTiles.clear();
                    soundPool.play(missSound, 1f, 1f, 1, 0, 1);
                }
                FLIPPED_TILES = 1;
            }
        }, 500);
    }

    private boolean compare(Drawable first, Drawable second) {
        Drawable.ConstantState stateA = first.getConstantState();
        Drawable.ConstantState stateB = second.getConstantState();
        if(stateA.equals(stateB))
            return true;
        else
            return false;
    }

    private void updateAllTiles() {
        for (int i = 0; i < allTiles.length; i++) {
            allTiles[i].updateDrawableState();
        }
    }

    /**
     * A method to pick 8 images out of the available images in the drawable folder
     * @param selection the array of images saved within the app's files
     */
    private void chose8Pics(int[] selection) {
        Random rand = new Random();

        int currentFill = 0;
        while (currentFill < PICS_ON_BOARD) {
            int randInt = rand.nextInt(selection.length);
            if (!picIdsOnMap.contains(selection[randInt])) {
                picIdsOnMap.add(selection[randInt]);
                currentFill++;
            }
        }
    }

    public boolean getWinCondition() {
        for (int i = 0; i < NUMBER_OF_TILES; i++) {
            if (allTiles[i].getFlipped()) {

            } else {
                return false;
            }
        }
        return true;
    }



    public int getPicId(int picRef) {
        return picIdsOnMap.get(picRef);
    }

}
