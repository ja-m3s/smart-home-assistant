package com.jam3s.remote;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Main")
@Route(value = "")
public class MainView extends HorizontalLayout {

    private Button getLightBulbStatus;
    private Paragraph lightBulbStatusParagraph; // Reference to the paragraph

    public MainView() {
        getLightBulbStatus = new Button("Light bulb status");
        getLightBulbStatus.addClickListener(e -> {
            Notification.show("Getting light bulb status...");
            if (lightBulbStatusParagraph == null) {
                lightBulbStatusParagraph = new Paragraph();
                add(lightBulbStatusParagraph);
            }
            lightBulbStatusParagraph.setText(RemoteApplication.getLightBulbStatus().toString());
        });
        getLightBulbStatus.addClickShortcut(Key.ENTER);

        setMargin(true);
        setVerticalComponentAlignment(Alignment.END, getLightBulbStatus);

        add(getLightBulbStatus);
    }

}
