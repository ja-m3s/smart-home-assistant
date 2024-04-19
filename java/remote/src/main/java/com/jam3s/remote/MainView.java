package com.jam3s.remote;

import java.time.Instant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Remote")
@Route(value = "")
public class MainView extends VerticalLayout {

    /**
     * Layout to hold the light bulb states.
     */
    private final VerticalLayout lightBulbDisplayVLayout = new VerticalLayout();

    /**
     * Main View.
     */
    public MainView() {

        setMargin(true);

        // Add a button to turn lights on/off.
        Button refreshLightBulbList = new Button("Refresh");
        refreshLightBulbList.addClickListener(d -> handleGetLightBulbStatus());
        VerticalLayout refreshButtonDisplayVLayout = new VerticalLayout();
        refreshButtonDisplayVLayout.add(refreshLightBulbList);

        add(refreshButtonDisplayVLayout, lightBulbDisplayVLayout);

    }

    private void handleGetLightBulbStatus() {
        //LOG.info("Getting light bulb status");
        Notification.show("Getting light bulb status...");
        lightBulbDisplayVLayout.removeAll(); // Remove existing content before adding new content
        // Iterate over the entries in the HashMap
        RemoteApplication.getLightBulbStatus().forEach((key, value) -> {
            // Extracting values from JSON
            String bulbStatus = value.optString("bulb_state");
            long timeTurnedOnMillis = value.optLong("time_turned_on");
            Instant timeTurnedOn = Instant.ofEpochMilli(timeTurnedOnMillis);

            // Create NativeLabels
            NativeLabel lightBulbNameNativeLabel = new NativeLabel(key);
            NativeLabel bulbStatusNativeLabel = new NativeLabel(bulbStatus);
            NativeLabel timeTurnedOnNativeLabel = new NativeLabel(timeTurnedOn.toString());
            // Add a button to turn lights on/off.
            Button toggleState = new Button("Toggle On/Off");
            toggleState.addClickListener(d -> handleToggleState(key));
            // Add the items to a HorizontalLayout
            HorizontalLayout entryLayout = new HorizontalLayout(lightBulbNameNativeLabel,
                    bulbStatusNativeLabel, timeTurnedOnNativeLabel, toggleState);
            // Add the HorizontalLayout to the VerticalLayout
            lightBulbDisplayVLayout.add(entryLayout);
        });
    }

    private void handleToggleState(final String key) {
        Notification.show("Toggling "+key);
        RemoteApplication.sendMessage(RemoteApplication.createTurnOnMessage(key));
    }
}
