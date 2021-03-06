package com.gophillygo.app.data.networkresource;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gophillygo.app.data.DestinationDao;
import com.gophillygo.app.data.DestinationWebservice;
import com.gophillygo.app.data.EventDao;
import com.gophillygo.app.data.models.Attraction;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationQueryResponse;
import com.gophillygo.app.data.models.Event;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Shared network query manager between events and destinations (attractions).
 * Subclass to get LiveData responses for either destinations or events.
 */

abstract public class AttractionNetworkBoundResource<T extends Attraction>
        extends NetworkBoundResource<List<T>, DestinationQueryResponse> {

    // maximum rate at which to refresh data from network
    private static final long RATE_LIMIT = TimeUnit.MINUTES.toMillis(15);

    private final DestinationWebservice webservice;
    private final DestinationDao destinationDao;
    private final EventDao eventDao;

    public AttractionNetworkBoundResource(DestinationWebservice webservice,
                                          DestinationDao destinationDao,
                                          EventDao eventDao) {
        this.webservice = webservice;
        this.destinationDao = destinationDao;
        this.eventDao = eventDao;
    }

    @Override
    protected void saveCallResult(@NonNull DestinationQueryResponse response) {
        Long timestamp = System.currentTimeMillis();

        // clear out existing database entries before adding new ones
        destinationDao.clear();
        eventDao.clear();

        // save destinations
        for (Destination item: response.getDestinations()) {
            item.setTimestamp(timestamp);
            destinationDao.save(item);
        }

        // save events
        for (Event item: response.getEvents()) {
            item.setTimestamp(timestamp);
            eventDao.save(item);
        }
    }

    @Override
    protected boolean shouldFetch(@Nullable List<T> data) {
        if (data == null || data.isEmpty()) {
            return true;
        }
        Attraction first = data.get(0);
        return System.currentTimeMillis() - first.getTimestamp() > RATE_LIMIT;
    }

    @NonNull @Override
    protected LiveData<ApiResponse<DestinationQueryResponse>> createCall() {
        return webservice.getDestinations();
    }

    @NonNull @Override
    abstract protected LiveData<List<T>> loadFromDb();
}
