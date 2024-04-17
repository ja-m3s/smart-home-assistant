package com.jam3s.remote;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Date;

import org.json.JSONObject;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Main")
@Route(value = "")
public class MainView extends HorizontalLayout {

    private VerticalLayout lightBulbDisplayVLayout; // Reference to the paragraph
    private ScheduledExecutorService executorService;

    public MainView() {
        executorService = Executors.newScheduledThreadPool(1);

        setMargin(true);

        // Schedule the task to execute every 1 second
        executorService.scheduleAtFixedRate(this::handleGetLightBulbStatus, 0, 1, TimeUnit.SECONDS);
    }

    private void handleGetLightBulbStatus() {
        Notification.show("Getting light bulb status...");
        if (lightBulbDisplayVLayout == null) {
            lightBulbDisplayVLayout = new VerticalLayout(); // Use VerticalLayout to stack components vertically
            add(lightBulbDisplayVLayout);
        }
        lightBulbDisplayVLayout.removeAll(); // Remove existing content before adding new content
    
        // Iterate over the entries in the HashMap
        RemoteApplication.getLightBulbStatus().forEach((key, value) -> {
            
            //Get the value of the hashmap and put it into a JSONObject for easy parsing.
            JSONObject msg = new JSONObject(value);

            //Create some Textfields to hold our values
            TextField lightBulbNameTextField = new TextField(key);
            TextField bulbStatusTextField = new TextField(msg.get("bulb_status").toString());
            Date timeTurnedOn = new Date(Long.parseLong(msg.get("time_turned_on").toString()));
            TextField timeTurnedOnTextField = new TextField(timeTurnedOn.toString());

            // Make the TextField read-only
            bulbStatusTextField.setReadOnly(true); 
            timeTurnedOnTextField.setReadOnly(true);
            
            // Add a button to turn lights on/off.
            Button toggleState = new Button("Toggle On/Off");
            toggleState.addClickListener(d -> handleToggleState(key));
            // Add the Label and TextField and Button to a HorizontalLayout
            HorizontalLayout entryLayout = new HorizontalLayout(lightBulbNameTextField, bulbStatusTextField,timeTurnedOnTextField, toggleState);
            lightBulbDisplayVLayout.add(entryLayout); // Add the HorizontalLayout to the VerticalLayout
        });
    }

    private void handleToggleState(String key) {
        Notification.show("Toggling light bulb...");
        try {
            RemoteApplication.sendMessage(RemoteApplication.createTurnOnMessage(key));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Shutdown the executor service when the UI is detached
        executorService.shutdown();
    }
}
