package com.jam3s.remote;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Main")
@Route(value = "")
public class MainView extends HorizontalLayout {

    private Button getLightBulbStatus;
    private VerticalLayout lightBulbStatusParagraph; // Reference to the paragraph

    public MainView() {
        getLightBulbStatus = new Button("Light bulb status");
        getLightBulbStatus.addClickListener(e -> {
            Notification.show("Getting light bulb status...");
            if (lightBulbStatusParagraph == null) {
                lightBulbStatusParagraph = new VerticalLayout(); // Use VerticalLayout to stack components vertically
                add(lightBulbStatusParagraph);
            }
            lightBulbStatusParagraph.removeAll(); // Remove existing content before adding new content
        
            // Iterate over the entries in the HashMap
            RemoteApplication.getLightBulbStatus().forEach((key, value) -> {
                NativeLabel keyLabel = new NativeLabel(key); // Create a Label with the key
                TextArea valueTextArea = new TextArea(); // Create a TextArea to display the message
                valueTextArea.setValue(value.toString()); // Set the value of the TextArea
                valueTextArea.setReadOnly(true); // Make the TextArea read-only
                // Add the Label and TextArea to a HorizontalLayout
                HorizontalLayout entryLayout = new HorizontalLayout(keyLabel, valueTextArea);
                lightBulbStatusParagraph.add(entryLayout); // Add the HorizontalLayout to the VerticalLayout
            });
        });
        getLightBulbStatus.addClickShortcut(Key.ENTER);

        setMargin(true);
        setVerticalComponentAlignment(Alignment.END, getLightBulbStatus);

        add(getLightBulbStatus);
    }

}
