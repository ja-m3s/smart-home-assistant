package com.jam3s.remote;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Main")
@Route(value = "")
public class MainView extends HorizontalLayout {

    private TextField name;
    private Button cycleBulbState;

    /* add a couple of things
     * - connect to rabbitmq
     * - read the queue and show me stuff about the lightbulbs
     * - lightbulb name, state and time turned on
     * - when it finds a lightbulb, add it to the display
     * - send a message via rabbitmq to turn the lightbulb on or off
     * ./gradlew :bootRun to run it
     */
    public MainView() {
        name = new TextField("Lightbulb");
        cycleBulbState = new Button("Cycle");
        cycleBulbState.addClickListener(e -> {
            Notification.show("Cycling " + name.getValue());
            //Rabbit mq - send a message
            TextField lightbulb = new TextField("A lighty bulby");
            add (lightbulb);
        });
        cycleBulbState.addClickShortcut(Key.ENTER);

        setMargin(true);
        setVerticalComponentAlignment(Alignment.END, name, cycleBulbState);

        add(name, cycleBulbState);
    }

}