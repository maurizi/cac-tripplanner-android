package com.gophillygo.app;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.databinding.ActivityPlaceDetailBinding;
import com.gophillygo.app.di.GpgViewModelFactory;
import com.synnapps.carouselview.CarouselView;

import javax.inject.Inject;

public class PlaceDetailActivity extends AppCompatActivity {

    public static final String DESTINATION_ID_KEY = "placeId";
    private static final String LOG_LABEL = "PlaceDetail";

    private static final int COLLAPSED_LINE_COUNT = 4;
    private static final int EXPANDED_MAX_LINES = 50;

    private long placeId = -1;
    private Destination destination;

    private LayoutInflater inflater;
    private ActivityPlaceDetailBinding binding;
    private CarouselView carouselView;
    private Toolbar toolbar;
    TextView descriptionView;
    private View.OnClickListener toggleClickListener;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    DestinationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_place_detail);
        binding.setActivity(this);

        inflater = getLayoutInflater();
        toolbar = findViewById(R.id.place_detail_toolbar);
        // disable default app name title display
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(DESTINATION_ID_KEY)) {
            placeId = getIntent().getLongExtra(DESTINATION_ID_KEY, -1);
        }

        if (placeId == -1) {
            Log.e(LOG_LABEL, "Place not found when attempting to load detail view.");
            finish();
        }

        carouselView = findViewById(R.id.place_detail_carousel);
        carouselView.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
        carouselView.setImageClickListener(position ->
                Log.d(LOG_LABEL, "Clicked item: "+ position));

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DestinationViewModel.class);
        viewModel.getDestination(placeId).observe(this, destination -> {
            // TODO: handle if destination not found (go to list of destinations?)
            if (destination == null) {
                Log.e(LOG_LABEL, "No matching destination found for ID " + placeId);
            }
            this.destination = destination;
            // set up data binding object
            binding.setDestination(destination);
            displayDestination();
        });

        // click handler for toggling expanding/collapsing description card
        descriptionView = findViewById(R.id.place_detail_description_text);
        toggleClickListener = v -> {
            TextView view = (TextView) v;
            int current = descriptionView.getMaxLines();
            if (current == COLLAPSED_LINE_COUNT) {
                descriptionView.setMaxLines(EXPANDED_MAX_LINES);
                // make links clickable in expanded view
                descriptionView.setMovementMethod(LinkMovementMethod.getInstance());
                view.setText(R.string.place_detail_description_collapse);
            } else {
                descriptionView.setMaxLines(COLLAPSED_LINE_COUNT);
                descriptionView.setEllipsize(TextUtils.TruncateAt.END);
                // disable clicking links to also disable scrolling
                descriptionView.setMovementMethod(null);
                // must reset click listener after unsetting movement method
                descriptionView.setOnClickListener(toggleClickListener);
                // set text again, to make ellipsize run
                descriptionView.setText(descriptionView.getText());
                view.setText(R.string.place_detail_description_expand);
            }
        };
    }

    // open map when user clicks map button
    public void goToMap(View view) {
        // TODO: #10 open within app map view, once implemented
        // for now, open Google Maps externally with a marker at the given location
        String locationString = destination.getLocation().toString();
        Uri gmapsUri = Uri.parse("geo:" + locationString + "?q=" + locationString + "(" +
                destination.getName() + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmapsUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    // open the GoPhillyGo website, passing the destination, when "get directions" clicked
    public void goToDirections(View view) {
        // pass parameters destination and destinationText to https://gophillygo.org/
        Uri directionsUri = new Uri.Builder().scheme("https").authority("gophillygo.org")
                // TODO: #9 send current user location as origin
                .appendQueryParameter("origin", "")
                .appendQueryParameter("originText", "")
                .appendQueryParameter("destination", destination.getLocation().toString())
                .appendQueryParameter("destinationText", destination.getAddress()).build();
        Intent intent = new Intent(Intent.ACTION_VIEW, directionsUri);
        startActivity(intent);
    }

    // open website for destination in browser
    public void goToWebsite(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(destination.getWebsiteUrl()));
        startActivity(intent);
    }

    @SuppressLint({"RestrictedApi", "RtlHardcoded"})
    private void displayDestination() {
        // set up carousel
        carouselView.setViewListener(new CarouselViewListener(this, false) {
            @Override
            public Destination getDestinationAt(int position) {
                return destination;
            }
        });
        carouselView.setPageCount(1);

        // set count of upcoming events
        int eventCount = destination.getEventCount();
        TextView upcomingEventsView = findViewById(R.id.place_detail_upcoming_events);
        String upcomingEventsText = getResources()
                .getQuantityString(R.plurals.place_upcoming_activities_count, eventCount, eventCount);
        upcomingEventsView.setText(upcomingEventsText);

        // TODO: #18 go to filtered event list with events for destination on click
        upcomingEventsView.setOnClickListener(v -> Log.d(LOG_LABEL,
                "Clicked upcoming events for destination " +  destination.getName()));


        TextView descriptionToggle = findViewById(R.id.place_detail_description_toggle);
        descriptionToggle.setOnClickListener(toggleClickListener);

        // show popover for flag options (been, want to go, etc.)
        // TODO: #25 implement user flags
        CardView flagOptionsCard = findViewById(R.id.place_detail_flag_options_card);
        flagOptionsCard.setOnClickListener(v -> {
            Log.d(LOG_LABEL, "Clicked flags button");
            PopupMenu menu = new PopupMenu(this, flagOptionsCard);
            menu.getMenuInflater().inflate(R.menu.place_options_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                Log.d(LOG_LABEL, "Clicked " + item.toString());
                return true;
            });

            // Force icons to show in the popup menu via the support library API
            // https://stackoverflow.com/questions/6805756/is-it-possible-to-display-icons-in-a-popupmenu
            MenuPopupHelper popupHelper = new MenuPopupHelper(this,
                    (MenuBuilder)menu.getMenu(),
                    flagOptionsCard);
            popupHelper.setForceShowIcon(true);
            popupHelper.setGravity(Gravity.END|Gravity.RIGHT);
            popupHelper.show();
        });
    }
}
