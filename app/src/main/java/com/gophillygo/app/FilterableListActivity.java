package com.gophillygo.app;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import cn.nekocode.badge.BadgeDrawable;

/**
 * Abstract list activity with a filter popover opened by a filter toolbar button.
 * Toolbar button updates to display count of filters currently applied.
 */

public abstract class FilterableListActivity extends AppCompatActivity
        implements FilterDialog.FilterChangeListener {

    private int layoutId, toolbarId, filterButtonId;

    private Button filterButton;
    private Drawable filterIcon;
    private Toolbar toolbar;

    public FilterableListActivity(int layoutId, int toolbarId, int filterButtonId) {
        this.layoutId = layoutId;
        this.toolbarId = toolbarId;
        this.filterButtonId = filterButtonId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        // set up toolbar
        toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        filterIcon = ContextCompat.getDrawable(this, R.drawable.ic_filter_list_white_24px);

        // set up filter button
        filterButton = findViewById(filterButtonId);
        filterButton.setOnClickListener(v -> {
            FilterDialog filterDialog = new FilterDialog();
            filterDialog.show(getSupportFragmentManager(), filterDialog.getTag());
        });
    }

    @Override
    public void filtersChanged(int setFilterCount) {
        // Change filter button's left drawable when filters set to either be a badge with the
        // filter count, or the default filter icon, if no filters set.
        if (setFilterCount > 0) {
            Drawable filterDrawable = new BadgeDrawable.Builder()
                    .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                    .badgeColor(ContextCompat.getColor(this, R.color.color_white))
                    .textColor(ContextCompat.getColor(this, R.color.color_primary))
                    .text1(String.valueOf(setFilterCount))
                    .build();
            filterButton.setCompoundDrawablesWithIntrinsicBounds(filterDrawable, null, null, null);
        } else {
            filterButton.setCompoundDrawablesWithIntrinsicBounds(filterIcon, null, null, null);
        }

    }
}
